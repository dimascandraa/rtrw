<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function dashboardFamilyScope(PDO $connection, array $user, bool $restricted): array
{
    $conditions = [];
    $parameters = [];

    if ($user['organization_id'] !== null) {
        $conditions[] = 'k.id_organisasi = :scope_organization_id';
        $parameters['scope_organization_id'] = $user['organization_id'];
    }

    if ($restricted) {
        if ($user['id_warga'] === null) {
            jsonError(403, 'resident_profile_required', 'Akun terbatas harus terhubung ke data warga.');
        }

        $statement = $connection->prepare(
            'SELECT id_keluarga
            FROM warga
            WHERE id = :resident_id
              AND status = \'aktif\'
              AND dihapus_pada IS NULL
            LIMIT 1'
        );
        $statement->execute(['resident_id' => $user['id_warga']]);
        $familyId = $statement->fetchColumn();
        if ($familyId === false) {
            jsonError(403, 'resident_profile_required', 'Data warga untuk akun ini tidak ditemukan.');
        }

        $conditions[] = 'k.id = :scope_family_id';
        $parameters['scope_family_id'] = (int) $familyId;
    } elseif ($user['rt_id'] !== null) {
        $conditions[] = 'k.id_rt = :scope_rt_id';
        $parameters['scope_rt_id'] = (int) $user['rt_id'];
    } elseif ($user['rw_id'] !== null) {
        $conditions[] = 'r.id_rw = :scope_rw_id';
        $parameters['scope_rw_id'] = (int) $user['rw_id'];
    }

    return [
        'where' => $conditions === [] ? '1 = 1' : implode(' AND ', $conditions),
        'parameters' => $parameters,
    ];
}

function dashboardRecentActivity(PDO $connection, array $user): array
{
    $permission = $connection->prepare(
        'SELECT 1
        FROM peran_izin_akses pia
        INNER JOIN izin_akses ia ON ia.id = pia.id_izin_akses
        WHERE pia.id_peran = (
            SELECT id_peran FROM penugasan_pengguna
            WHERE id = :assignment_id AND aktif = 1
        )
          AND ia.kode = :permission_code
        LIMIT 1'
    );
    $permission->execute([
        'assignment_id' => $user['assignment_id'],
        'permission_code' => 'laporan_aktivitas.read',
    ]);
    if ($permission->fetchColumn() === false) {
        return [];
    }

    $conditions = [];
    $parameters = [];
    if ($user['organization_id'] !== null) {
        $conditions[] = 'a.id_organisasi = :organization_id';
        $parameters['organization_id'] = $user['organization_id'];
    }

    $where = $conditions === [] ? '' : 'WHERE ' . implode(' AND ', $conditions);
    $statement = $connection->prepare(
        'SELECT a.aksi, a.nama_tabel, a.id_data, a.dibuat_pada, u.nama AS actor
        FROM log_audit a
        LEFT JOIN pengguna u ON u.id = a.id_pengguna
        ' . $where . '
        ORDER BY a.dibuat_pada DESC, a.id DESC
        LIMIT 5'
    );
    $statement->execute($parameters);

    $actionLabels = [
        'login' => 'Masuk ke sistem',
        'verifikasi_pembayaran' => 'Pembayaran diverifikasi',
        'tolak_pembayaran' => 'Pembayaran ditolak',
        'setujui_surat' => 'Pengajuan surat disetujui',
    ];

    return array_map(static function (array $activity) use ($actionLabels): array {
        $action = $activity['aksi'];
        $detail = $activity['nama_tabel'] === null
            ? ''
            : ucfirst(str_replace('_', ' ', $activity['nama_tabel']))
                . ($activity['id_data'] === null ? '' : ' #' . $activity['id_data']);

        return [
            'title' => $actionLabels[$action] ?? ucfirst(str_replace('_', ' ', $action)),
            'detail' => $detail,
            'actor' => $activity['actor'] ?? 'Pengguna',
            'createdAt' => $activity['dibuat_pada'],
        ];
    }, $statement->fetchAll());
}

function dashboardSummary(PDO $connection, array $user): void
{
    $residentPermission = requirePermission($connection, $user, 'warga.read');
    $financePermission = requirePermission($connection, $user, 'laporan_keuangan.read');
    $restricted = (int) $residentPermission['terbatas'] === 1
        || (int) $financePermission['terbatas'] === 1;
    $scope = dashboardFamilyScope($connection, $user, $restricted);

    $residentCount = $connection->prepare(
        'SELECT COUNT(*)
        FROM warga w
        INNER JOIN keluarga k ON k.id = w.id_keluarga
        LEFT JOIN rt r ON r.id = k.id_rt
        WHERE w.status = \'aktif\'
          AND w.dihapus_pada IS NULL
          AND k.dihapus_pada IS NULL
          AND ' . $scope['where']
    );
    $residentCount->execute($scope['parameters']);
    $residentTotal = (int) $residentCount->fetchColumn();

    $familyCount = $connection->prepare(
        'SELECT COUNT(*)
        FROM keluarga k
        LEFT JOIN rt r ON r.id = k.id_rt
        WHERE k.dihapus_pada IS NULL
          AND ' . $scope['where']
    );
    $familyCount->execute($scope['parameters']);
    $familyTotal = (int) $familyCount->fetchColumn();

    $periodOrganizationCondition = '';
    $periodParameters = [];
    if ($user['organization_id'] !== null) {
        $periodOrganizationCondition = ' AND p.id_organisasi = :period_organization_id';
        $periodParameters['period_organization_id'] = $user['organization_id'];
    }

    $periodStatement = $connection->prepare(
        'SELECT GROUP_CONCAT(DISTINCT p.nama ORDER BY p.periode_mulai DESC SEPARATOR \', \') AS nama
        FROM periode_tagihan p
        WHERE p.status = \'terbit\'
          AND p.periode_mulai <= CURDATE()
          AND p.periode_akhir >= CURDATE()
        ' . $periodOrganizationCondition
    );
    $periodStatement->execute($periodParameters);
    $period = $periodStatement->fetch();

    $invoiceSummary = [
        'name' => $period === false || $period['nama'] === null ? null : $period['nama'],
        'billed' => 0.0,
        'collected' => 0.0,
        'paidFamilies' => 0,
        'unpaidInvoices' => 0,
        'pendingPayments' => 0,
    ];

    if ($period !== false && $period['nama'] !== null) {
        $invoiceOrganizationCondition = '';
        $invoiceOrganizationParameters = [];
        if ($user['organization_id'] !== null) {
            $invoiceOrganizationCondition = '
              AND t.id_organisasi = :invoice_organization_id
              AND periode.id_organisasi = :invoice_period_organization_id';
            $invoiceOrganizationParameters = [
                'invoice_organization_id' => $user['organization_id'],
                'invoice_period_organization_id' => $user['organization_id'],
            ];
        }

        $invoiceStatement = $connection->prepare(
            'SELECT
                COALESCE(SUM(t.total_nominal), 0) AS billed,
                COALESCE(SUM(t.nominal_dibayar), 0) AS collected,
                COALESCE(SUM(CASE WHEN t.status = \'lunas\' THEN 1 ELSE 0 END), 0) AS paid_families,
                COALESCE(SUM(CASE WHEN t.status IN (\'belum_dibayar\', \'sebagian\', \'lewat_jatuh_tempo\') THEN 1 ELSE 0 END), 0) AS unpaid_invoices
            FROM tagihan t
            INNER JOIN periode_tagihan periode ON periode.id = t.id_periode_tagihan
            INNER JOIN keluarga k ON k.id = t.id_keluarga
            LEFT JOIN rt r ON r.id = k.id_rt
            WHERE periode.status = \'terbit\'
              AND periode.periode_mulai <= CURDATE()
              AND periode.periode_akhir >= CURDATE()
              AND ' . $scope['where'] . $invoiceOrganizationCondition
        );
        $invoiceStatement->execute(array_merge($scope['parameters'], $invoiceOrganizationParameters));
        $invoice = $invoiceStatement->fetch();
        if ($invoice !== false) {
            $invoiceSummary['billed'] = (float) $invoice['billed'];
            $invoiceSummary['collected'] = (float) $invoice['collected'];
            $invoiceSummary['paidFamilies'] = (int) $invoice['paid_families'];
            $invoiceSummary['unpaidInvoices'] = (int) $invoice['unpaid_invoices'];
        }

        $paymentOrganizationCondition = '';
        $paymentOrganizationParameters = [];
        if ($user['organization_id'] !== null) {
            $paymentOrganizationCondition = '
              AND p.id_organisasi = :payment_organization_id
              AND periode.id_organisasi = :payment_period_organization_id';
            $paymentOrganizationParameters = [
                'payment_organization_id' => $user['organization_id'],
                'payment_period_organization_id' => $user['organization_id'],
            ];
        }

        $pendingPaymentStatement = $connection->prepare(
            'SELECT COUNT(*)
            FROM pembayaran p
            INNER JOIN tagihan t ON t.id = p.id_tagihan
            INNER JOIN periode_tagihan periode ON periode.id = t.id_periode_tagihan
            INNER JOIN keluarga k ON k.id = t.id_keluarga
            LEFT JOIN rt r ON r.id = k.id_rt
            WHERE p.status = \'menunggu_verifikasi\'
              AND periode.status = \'terbit\'
              AND periode.periode_mulai <= CURDATE()
              AND periode.periode_akhir >= CURDATE()
              AND ' . $scope['where'] . $paymentOrganizationCondition
        );
        $pendingPaymentStatement->execute(array_merge($scope['parameters'], $paymentOrganizationParameters));
        $invoiceSummary['pendingPayments'] = (int) $pendingPaymentStatement->fetchColumn();
    }

    $cashBalance = null;
    if (!$restricted) {
        $cashOrganizationCondition = '';
        $cashParameters = [];
        if ($user['organization_id'] !== null) {
            $cashOrganizationCondition = 'WHERE id_organisasi = :cash_organization_id';
            $cashParameters['cash_organization_id'] = $user['organization_id'];
        }

        $cashStatement = $connection->prepare(
            'SELECT COALESCE(SUM(saldo), 0)
            FROM v_saldo_kas ' . $cashOrganizationCondition
        );
        $cashStatement->execute($cashParameters);
        $cashBalance = (float) $cashStatement->fetchColumn();
    }

    $activities = dashboardRecentActivity($connection, $user);

    jsonSuccess([
        'organization' => [
            'id' => $user['organization_id'] === null ? null : (int) $user['organization_id'],
            'name' => $user['organization_name'],
            'rwNumber' => $user['rw_number'],
            'rtNumber' => $user['rt_number'],
        ],
        'stats' => [
            'residents' => $residentTotal,
            'families' => $familyTotal,
            'unpaidInvoices' => $invoiceSummary['unpaidInvoices'],
            'cashBalance' => $cashBalance,
        ],
        'period' => $invoiceSummary,
        'activities' => $activities,
    ]);
}
