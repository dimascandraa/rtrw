<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function listAnnouncements(PDO $connection, array $user): void
{
    requirePermission($connection, $user, 'pengumuman.read');

    $limit = filter_var($_GET['limit'] ?? '5', FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 1, 'max_range' => 50],
    ]);
    if ($limit === false) {
        jsonError(422, 'validation_error', 'Parameter limit harus bernilai 1 sampai 50.');
    }

    $conditions = [
        'p.dihapus_pada IS NULL',
        'p.dipublikasikan_pada IS NOT NULL',
        'p.dipublikasikan_pada <= NOW()',
        '(p.kedaluwarsa_pada IS NULL OR p.kedaluwarsa_pada > NOW())',
    ];
    $parameters = [];
    $targetScopes = [];

    if ($user['organization_id'] !== null) {
        $conditions[] = 'p.id_organisasi = :organization_id';
        $parameters['organization_id'] = $user['organization_id'];
    }
    if ($user['rt_id'] !== null) {
        $conditions[] = '(p.id_rt IS NULL OR p.id_rt = :rt_id)';
        $parameters['rt_id'] = $user['rt_id'];
    } elseif ($user['rw_id'] !== null) {
        $conditions[] = '(p.id_rt IS NULL OR p.id_rt IN (
            SELECT rt_scope.id FROM rt rt_scope WHERE rt_scope.id_rw = :rw_id
        ))';
        $parameters['rw_id'] = $user['rw_id'];
    }

    if ($user['rt_id'] !== null) {
        $targetScopes[] = 'target.id_rt = :target_rt_id';
        $parameters['target_rt_id'] = $user['rt_id'];
    }
    if ($user['rw_id'] !== null) {
        $targetScopes[] = 'target.id_rw = :target_rw_id';
        $parameters['target_rw_id'] = $user['rw_id'];
        $targetScopes[] = 'target.id_rt IN (
            SELECT target_rt_scope.id FROM rt target_rt_scope
            WHERE target_rt_scope.id_rw = :target_rt_rw_id
        )';
        $parameters['target_rt_rw_id'] = $user['rw_id'];
    } elseif ($user['rt_id'] !== null) {
        $targetScopes[] = 'target.id_rw = (
            SELECT target_rt_scope.id_rw FROM rt target_rt_scope
            WHERE target_rt_scope.id = :target_rt_owner_id
        )';
        $parameters['target_rt_owner_id'] = $user['rt_id'];
    }
    $targetScope = $targetScopes === [] ? '1 = 1' : implode(' OR ', $targetScopes);
    $conditions[] = '(
        NOT EXISTS (
            SELECT 1 FROM target_pengumuman target
            WHERE target.id_pengumuman = p.id
        )
        OR EXISTS (
            SELECT 1 FROM target_pengumuman target
            WHERE target.id_pengumuman = p.id AND (' . $targetScope . ')
        )
    )';

    $statement = $connection->prepare(
        'SELECT p.id, p.judul AS title, p.isi AS body, p.dipublikasikan_pada AS published_at
        FROM pengumuman p
        WHERE ' . implode(' AND ', $conditions) . '
        ORDER BY p.dipublikasikan_pada DESC, p.id DESC
        LIMIT :limit'
    );
    foreach ($parameters as $name => $value) {
        $statement->bindValue(':' . $name, $value, PDO::PARAM_INT);
    }
    $statement->bindValue(':limit', $limit, PDO::PARAM_INT);
    $statement->execute();

    $items = array_map(static function (array $announcement): array {
        return [
            'id' => (int) $announcement['id'],
            'title' => $announcement['title'],
            'body' => $announcement['body'],
            'publishedAt' => $announcement['published_at'],
        ];
    }, $statement->fetchAll());

    jsonSuccess(['items' => $items]);
}
