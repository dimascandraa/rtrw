package api;

import app.AppConfig;
import util.Json;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Satu-satunya kelas yang berbicara HTTP ke backend.
 * Kelas API lain (AuthApi, ResidentApi, ...) memakai kelas ini.
 * Method di sini bersifat blocking, jadi panggil dari SwingWorker, bukan dari UI thread.
 */
public final class ApiClient {
    private static final CookieManager COOKIES = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .cookieHandler(COOKIES)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private ApiClient() {}

    public static Map<String, Object> get(String path) throws ApiException {
        return send("GET", path, null);
    }

    public static Map<String, Object> post(String path, Map<String, Object> body) throws ApiException {
        return send("POST", path, body);
    }

    public static Map<String, Object> put(String path, Map<String, Object> body) throws ApiException {
        return send("PUT", path, body);
    }

    public static Map<String, Object> delete(String path) throws ApiException {
        return send("DELETE", path, null);
    }

    private static Map<String, Object> send(String method, String path, Map<String, Object> body)
            throws ApiException {
        HttpRequest.Builder rb;
        try {
            rb = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.baseUrl() + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json");
        } catch (IllegalArgumentException e) {
            throw new ApiException("URL API tidak valid. Periksa api.base.url di config.properties");
        }

        if (body != null) {
            rb.header("Content-Type", "application/json");
            rb.method(method, HttpRequest.BodyPublishers.ofString(Json.stringify(body)));
        } else {
            rb.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> res;
        try {
            res = HTTP.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Permintaan dibatalkan");
        } catch (IOException e) {
            throw new ApiException("Tidak dapat terhubung ke server: " + e.getMessage());
        }

        String text = res.body() == null ? "" : res.body().trim();
        Map<String, Object> json;
        try {
            json = Json.parseObject(text);
        } catch (IllegalArgumentException e) {
            throw new ApiException("API mengirim respons yang tidak valid.", res.statusCode());
        }

        if (res.statusCode() >= 400) {
            Object errorObject = json.get("error");
            Map<String, Object> error = errorObject instanceof Map
                    ? toStringKeyedMap(errorObject)
                    : json;
            Object message = error.get("message");
            throw new ApiException(message == null
                    ? "API menjawab HTTP " + res.statusCode()
                    : message.toString(), res.statusCode());
        }

        Object data = json.get("data");
        if (!(data instanceof Map<?, ?>)) {
            throw new ApiException("Format respons API tidak sesuai: properti data tidak ditemukan.",
                    res.statusCode());
        }
        return toStringKeyedMap(data);
    }

    private static Map<String, Object> toStringKeyedMap(Object value) {
        Map<?, ?> source = (Map<?, ?>) value;
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() instanceof String) {
                result.put((String) entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
