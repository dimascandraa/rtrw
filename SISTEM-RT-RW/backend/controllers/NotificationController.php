<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/middleware/auth.php';
require_once dirname(__DIR__) . '/helpers/response.php';

function listNotifications(PDO $connection, array $user): void
{
    $limit = filter_var($_GET['limit'] ?? '5', FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 1, 'max_range' => 50],
    ]);
    if ($limit === false) {
        jsonError(422, 'validation_error', 'Parameter limit harus bernilai 1 sampai 50.');
    }

    $conditions = ['id_pengguna = :user_id'];
    $parameters = ['user_id' => (int) $user['id']];
    if ($user['organization_id'] !== null) {
        $conditions[] = '(id_organisasi IS NULL OR id_organisasi = :organization_id)';
        $parameters['organization_id'] = (int) $user['organization_id'];
    }
    $where = implode(' AND ', $conditions);

    $statement = $connection->prepare(
        'SELECT id, judul AS title, isi AS message, jenis AS type,
                dibaca_pada AS read_at, dibuat_pada AS created_at
        FROM notifikasi
        WHERE ' . $where . '
        ORDER BY (dibaca_pada IS NULL) DESC, dibuat_pada DESC, id DESC
        LIMIT :limit'
    );
    foreach ($parameters as $name => $value) {
        $statement->bindValue(':' . $name, $value, PDO::PARAM_INT);
    }
    $statement->bindValue(':limit', $limit, PDO::PARAM_INT);
    $statement->execute();
    $items = $statement->fetchAll();

    $unreadCount = $connection->prepare(
        'SELECT COUNT(*)
        FROM notifikasi
        WHERE ' . $where . '
          AND dibaca_pada IS NULL
        '
    );
    foreach ($parameters as $name => $value) {
        $unreadCount->bindValue(':' . $name, $value, PDO::PARAM_INT);
    }
    $unreadCount->execute();

    $items = array_map(static function (array $notification): array {
        return [
            'id' => (int) $notification['id'],
            'title' => $notification['title'],
            'message' => $notification['message'],
            'type' => $notification['type'],
            'readAt' => $notification['read_at'],
            'createdAt' => $notification['created_at'],
        ];
    }, $items);

    jsonSuccess([
        'items' => $items,
        'unreadCount' => (int) $unreadCount->fetchColumn(),
    ]);
}
