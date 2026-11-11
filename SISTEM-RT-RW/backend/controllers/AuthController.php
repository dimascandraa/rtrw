<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function login(PDO $connection, array $configuration): void
{
    $body = jsonRequestBody();
    $identity = $body['identity'] ?? '';
    $password = $body['password'] ?? '';

    if (!is_string($identity) || !is_string($password)) {
        jsonError(422, 'validation_error', 'Email/telepon dan kata sandi wajib diisi.');
    }

    $identity = trim($identity);
    if ($identity === '' || $password === '' || strlen($identity) > 150 || strlen($password) > 255) {
        jsonError(422, 'validation_error', 'Email/telepon dan kata sandi wajib diisi dengan format yang valid.');
    }

    $statement = $connection->prepare(
        'SELECT id, id_warga, nama AS name, email, hash_kata_sandi, status,
                percobaan_login_gagal, terkunci_sampai
        FROM pengguna
        WHERE (email = :email OR telepon = :phone)
          AND dihapus_pada IS NULL
        LIMIT 1'
    );
    $statement->execute([
        'email' => $identity,
        'phone' => $identity,
    ]);
    $account = $statement->fetch();

    if ($account === false) {
        jsonError(401, 'invalid_credentials', 'Email/telepon atau kata sandi salah.');
    }

    if ($account['terkunci_sampai'] !== null && strtotime($account['terkunci_sampai']) > time()) {
        jsonError(429, 'account_locked', 'Akun dikunci sementara karena terlalu banyak percobaan masuk.');
    }

    if (!password_verify($password, $account['hash_kata_sandi'])) {
        $failedAttempts = min((int) $account['percobaan_login_gagal'] + 1, 65535);
        $locked = $failedAttempts >= 5 ? 1 : 0;
        $updateAttempts = $connection->prepare(
            'UPDATE pengguna
            SET percobaan_login_gagal = :attempts,
                terkunci_sampai = CASE
                    WHEN :locked = 1 THEN DATE_ADD(NOW(), INTERVAL 15 MINUTE)
                    ELSE NULL
                END
            WHERE id = :user_id'
        );
        $updateAttempts->execute([
            'attempts' => $failedAttempts,
            'locked' => $locked,
            'user_id' => $account['id'],
        ]);
        jsonError(401, 'invalid_credentials', 'Email/telepon atau kata sandi salah.');
    }

    if ($account['status'] !== 'aktif') {
        jsonError(403, 'account_inactive', 'Akun belum aktif. Hubungi pengurus RT/RW Anda.');
    }

    $assignment = findActiveAssignment($connection, (int) $account['id']);
    if ($assignment === null) {
        jsonError(403, 'role_not_assigned', 'Akun belum memiliki penugasan wilayah yang aktif.');
    }

    $user = array_merge($account, $assignment);
    $token = bin2hex(random_bytes(32));
    $tokenHash = hash('sha256', $token);
    $expiresAt = time() + $configuration['session_ttl'];
    $expiresAtSql = date('Y-m-d H:i:s', $expiresAt);
    $deviceName = substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? 'Web browser'), 0, 150);
    $userAgent = substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255);
    $ipAddress = substr((string) ($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45);

    $connection->beginTransaction();
    try {
        $updateAccount = $connection->prepare(
            'UPDATE pengguna
            SET percobaan_login_gagal = 0,
                terkunci_sampai = NULL,
                login_terakhir_pada = NOW()
            WHERE id = :user_id'
        );
        $updateAccount->execute(['user_id' => $account['id']]);

        $insertSession = $connection->prepare(
            'INSERT INTO sesi_pengguna
                (id_pengguna, nama_perangkat, hash_token, alamat_ip, agen_pengguna,
                 terakhir_aktif_pada, kedaluwarsa_pada)
            VALUES
                (:user_id, :device_name, :token_hash, :ip_address, :user_agent,
                 NOW(), :expires_at)'
        );
        $insertSession->execute([
            'user_id' => $account['id'],
            'device_name' => $deviceName,
            'token_hash' => $tokenHash,
            'ip_address' => $ipAddress,
            'user_agent' => $userAgent,
            'expires_at' => $expiresAtSql,
        ]);

        $insertAudit = $connection->prepare(
            'INSERT INTO log_audit (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, alamat_ip, agen_pengguna)
            VALUES (:organization_id, :actor_id, \'login\', \'pengguna\', :record_id, :ip_address, :user_agent)'
        );
        $insertAudit->execute([
            'organization_id' => $user['organization_id'],
            'actor_id' => $account['id'],
            'record_id' => $account['id'],
            'ip_address' => $ipAddress,
            'user_agent' => $userAgent,
        ]);

        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }

    if (!setAuthCookie($token, $expiresAt, $configuration['secure_cookie'])) {
        $revokeSession = $connection->prepare(
            'UPDATE sesi_pengguna SET dicabut_pada = NOW() WHERE hash_token = :token_hash'
        );
        $revokeSession->execute(['token_hash' => $tokenHash]);
        jsonError(500, 'session_error', 'Sesi tidak dapat dibuat. Coba lagi.');
    }

    jsonSuccess([
        'user' => publicUser($user),
        'expiresAt' => date(DATE_ATOM, $expiresAt),
    ]);
}

function currentUser(PDO $connection): void
{
    $user = authenticatedUser($connection);
    jsonSuccess(['user' => publicUser($user)]);
}

function logout(PDO $connection, array $configuration): void
{
    $token = $_COOKIE['rtrw_session'] ?? '';
    if (is_string($token) && preg_match('/\A[a-f0-9]{64}\z/', $token)) {
        $statement = $connection->prepare(
            'UPDATE sesi_pengguna
            SET dicabut_pada = NOW()
            WHERE hash_token = :token_hash AND dicabut_pada IS NULL'
        );
        $statement->execute(['token_hash' => hash('sha256', $token)]);
    }

    if (!setAuthCookie('', time() - 3600, $configuration['secure_cookie'])) {
        jsonError(500, 'session_error', 'Cookie sesi tidak dapat dihapus. Coba lagi.');
    }

    jsonSuccess(['message' => 'Anda telah keluar.']);
}
