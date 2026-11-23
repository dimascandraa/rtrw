<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/controllers/AuthController.php';
require_once dirname(__DIR__) . '/controllers/DashboardController.php';
require_once dirname(__DIR__) . '/controllers/AnnouncementController.php';
require_once dirname(__DIR__) . '/controllers/NotificationController.php';
require_once dirname(__DIR__) . '/controllers/ResourceController.php';
require_once dirname(__DIR__) . '/controllers/AccountController.php';
require_once dirname(__DIR__) . '/controllers/LetterController.php';
require_once dirname(__DIR__) . '/controllers/OperationController.php';
require_once dirname(__DIR__) . '/controllers/ContentController.php';

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
    if ($route === 'auth/register' && $method === 'POST') {
        registerAccount($connection);
    }
    if ($route === 'auth/logout' && $method === 'POST') {
        logout($connection, $configuration);
    }
    if ($route === 'auth/me' && $method === 'GET') {
        currentUser($connection);
    }
    if ($route === 'auth/profile' && $method === 'PATCH') {
        updateAccountProfile($connection);
    }
    if ($route === 'auth/password' && $method === 'POST') {
        changeAccountPassword($connection);
    }
    if ($route === 'registrations/pending' && $method === 'GET') {
        pendingRegistrations($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Aregistrations\/([1-9][0-9]*)\/approve\z/', $route, $registrationMatch) === 1 && $method === 'POST') {
        approveRegistration($connection, authenticatedUser($connection), (int) $registrationMatch[1]);
    }
    if ($route === 'letters' && $method === 'POST') {
        createLetterRequest($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Aletters\/([1-9][0-9]*)\/decision\z/', $route, $letterMatch) === 1 && $method === 'POST') {
        decideLetterRequest($connection, authenticatedUser($connection), (int) $letterMatch[1]);
    }
    if ($route === 'families' && $method === 'POST') {
        createFamily($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Afamilies\/([1-9][0-9]*)\z/', $route, $familyMatch) === 1 && $method === 'PATCH') {
        updateFamily($connection, authenticatedUser($connection), (int) $familyMatch[1]);
    }
    if (preg_match('/\Afamilies\/([1-9][0-9]*)\z/', $route, $familyMatch) === 1 && $method === 'DELETE') {
        deleteFamily($connection, authenticatedUser($connection), (int) $familyMatch[1]);
    }
    if ($route === 'residents' && $method === 'POST') {
        createResident($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Aresidents\/([1-9][0-9]*)\z/', $route, $residentMatch) === 1 && $method === 'PATCH') {
        updateResident($connection, authenticatedUser($connection), (int) $residentMatch[1]);
    }
    if (preg_match('/\Aresidents\/([1-9][0-9]*)\z/', $route, $residentMatch) === 1 && $method === 'DELETE') {
        deleteResident($connection, authenticatedUser($connection), (int) $residentMatch[1]);
    }
    if ($route === 'invoices' && $method === 'POST') {
        createInvoice($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Ainvoices\/([1-9][0-9]*)\z/', $route, $invoiceMatch) === 1 && $method === 'PATCH') {
        updateInvoice($connection, authenticatedUser($connection), (int) $invoiceMatch[1]);
    }
    if (preg_match('/\Ainvoices\/([1-9][0-9]*)\z/', $route, $invoiceMatch) === 1 && $method === 'DELETE') {
        deleteInvoice($connection, authenticatedUser($connection), (int) $invoiceMatch[1]);
    }
    if (preg_match('/\Apayments\/([1-9][0-9]*)\/decision\z/', $route, $paymentMatch) === 1 && $method === 'POST') {
        decidePayment($connection, authenticatedUser($connection), (int) $paymentMatch[1]);
    }
    if ($route === 'announcements' && $method === 'POST') {
        writeAnnouncement($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Aannouncements\/([1-9][0-9]*)\z/', $route, $announcementMatch) === 1 && $method === 'PATCH') {
        writeAnnouncement($connection, authenticatedUser($connection), (int) $announcementMatch[1]);
    }
    if (preg_match('/\Aannouncements\/([1-9][0-9]*)\z/', $route, $announcementMatch) === 1 && $method === 'DELETE') {
        deleteAnnouncement($connection, authenticatedUser($connection), (int) $announcementMatch[1]);
    }
    if ($route === 'activities' && $method === 'POST') {
        writeActivity($connection, authenticatedUser($connection));
    }
    if (preg_match('/\Aactivities\/([1-9][0-9]*)\z/', $route, $activityMatch) === 1 && $method === 'PATCH') {
        writeActivity($connection, authenticatedUser($connection), (int) $activityMatch[1]);
    }
    if (preg_match('/\Aactivities\/([1-9][0-9]*)\z/', $route, $activityMatch) === 1 && $method === 'DELETE') {
        deleteActivity($connection, authenticatedUser($connection), (int) $activityMatch[1]);
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
    if (preg_match('/\Aresources\/([a-z-]+)\z/', $route, $resourceMatch) === 1 && $method === 'GET') {
        listResource($connection, authenticatedUser($connection), $resourceMatch[1]);
    }

    $knownRoutes = [
        'health' => ['GET'],
        'auth/login' => ['POST'],
        'auth/register' => ['POST'],
        'auth/logout' => ['POST'],
        'auth/me' => ['GET'],
        'auth/profile' => ['PATCH'],
        'auth/password' => ['POST'],
        'registrations/pending' => ['GET'],
        'letters' => ['POST'],
        'dashboard/summary' => ['GET'],
        'announcements' => ['GET'],
        'notifications' => ['GET'],
        'families' => ['POST'],
        'residents' => ['POST'],
        'invoices' => ['POST'],
        'activities' => ['POST'],
    ];
    if (preg_match('/\Aresources\/[a-z-]+\z/', $route) === 1) {
        $knownRoutes[$route] = ['GET'];
    }
    if (preg_match('/\Aregistrations\/[1-9][0-9]*\/approve\z/', $route) === 1) {
        $knownRoutes[$route] = ['POST'];
    }
    if (preg_match('/\Aletters\/[1-9][0-9]*\/decision\z/', $route) === 1) {
        $knownRoutes[$route] = ['POST'];
    }
    if (preg_match('/\A(?:families|residents|invoices|announcements|activities)\/[1-9][0-9]*\z/', $route) === 1) {
        $knownRoutes[$route] = ['PATCH', 'DELETE'];
    }
    if (preg_match('/\Apayments\/[1-9][0-9]*\/decision\z/', $route) === 1) {
        $knownRoutes[$route] = ['POST'];
    }
    if (isset($knownRoutes[$route])) {
        header('Allow: ' . implode(', ', $knownRoutes[$route]));
        jsonError(405, 'method_not_allowed', 'Metode HTTP tidak diizinkan untuk endpoint ini.');
    }

    jsonError(404, 'not_found', 'Endpoint tidak ditemukan.');
}
