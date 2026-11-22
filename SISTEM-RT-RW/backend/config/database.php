<?php
declare(strict_types=1);

require_once __DIR__ . '/env.php';

class DatabaseConnectionException extends RuntimeException
{
}

class DatabaseConfigurationException extends RuntimeException
{
}

function databaseConnection(): PDO
{
    static $connection = null;
    if ($connection instanceof PDO) {
        return $connection;
    }

    $host = getenv('DB_HOST') ?: '127.0.0.1';
    $database = getenv('DB_NAME') ?: 'sistem_rt_rw';
    $username = getenv('DB_USER');
    $password = getenv('DB_PASSWORD');
    $port = filter_var(getenv('DB_PORT') ?: '3306', FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 1, 'max_range' => 65535],
    ]);

    if ($username === false || $username === '') {
        throw new DatabaseConfigurationException('DB_USER must be configured in backend/.env.');
    }
    if ($port === false) {
        throw new DatabaseConfigurationException('DB_PORT must be a valid TCP port.');
    }

    $dsn = sprintf(
        'mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4',
        $host,
        $port,
        $database
    );

    try {
        $connection = new PDO($dsn, $username, $password === false ? '' : $password, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]);
    } catch (PDOException $exception) {
        throw new DatabaseConnectionException('The MySQL connection could not be established.', 0, $exception);
    }

    $connection->exec("SET time_zone = '+07:00'");

    return $connection;
}
