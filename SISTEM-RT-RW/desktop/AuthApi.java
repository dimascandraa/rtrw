package api;

import model.User;
import util.SessionManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Login menggunakan endpoint dan format yang sama dengan aplikasi web.
 */
public final class AuthApi {
    private AuthApi() {}

    public static User login(String identity, String password) throws ApiException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("identity", identity);
        body.put("password", password);

        Map<String, Object> response = ApiClient.post("/auth/login", body);
        Map<String, Object> userData = asMap(response.get("user"));
        if (userData == null) {
            throw new ApiException("Format respons login tidak sesuai: data pengguna tidak ditemukan.");
        }

        User user = toUser(userData);
        SessionManager.start(user);
        return user;
    }

    public static User currentUser() throws ApiException {
        Map<String, Object> response = ApiClient.get("/auth/me");
        Map<String, Object> userData = asMap(response.get("user"));
        if (userData == null) {
            throw new ApiException("Format respons sesi tidak sesuai: data pengguna tidak ditemukan.");
        }
        User user = toUser(userData);
        SessionManager.start(user);
        return user;
    }

    public static void logout() throws ApiException {
        ApiClient.post("/auth/logout", new LinkedHashMap<String, Object>());
        SessionManager.clear();
    }

    private static User toUser(Map<String, Object> userData) throws ApiException {
        Object id = userData.get("id");
        Object name = userData.get("name");
        Object role = userData.get("role");
        if (!(id instanceof Number) || !(name instanceof String) || !(role instanceof String)) {
            throw new ApiException("Format data pengguna dari API tidak valid.");
        }

        return new User(
                ((Number) id).intValue(),
                (String) name,
                (String) role,
                stringValue(userData.get("organizationName")),
                stringValue(userData.get("region")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }
}
