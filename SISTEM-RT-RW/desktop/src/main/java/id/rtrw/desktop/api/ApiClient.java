package id.rtrw.desktop.api;

import id.rtrw.desktop.config.AppConfig;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
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
    private boolean authenticated;
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

    /** GET /api/v1/health. */
    public Object health() throws ApiException {
        return get("/api/v1/health");
    }

    /** POST /api/v1/auth/login. Gagal bila kredensial salah atau server tidak terjangkau. */
    public void login(String identifier, String password) throws ApiException {
        logout();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(config.loginField(), identifier);
        body.put("password", password);
        body.put("client", "desktop");
        Object response = send("POST", "/api/v1/auth/login", body);
        this.token = ApiData.findString(response, "accessToken", "token", "access_token", "jwt");
        this.user = ApiData.findMap(response, "user", "pengguna", "profile");
        this.authenticated = true;
    }

    /** POST /api/v1/auth/logout, bila ada sesi aktif. */
    public void logoutServer() throws ApiException {
        if (authenticated) {
            send("POST", "/api/v1/auth/logout", new LinkedHashMap<>());
        }
        logout();
    }

    /** Menghapus token, data pengguna, dan cookie sesi dari memori. */
    public void logout() {
        this.token = null;
        this.authenticated = false;
        this.user = Collections.emptyMap();
        this.http = newHttpClient();
    }

    /** GET /api/v1/auth/me. */
    public Object currentUser() throws ApiException {
        Object response = get("/api/v1/auth/me");
        Map<String, Object> found = ApiData.findMap(response, "user", "pengguna", "profile");
        if (!found.isEmpty()) {
            this.user = found;
        }
        return response;
    }

    /** GET /api/v1/dashboard/summary. */
    public Object dashboardSummary() throws ApiException {
        return get("/api/v1/dashboard/summary");
    }

    /** GET /api/v1/announcements. */
    public Object announcements() throws ApiException {
        return get("/api/v1/announcements?limit=50");
    }

    /** GET /api/v1/notifications. */
    public Object notifications() throws ApiException {
        return get("/api/v1/notifications?limit=50");
    }

    /** GET /api/v1/resources/{resource}. */
    public Object resource(String resource) throws ApiException {
        return resource(resource, 100, 0);
    }

    public Object resource(String resource, int limit, int offset) throws ApiException {
        return resource(resource, limit, offset, "");
    }

    public Object resource(String resource, int limit, int offset, String search) throws ApiException {
        if (resource == null || !resource.matches("[a-z-]+")) {
            throw new ApiException("Nama resource API tidak valid.", 0);
        }
        if (limit < 1 || limit > 100 || offset < 0 || offset > 1000000) {
            throw new ApiException("Parameter halaman resource API tidak valid.", 0);
        }
        String path = "/api/v1/resources/" + resource + "?limit=" + limit + "&offset=" + offset;
        if (search != null && !search.trim().isEmpty()) {
            path += "&search=" + URLEncoder.encode(search.trim(), StandardCharsets.UTF_8);
        }
        return get(path);
    }

    public Object pendingRegistrations() throws ApiException {
        return get("/api/v1/registrations/pending");
    }

    public Object registerAccount(Map<String, Object> body) throws ApiException {
        return post("/api/v1/auth/register", body);
    }

    public Object approveRegistration(long id) throws ApiException {
        return post("/api/v1/registrations/" + positiveId(id) + "/approve", new LinkedHashMap<>());
    }

    public Object createLetterRequest(Map<String, Object> body) throws ApiException {
        return post("/api/v1/letters", body);
    }

    public Object decideLetter(long id, Map<String, Object> body) throws ApiException {
        return post("/api/v1/letters/" + positiveId(id) + "/decision", body);
    }

    public Object updateProfile(Map<String, Object> body) throws ApiException {
        Object response = send("PATCH", "/api/v1/auth/profile", body);
        Map<String, Object> found = ApiData.findMap(response, "user", "pengguna", "profile");
        if (!found.isEmpty()) {
            this.user = found;
        }
        return response;
    }

    public Object changePassword(Map<String, Object> body) throws ApiException {
        return post("/api/v1/auth/password", body);
    }

    public Object createFamily(Map<String, Object> body) throws ApiException {
        return post("/api/v1/families", body);
    }

    public Object updateFamily(long id, Map<String, Object> body) throws ApiException {
        return patch("/api/v1/families/" + positiveId(id), body);
    }

    public Object deleteFamily(long id) throws ApiException {
        return delete("/api/v1/families/" + positiveId(id));
    }

    public Object createResident(Map<String, Object> body) throws ApiException {
        return post("/api/v1/residents", body);
    }

    public Object updateResident(long id, Map<String, Object> body) throws ApiException {
        return patch("/api/v1/residents/" + positiveId(id), body);
    }

    public Object deleteResident(long id) throws ApiException {
        return delete("/api/v1/residents/" + positiveId(id));
    }

    public Object createInvoice(Map<String, Object> body) throws ApiException {
        return post("/api/v1/invoices", body);
    }

    public Object updateInvoice(long id, Map<String, Object> body) throws ApiException {
        return patch("/api/v1/invoices/" + positiveId(id), body);
    }

    public Object deleteInvoice(long id) throws ApiException {
        return delete("/api/v1/invoices/" + positiveId(id));
    }

    public Object decidePayment(long id, Map<String, Object> body) throws ApiException {
        return post("/api/v1/payments/" + positiveId(id) + "/decision", body);
    }

    public Object createAnnouncement(Map<String, Object> body) throws ApiException {
        return post("/api/v1/announcements", body);
    }

    public Object updateAnnouncement(long id, Map<String, Object> body) throws ApiException {
        return patch("/api/v1/announcements/" + positiveId(id), body);
    }

    public Object deleteAnnouncement(long id) throws ApiException {
        return delete("/api/v1/announcements/" + positiveId(id));
    }

    public Object createActivity(Map<String, Object> body) throws ApiException {
        return post("/api/v1/activities", body);
    }

    public Object updateActivity(long id, Map<String, Object> body) throws ApiException {
        return patch("/api/v1/activities/" + positiveId(id), body);
    }

    public Object deleteActivity(long id) throws ApiException {
        return delete("/api/v1/activities/" + positiveId(id));
    }

    private static long positiveId(long id) throws ApiException {
        if (id < 1) {
            throw new ApiException("ID data tidak valid.", 0);
        }
        return id;
    }

    public String userName() {
        Object v = ApiData.firstValue(user, "nama", "nama_lengkap", "name", "email", "username");
        return v == null ? "Pengguna" : v.toString();
    }

    public String userEmail() {
        Object value = ApiData.firstValue(user, "email");
        return value == null ? "" : value.toString();
    }

    public String userPhone() {
        Object value = ApiData.firstValue(user, "phone", "telepon", "no_telepon");
        return value == null ? "" : value.toString();
    }

    public String userRoleCode() {
        Object value = ApiData.firstValue(user, "roleCode", "role_code");
        return value == null ? "" : value.toString();
    }

    public boolean hasPermission(String permission) {
        Object values = user.get("permissions");
        if (!(values instanceof List)) {
            return false;
        }
        return ((List<?>) values).contains(permission);
    }

    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
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

    public Object patch(String path, Object body) throws ApiException {
        return send("PATCH", path, body);
    }

    public Object delete(String path) throws ApiException {
        return send("DELETE", path, null);
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
