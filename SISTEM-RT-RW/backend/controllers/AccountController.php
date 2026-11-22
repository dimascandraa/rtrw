<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function registerAccount(PDO $connection): void
{
    $body = jsonRequestBody();
    $nameInput = $body['name'] ?? null;
    $emailInput = $body['email'] ?? '';
    $phoneInput = $body['phone'] ?? null;
    $password = $body['password'] ?? '';

    if (!is_string($nameInput) || !is_string($emailInput) || !is_string($phoneInput)
        || !is_string($password)) {
        jsonError(422, 'validation_error', 'Format data pendaftaran tidak valid.');
    }
    $name = trim($nameInput);
    $email = strtolower(trim($emailInput));
    $phone = trim($phoneInput);
    if ($name === '' || strlen($name) > 150
        || $phone === ''
        || ($email !== '' && (strlen($email) > 150 || filter_var($email, FILTER_VALIDATE_EMAIL) === false))
        || strlen($phone) > 20 || strlen($password) < 8 || strlen($password) > 255) {
        jsonError(422, 'validation_error', 'Nama, email atau telepon yang valid, dan kata sandi minimal 8 karakter wajib diisi.');
    }

    $duplicate = $connection->prepare(
        'SELECT id FROM pengguna
        WHERE ((:email_check <> \'\' AND email = :email) OR (:phone_check <> \'\' AND telepon = :phone))
        LIMIT 1'
    );
    $duplicate->execute([
        'email_check' => $email,
        'email' => $email,
        'phone_check' => $phone,
        'phone' => $phone,
    ]);
    if ($duplicate->fetchColumn() !== false) {
        jsonError(409, 'account_exists', 'Email atau nomor telepon sudah terdaftar.');
    }

    $connection->beginTransaction();
    try {
        $insert = $connection->prepare(
            'INSERT INTO pengguna (nama, email, telepon, hash_kata_sandi, status)
            VALUES (:name, :email, :phone, :password_hash, \'menunggu_verifikasi\')'
        );
        $insert->execute([
            'name' => $name,
            'email' => $email === '' ? null : $email,
            'phone' => $phone === '' ? null : $phone,
            'password_hash' => password_hash($password, PASSWORD_DEFAULT),
        ]);
        $userId = (int) $connection->lastInsertId();
        $audit = $connection->prepare(
            'INSERT INTO log_audit (id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
            VALUES (:user_id, \'registrasi\', \'pengguna\', :record_id, :ip_address, :user_agent)'
        );
        $audit->execute([
            'user_id' => $userId,
            'record_id' => $userId,
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

    jsonSuccess([
        'message' => 'Pendaftaran berhasil. Akun menunggu persetujuan pengurus RT/RW atau admin.',
        'status' => 'menunggu_verifikasi',
    ], 201);
}

function updateAccountProfile(PDO $connection): void
{
    $user = authenticatedUser($connection);
    $body = jsonRequestBody();
    $nameInput = $body['name'] ?? null;
    $emailInput = $body['email'] ?? '';
    $phoneInput = $body['phone'] ?? '';
    if (!is_string($nameInput) || !is_string($emailInput) || !is_string($phoneInput)) {
        jsonError(422, 'validation_error', 'Format data profil tidak valid.');
    }
    $name = trim($nameInput);
    $email = strtolower(trim($emailInput));
    $phone = trim($phoneInput);

    if ($name === '' || strlen($name) > 150
        || ($email !== '' && (strlen($email) > 150 || filter_var($email, FILTER_VALIDATE_EMAIL) === false))
        || strlen($phone) > 20) {
        jsonError(422, 'validation_error', 'Nama wajib diisi. Periksa format email dan nomor telepon.');
    }
    if ($email === '' && $phone === '') {
        jsonError(422, 'validation_error', 'Email atau nomor telepon harus tetap tersedia untuk masuk.');
    }

    $duplicate = $connection->prepare(
        'SELECT id FROM pengguna
        WHERE id <> :user_id
          AND ((:email_check <> \'\' AND email = :email) OR (:phone_check <> \'\' AND telepon = :phone))
        LIMIT 1'
    );
    $duplicate->execute([
        'user_id' => $user['id'],
        'email_check' => $email,
        'email' => $email,
        'phone_check' => $phone,
        'phone' => $phone,
    ]);
    if ($duplicate->fetchColumn() !== false) {
        jsonError(409, 'account_exists', 'Email atau nomor telepon sudah digunakan akun lain.');
    }

    $update = $connection->prepare(
        'UPDATE pengguna
        SET nama = :name, email = :email, telepon = :phone,
            email_terverifikasi_pada = :email_verified_at,
            telepon_terverifikasi_pada = :phone_verified_at
        WHERE id = :user_id'
    );
    $connection->beginTransaction();
    try {
        $verification = $connection->prepare(
            'SELECT email, telepon, email_terverifikasi_pada, telepon_terverifikasi_pada
            FROM pengguna WHERE id = :user_id FOR UPDATE'
        );
        $verification->execute(['user_id' => $user['id']]);
        $previousProfile = $verification->fetch();
        if ($previousProfile === false) {
            $connection->rollBack();
            jsonError(404, 'account_not_found', 'Akun tidak ditemukan.');
        }
        $update->execute([
            'name' => $name,
            'email' => $email === '' ? null : $email,
            'phone' => $phone === '' ? null : $phone,
            'email_verified_at' => $previousProfile['email'] === ($email === '' ? null : $email)
                ? $previousProfile['email_terverifikasi_pada'] : null,
            'phone_verified_at' => $previousProfile['telepon'] === ($phone === '' ? null : $phone)
                ? $previousProfile['telepon_terverifikasi_pada'] : null,
            'user_id' => $user['id'],
        ]);
        writeAccountAudit($connection, $user, 'ubah_profil');
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }

    $user['name'] = $name;
    $user['email'] = $email === '' ? null : $email;
    $user['phone'] = $phone === '' ? null : $phone;
    jsonSuccess(['user' => publicUser($connection, $user)]);
}

function changeAccountPassword(PDO $connection): void
{
    $user = authenticatedUser($connection);
    $body = jsonRequestBody();
    $currentPassword = $body['currentPassword'] ?? '';
    $newPassword = $body['newPassword'] ?? '';
    if (!is_string($currentPassword) || !is_string($newPassword)
        || $currentPassword === '' || strlen($newPassword) < 8 || strlen($newPassword) > 255) {
        jsonError(422, 'validation_error', 'Kata sandi saat ini wajib diisi dan kata sandi baru minimal 8 karakter.');
    }

    $passwordQuery = $connection->prepare('SELECT hash_kata_sandi FROM pengguna WHERE id = :user_id');
    $passwordQuery->execute(['user_id' => $user['id']]);
    $passwordHash = $passwordQuery->fetchColumn();
    if (!is_string($passwordHash) || !password_verify($currentPassword, $passwordHash)) {
        jsonError(422, 'invalid_password', 'Kata sandi saat ini tidak sesuai.');
    }

    $connection->beginTransaction();
    try {
        $updatePassword = $connection->prepare(
            'UPDATE pengguna SET hash_kata_sandi = :password_hash WHERE id = :user_id'
        );
        $updatePassword->execute([
            'password_hash' => password_hash($newPassword, PASSWORD_DEFAULT),
            'user_id' => $user['id'],
        ]);
        $revokeOtherSessions = $connection->prepare(
            'UPDATE sesi_pengguna
            SET dicabut_pada = NOW()
            WHERE id_pengguna = :user_id AND id <> :session_id AND dicabut_pada IS NULL'
        );
        $revokeOtherSessions->execute([
            'user_id' => $user['id'],
            'session_id' => $user['session_id'],
        ]);
        writeAccountAudit($connection, $user, 'ubah_kata_sandi');
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }

    jsonSuccess(['message' => 'Kata sandi berhasil diperbarui.']);
}

function pendingRegistrations(PDO $connection, array $user): void
{
    requireRegistrationApprover($user);

    $query = 'SELECT u.id, u.nama, u.email, u.telepon, u.dibuat_pada,
            w.id AS id_warga, w.nama_lengkap AS nama_warga,
            rt.nomor_rt, rw.nomor_rw
        FROM pengguna u
        LEFT JOIN warga w ON (
            (u.id_warga IS NOT NULL AND w.id = u.id_warga)
            OR (u.id_warga IS NULL AND w.telepon = u.telepon)
        ) AND w.status = \'aktif\' AND w.dihapus_pada IS NULL
        LEFT JOIN keluarga k ON k.id = w.id_keluarga AND k.dihapus_pada IS NULL
        LEFT JOIN rt ON rt.id = k.id_rt
        LEFT JOIN rw ON rw.id = rt.id_rw
        WHERE u.status = \'menunggu_verifikasi\' AND u.dihapus_pada IS NULL';
    $parameters = [];
    if ($user['role_code'] !== 'super_admin') {
        $query .= ' AND w.id_organisasi = :organization_id';
        $parameters['organization_id'] = (int) $user['organization_id'];
        if ($user['role_code'] === 'ketua_rt') {
            $query .= ' AND rt.id = :rt_id';
            $parameters['rt_id'] = (int) $user['rt_id'];
        } elseif ($user['rw_id'] !== null) {
            $query .= ' AND rw.id = :rw_id';
            $parameters['rw_id'] = (int) $user['rw_id'];
        }
    }
    $query .= ' ORDER BY u.dibuat_pada DESC LIMIT 100';
    $statement = $connection->prepare($query);
    $statement->execute($parameters);
    jsonSuccess(['items' => $statement->fetchAll()]);
}

function approveRegistration(PDO $connection, array $user, int $registrationId): void
{
    requireRegistrationApprover($user);
    if ($registrationId < 1) {
        jsonError(422, 'validation_error', 'ID pendaftaran tidak valid.');
    }

    $connection->beginTransaction();
    try {
        $accountQuery = $connection->prepare(
            'SELECT id, id_warga, nama, telepon FROM pengguna
            WHERE id = :registration_id AND status = \'menunggu_verifikasi\'
              AND dihapus_pada IS NULL FOR UPDATE'
        );
        $accountQuery->execute(['registration_id' => $registrationId]);
        $account = $accountQuery->fetch();
        if ($account === false || ($account['id_warga'] === null && $account['telepon'] === null)) {
            $connection->rollBack();
            jsonError(404, 'registration_not_found', 'Pendaftaran tidak ditemukan atau belum terhubung ke data warga.');
        }

        $residentQuery = 'SELECT w.id, w.id_organisasi, k.id_rt, rt.id_rw
            FROM warga w
            INNER JOIN keluarga k ON k.id = w.id_keluarga
            INNER JOIN rt ON rt.id = k.id_rt
            WHERE ((:linked_resident_id IS NOT NULL AND w.id = :linked_resident_id_match)
                OR (:linked_resident_id_null IS NULL AND w.telepon = :phone))
              AND w.status = \'aktif\'
              AND w.dihapus_pada IS NULL AND k.dihapus_pada IS NULL';
        $residentParameters = [
            'linked_resident_id' => $account['id_warga'],
            'linked_resident_id_match' => $account['id_warga'],
            'linked_resident_id_null' => $account['id_warga'],
            'phone' => $account['telepon'],
        ];
        if ($user['role_code'] !== 'super_admin') {
            $residentQuery .= ' AND w.id_organisasi = :organization_id';
            $residentParameters['organization_id'] = (int) $user['organization_id'];
            if ($user['role_code'] === 'ketua_rt') {
                $residentQuery .= ' AND rt.id = :rt_id';
                $residentParameters['rt_id'] = (int) $user['rt_id'];
            } elseif ($user['rw_id'] !== null) {
                $residentQuery .= ' AND rt.id_rw = :rw_id';
                $residentParameters['rw_id'] = (int) $user['rw_id'];
            }
        }
        $residentQuery .= ' LIMIT 1 FOR UPDATE';
        $residentStatement = $connection->prepare($residentQuery);
        $residentStatement->execute($residentParameters);
        $resident = $residentStatement->fetch();
        if ($resident === false) {
            $connection->rollBack();
            jsonError(422, 'resident_not_found', 'Nomor telepon pendaftar belum cocok dengan data warga aktif di wilayah Anda. Perbarui data warga lalu coba lagi.');
        }

        $existingAccount = $connection->prepare(
            'SELECT id FROM pengguna
            WHERE id_warga = :resident_id AND id <> :registration_id
            LIMIT 1'
        );
        $existingAccount->execute([
            'resident_id' => $resident['id'],
            'registration_id' => $registrationId,
        ]);
        if ($existingAccount->fetchColumn() !== false) {
            $connection->rollBack();
            jsonError(409, 'resident_account_exists', 'Data warga tersebut sudah terhubung ke akun lain.');
        }

        $residentRole = $connection->prepare('SELECT id FROM peran WHERE kode = \'warga\' LIMIT 1');
        $residentRole->execute();
        $roleId = $residentRole->fetchColumn();
        if ($roleId === false) {
            throw new RuntimeException('The warga role is not configured.');
        }

        $activate = $connection->prepare(
            'UPDATE pengguna SET id_warga = :resident_id, status = \'aktif\'
            WHERE id = :user_id AND status = \'menunggu_verifikasi\''
        );
        $activate->execute(['resident_id' => $resident['id'], 'user_id' => $registrationId]);
        $assignment = $connection->prepare(
            'INSERT INTO penugasan_pengguna (id_pengguna, id_peran, id_organisasi, id_rw, id_rt, aktif, mulai_pada)
            VALUES (:user_id, :role_id, :organization_id, :rw_id, :rt_id, 1, CURDATE())'
        );
        $assignment->execute([
            'user_id' => $registrationId,
            'role_id' => (int) $roleId,
            'organization_id' => (int) $resident['id_organisasi'],
            'rw_id' => (int) $resident['id_rw'],
            'rt_id' => (int) $resident['id_rt'],
        ]);
        $audit = $connection->prepare(
            'INSERT INTO log_audit (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
            VALUES (:organization_id, :actor_id, \'setujui_registrasi\', \'pengguna\', :record_id, :ip_address, :user_agent)'
        );
        $audit->execute([
            'organization_id' => (int) $resident['id_organisasi'],
            'actor_id' => $user['id'],
            'record_id' => $registrationId,
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

    jsonSuccess(['message' => 'Pendaftaran disetujui. Akun aktif sebagai Warga.']);
}

function requireRegistrationApprover(array $user): void
{
    if (!in_array($user['role_code'], ['super_admin', 'admin_rw', 'ketua_rw', 'ketua_rt'], true)) {
        jsonError(403, 'forbidden', 'Hanya admin, Ketua RW, atau Ketua RT yang dapat memverifikasi pendaftaran.');
    }
    if ($user['role_code'] !== 'super_admin' && $user['organization_id'] === null) {
        jsonError(403, 'organization_required', 'Akun pengurus belum terhubung ke organisasi.');
    }
}

function writeAccountAudit(PDO $connection, array $user, string $action): void
{
    $audit = $connection->prepare(
        'INSERT INTO log_audit (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
        VALUES (:organization_id, :user_id, :action, \'pengguna\', :record_id, :ip_address, :user_agent)'
    );
    $audit->execute([
        'organization_id' => $user['organization_id'],
        'user_id' => $user['id'],
        'action' => $action,
        'record_id' => $user['id'],
        'ip_address' => substr((string) ($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45),
        'user_agent' => substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
    ]);
}
