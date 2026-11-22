package api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Memuat data beranda dari endpoint yang juga dipakai aplikasi web. */
public final class DashboardApi {
    private DashboardApi() {}

    public static Map<String, Object> loadHome() throws ApiException {
        Map<String, Object> summary = ApiClient.get("/dashboard/summary");
        Map<String, Object> announcements = ApiClient.get("/announcements?limit=5");
        Map<String, Object> notifications = ApiClient.get("/notifications?limit=5");

        requireMap(summary.get("organization"), "organization");
        requireMap(summary.get("stats"), "stats");
        requireMap(summary.get("period"), "period");
        requireList(summary.get("activities"), "activities");
        requireList(announcements.get("items"), "announcements.items");
        requireList(notifications.get("items"), "notifications.items");
        if (!(notifications.get("unreadCount") instanceof Number)) {
            throw new ApiException("Format respons notifikasi tidak valid.");
        }

        Map<String, Object> home = new LinkedHashMap<>();
        home.put("summary", summary);
        home.put("announcements", announcements.get("items"));
        home.put("notifications", notifications.get("items"));
        home.put("unreadCount", notifications.get("unreadCount"));
        return home;
    }

    private static void requireMap(Object value, String field) throws ApiException {
        if (!(value instanceof Map)) {
            throw new ApiException("Format respons API tidak valid: " + field + " harus berupa objek.");
        }
    }

    private static void requireList(Object value, String field) throws ApiException {
        if (!(value instanceof List)) {
            throw new ApiException("Format respons API tidak valid: " + field + " harus berupa daftar.");
        }
    }
}
