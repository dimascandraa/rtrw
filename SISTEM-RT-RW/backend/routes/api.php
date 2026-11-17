<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/controllers/AuthController.php';
require_once dirname(__DIR__) . '/controllers/DashboardController.php';
require_once dirname(__DIR__) . '/controllers/AnnouncementController.php';
require_once dirname(__DIR__) . '/controllers/NotificationController.php';

function dispatchApi(PDO $connection, array $configuration): void
{
    $requestPath = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
    $apiMarker = '/api/v1';
    $markerPosition = is_string($requestPath) ? strpos($requestPath, $apiMarker) : false;
    if ($markerPosition === false) {
        jsonError(404, 'not_found', 'Endpoint tidak ditemukan.');
    }

    $route = trim(substr($requestPath, $markerPosition + strlen($apiMarker)), '/');
    $method = strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');

    if ($route === 'health' && $method === 'GET') {
        $connection->query('SELECT 1')->fetchColumn();
        jsonSuccess(['status' => 'ok']);
    }
    if ($route === 'auth/login' && $method === 'POST') {
        login($connection, $configuration);
    }
    if ($route === 'auth/logout' && $method === 'POST') {
        logout($connection, $configuration);
    }
    if ($route === 'auth/me' && $method === 'GET') {
        currentUser($connection);
    }

    if ($route === 'dashboard/summary' && $method === 'GET') {
        dashboardSummary($connection, authenticatedUser($connection));
    }
    if ($route === 'announcements' && $method === 'GET') {
        listAnnouncements($connection, authenticatedUser($connection));
    }
    if ($route === 'notifications' && $method === 'GET') {
        listNotifications($connection, authenticatedUser($connection));
    }

    $knownRoutes = [
        'health' => ['GET'],
        'auth/login' => ['POST'],
        'auth/logout' => ['POST'],
        'auth/me' => ['GET'],
        'dashboard/summary' => ['GET'],
        'announcements' => ['GET'],
        'notifications' => ['GET'],
    ];
    if (isset($knownRoutes[$route])) {
        header('Allow: ' . implode(', ', $knownRoutes[$route]));
        jsonError(405, 'method_not_allowed', 'Metode HTTP tidak diizinkan untuk endpoint ini.');
    }

    jsonError(404, 'not_found', 'Endpoint tidak ditemukan.');
}
