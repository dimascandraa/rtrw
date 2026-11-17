package api;

import app.AppConfig;
import model.User;
import util.SessionManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Login ke backend: POST /api/login
 *
 * ASUMSI (cocokkan dengan dokumentasi API dari tim Backend):
 *   request : {"username": "...", "password": "..."}
 *   response: {"token": "...", "user": {"id": 1, "name": "...", "role": "admin"}}
 *             (boleh juga dibungkus di dalam "data")
 */
public final class AuthApi {
    private AuthApi() {}

    public static User login(String username, String password) throws ApiException {
        User user;
        if (AppConfig.mockMode()) {
            user = mockLogin(username, password);
            SessionManager.start("mock-token", user);
            return user;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", password);

        Map<String, Object> res = ApiClient.post("/api/login", body);
        Map<String, Object> data = asMap(res.get("data"));
        if (data == null) data = res;

        Object token = data.get("token");
        Map<String, Object> u = asMap(data.get("user"));
        if (token == null || u == null) {
            throw new ApiException("Format respons login tidak sesuai dokumentasi API");
        }

        user = new User(
                u.get("id") instanceof Number ? ((Number) u.get("id")).intValue() : 0,
                String.valueOf(u.get("name")),
                String.valueOf(u.get("role")));
        SessionManager.start(token.toString(), user);
        return user;
    }

    /** Akun tiruan untuk pengembangan. Bukan data warga asli. */
    private static User mockLogin(String username, String password) throws ApiException {
        try {
            Thread.sleep(400); // simulasi jaringan
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if ("admin".equals(username) && "admin123".equals(password)) return new User(1, "Admin Dummy", "admin");
        if ("rt".equals(username) && "rt123".equals(password)) return new User(2, "Ketua RT Dummy", "rt");
        if ("rw".equals(username) && "rw123".equals(password)) return new User(3, "Ketua RW Dummy", "rw");
        throw new ApiException("Username atau password salah", 401);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }
}
