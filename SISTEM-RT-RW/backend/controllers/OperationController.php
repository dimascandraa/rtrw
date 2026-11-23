<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function operationPositiveId($value, string $label): int
{
    $id = filter_var($value, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]);
    if ($id === false) {
        jsonError(422, 'validation_error', $label . ' tidak valid.');
    }

    return (int) $id;
}

function operationText(array $body, string $key, string $label, int $maximum, bool $required = true): ?string
{
    $value = $body[$key] ?? null;
    if ($value === null && !$required) {
        return null;
    }
    if (!is_string($value)) {
        jsonError(422, 'validation_error', $label . ' harus berupa teks.');
    }
    $value = trim($value);
    if (($required && $value === '') || strlen($value) > $maximum) {
        jsonError(422, 'validation_error', $label . ' wajib diisi dan maksimal ' . $maximum . ' karakter.');
    }

    return $value === '' ? null : $value;
}

function operationDate($value, string $label): string
{
    if (!is_string($value)) {
        jsonError(422, 'validation_error', $label . ' harus memakai format YYYY-MM-DD.');
    }
    $date = DateTimeImmutable::createFromFormat('!Y-m-d', $value);
    $errors = DateTimeImmutable::getLastErrors();
    if ($date === false || ($errors !== false && ($errors['warning_count'] > 0 || $errors['error_count'] > 0))
        || $date->format('Y-m-d') !== $value) {
        jsonError(422, 'validation_error', $label . ' harus memakai tanggal yang valid.');
    }

    return $value;
}

function operationMoneyCents($value): int
{
    if (!is_string($value) && !is_int($value) && !is_float($value)) {
        jsonError(422, 'validation_error', 'Nominal harus berupa angka positif.');
    }
    $text = trim((string) $value);
    if (preg_match('/\A([0-9]{1,13})(?:\.([0-9]{1,2}))?\z/', $text, $matches) !== 1) {
        jsonError(422, 'validation_error', 'Nominal harus positif dan memiliki maksimal dua angka desimal.');
    }
    $fraction = str_pad($matches[2] ?? '', 2, '0');
    $cents = ((int) $matches[1] * 100) + (int) $fraction;
    if ($cents < 1) {
        jsonError(422, 'validation_error', 'Nominal harus lebih besar dari nol.');
    }

    return $cents;
}

function operationStoredMoneyCents($value): int
{
    if (!is_string($value) && !is_int($value) && !is_float($value)) {
        jsonError(500, 'invalid_stored_amount', 'Nominal tersimpan tidak valid.');
    }
    $text = trim((string) $value);
    if (preg_match('/\A([0-9]{1,13})(?:\.([0-9]{1,2}))?\z/', $text, $matches) !== 1) {
        jsonError(500, 'invalid_stored_amount', 'Nominal tersimpan tidak valid.');
    }

    return ((int) $matches[1] * 100) + (int) str_pad($matches[2] ?? '', 2, '0');
}

function operationMoneyText(int $cents): string
{
    return intdiv($cents, 100) . '.' . str_pad((string) ($cents % 100), 2, '0', STR_PAD_LEFT);
}

function operationOrganizationId(array $user, array $body): int
{
    if ($user['organization_id'] !== null) {
        return (int) $user['organization_id'];
    }
    if ($user['role_code'] === 'super_admin') {
        return operationPositiveId($body['organizationId'] ?? $_GET['organizationId'] ?? null, 'organizationId');
    }

    jsonError(403, 'organization_required', 'Akun ini belum terhubung ke organisasi yang dapat dikelola.');
}

function operationScope(string $organizationColumn, string $rtColumn, array $user, int $organizationId): array
{
    $where = [$organizationColumn . ' = :scope_organization_id'];
    $parameters = ['scope_organization_id' => $organizationId];
    if ($user['role_code'] !== 'super_admin' && $user['rt_id'] !== null) {
        $where[] = $rtColumn . ' = :scope_rt_id';
        $parameters['scope_rt_id'] = (int) $user['rt_id'];
    } elseif ($user['role_code'] !== 'super_admin' && $user['rw_id'] !== null) {
        $where[] = $rtColumn . ' IN (SELECT scope_rt.id FROM rt scope_rt WHERE scope_rt.id_rw = :scope_rw_id)';
        $parameters['scope_rw_id'] = (int) $user['rw_id'];
    }

    return ['where' => implode(' AND ', $where), 'parameters' => $parameters];
}

function operationAudit(PDO $connection, array $user, int $organizationId, string $action, string $table, int $id): void
{
    $statement = $connection->prepare(
        'INSERT INTO log_audit
            (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
        VALUES (:organization_id, :user_id, :action, :table_name, :record_id, :ip_address, :user_agent)'
    );
    $statement->execute([
        'organization_id' => $organizationId,
        'user_id' => (int) $user['id'],
        'action' => $action,
        'table_name' => $table,
        'record_id' => $id,
        'ip_address' => substr((string) ($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45),
        'user_agent' => substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
    ]);
}

function operationFamily(PDO $connection, array $user, int $organizationId, int $familyId, bool $lock = false): array
{
    $scope = operationScope('k.id_organisasi', 'k.id_rt', $user, $organizationId);
    $query = 'SELECT k.id, k.id_organisasi, k.id_rt FROM keluarga k
        WHERE k.id = :family_id AND k.dihapus_pada IS NULL AND ' . $scope['where']
        . ($lock ? ' FOR UPDATE' : '');
    $statement = $connection->prepare($query);
    $statement->execute(array_merge(['family_id' => $familyId], $scope['parameters']));
    $family = $statement->fetch();
    if ($family === false) {
        jsonError(404, 'family_not_found', 'Keluarga tidak ditemukan atau berada di luar wilayah Anda.');
    }

    return $family;
}

function createFamily(PDO $connection, array $user): void
{
    requirePermission($connection, $user, 'keluarga.create');
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $rtId = operationPositiveId($body['rtId'] ?? null, 'rtId');
    $familyNumber = operationText($body, 'familyNumber', 'Nomor KK', 16);
    $address = operationText($body, 'address', 'Alamat', 255);
    $houseNumber = operationText($body, 'houseNumber', 'Nomor rumah', 20, false);
    $housingType = $body['housingType'] ?? 'milik_sendiri';
    $housingTypes = ['milik_sendiri', 'kontrak', 'kos', 'kosong'];
    if (preg_match('/\A[0-9]{16}\z/', (string) $familyNumber) !== 1
        || !is_string($housingType) || !in_array($housingType, $housingTypes, true)) {
        jsonError(422, 'validation_error', 'Nomor KK harus 16 digit dan jenis hunian harus valid.');
    }

    $region = $connection->prepare(
        'SELECT id FROM rt WHERE id = :rt_id AND id_organisasi = :organization_id AND dihapus_pada IS NULL'
    );
    $region->execute(['rt_id' => $rtId, 'organization_id' => $organizationId]);
    if ($region->fetchColumn() === false) {
        jsonError(422, 'invalid_region', 'RT tidak tersedia dalam organisasi Anda.');
    }
    if ($user['role_code'] !== 'super_admin' && $user['rt_id'] !== null && (int) $user['rt_id'] !== $rtId) {
        jsonError(403, 'forbidden_region', 'Anda tidak dapat mengelola keluarga di RT lain.');
    }
    if ($user['role_code'] !== 'super_admin' && $user['rw_id'] !== null) {
        $rwCheck = $connection->prepare('SELECT id FROM rt WHERE id = :rt_id AND id_rw = :rw_id');
        $rwCheck->execute(['rt_id' => $rtId, 'rw_id' => (int) $user['rw_id']]);
        if ($rwCheck->fetchColumn() === false) {
            jsonError(403, 'forbidden_region', 'RT berada di luar wilayah RW Anda.');
        }
    }

    $duplicate = $connection->prepare('SELECT id FROM keluarga WHERE nomor_kk = :family_number LIMIT 1');
    $duplicate->execute(['family_number' => $familyNumber]);
    if ($duplicate->fetchColumn() !== false) {
        jsonError(409, 'family_exists', 'Nomor KK sudah terdaftar.');
    }

    $connection->beginTransaction();
    try {
        $insert = $connection->prepare(
            'INSERT INTO keluarga (id_organisasi, id_rt, nomor_kk, alamat, nomor_rumah, jenis_hunian)
            VALUES (:organization_id, :rt_id, :family_number, :address, :house_number, :housing_type)'
        );
        $insert->execute([
            'organization_id' => $organizationId,
            'rt_id' => $rtId,
            'family_number' => $familyNumber,
            'address' => $address,
            'house_number' => $houseNumber,
            'housing_type' => $housingType,
        ]);
        $familyId = (int) $connection->lastInsertId();
        operationAudit($connection, $user, $organizationId, 'buat_keluarga', 'keluarga', $familyId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }

    jsonSuccess(['id' => $familyId, 'message' => 'Data keluarga berhasil ditambahkan.'], 201);
}

function updateFamily(PDO $connection, array $user, int $familyId): void
{
    requirePermission($connection, $user, 'keluarga.update');
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $address = operationText($body, 'address', 'Alamat', 255);
    $houseNumber = operationText($body, 'houseNumber', 'Nomor rumah', 20, false);
    $housingType = $body['housingType'] ?? 'milik_sendiri';
    if (!is_string($housingType) || !in_array($housingType, ['milik_sendiri', 'kontrak', 'kos', 'kosong'], true)) {
        jsonError(422, 'validation_error', 'Jenis hunian tidak valid.');
    }

    $connection->beginTransaction();
    try {
        $family = operationFamily($connection, $user, $organizationId, $familyId, true);
        $update = $connection->prepare(
            'UPDATE keluarga SET alamat = :address, nomor_rumah = :house_number, jenis_hunian = :housing_type
            WHERE id = :family_id'
        );
        $update->execute([
            'address' => $address,
            'house_number' => $houseNumber,
            'housing_type' => $housingType,
            'family_id' => $familyId,
        ]);
        operationAudit($connection, $user, $organizationId, 'ubah_keluarga', 'keluarga', (int) $family['id']);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Data keluarga berhasil diperbarui.']);
}

function deleteFamily(PDO $connection, array $user, int $familyId): void
{
    requirePermission($connection, $user, 'keluarga.delete');
    $organizationId = operationOrganizationId($user, []);
    $connection->beginTransaction();
    try {
        $family = operationFamily($connection, $user, $organizationId, $familyId, true);
        $dependents = $connection->prepare(
            'SELECT
                (SELECT COUNT(*) FROM warga WHERE id_keluarga = :resident_family AND dihapus_pada IS NULL)
                + (SELECT COUNT(*) FROM tagihan WHERE id_keluarga = :invoice_family) AS total'
        );
        $dependents->execute(['resident_family' => $familyId, 'invoice_family' => $familyId]);
        if ((int) $dependents->fetchColumn() > 0) {
            $connection->rollBack();
            jsonError(409, 'family_in_use', 'Keluarga yang masih memiliki warga atau tagihan tidak dapat dihapus.');
        }
        $update = $connection->prepare('UPDATE keluarga SET dihapus_pada = NOW() WHERE id = :family_id');
        $update->execute(['family_id' => $familyId]);
        operationAudit($connection, $user, $organizationId, 'hapus_keluarga', 'keluarga', (int) $family['id']);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Data keluarga berhasil dihapus.']);
}

function residentWriteScope(PDO $connection, array $user, int $organizationId, int $residentId, bool $lock = false): array
{
    $scope = operationScope('w.id_organisasi', 'k.id_rt', $user, $organizationId);
    $query = 'SELECT w.id, w.id_organisasi, w.id_keluarga, w.nama_lengkap
        FROM warga w
        INNER JOIN keluarga k ON k.id = w.id_keluarga
        WHERE w.id = :resident_id AND w.dihapus_pada IS NULL AND k.dihapus_pada IS NULL
          AND ' . $scope['where'] . ($lock ? ' FOR UPDATE' : '');
    $statement = $connection->prepare($query);
    $statement->execute(array_merge(['resident_id' => $residentId], $scope['parameters']));
    $resident = $statement->fetch();
    if ($resident === false) {
        jsonError(404, 'resident_not_found', 'Data warga tidak ditemukan atau berada di luar wilayah Anda.');
    }

    return $resident;
}

function createResident(PDO $connection, array $user): void
{
    requirePermission($connection, $user, 'warga.create');
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $familyId = operationPositiveId($body['familyId'] ?? null, 'familyId');
    $nik = $body['nik'] ?? null;
    if (!is_string($nik) || preg_match('/\A[0-9]{16}\z/', $nik) !== 1) {
        jsonError(422, 'validation_error', 'NIK harus terdiri dari 16 digit.');
    }
    $name = operationText($body, 'name', 'Nama lengkap', 150);
    $sex = $body['sex'] ?? null;
    $relationship = $body['relationship'] ?? 'lainnya';
    $sexValues = ['L', 'P'];
    $relationshipValues = ['kepala_keluarga', 'suami', 'istri', 'anak', 'orang_tua', 'famili_lain', 'lainnya'];
    if (!is_string($sex) || !in_array($sex, $sexValues, true)
        || !is_string($relationship) || !in_array($relationship, $relationshipValues, true)) {
        jsonError(422, 'validation_error', 'Jenis kelamin atau hubungan keluarga tidak valid.');
    }
    $status = $body['status'] ?? 'aktif';
    $livingStatus = $body['livingStatus'] ?? 'tetap';
    if (!is_string($status) || !in_array($status, ['aktif', 'pindah', 'meninggal', 'nonaktif'], true)
        || !is_string($livingStatus) || !in_array($livingStatus, ['tetap', 'kontrak', 'kos', 'sementara'], true)) {
        jsonError(422, 'validation_error', 'Status warga atau status tinggal tidak valid.');
    }
    $birthPlace = operationText($body, 'birthPlace', 'Tempat lahir', 100, false);
    $birthDate = isset($body['birthDate']) && $body['birthDate'] !== ''
        ? operationDate($body['birthDate'], 'Tanggal lahir') : null;
    $occupation = operationText($body, 'occupation', 'Pekerjaan', 100, false);
    $phone = operationText($body, 'phone', 'Nomor telepon', 20, false);

    $duplicate = $connection->prepare('SELECT id FROM warga WHERE nik = :nik LIMIT 1');
    $duplicate->execute(['nik' => $nik]);
    if ($duplicate->fetchColumn() !== false) {
        jsonError(409, 'resident_exists', 'NIK sudah terdaftar.');
    }

    $connection->beginTransaction();
    try {
        operationFamily($connection, $user, $organizationId, $familyId, true);
        $insert = $connection->prepare(
            'INSERT INTO warga
                (id_organisasi, id_keluarga, nik, nama_lengkap, tempat_lahir, tanggal_lahir,
                 jenis_kelamin, hubungan_keluarga, pekerjaan, telepon, status_tinggal, status)
            VALUES
                (:organization_id, :family_id, :nik, :name, :birth_place, :birth_date,
                 :sex, :relationship, :occupation, :phone, :living_status, :status)'
        );
        $insert->execute([
            'organization_id' => $organizationId,
            'family_id' => $familyId,
            'nik' => $nik,
            'name' => $name,
            'birth_place' => $birthPlace,
            'birth_date' => $birthDate,
            'sex' => $sex,
            'relationship' => $relationship,
            'occupation' => $occupation,
            'phone' => $phone,
            'living_status' => $livingStatus,
            'status' => $status,
        ]);
        $residentId = (int) $connection->lastInsertId();
        operationAudit($connection, $user, $organizationId, 'buat_warga', 'warga', $residentId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['id' => $residentId, 'message' => 'Data warga berhasil ditambahkan.'], 201);
}

function updateResident(PDO $connection, array $user, int $residentId): void
{
    requirePermission($connection, $user, 'warga.update');
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $sets = [];
    $parameters = ['resident_id' => $residentId];
    $textFields = [
        'name' => ['nama_lengkap', 'Nama lengkap', 150, true],
        'birthPlace' => ['tempat_lahir', 'Tempat lahir', 100, false],
        'occupation' => ['pekerjaan', 'Pekerjaan', 100, false],
        'phone' => ['telepon', 'Nomor telepon', 20, false],
    ];
    foreach ($textFields as $key => $field) {
        if (array_key_exists($key, $body)) {
            $sets[] = $field[0] . ' = :' . $key;
            $parameters[$key] = operationText($body, $key, $field[1], $field[2], $field[3]);
        }
    }
    $enumFields = [
        'sex' => ['jenis_kelamin', ['L', 'P']],
        'relationship' => ['hubungan_keluarga', ['kepala_keluarga', 'suami', 'istri', 'anak', 'orang_tua', 'famili_lain', 'lainnya']],
        'status' => ['status', ['aktif', 'pindah', 'meninggal', 'nonaktif']],
        'livingStatus' => ['status_tinggal', ['tetap', 'kontrak', 'kos', 'sementara']],
    ];
    foreach ($enumFields as $key => $field) {
        if (array_key_exists($key, $body)) {
            if (!is_string($body[$key]) || !in_array($body[$key], $field[1], true)) {
                jsonError(422, 'validation_error', 'Nilai ' . $key . ' tidak valid.');
            }
            $sets[] = $field[0] . ' = :' . $key;
            $parameters[$key] = $body[$key];
        }
    }
    if (array_key_exists('birthDate', $body)) {
        $sets[] = 'tanggal_lahir = :birth_date';
        $parameters['birth_date'] = $body['birthDate'] === ''
            ? null : operationDate($body['birthDate'], 'Tanggal lahir');
    }
    if ($sets === []) {
        jsonError(422, 'validation_error', 'Setidaknya satu kolom profil warga harus diperbarui.');
    }

    $connection->beginTransaction();
    try {
        residentWriteScope($connection, $user, $organizationId, $residentId, true);
        $update = $connection->prepare('UPDATE warga SET ' . implode(', ', $sets) . ' WHERE id = :resident_id');
        $update->execute($parameters);
        operationAudit($connection, $user, $organizationId, 'ubah_warga', 'warga', $residentId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Data warga berhasil diperbarui.']);
}

function deleteResident(PDO $connection, array $user, int $residentId): void
{
    requirePermission($connection, $user, 'warga.delete');
    $organizationId = operationOrganizationId($user, []);
    $connection->beginTransaction();
    try {
        residentWriteScope($connection, $user, $organizationId, $residentId, true);
        $account = $connection->prepare('SELECT id FROM pengguna WHERE id_warga = :resident_id LIMIT 1 FOR UPDATE');
        $account->execute(['resident_id' => $residentId]);
        if ($account->fetchColumn() !== false) {
            $connection->rollBack();
            jsonError(409, 'resident_has_account', 'Data warga terhubung ke akun. Nonaktifkan akun terlebih dahulu.');
        }
        $updateFamily = $connection->prepare(
            'UPDATE keluarga SET id_kepala_keluarga = NULL WHERE id_kepala_keluarga = :resident_id'
        );
        $updateFamily->execute(['resident_id' => $residentId]);
        $updateResident = $connection->prepare(
            'UPDATE warga SET status = \'nonaktif\', dihapus_pada = NOW() WHERE id = :resident_id'
        );
        $updateResident->execute(['resident_id' => $residentId]);
        operationAudit($connection, $user, $organizationId, 'hapus_warga', 'warga', $residentId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Data warga berhasil dihapus.']);
}

function invoiceWriteScope(PDO $connection, array $user, int $organizationId, int $invoiceId, bool $lock = false): array
{
    $scope = operationScope('t.id_organisasi', 'k.id_rt', $user, $organizationId);
    $query = 'SELECT t.id, t.id_organisasi, t.id_keluarga, t.id_periode_tagihan, t.total_nominal,
            t.nominal_dibayar, t.status, t.tanggal_jatuh_tempo
        FROM tagihan t
        INNER JOIN keluarga k ON k.id = t.id_keluarga
        WHERE t.id = :invoice_id AND ' . $scope['where'] . ($lock ? ' FOR UPDATE' : '');
    $statement = $connection->prepare($query);
    $statement->execute(array_merge(['invoice_id' => $invoiceId], $scope['parameters']));
    $invoice = $statement->fetch();
    if ($invoice === false) {
        jsonError(404, 'invoice_not_found', 'Tagihan tidak ditemukan atau berada di luar wilayah Anda.');
    }

    return $invoice;
}

function createInvoice(PDO $connection, array $user): void
{
    requirePermission($connection, $user, 'tagihan.create');
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $familyId = operationPositiveId($body['familyId'] ?? null, 'familyId');
    $periodId = operationPositiveId($body['periodId'] ?? null, 'periodId');
    $categoryId = operationPositiveId($body['categoryId'] ?? null, 'categoryId');
    $amount = operationMoneyCents($body['amount'] ?? null);
    $dueDate = operationDate($body['dueDate'] ?? null, 'Tanggal jatuh tempo');
    $invoiceStatus = $dueDate < date('Y-m-d') ? 'lewat_jatuh_tempo' : 'belum_dibayar';
    $description = operationText($body, 'description', 'Deskripsi', 255, false);

    $period = $connection->prepare(
        'SELECT id FROM periode_tagihan
        WHERE id = :period_id AND id_organisasi = :organization_id AND status = \'terbit\''
    );
    $period->execute(['period_id' => $periodId, 'organization_id' => $organizationId]);
    if ($period->fetchColumn() === false) {
        jsonError(422, 'invalid_period', 'Periode tagihan tidak tersedia atau belum diterbitkan.');
    }
    $category = $connection->prepare(
        'SELECT id FROM kategori_iuran WHERE id = :category_id AND id_organisasi = :organization_id AND aktif = 1'
    );
    $category->execute(['category_id' => $categoryId, 'organization_id' => $organizationId]);
    if ($category->fetchColumn() === false) {
        jsonError(422, 'invalid_category', 'Kategori iuran tidak tersedia di organisasi Anda.');
    }
    $existing = $connection->prepare(
        'SELECT id FROM tagihan WHERE id_keluarga = :family_id AND id_periode_tagihan = :period_id LIMIT 1'
    );
    $existing->execute(['family_id' => $familyId, 'period_id' => $periodId]);
    if ($existing->fetchColumn() !== false) {
        jsonError(409, 'invoice_exists', 'Keluarga sudah memiliki tagihan pada periode tersebut.');
    }

    $connection->beginTransaction();
    try {
        operationFamily($connection, $user, $organizationId, $familyId, true);
        $insert = $connection->prepare(
            'INSERT INTO tagihan
                (id_organisasi, id_keluarga, id_periode_tagihan, nomor_tagihan, total_nominal,
                 nominal_dibayar, status, tanggal_jatuh_tempo)
            VALUES
                (:organization_id, :family_id, :period_id, :invoice_number, :amount, 0,
                 :status, :due_date)'
        );
        $insert->execute([
            'organization_id' => $organizationId,
            'family_id' => $familyId,
            'period_id' => $periodId,
            'invoice_number' => 'TAG-' . date('Ymd') . '-' . strtoupper(bin2hex(random_bytes(4))),
            'amount' => operationMoneyText($amount),
            'status' => $invoiceStatus,
            'due_date' => $dueDate,
        ]);
        $invoiceId = (int) $connection->lastInsertId();
        $detail = $connection->prepare(
            'INSERT INTO rincian_tagihan (id_tagihan, id_kategori_iuran, deskripsi, nominal)
            VALUES (:invoice_id, :category_id, :description, :amount)'
        );
        $detail->execute([
            'invoice_id' => $invoiceId,
            'category_id' => $categoryId,
            'description' => $description,
            'amount' => operationMoneyText($amount),
        ]);
        operationAudit($connection, $user, $organizationId, 'buat_tagihan', 'tagihan', $invoiceId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['id' => $invoiceId, 'message' => 'Tagihan berhasil dibuat.'], 201);
}

function updateInvoice(PDO $connection, array $user, int $invoiceId): void
{
    requirePermission($connection, $user, 'tagihan.update');
    $body = jsonRequestBody();
    $organizationId = operationOrganizationId($user, $body);
    $categoryId = operationPositiveId($body['categoryId'] ?? null, 'categoryId');
    $amount = operationMoneyCents($body['amount'] ?? null);
    $dueDate = operationDate($body['dueDate'] ?? null, 'Tanggal jatuh tempo');
    $invoiceStatus = $dueDate < date('Y-m-d') ? 'lewat_jatuh_tempo' : 'belum_dibayar';
    $description = operationText($body, 'description', 'Deskripsi', 255, false);
    $category = $connection->prepare(
        'SELECT id FROM kategori_iuran WHERE id = :category_id AND id_organisasi = :organization_id AND aktif = 1'
    );
    $category->execute(['category_id' => $categoryId, 'organization_id' => $organizationId]);
    if ($category->fetchColumn() === false) {
        jsonError(422, 'invalid_category', 'Kategori iuran tidak tersedia di organisasi Anda.');
    }

    $connection->beginTransaction();
    try {
        $invoice = invoiceWriteScope($connection, $user, $organizationId, $invoiceId, true);
        $payments = $connection->prepare('SELECT COUNT(*) FROM pembayaran WHERE id_tagihan = :invoice_id');
        $payments->execute(['invoice_id' => $invoiceId]);
        if (operationStoredMoneyCents($invoice['nominal_dibayar']) !== 0
            || $invoice['status'] === 'batal' || (int) $payments->fetchColumn() > 0) {
            $connection->rollBack();
            jsonError(409, 'invoice_locked', 'Tagihan yang memiliki riwayat pembayaran atau dibatalkan tidak dapat diubah.');
        }
        $update = $connection->prepare(
            'UPDATE tagihan SET total_nominal = :amount, tanggal_jatuh_tempo = :due_date,
                status = :status WHERE id = :invoice_id'
        );
        $update->execute([
            'amount' => operationMoneyText($amount),
            'due_date' => $dueDate,
            'status' => $invoiceStatus,
            'invoice_id' => $invoiceId,
        ]);
        $deleteDetails = $connection->prepare('DELETE FROM rincian_tagihan WHERE id_tagihan = :invoice_id');
        $deleteDetails->execute(['invoice_id' => $invoiceId]);
        $detail = $connection->prepare(
            'INSERT INTO rincian_tagihan (id_tagihan, id_kategori_iuran, deskripsi, nominal)
            VALUES (:invoice_id, :category_id, :description, :amount)'
        );
        $detail->execute([
            'invoice_id' => $invoiceId,
            'category_id' => $categoryId,
            'description' => $description,
            'amount' => operationMoneyText($amount),
        ]);
        operationAudit($connection, $user, $organizationId, 'ubah_tagihan', 'tagihan', $invoiceId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Tagihan berhasil diperbarui.']);
}

function deleteInvoice(PDO $connection, array $user, int $invoiceId): void
{
    requirePermission($connection, $user, 'tagihan.delete');
    $organizationId = operationOrganizationId($user, []);
    $connection->beginTransaction();
    try {
        $invoice = invoiceWriteScope($connection, $user, $organizationId, $invoiceId, true);
        $payments = $connection->prepare('SELECT COUNT(*) FROM pembayaran WHERE id_tagihan = :invoice_id');
        $payments->execute(['invoice_id' => $invoiceId]);
        if ((int) $payments->fetchColumn() > 0) {
            $connection->rollBack();
            jsonError(409, 'invoice_has_payments', 'Tagihan yang memiliki riwayat pembayaran tidak dapat dihapus.');
        }
        if ($invoice['status'] === 'batal') {
            $connection->rollBack();
            jsonError(409, 'invoice_cancelled', 'Tagihan sudah dibatalkan.');
        }
        $update = $connection->prepare('UPDATE tagihan SET status = \'batal\' WHERE id = :invoice_id');
        $update->execute(['invoice_id' => $invoiceId]);
        operationAudit($connection, $user, $organizationId, 'batalkan_tagihan', 'tagihan', $invoiceId);
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => 'Tagihan berhasil dibatalkan.']);
}

function decidePayment(PDO $connection, array $user, int $paymentId): void
{
    requirePermission($connection, $user, 'pembayaran.update');
    $body = jsonRequestBody();
    $decision = $body['keputusan'] ?? null;
    $noteInput = $body['catatan'] ?? '';
    if (!is_string($decision) || !in_array($decision, ['setuju', 'tolak'], true)
        || !is_string($noteInput) || strlen(trim($noteInput)) > 255) {
        jsonError(422, 'validation_error', 'Keputusan harus setuju/tolak dan catatan maksimal 255 karakter.');
    }
    $organizationId = operationOrganizationId($user, $body);
    $scope = operationScope('p.id_organisasi', 'k.id_rt', $user, $organizationId);
    $connection->beginTransaction();
    try {
        $query = 'SELECT p.id, p.id_organisasi, p.id_tagihan, p.id_pengguna_pembayar, p.nominal,
                p.status, p.dibayar_pada, t.total_nominal, t.nominal_dibayar, t.status AS invoice_status,
                t.nomor_tagihan
            FROM pembayaran p
            INNER JOIN tagihan t ON t.id = p.id_tagihan
            INNER JOIN keluarga k ON k.id = t.id_keluarga
            WHERE p.id = :payment_id AND ' . $scope['where'] . ' FOR UPDATE';
        $paymentQuery = $connection->prepare($query);
        $paymentQuery->execute(array_merge(['payment_id' => $paymentId], $scope['parameters']));
        $payment = $paymentQuery->fetch();
        if ($payment === false) {
            $connection->rollBack();
            jsonError(404, 'payment_not_found', 'Pembayaran tidak ditemukan atau berada di luar wilayah Anda.');
        }
        if ($payment['status'] !== 'menunggu_verifikasi') {
            $connection->rollBack();
            jsonError(409, 'payment_already_decided', 'Pembayaran sudah pernah diproses.');
        }

        $newStatus = $decision === 'setuju' ? 'terverifikasi' : 'ditolak';
        $note = trim($noteInput);
        $updatePayment = $connection->prepare(
            'UPDATE pembayaran SET status = :status, diverifikasi_oleh = :actor_id,
                diverifikasi_pada = NOW(), catatan_verifikasi = :note
            WHERE id = :payment_id'
        );
        $updatePayment->execute([
            'status' => $newStatus,
            'actor_id' => (int) $user['id'],
            'note' => $note === '' ? null : $note,
            'payment_id' => $paymentId,
        ]);

        if ($decision === 'setuju') {
            $paidCents = operationStoredMoneyCents($payment['nominal']);
            $currentCents = operationStoredMoneyCents($payment['nominal_dibayar']);
            $totalCents = operationStoredMoneyCents($payment['total_nominal']);
            if ($currentCents + $paidCents > $totalCents) {
                $connection->rollBack();
                jsonError(409, 'payment_exceeds_balance', 'Nominal pembayaran melebihi sisa tagihan.');
            }
            $newPaid = $currentCents + $paidCents;
            $invoiceStatus = $newPaid >= $totalCents ? 'lunas'
                : ($newPaid > 0 ? 'sebagian'
                    : ($payment['invoice_status'] === 'lewat_jatuh_tempo' ? 'lewat_jatuh_tempo' : 'belum_dibayar'));
            $updateInvoice = $connection->prepare(
                'UPDATE tagihan SET nominal_dibayar = :paid, status = :status WHERE id = :invoice_id'
            );
            $updateInvoice->execute([
                'paid' => operationMoneyText($newPaid),
                'status' => $invoiceStatus,
                'invoice_id' => (int) $payment['id_tagihan'],
            ]);
            $cash = $connection->prepare(
                'INSERT INTO transaksi_kas
                    (id_organisasi, jenis_transaksi, id_pembayaran, nominal, tanggal_transaksi, keterangan, dicatat_oleh)
                VALUES (:organization_id, \'masuk\', :payment_id, :amount, DATE(:paid_at), :description, :actor_id)'
            );
            $cash->execute([
                'organization_id' => $organizationId,
                'payment_id' => $paymentId,
                'amount' => operationMoneyText($paidCents),
                'paid_at' => $payment['dibayar_pada'],
                'description' => 'Pembayaran tagihan ' . $payment['nomor_tagihan'],
                'actor_id' => (int) $user['id'],
            ]);
        }

        $proofStatus = $decision === 'setuju' ? 'diterima' : 'ditolak';
        $updateProofs = $connection->prepare(
            'UPDATE bukti_pembayaran SET status_verifikasi = :status, catatan = :note
            WHERE id_pembayaran = :payment_id AND status_verifikasi = \'menunggu\''
        );
        $updateProofs->execute([
            'status' => $proofStatus,
            'note' => $note === '' ? null : $note,
            'payment_id' => $paymentId,
        ]);
        operationAudit(
            $connection,
            $user,
            $organizationId,
            $decision === 'setuju' ? 'verifikasi_pembayaran' : 'tolak_pembayaran',
            'pembayaran',
            $paymentId
        );
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
    jsonSuccess(['message' => $decision === 'setuju' ? 'Pembayaran berhasil diverifikasi.' : 'Pembayaran ditolak.']);
}
