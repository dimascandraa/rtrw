<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/helpers/response.php';

function findActiveAssignment(PDO $connection, int $userId): ?array
{
    $statement = $connection->prepare(
        'SELECT
            pp.id AS assignment_id,
            pp.id_organisasi AS organization_id,
            pp.id_rw AS rw_id,
            pp.id_rt AS rt_id,
            r.kode AS role_code,
            r.nama AS role_name,
            o.nama AS organization_name,
            rw.nomor_rw AS rw_number,
            rt.nomor_rt AS rt_number
        FROM penugasan_pengguna pp
        INNER JOIN peran r ON r.id = pp.id_peran
        LEFT JOIN organisasi o ON o.id = pp.id_organisasi
        LEFT JOIN rw ON rw.id = pp.id_rw
        LEFT JOIN rt ON rt.id = pp.id_rt
        WHERE pp.id_pengguna = :user_id
          AND pp.aktif = 1
          AND (pp.id_organisasi IS NULL OR o.status = \'aktif\')
        ORDER BY pp.id
        LIMIT 1'
    );
    $statement->execute(['user_id' => $userId]);
    $assignment = $statement->fetch();

    return $assignment === false ? null : $assignment;
}

function authenticatedUser(PDO $connection): array
{
    $token = $_COOKIE['rtrw_session'] ?? '';
    if (!is_string($token) || !preg_match('/\A[a-f0-9]{64}\z/', $token)) {
        jsonError(401, 'unauthenticated', 'Silakan masuk untuk melanjutkan.');
    }

    $statement = $connection->prepare(
        'SELECT
            s.id AS session_id,
            u.id AS id,
            u.id_warga,
            u.nama AS name,
            u.email,
            u.status,
            pp.id AS assignment_id,
            pp.id_organisasi AS organization_id,
            pp.id_rw AS rw_id,
            pp.id_rt AS rt_id,
            r.kode AS role_code,
            r.nama AS role_name,
            o.nama AS organization_name,
            rw.nomor_rw AS rw_number,
            rt.nomor_rt AS rt_number
        FROM sesi_pengguna s
        INNER JOIN pengguna u ON u.id = s.id_pengguna
        INNER JOIN penugasan_pengguna pp ON pp.id_pengguna = u.id AND pp.aktif = 1
        INNER JOIN peran r ON r.id = pp.id_peran
        LEFT JOIN organisasi o ON o.id = pp.id_organisasi
        LEFT JOIN rw ON rw.id = pp.id_rw
        LEFT JOIN rt ON rt.id = pp.id_rt
        WHERE s.hash_token = :token_hash
          AND s.dicabut_pada IS NULL
          AND (s.kedaluwarsa_pada IS NULL OR s.kedaluwarsa_pada > NOW())
          AND u.status = \'aktif\'
          AND u.dihapus_pada IS NULL
          AND (pp.id_organisasi IS NULL OR o.status = \'aktif\')
        ORDER BY pp.id
        LIMIT 1'
    );
    $statement->execute(['token_hash' => hash('sha256', $token)]);
    $user = $statement->fetch();

    if ($user === false) {
        setAuthCookie('', time() - 3600, false);
        jsonError(401, 'session_expired', 'Sesi Anda berakhir. Silakan masuk kembali.');
    }

    $touchSession = $connection->prepare(
        'UPDATE sesi_pengguna SET terakhir_aktif_pada = NOW() WHERE id = :session_id'
    );
    $touchSession->execute(['session_id' => $user['session_id']]);

    return $user;
}

function requirePermission(PDO $connection, array $user, string $permissionCode): array
{
    if ($user['role_code'] === 'super_admin') {
        return ['terbatas' => 0];
    }

    $statement = $connection->prepare(
        'SELECT pia.terbatas
        FROM peran_izin_akses pia
        INNER JOIN izin_akses ia ON ia.id = pia.id_izin_akses
        INNER JOIN penugasan_pengguna pp ON pp.id_peran = pia.id_peran
        WHERE pp.id = :assignment_id
          AND pp.aktif = 1
          AND ia.kode = :permission_code
        LIMIT 1'
    );
    $statement->execute([
        'assignment_id' => $user['assignment_id'],
        'permission_code' => $permissionCode,
    ]);
    $permission = $statement->fetch();

    if ($permission === false) {
        jsonError(403, 'forbidden', 'Anda tidak memiliki izin untuk mengakses data ini.');
    }

    return $permission;
}

function setAuthCookie(string $value, int $expiresAt, bool $secure): bool
{
    return setcookie('rtrw_session', $value, [
        'expires' => $expiresAt,
        'path' => '/',
        'secure' => $secure,
        'httponly' => true,
        'samesite' => 'Lax',
    ]);
}

function publicUser(array $user): array
{
    $regionParts = [];
    if (!empty($user['rt_number'])) {
        $regionParts[] = 'RT ' . $user['rt_number'];
    }
    if (!empty($user['rw_number'])) {
        $regionParts[] = 'RW ' . $user['rw_number'];
    }

    return [
        'id' => (int) $user['id'],
        'name' => $user['name'],
        'email' => $user['email'],
        'role' => $user['role_name'],
        'roleCode' => $user['role_code'],
        'organizationId' => $user['organization_id'] === null ? null : (int) $user['organization_id'],
        'organizationName' => $user['organization_name'],
        'rwId' => $user['rw_id'] === null ? null : (int) $user['rw_id'],
        'rwNumber' => $user['rw_number'],
        'rtId' => $user['rt_id'] === null ? null : (int) $user['rt_id'],
        'rtNumber' => $user['rt_number'],
        'region' => $regionParts === [] ? 'Semua wilayah' : implode(' / ', $regionParts),
    ];
}
