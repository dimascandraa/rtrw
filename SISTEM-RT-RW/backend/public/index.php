<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/config/database.php';
require_once dirname(__DIR__) . '/config/app.php';
require_once dirname(__DIR__) . '/helpers/response.php';
require_once dirname(__DIR__) . '/routes/api.php';

header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: same-origin');

try {
    loadEnvironment(dirname(__DIR__) . DIRECTORY_SEPARATOR . '.env');
    $configuration = appConfiguration();
    date_default_timezone_set($configuration['timezone']);
    $connection = databaseConnection();
    dispatchApi($connection, $configuration);
} catch (DatabaseConfigurationException $exception) {
    error_log('Sistem RT/RW database configuration error: ' . $exception->getMessage());
    jsonError(503, 'database_not_configured', 'Konfigurasi database belum lengkap. Salin backend/.env.example menjadi backend/.env.');
} catch (DatabaseConnectionException $exception) {
    error_log('Sistem RT/RW database connection failed: ' . $exception->getPrevious()->getMessage());
    jsonError(503, 'database_unavailable', 'Database belum dapat dihubungi. Periksa MySQL dan backend/.env.');
} catch (PDOException $exception) {
    error_log('Sistem RT/RW database query failed: ' . $exception->getMessage());
    jsonError(500, 'database_error', 'Permintaan database gagal diproses.');
} catch (RuntimeException $exception) {
    error_log('Sistem RT/RW configuration error: ' . $exception->getMessage());
    jsonError(500, 'configuration_error', 'Konfigurasi backend tidak valid. Periksa backend/.env.');
} catch (Throwable $exception) {
    error_log('Sistem RT/RW API error: ' . $exception->getMessage());
    jsonError(500, 'internal_error', 'Terjadi kesalahan pada server.');
}
