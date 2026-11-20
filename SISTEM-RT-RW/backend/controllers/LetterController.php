<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function createLetterRequest(PDO $connection, array $user): void
{
    requirePermission($connection, $user, 'pengajuan_surat.create');
    if ($user['id_warga'] === null || $user['organization_id'] === null) {
        jsonError(403, 'resident_required', 'Akun belum terhubung ke data warga aktif. Hubungi pengurus RT/RW.');
    }
    $residentQuery = $connection->prepare(
        'SELECT id FROM warga
        WHERE id = :resident_id AND id_organisasi = :organization_id
          AND status = \'aktif\' AND dihapus_pada IS NULL'
    );
    $residentQuery->execute([
        'resident_id' => $user['id_warga'],
        'organization_id' => $user['organization_id'],
    ]);
    if ($residentQuery->fetchColumn() === false) {
        jsonError(403, 'resident_inactive', 'Data warga belum aktif. Hubungi pengurus RT/RW.');
    }
    $body = jsonRequestBody();
    $typeId = filter_var($body['jenisSuratId'] ?? null, FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 1],
    ]);
    $purposeInput = $body['keperluan'] ?? null;
    if (!is_string($purposeInput)) {
        jsonError(422, 'validation_error', 'Keperluan harus berupa teks.');
    }
    $purpose = trim($purposeInput);
    if ($typeId === false || $purpose === '' || strlen($purpose) > 255) {
        jsonError(422, 'validation_error', 'Pilih jenis surat dan isi keperluan maksimal 255 karakter.');
    }

    $typeQuery = $connection->prepare(
        'SELECT id FROM jenis_surat
        WHERE id = :type_id AND id_organisasi = :organization_id AND aktif = 1'
    );
    $typeQuery->execute([
        'type_id' => $typeId,
        'organization_id' => $user['organization_id'],
    ]);
    if ($typeQuery->fetchColumn() === false) {
        jsonError(422, 'invalid_letter_type', 'Jenis surat tidak tersedia di wilayah Anda.');
    }

    $connection->beginTransaction();
    try {
        $insert = $connection->prepare(
            'INSERT INTO pengajuan_surat
                (id_organisasi, id_warga, id_jenis_surat, id_pengguna_pengaju, keperluan, status)
            VALUES (:organization_id, :resident_id, :type_id, :user_id, :purpose, \'diajukan\')'
        );
        $insert->execute([
            'organization_id' => $user['organization_id'],
            'resident_id' => $user['id_warga'],
            'type_id' => $typeId,
            'user_id' => $user['id'],
            'purpose' => $purpose,
        ]);
        $letterId = (int) $connection->lastInsertId();
        $audit = $connection->prepare(
            'INSERT INTO log_audit (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
            VALUES (:organization_id, :user_id, \'ajukan_surat\', \'pengajuan_surat\', :record_id, :ip_address, :user_agent)'
        );
        $audit->execute([
            'organization_id' => $user['organization_id'],
            'user_id' => $user['id'],
            'record_id' => $letterId,
            'ip_address' => substr((string) ($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45),
            'user_agent' => substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
        ]);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }

    jsonSuccess(['message' => 'Pengajuan surat berhasil dikirim.', 'id' => $letterId], 201);
}

function decideLetterRequest(PDO $connection, array $user, int $letterId): void
{
    if (!in_array($user['role_code'], ['super_admin', 'admin_rw', 'ketua_rw', 'ketua_rt', 'sekretaris'], true)) {
        jsonError(403, 'forbidden', 'Peran Anda tidak dapat memproses pengajuan surat.');
    }
    requirePermission($connection, $user, 'pengajuan_surat.approve');
    $body = jsonRequestBody();
    $decision = $body['keputusan'] ?? '';
    $noteInput = $body['catatan'] ?? '';
    if (!is_string($decision) || !is_string($noteInput)) {
        jsonError(422, 'validation_error', 'Format keputusan surat tidak valid.');
    }
    $note = trim($noteInput);
    if (!in_array($decision, ['setuju', 'tolak'], true) || strlen($note) > 500) {
        jsonError(422, 'validation_error', 'Keputusan harus setuju atau tolak dan catatan maksimal 500 karakter.');
    }

    $connection->beginTransaction();
    try {
        $query = 'SELECT s.id, s.id_organisasi, s.status
            FROM pengajuan_surat s
            INNER JOIN warga w ON w.id = s.id_warga
            INNER JOIN keluarga k ON k.id = w.id_keluarga
            INNER JOIN rt ON rt.id = k.id_rt
            WHERE s.id = :letter_id AND s.status IN (\'diajukan\', \'diproses\')';
        $parameters = ['letter_id' => $letterId];
        if ($user['role_code'] !== 'super_admin') {
            $query .= ' AND s.id_organisasi = :organization_id';
            $parameters['organization_id'] = (int) $user['organization_id'];
            if ($user['role_code'] === 'ketua_rt') {
                $query .= ' AND rt.id = :rt_id';
                $parameters['rt_id'] = (int) $user['rt_id'];
            } elseif ($user['rw_id'] !== null) {
                $query .= ' AND rt.id_rw = :rw_id';
                $parameters['rw_id'] = (int) $user['rw_id'];
            }
        }
        $query .= ' FOR UPDATE';
        $letterQuery = $connection->prepare($query);
        $letterQuery->execute($parameters);
        $letter = $letterQuery->fetch();
        if ($letter === false) {
            $connection->rollBack();
            jsonError(404, 'letter_not_found', 'Pengajuan tidak ditemukan atau berada di luar wilayah Anda.');
        }

        $newStatus = $decision === 'setuju' ? 'disetujui' : 'ditolak';
        $update = $connection->prepare(
            'UPDATE pengajuan_surat SET status = :status, diproses_oleh = :approver_id WHERE id = :letter_id'
        );
        $update->execute([
            'status' => $newStatus,
            'approver_id' => $user['id'],
            'letter_id' => $letterId,
        ]);
        $history = $connection->prepare(
            'INSERT INTO persetujuan_surat
                (id_pengajuan_surat, id_pengguna_penyetuju, tahap, keputusan, catatan)
            VALUES (:letter_id, :approver_id, 1, :decision, :note)'
        );
        $history->execute([
            'letter_id' => $letterId,
            'approver_id' => $user['id'],
            'decision' => $decision,
            'note' => $note === '' ? null : $note,
        ]);
        $audit = $connection->prepare(
            'INSERT INTO log_audit (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
            VALUES (:organization_id, :user_id, :action, \'pengajuan_surat\', :record_id, :ip_address, :user_agent)'
        );
        $audit->execute([
            'organization_id' => $letter['id_organisasi'],
            'user_id' => $user['id'],
            'action' => $decision === 'setuju' ? 'setujui_surat' : 'tolak_surat',
            'record_id' => $letterId,
            'ip_address' => substr((string) ($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45),
            'user_agent' => substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
        ]);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }

    jsonSuccess(['message' => $decision === 'setuju' ? 'Pengajuan surat disetujui.' : 'Pengajuan surat ditolak.']);
}
