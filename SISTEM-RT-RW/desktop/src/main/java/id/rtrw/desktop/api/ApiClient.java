package id.rtrw.desktop.api;

import id.rtrw.desktop.config.AppConfig;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Klien REST API (JSON) untuk Backend PHP Native.
 *
 * Autentikasi: bila respons login memuat token, token dikirim lewat header
 * "Authorization: Bearer ...". Cookie sesi PHP juga disimpan otomatis, sehingga
 * backend berbasis session tetap bekerja. Token hanya disimpan di memori.
 *
 * Semua metode bersifat blocking: panggil dari SwingWorker, bukan dari EDT.
 */
public final class ApiClient {

    private final AppConfig config;
    private HttpClient http;
    private String baseUrl;
    private String token;
    private Map<String, Object> user = Collections.emptyMap();

    public ApiClient(AppConfig config) {
        this.config = config;
        this.baseUrl = normalize(config.baseUrl());
        this.http = newHttpClient();
    }

    private HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .connectTimeout(Duration.ofSeconds(config.timeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private static String normalize(String url) {
        String u = url == null ? "" : url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = normalize(url);
        config.setBaseUrl(this.baseUrl);
    }

    // ------------------------------------------------------------------ sesi

    /** POST /api/login. Gagal bila kredensial salah atau server tidak terjangkau. */
    public void login(String identifier, String password) throws ApiException {
        logout();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(config.loginField(), identifier);
        body.put("password", password);
        Object response = send("POST", "/api/login", body);
        this.token = ApiData.findString(response, "token", "access_token", "jwt");
        this.user = ApiData.findMap(response, "user", "pengguna", "profile");
    }

    /** Menghapus token, data pengguna, dan cookie sesi dari memori. */
    public void logout() {
        this.token = null;
        this.user = Collections.emptyMap();
        this.http = newHttpClient();
    }

    public String userName() {
        Object v = ApiData.firstValue(user, "nama", "nama_lengkap", "name", "email", "username");
        return v == null ? "Pengguna" : v.toString();
    }

    public String userRole() {
        return roleText(ApiData.firstValue(user, "peran", "role", "nama_peran", "jabatan"));
    }

    @SuppressWarnings("unchecked")
    private static String roleText(Object value) {
        if (value instanceof Map) {
            return roleText(ApiData.firstValue((Map<String, Object>) value, "nama", "name", "peran"));
        }
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            return list.isEmpty() ? "" : roleText(list.get(0));
        }
        return value == null ? "" : value.toString();
    }

    // ------------------------------------------------------------------ permintaan

    public Object get(String path) throws ApiException {
        return send("GET", path, null);
    }

    public Object post(String path, Object body) throws ApiException {
        return send("POST", path, body);
    }

    private Object send(String method, String path, Object body) throws ApiException {
        HttpRequest.Builder builder;
        try {
            builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(config.timeoutSeconds()))
                    .header("Accept", "application/json");
        } catch (IllegalArgumentException e) {
            throw new ApiException("Alamat server tidak valid: " + baseUrl, 0, e);
        }
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json; charset=utf-8");
            builder.method(method, HttpRequest.BodyPublishers.ofString(Json.stringify(body), StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new ApiException("Server tidak merespons (waktu habis). Coba lagi.", 0, e);
        } catch (IOException e) {
            throw new ApiException("Tidak dapat terhubung ke server (" + baseUrl
                    + "). Pastikan server berjalan dan alamatnya benar.", 0, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Permintaan dibatalkan.", 0, e);
        }

        int status = response.statusCode();
        String text = response.body();
        Object json = null;
        if (text != null && !text.trim().isEmpty()) {
            try {
                json = Json.parse(text);
            } catch (Json.JsonException e) {
                if (status >= 200 && status < 300) {
                    throw new ApiException("Respons server bukan JSON yang valid: " + e.getMessage(), status, e);
                }
            }
        }

        if (status >= 200 && status < 300) {
            if (ApiData.isFailure(json)) {
                String msg = ApiData.message(json);
                throw new ApiException(msg != null ? msg : "Permintaan ditolak server.", status);
            }
            return json;
        }

        String serverMessage = ApiData.message(json);
        if (serverMessage != null) {
            throw new ApiException(serverMessage, status);
        }
        if (status == 401) {
            throw new ApiException("Sesi berakhir atau kredensial salah. Silakan masuk kembali.", status);
        }
        if (status == 403) {
            throw new ApiException("Anda tidak memiliki hak akses untuk data ini.", status);
        }
        if (status == 404) {
            throw new ApiException("Endpoint " + path + " tidak ditemukan di server (HTTP 404).", status);
        }
        throw new ApiException("Server mengembalikan kesalahan HTTP " + status + ".", status);
    }
}
