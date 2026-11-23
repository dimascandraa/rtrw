<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

require_once dirname(__DIR__) . '/helpers/announcement-target-scope.php';

function listResource(PDO $connection, array $user, string $resource): void
{
    $resources = [
        'warga' => [
            'permission' => 'warga.read',
            'organization' => 'w.id_organisasi',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'limited' => 'w.id = :resident_id',
            'search' => ['w.nama_lengkap', 'w.telepon', 'rt.nomor_rt', 'rw.nomor_rw'],
            'query' => 'SELECT w.id, w.id_organisasi, w.nama_lengkap AS nama, w.telepon, w.status,
                    w.jenis_kelamin, w.id_keluarga, k.nomor_rumah, rt.nomor_rt, rw.nomor_rw
                FROM warga w
                INNER JOIN keluarga k ON k.id = w.id_keluarga
                INNER JOIN rt ON rt.id = k.id_rt
                INNER JOIN rw ON rw.id = rt.id_rw
                WHERE w.dihapus_pada IS NULL AND k.dihapus_pada IS NULL',
        ],
        'mutasi' => [
            'permission' => 'mutasi_warga.read',
            'organization' => 'm.id_organisasi',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'limited' => 'm.id_warga = :resident_id',
            'search' => ['w.nama_lengkap', 'm.jenis_mutasi', 'm.asal_tujuan', 'm.catatan'],
            'query' => 'SELECT m.id, w.nama_lengkap AS nama_warga, m.jenis_mutasi,
                    m.tanggal_berlaku, m.asal_tujuan, m.catatan
                FROM mutasi_warga m
                INNER JOIN warga w ON w.id = m.id_warga
                INNER JOIN keluarga k ON k.id = w.id_keluarga
                INNER JOIN rt ON rt.id = k.id_rt
                INNER JOIN rw ON rw.id = rt.id_rw
                WHERE 1 = 1',
        ],
        'kategori-iuran' => [
            'permission' => 'kategori_iuran.read',
            'organization' => 'c.id_organisasi',
            'search' => ['c.nama', 'c.jenis_iuran', 'c.deskripsi'],
            'query' => 'SELECT c.id, c.nama, c.jenis_iuran, c.deskripsi, c.aktif
                FROM kategori_iuran c WHERE 1 = 1',
        ],
        'keluarga' => [
            'permission' => 'keluarga.read',
            'organization' => 'k.id_organisasi',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'limited' => 'k.id = (
                SELECT id_keluarga FROM warga
                WHERE id = :resident_id AND status = \'aktif\' AND dihapus_pada IS NULL
            )',
            'search' => ['k.alamat', 'k.nomor_rumah', 'rt.nomor_rt', 'rw.nomor_rw'],
            'query' => 'SELECT k.id, k.id_organisasi, k.id_rt, k.alamat, k.nomor_rumah, k.jenis_hunian,
                    rt.nomor_rt, rw.nomor_rw,
                    (SELECT COUNT(*) FROM warga w
                     WHERE w.id_keluarga = k.id AND w.status = \'aktif\' AND w.dihapus_pada IS NULL) AS jumlah_warga
                FROM keluarga k
                INNER JOIN rt ON rt.id = k.id_rt
                INNER JOIN rw ON rw.id = rt.id_rw
                WHERE k.dihapus_pada IS NULL',
        ],
        'periode-tagihan' => [
            'permission' => 'tagihan.read',
            'organization' => 'p.id_organisasi',
            'search' => ['p.nama', 'p.status'],
            'query' => 'SELECT p.id, p.id_organisasi, p.nama, p.periode_mulai, p.periode_akhir, p.status
                FROM periode_tagihan p WHERE 1 = 1',
        ],
        'tagihan' => [
            'permission' => 'tagihan.read',
            'organization' => 't.id_organisasi',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'limited' => 'k.id = (
                SELECT id_keluarga FROM warga
                WHERE id = :resident_id AND status = \'aktif\' AND dihapus_pada IS NULL
            )',
            'search' => ['t.nomor_tagihan', 'k.nomor_rumah', 'p.nama', 't.status'],
            'query' => 'SELECT t.id, t.id_organisasi, t.id_keluarga, t.id_periode_tagihan, t.nomor_tagihan, p.nama AS periode, k.nomor_rumah,
                    t.total_nominal, t.nominal_dibayar, t.status, t.tanggal_jatuh_tempo
                FROM tagihan t
                INNER JOIN keluarga k ON k.id = t.id_keluarga
                INNER JOIN rt ON rt.id = k.id_rt
                INNER JOIN rw ON rw.id = rt.id_rw
                INNER JOIN periode_tagihan p ON p.id = t.id_periode_tagihan
                WHERE 1 = 1',
        ],
        'pembayaran' => [
            'permission' => 'pembayaran.read',
            'organization' => 'p.id_organisasi',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'limited' => 'k.id = (
                SELECT id_keluarga FROM warga
                WHERE id = :resident_id AND status = \'aktif\' AND dihapus_pada IS NULL
            )',
            'search' => ['t.nomor_tagihan', 'p.metode', 'p.status'],
            'query' => 'SELECT p.id, p.id_organisasi, p.id_tagihan, t.nomor_tagihan, p.nominal, p.metode, p.status,
                    p.referensi_pembayaran, p.catatan_verifikasi, p.dibayar_pada, p.diverifikasi_pada
                FROM pembayaran p
                INNER JOIN tagihan t ON t.id = p.id_tagihan
                INNER JOIN keluarga k ON k.id = t.id_keluarga
                INNER JOIN rt ON rt.id = k.id_rt
                INNER JOIN rw ON rw.id = rt.id_rw
                WHERE 1 = 1',
        ],
        'transaksi-kas' => [
            'permission' => 'laporan_keuangan.read',
            'organization' => 'x.id_organisasi',
            'limited' => null,
            'search' => ['x.jenis_transaksi', 'x.keterangan'],
            'query' => 'SELECT x.id, x.jenis_transaksi, x.nominal, x.tanggal_transaksi,
                    x.keterangan, kp.nama AS kategori
                FROM transaksi_kas x
                LEFT JOIN kategori_pengeluaran kp ON kp.id = x.id_kategori_pengeluaran
                WHERE 1 = 1',
        ],
        'jenis-surat' => [
            'permission' => 'pengajuan_surat.create',
            'organization' => 'j.id_organisasi',
            'search' => ['j.nama', 'j.kode'],
            'query' => 'SELECT j.id, j.kode, j.nama, j.aktif
                FROM jenis_surat j WHERE j.aktif = 1',
        ],
        'pengajuan-surat' => [
            'permission' => 'pengajuan_surat.read',
            'organization' => 's.id_organisasi',
            'limited' => 's.id_warga = :resident_id',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'search' => ['s.keperluan', 's.status', 'j.nama', 'w.nama_lengkap'],
            'query' => 'SELECT s.id, j.nama AS jenis_surat, w.nama_lengkap AS nama_warga,
                    s.keperluan, s.status, s.diajukan_pada, s.nomor_surat
                FROM pengajuan_surat s
                INNER JOIN jenis_surat j ON j.id = s.id_jenis_surat
                INNER JOIN warga w ON w.id = s.id_warga
                INNER JOIN keluarga k ON k.id = w.id_keluarga
                INNER JOIN rt ON rt.id = k.id_rt
                INNER JOIN rw ON rw.id = rt.id_rw
                WHERE 1 = 1',
        ],
        'survei' => [
            'permission' => 'warga.read',
            'organization' => 's.id_organisasi',
            'limited' => 's.berakhir_pada >= NOW()',
            'search' => ['s.judul', 's.deskripsi'],
            'query' => 'SELECT s.id, s.judul, s.deskripsi, s.dimulai_pada,
                    s.berakhir_pada, s.anonim,
                    (SELECT COUNT(*) FROM jawaban_survei j WHERE j.id_survei = s.id) AS jumlah_jawaban
                FROM survei s WHERE 1 = 1',
        ],
        'pengumuman' => [
            'permission' => 'pengumuman.read',
            'organization' => 'p.id_organisasi',
            'rt' => 'p.id_rt',
            'rw' => 'rw.id',
            'include_global_for_region' => true,
            'region_safe_when_restricted' => true,
            'search' => ['p.judul', 'p.isi'],
            'query' => 'SELECT p.id, p.id_organisasi, p.id_rt, p.judul, p.isi, p.dipublikasikan_pada,
                    p.kedaluwarsa_pada
                FROM pengumuman p
                LEFT JOIN rt ON rt.id = p.id_rt
                LEFT JOIN rw ON rw.id = rt.id_rw
                WHERE p.dihapus_pada IS NULL',
        ],
        'kegiatan' => [
            'permission' => 'kegiatan.read',
            'organization' => 'k.id_organisasi',
            'rt' => 'k.id_rt',
            'rw' => 'rw.id',
            'include_global_for_region' => true,
            'region_safe_when_restricted' => true,
            'search' => ['k.judul', 'k.deskripsi', 'k.jenis', 'k.lokasi'],
            'query' => 'SELECT k.id, k.id_organisasi, k.id_rt, k.judul, k.deskripsi, k.jenis, k.mulai_pada,
                    k.selesai_pada, k.lokasi, rt.nomor_rt
                FROM kegiatan k
                LEFT JOIN rt ON rt.id = k.id_rt
                LEFT JOIN rw ON rw.id = rt.id_rw
                WHERE k.dihapus_pada IS NULL',
        ],
        'pengaduan' => [
            'permission' => 'pengaduan.read',
            'organization' => 'p.id_organisasi',
            'limited' => 'p.id_warga = :resident_id',
            'search' => ['p.judul', 'p.deskripsi', 'p.kategori', 'p.status'],
            'query' => 'SELECT p.id, p.kategori, p.judul, p.deskripsi, p.status,
                    p.bersifat_privat, p.dibuat_pada
                FROM pengaduan p WHERE 1 = 1',
        ],
        'kontak-darurat' => [
            'permission' => 'warga.read',
            'organization' => 'c.id_organisasi',
            'limited' => 'c.aktif = 1',
            'search' => ['c.kategori', 'c.nama', 'c.telepon', 'c.alamat'],
            'query' => 'SELECT c.id, c.kategori, c.nama, c.telepon, c.alamat
                FROM kontak_darurat c WHERE c.aktif = 1',
        ],
        'pengguna' => [
            'permission' => 'pengguna.read',
            'organization' => 'pp.id_organisasi',
            'search' => ['u.nama', 'u.email', 'u.status', 'r.nama'],
            'query' => 'SELECT u.id, u.nama, u.email, u.status, r.nama AS peran,
                    u.login_terakhir_pada
                FROM pengguna u
                INNER JOIN penugasan_pengguna pp ON pp.id_pengguna = u.id AND pp.aktif = 1
                INNER JOIN peran r ON r.id = pp.id_peran
                WHERE u.dihapus_pada IS NULL',
        ],
        'peran' => [
            'permission' => 'pengguna.read',
            'search' => ['r.kode', 'r.nama', 'r.deskripsi'],
            'query' => 'SELECT r.id, r.kode, r.nama, r.deskripsi FROM peran r WHERE 1 = 1',
        ],
        'audit-log' => [
            'permission' => 'laporan_aktivitas.read',
            'organization' => 'a.id_organisasi',
            'search' => ['a.aksi', 'a.nama_tabel', 'u.nama'],
            'query' => 'SELECT a.id, a.aksi, a.nama_tabel, a.id_data,
                    a.dibuat_pada, u.nama AS pengguna
                FROM log_audit a
                LEFT JOIN pengguna u ON u.id = a.id_pengguna
                WHERE 1 = 1',
        ],
        'sesi' => [
            'permission' => null,
            'organization' => 'pp.id_organisasi',
            'fixed' => 's.id_pengguna = :actor_id',
            'search' => ['u.nama', 's.nama_perangkat'],
            'query' => 'SELECT s.id, u.nama AS pengguna, s.nama_perangkat,
                    s.terakhir_aktif_pada, s.kedaluwarsa_pada, s.dicabut_pada
                FROM sesi_pengguna s
                INNER JOIN pengguna u ON u.id = s.id_pengguna
                INNER JOIN penugasan_pengguna pp ON pp.id_pengguna = u.id AND pp.aktif = 1
                WHERE 1 = 1',
        ],
        'wilayah' => [
            'permission' => 'wilayah.read',
            'organization' => 'rw.id_organisasi',
            'rt' => 'rt.id',
            'rw' => 'rw.id',
            'search' => ['rw.nomor_rw', 'rt.nomor_rt', 'rw.nama_ketua', 'rt.nama_ketua'],
            'query' => 'SELECT rw.id AS id_rw, rw.nomor_rw, rw.nama_ketua AS ketua_rw,
                    rt.id AS id_rt, rt.nomor_rt, rt.nama_ketua AS ketua_rt
                FROM rw
                LEFT JOIN rt ON rt.id_rw = rw.id
                WHERE rw.dihapus_pada IS NULL',
        ],
        'pengaturan' => [
            'permission' => 'pengaturan.read',
            'organization' => 'p.id_organisasi',
            'search' => ['p.kunci_pengaturan', 'p.nilai_pengaturan'],
            'query' => 'SELECT p.id, p.kunci_pengaturan, p.nilai_pengaturan, p.diperbarui_pada
                FROM pengaturan p WHERE 1 = 1',
        ],
        'cadangan-data' => [
            'permission' => 'pengaturan.read',
            'organization' => 'c.id_organisasi',
            'search' => ['c.jenis_cadangan', 'c.status'],
            'query' => 'SELECT c.id, c.jenis_cadangan, c.status, c.ukuran,
                    c.terenkripsi, c.dibuat_pada, c.selesai_pada
                FROM cadangan_data c WHERE 1 = 1',
        ],
    ];

    if (!isset($resources[$resource])) {
        jsonError(404, 'not_found', 'Resource tidak ditemukan.');
    }

    $configuration = $resources[$resource];
    if ($user['organization_id'] === null && $user['role_code'] !== 'super_admin') {
        jsonError(403, 'organization_required', 'Akun ini belum terhubung ke organisasi yang dapat diakses.');
    }
    $permission = $configuration['permission'] === null
        ? ['terbatas' => 0]
        : requirePermission($connection, $user, $configuration['permission']);
    if ($resource === 'pengumuman' && $user['role_code'] === 'warga') {
        $configuration['query'] .= ' AND p.dipublikasikan_pada IS NOT NULL
            AND p.dipublikasikan_pada <= NOW()
            AND (p.kedaluwarsa_pada IS NULL OR p.kedaluwarsa_pada > NOW())';
    }
    $limit = filter_var($_GET['limit'] ?? '50', FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 1, 'max_range' => 100],
    ]);
    $offset = filter_var($_GET['offset'] ?? '0', FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 0, 'max_range' => 1000000],
    ]);
    if ($limit === false || $offset === false) {
        jsonError(422, 'validation_error', 'Parameter limit harus 1–100 dan offset harus 0 atau lebih.');
    }

    $query = $configuration['query'];
    $parameters = [];
    if (isset($configuration['organization']) && $user['organization_id'] !== null) {
        $query .= ' AND ' . $configuration['organization'] . ' = :organization_id';
        $parameters['organization_id'] = (int) $user['organization_id'];
    }
    if (isset($configuration['fixed'])) {
        $query .= ' AND ' . $configuration['fixed'];
        $parameters['actor_id'] = (int) $user['id'];
    }
    if ($resource === 'pengumuman') {
        $targetFilter = announcementTargetScope($user);
        $query .= ' AND ' . $targetFilter['sql'];
        $parameters = array_merge($parameters, $targetFilter['parameters']);
    }

    $regionalLetterApprover = $resource === 'pengajuan-surat'
        && in_array($user['role_code'], ['ketua_rt', 'ketua_rw'], true);
    $regionalContentAccess = !empty($configuration['region_safe_when_restricted'])
        && ($user['rt_id'] !== null || $user['rw_id'] !== null);
    if ((int) $permission['terbatas'] === 1 && !$regionalLetterApprover && !$regionalContentAccess) {
        if (!isset($configuration['limited']) || $user['id_warga'] === null) {
            jsonError(403, 'restricted_resource', 'Data fitur ini tidak tersedia untuk akses terbatas.');
        }
        $query .= ' AND ' . $configuration['limited'];
        if (strpos($configuration['limited'], ':resident_id') !== false) {
            $parameters['resident_id'] = (int) $user['id_warga'];
        }
    } elseif (isset($configuration['rt']) && $user['rt_id'] !== null) {
        if (!empty($configuration['include_global_for_region'])) {
            $query .= ' AND (' . $configuration['rt'] . ' = :rt_id OR ' . $configuration['rt'] . ' IS NULL)';
        } else {
            $query .= ' AND ' . $configuration['rt'] . ' = :rt_id';
        }
        $parameters['rt_id'] = (int) $user['rt_id'];
    } elseif (isset($configuration['rw']) && $user['rw_id'] !== null) {
        if (!empty($configuration['include_global_for_region'])) {
            $query .= ' AND (' . $configuration['rw'] . ' = :rw_id OR ' . $configuration['rt'] . ' IS NULL)';
        } else {
            $query .= ' AND ' . $configuration['rw'] . ' = :rw_id';
        }
        $parameters['rw_id'] = (int) $user['rw_id'];
    }

    $search = trim((string) ($_GET['search'] ?? ''));
    if ($search !== '') {
        if (strlen($search) > 100) {
            jsonError(422, 'validation_error', 'Parameter search maksimal 100 karakter.');
        }
        $searchParts = [];
        foreach ($configuration['search'] as $index => $column) {
            $placeholder = 'search_' . $index;
            $searchParts[] = $column . ' LIKE :' . $placeholder;
            $parameters[$placeholder] = '%' . $search . '%';
        }
        $query .= ' AND (' . implode(' OR ', $searchParts) . ')';
    }

    $query .= ' ORDER BY 1 DESC LIMIT :limit OFFSET :offset';
    $statement = $connection->prepare($query);
    foreach ($parameters as $name => $value) {
        $statement->bindValue(':' . $name, $value, is_int($value) ? PDO::PARAM_INT : PDO::PARAM_STR);
    }
    $statement->bindValue(':limit', $limit + 1, PDO::PARAM_INT);
    $statement->bindValue(':offset', $offset, PDO::PARAM_INT);
    $statement->execute();

    $items = $statement->fetchAll();
    $hasMore = count($items) > $limit;
    if ($hasMore) {
        array_pop($items);
    }

    jsonSuccess([
        'items' => $items,
        'pagination' => [
            'limit' => $limit,
            'offset' => $offset,
            'hasMore' => $hasMore,
        ],
    ]);
}
