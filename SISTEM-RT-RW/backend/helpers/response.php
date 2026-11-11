<?php
declare(strict_types=1);

function jsonResponse(int $statusCode, array $payload): void
{
    http_response_code($statusCode);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');

    $json = json_encode(
        $payload,
        JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_INVALID_UTF8_SUBSTITUTE
    );
    if ($json === false) {
        http_response_code(500);
        echo '{"error":{"code":"serialization_error","message":"Respons API tidak dapat dibuat."}}';
        exit;
    }

    echo $json;
    exit;
}

function jsonSuccess($data, int $statusCode = 200): void
{
    jsonResponse($statusCode, ['data' => $data]);
}

function jsonError(int $statusCode, string $code, string $message): void
{
    jsonResponse($statusCode, [
        'error' => [
            'code' => $code,
            'message' => $message,
        ],
    ]);
}

function jsonRequestBody(): array
{
    $contentType = strtolower(trim(explode(';', $_SERVER['CONTENT_TYPE'] ?? '')[0]));
    if ($contentType !== 'application/json') {
        jsonError(415, 'unsupported_media_type', 'Kirim body dengan Content-Type application/json.');
    }

    $body = file_get_contents('php://input');
    if ($body === false) {
        jsonError(400, 'invalid_request', 'Body permintaan tidak dapat dibaca.');
    }
    if (strlen($body) > 16384) {
        jsonError(413, 'request_too_large', 'Body permintaan melebihi batas ukuran.');
    }

    try {
        $decoded = json_decode($body, false, 32, JSON_THROW_ON_ERROR);
    } catch (JsonException $exception) {
        jsonError(400, 'invalid_json', 'Body permintaan harus berupa JSON yang valid.');
    }

    if (!$decoded instanceof stdClass) {
        jsonError(400, 'invalid_json', 'Body permintaan harus berupa objek JSON.');
    }

    return (array) $decoded;
}
