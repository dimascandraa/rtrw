<?php
declare(strict_types=1);

function loadEnvironment(string $filePath): void
{
    if (!is_file($filePath)) {
        return;
    }

    $lines = file($filePath, FILE_IGNORE_NEW_LINES);
    if ($lines === false) {
        throw new RuntimeException('Unable to read the backend environment file.');
    }

    foreach ($lines as $lineNumber => $line) {
        $line = trim($line);
        if ($line === '' || $line[0] === '#') {
            continue;
        }

        $separator = strpos($line, '=');
        if ($separator === false) {
            throw new RuntimeException('Invalid environment setting on line ' . ($lineNumber + 1) . '.');
        }

        $name = trim(substr($line, 0, $separator));
        $value = trim(substr($line, $separator + 1));
        if (!preg_match('/\A[A-Z_][A-Z0-9_]*\z/', $name)) {
            throw new RuntimeException('Invalid environment variable name on line ' . ($lineNumber + 1) . '.');
        }

        if (strlen($value) >= 2 && (
            ($value[0] === '"' && substr($value, -1) === '"')
            || ($value[0] === "'" && substr($value, -1) === "'")
        )) {
            $value = substr($value, 1, -1);
        }

        if (getenv($name) === false) {
            putenv($name . '=' . $value);
            $_ENV[$name] = $value;
        }
    }
}
