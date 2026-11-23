<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/controllers/OperationController.php';

function contentDateTime($value, string $label, bool $required): ?string
{
    if ($value === null || $value === '') {
        if ($required) {
            jsonError(422, 'validation_error', $label . ' wajib diisi.');
        }
        return null;
    }
    if (!is_string($value)) {
        jsonError(422, 'validation_error', $label . ' harus berupa tanggal dan waktu.');
    }
    $normalized = str_replace('T', ' ', trim($value));
    if (preg_match('/\A[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}\z/', $normalized) === 1) {
        $normalized .= ':00';
    }
    $date = DateTimeImmutable::createFromFormat('!Y-m-d H:i:s', $normalized);
    $errors = DateTimeImmutable::getLastErrors();
    if ($date === false || ($errors !== false && ($errors['warning_count'] > 0 || $errors['error_count'] > 0))
        || $date->format('Y-m-d H:i:s') !== $normalized) {
        jsonError(422, 'validation_error', $label . ' harus memakai format YYYY-MM-DD HH:MM:SS.');
    }

    return $normalized;
}

function contentTargetRt(PDO $connection, array $user, int $organizationId, $value, array $permission): ?int
{
    if ($value === null || $value === '') {
        if ((int) $permission['terbatas'] === 1) {
            jsonError(403, 'restricted_region', 'Akun terbatas hanya dapat menerbitkan konten untuk RT sendiri.');
        }
        return null;
    }
    $rtId = operationPositiveId($value, 'rtId');
    $rt = $connection->prepare(
        'SELECT id FROM rt
        WHERE id = :rt_id AND id_organisasi = :organization_id AND dihapus_pada IS NULL'
    );
    $rt->execute(['rt_id' => $rtId, 'organization_id' => $organizationId]);
    if ($rt->fetchColumn() === false) {
        jsonError(422, 'invalid_region', 'RT tidak tersedia dalam organisasi Anda.');
    }
    if ($user['role_code'] !== 'super_admin' && $user['rt_id'] !== null && (int) $user['rt_id'] !== $rtId) {
        jsonError(403, 'forbidden_region', 'Anda tidak dapat menerbitkan konten untuk RT lain.');
    }
    if ($user['role_code'] !== 'super_admin' && $user['rw_id'] !== null) {
        $rw = $connection->prepare('SELECT id FROM rt WHERE id = :rt_id AND id_rw = :rw_id');
        $rw->execute(['rt_id' => $rtId, 'rw_id' => (int) $user['rw_id']]);
        if ($rw->fetchColumn() === false) {
            jsonError(403, 'forbidden_region', 'RT berada di luar wilayah RW Anda.');
        }
    }

    return $rtId;
}

function contentWriteScope(string $organizationColumn, string $rtColumn, array $user, int $organizationId): array
{
    $where = [$organizationColumn . ' = :scope_organization_id'];
    $parameters = ['scope_organization_id' => $organizationId];
    if ($user['role_code'] !== 'super_admin' && $user['rt_id'] !== null) {
        $where[] = $rtColumn . ' = :scope_rt_id';
        $parameters['scope_rt_id'] = (int) $user['rt_id'];
    } elseif ($user['role_code'] !== 'super_admin' && $user['rw_id'] !== null) {
        $where[] = '(' . $rtColumn . ' IS NULL OR ' . $rtColumn
            . ' IN (SELECT scope_rt.id FROM rt scope_rt WHERE scope_rt.id_rw = :scope_rw_id))';
        $parameters['scope_rw_id'] = (int) $user['rw_id'];
    }

    return ['where' => implode(' AND ', $where), 'parameters' => $parameters];
}

function writeAnnouncement(PDO $connection, array $user, ?int $announcementId = null): void
{
    $permissionCode = $announcementId === null ? 'pengumuman.create' : 'pengumuman.update';
    $permission = requirePermission($connection, $user, $permissionCode);
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $title = operationText($body, 'title', 'Judul', 200);
    $content = operationText($body, 'body', 'Isi pengumuman', 65535);
    $publishAt = contentDateTime($body['publishAt'] ?? null, 'Waktu publikasi', false);
    $expiresAt = contentDateTime($body['expiresAt'] ?? null, 'Waktu kedaluwarsa', false);
    if ($publishAt !== null && $expiresAt !== null && $expiresAt <= $publishAt) {
        jsonError(422, 'validation_error', 'Waktu kedaluwarsa harus setelah waktu publikasi.');
    }
    $rtId = contentTargetRt($connection, $user, $organizationId, $body['rtId'] ?? null, $permission);

    if ($announcementId !== null) {
        $scope = contentWriteScope('p.id_organisasi', 'p.id_rt', $user, $organizationId);
        $existing = $connection->prepare(
            'SELECT p.id FROM pengumuman p
            WHERE p.id = :content_id AND p.dihapus_pada IS NULL AND ' . $scope['where']
        );
        $existing->execute(array_merge(['content_id' => $announcementId], $scope['parameters']));
        if ($existing->fetchColumn() === false) {
            jsonError(404, 'announcement_not_found', 'Pengumuman tidak ditemukan atau berada di luar wilayah Anda.');
        }
    }

    $connection->beginTransaction();
    try {
        if ($announcementId === null) {
            $statement = $connection->prepare(
                'INSERT INTO pengumuman
                    (id_organisasi, id_rt, judul, isi, dibuat_oleh, dipublikasikan_pada, kedaluwarsa_pada)
                VALUES (:organization_id, :rt_id, :title, :body, :actor_id, :publish_at, :expires_at)'
            );
            $statement->execute([
                'organization_id' => $organizationId,
                'rt_id' => $rtId,
                'title' => $title,
                'body' => $content,
                'actor_id' => (int) $user['id'],
                'publish_at' => $publishAt,
                'expires_at' => $expiresAt,
            ]);
            $announcementId = (int) $connection->lastInsertId();
            $action = $publishAt === null ? 'buat_draf_pengumuman' : 'terbitkan_pengumuman';
        } else {
            $statement = $connection->prepare(
                'UPDATE pengumuman SET id_rt = :rt_id, judul = :title, isi = :body,
                    dipublikasikan_pada = :publish_at, kedaluwarsa_pada = :expires_at
                WHERE id = :content_id'
            );
            $statement->execute([
                'rt_id' => $rtId,
                'title' => $title,
                'body' => $content,
                'publish_at' => $publishAt,
                'expires_at' => $expiresAt,
                'content_id' => $announcementId,
            ]);
            $action = 'ubah_pengumuman';
        }
        operationAudit($connection, $user, $organizationId, $action, 'pengumuman', $announcementId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess([
        'id' => $announcementId,
        'message' => $publishAt === null ? 'Draf pengumuman berhasil disimpan.' : 'Pengumuman berhasil diterbitkan.',
    ], $permissionCode === 'pengumuman.create' ? 201 : 200);
}

function deleteAnnouncement(PDO $connection, array $user, int $announcementId): void
{
    requirePermission($connection, $user, 'pengumuman.delete');
    $organizationId = operationOrganizationId($user, []);
    $scope = contentWriteScope('p.id_organisasi', 'p.id_rt', $user, $organizationId);
    $statement = $connection->prepare(
        'SELECT p.id FROM pengumuman p
        WHERE p.id = :content_id AND p.dihapus_pada IS NULL AND ' . $scope['where']
    );
    $statement->execute(array_merge(['content_id' => $announcementId], $scope['parameters']));
    if ($statement->fetchColumn() === false) {
        jsonError(404, 'announcement_not_found', 'Pengumuman tidak ditemukan atau berada di luar wilayah Anda.');
    }
    $connection->beginTransaction();
    try {
        $delete = $connection->prepare('UPDATE pengumuman SET dihapus_pada = NOW() WHERE id = :content_id');
        $delete->execute(['content_id' => $announcementId]);
        operationAudit($connection, $user, $organizationId, 'hapus_pengumuman', 'pengumuman', $announcementId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Pengumuman berhasil dihapus.']);
}

function writeActivity(PDO $connection, array $user, ?int $activityId = null): void
{
    $permissionCode = $activityId === null ? 'kegiatan.create' : 'kegiatan.update';
    $permission = requirePermission($connection, $user, $permissionCode);
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $title = operationText($body, 'title', 'Judul', 200);
    $description = operationText($body, 'description', 'Deskripsi', 65535, false);
    $location = operationText($body, 'location', 'Lokasi', 200, false);
    $type = $body['type'] ?? 'lainnya';
    if (!is_string($type) || !in_array($type, ['kerja_bakti', 'rapat', 'posyandu', 'sosial', 'lainnya'], true)) {
        jsonError(422, 'validation_error', 'Jenis kegiatan tidak valid.');
    }
    $startAt = contentDateTime($body['startAt'] ?? null, 'Waktu mulai', true);
    $endAt = contentDateTime($body['endAt'] ?? null, 'Waktu selesai', false);
    if ($endAt !== null && $endAt < $startAt) {
        jsonError(422, 'validation_error', 'Waktu selesai tidak boleh sebelum waktu mulai.');
    }
    $rtId = contentTargetRt($connection, $user, $organizationId, $body['rtId'] ?? null, $permission);

    if ($activityId !== null) {
        $scope = contentWriteScope('k.id_organisasi', 'k.id_rt', $user, $organizationId);
        $existing = $connection->prepare(
            'SELECT k.id FROM kegiatan k
            WHERE k.id = :content_id AND k.dihapus_pada IS NULL AND ' . $scope['where']
        );
        $existing->execute(array_merge(['content_id' => $activityId], $scope['parameters']));
        if ($existing->fetchColumn() === false) {
            jsonError(404, 'activity_not_found', 'Kegiatan tidak ditemukan atau berada di luar wilayah Anda.');
        }
    }

    $connection->beginTransaction();
    try {
        if ($activityId === null) {
            $statement = $connection->prepare(
                'INSERT INTO kegiatan
                    (id_organisasi, id_rt, judul, deskripsi, jenis, mulai_pada, selesai_pada, lokasi, dibuat_oleh)
                VALUES (:organization_id, :rt_id, :title, :description, :type, :start_at, :end_at, :location, :actor_id)'
            );
            $statement->execute([
                'organization_id' => $organizationId,
                'rt_id' => $rtId,
                'title' => $title,
                'description' => $description,
                'type' => $type,
                'start_at' => $startAt,
                'end_at' => $endAt,
                'location' => $location,
                'actor_id' => (int) $user['id'],
            ]);
            $activityId = (int) $connection->lastInsertId();
            $action = 'buat_kegiatan';
        } else {
            $statement = $connection->prepare(
                'UPDATE kegiatan SET id_rt = :rt_id, judul = :title, deskripsi = :description,
                    jenis = :type, mulai_pada = :start_at, selesai_pada = :end_at, lokasi = :location
                WHERE id = :content_id'
            );
            $statement->execute([
                'rt_id' => $rtId,
                'title' => $title,
                'description' => $description,
                'type' => $type,
                'start_at' => $startAt,
                'end_at' => $endAt,
                'location' => $location,
                'content_id' => $activityId,
            ]);
            $action = 'ubah_kegiatan';
        }
        operationAudit($connection, $user, $organizationId, $action, 'kegiatan', $activityId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['id' => $activityId, 'message' => 'Kegiatan berhasil disimpan.'],
        $permissionCode === 'kegiatan.create' ? 201 : 200);
}

function deleteActivity(PDO $connection, array $user, int $activityId): void
{
    requirePermission($connection, $user, 'kegiatan.delete');
    $organizationId = operationOrganizationId($user, []);
    $scope = contentWriteScope('k.id_organisasi', 'k.id_rt', $user, $organizationId);
    $statement = $connection->prepare(
        'SELECT k.id FROM kegiatan k
        WHERE k.id = :content_id AND k.dihapus_pada IS NULL AND ' . $scope['where']
    );
    $statement->execute(array_merge(['content_id' => $activityId], $scope['parameters']));
    if ($statement->fetchColumn() === false) {
        jsonError(404, 'activity_not_found', 'Kegiatan tidak ditemukan atau berada di luar wilayah Anda.');
    }
    $connection->beginTransaction();
    try {
        $delete = $connection->prepare('UPDATE kegiatan SET dihapus_pada = NOW() WHERE id = :content_id');
        $delete->execute(['content_id' => $activityId]);
        operationAudit($connection, $user, $organizationId, 'hapus_kegiatan', 'kegiatan', $activityId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Kegiatan berhasil dihapus.']);
}
