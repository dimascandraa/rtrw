package api;

import app.AppConfig;
import util.Json;
import util.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Satu-satunya kelas yang berbicara HTTP ke backend.
 * Kelas API lain (AuthApi, ResidentApi, ...) memakai kelas ini.
 * Method di sini bersifat blocking, jadi panggil dari SwingWorker, bukan dari UI thread.
 */
public final class ApiClient {
    private static final HttpClient HTTP = HttpClient.newBuilder()
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

        if (SessionManager.getToken() != null) {
            rb.header("Authorization", "Bearer " + SessionManager.getToken());
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
        Map<String, Object> json = Collections.emptyMap();
        if (!text.isEmpty()) {
            try {
                json = Json.parseObject(text);
            } catch (IllegalArgumentException e) {
                if (res.statusCode() < 400) throw new ApiException("Respons server bukan JSON yang valid");
            }
        }

        if (res.statusCode() >= 400) {
            Object msg = json.get("message");
            throw new ApiException(msg != null ? msg.toString() : "Server menjawab HTTP " + res.statusCode(),
                    res.statusCode());
        }
        return json;
    }
}
