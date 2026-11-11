<?php
declare(strict_types=1);

function appConfiguration(): array
{
    $timezone = getenv('APP_TIMEZONE') ?: 'Asia/Jakarta';
    new DateTimeZone($timezone);

    $sessionTtl = filter_var(getenv('APP_SESSION_TTL_SECONDS') ?: '604800', FILTER_VALIDATE_INT, [
        'options' => ['min_range' => 900, 'max_range' => 2592000],
    ]);
    if ($sessionTtl === false) {
        throw new RuntimeException('APP_SESSION_TTL_SECONDS must be between 900 and 2592000.');
    }

    $secureCookieSetting = getenv('APP_SECURE_COOKIE');
    if ($secureCookieSetting === false || $secureCookieSetting === '') {
        $secureCookie = !empty($_SERVER['HTTPS']) && strtolower((string) $_SERVER['HTTPS']) !== 'off';
    } else {
        $secureCookie = filter_var($secureCookieSetting, FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE);
        if ($secureCookie === null) {
            throw new RuntimeException('APP_SECURE_COOKIE must be true or false.');
        }
    }

    return [
        'timezone' => $timezone,
        'session_ttl' => $sessionTtl,
        'secure_cookie' => $secureCookie,
    ];
}
