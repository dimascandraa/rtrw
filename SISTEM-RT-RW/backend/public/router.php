<?php
declare(strict_types=1);

$requestPath = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
$requestPath = is_string($requestPath) ? rawurldecode($requestPath) : '/';

if (strpos($requestPath, '/api/v1') !== false) {
    require __DIR__ . '/index.php';
    exit;
}

$webRoot = realpath(dirname(__DIR__, 2) . DIRECTORY_SEPARATOR . 'web');
if ($webRoot === false) {
    http_response_code(500);
    exit('Web directory is not available.');
}

$relativePath = ltrim($requestPath, '/');
if ($relativePath === '') {
    $relativePath = 'index.html';
}
$filePath = realpath($webRoot . DIRECTORY_SEPARATOR . str_replace(
    ['/', '\\'],
    DIRECTORY_SEPARATOR,
    $relativePath
));

if ($filePath === false || (
    $filePath !== $webRoot
    && strpos($filePath, $webRoot . DIRECTORY_SEPARATOR) !== 0
)) {
    http_response_code(404);
    exit('Not found.');
}

if (is_dir($filePath)) {
    $filePath = realpath($filePath . DIRECTORY_SEPARATOR . 'index.html');
}
if ($filePath === false || !is_file($filePath)) {
    http_response_code(404);
    exit('Not found.');
}

$mimeTypes = [
    'css' => 'text/css; charset=utf-8',
    'html' => 'text/html; charset=utf-8',
    'js' => 'text/javascript; charset=utf-8',
    'json' => 'application/json; charset=utf-8',
    'svg' => 'image/svg+xml',
];
$extension = strtolower(pathinfo($filePath, PATHINFO_EXTENSION));
header('Content-Type: ' . ($mimeTypes[$extension] ?? 'application/octet-stream'));
header('X-Content-Type-Options: nosniff');
readfile($filePath);
