package id.rtrw.desktop;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.api.Json;
import id.rtrw.desktop.config.AppConfig;
import id.rtrw.desktop.dev.MockServer;
import id.rtrw.desktop.util.CsvExporter;

import com.sun.net.httpserver.HttpServer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pengujian tanpa framework (tidak memakai JUnit): jalankan ./test.sh.
 * Keluar dengan kode 1 bila ada pengujian yang gagal.
 */
public final class TestRunner {

    private static int failed;
    private static int total;

    private TestRunner() {
    }

    private static void check(String name, boolean condition) {
        total++;
        if (condition) {
            System.out.println("  OK    " + name);
        } else {
            failed++;
            System.out.println("  GAGAL " + name);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        System.out.println("== Json ==");
        Object parsed = Json.parse("{\"a\":1,\"b\":[true,null,2.5,\"x\\n\\u00e9\"],\"c\":{\"d\":\"\\\"q\\\"\"}}");
        Map<String, Object> map = (Map<String, Object>) parsed;
        check("angka bulat menjadi Long", Long.valueOf(1).equals(map.get("a")));
        List<Object> arr = (List<Object>) map.get("b");
        check("array: boolean, null, desimal, escape unicode",
                Boolean.TRUE.equals(arr.get(0)) && arr.get(1) == null && Double.valueOf(2.5).equals(arr.get(2))
                        && "x\né".equals(arr.get(3)));
        check("objek bersarang dan tanda kutip", "\"q\"".equals(((Map<String, Object>) map.get("c")).get("d")));
        check("urutan kunci dipertahankan", map.keySet().iterator().next().equals("a"));
        Object again = Json.parse(Json.stringify(parsed));
        check("parse(stringify(x)) sama dengan x", parsed.equals(again));
        boolean threw = false;
        try {
            Json.parse("{\"a\":}");
        } catch (Json.JsonException e) {
            threw = true;
        }
        check("JSON rusak menghasilkan JsonException", threw);
        check("stringify melepas karakter kontrol", Json.stringify("a\tb\"c").equals("\"a\\tb\\\"c\""));

        System.out.println("== ApiData ==");
        check("rows: array di akar", ApiData.rows(Json.parse("[{\"id\":1},{\"id\":2}]")).size() == 2);
        check("rows: dalam data", ApiData.rows(Json.parse("{\"success\":true,\"data\":[{\"id\":1}]}")).size() == 1);
        check("rows: dalam data.items",
                ApiData.rows(Json.parse("{\"data\":{\"items\":[{\"id\":1},{\"id\":2},{\"id\":3}],\"total\":3}}")).size() == 3);
        check("rows: objek tunggal menjadi satu baris", ApiData.rows(Json.parse("{\"data\":{\"id\":9,\"nama\":\"x\"}}")).size() == 1);
        check("rows: data kosong", ApiData.rows(Json.parse("{\"data\":[]}")).isEmpty());
<<<<<<< HEAD
        check("unreadCount: nilai numerik di dalam data",
                Integer.valueOf(2).equals(ApiData.findInteger(
                        Json.parse("{\"data\":{\"items\":[],\"unreadCount\":2}}"), "unreadCount")));
=======
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
        check("display: angka bulat tanpa .0", ApiData.display(Double.valueOf(50000)).equals("50000"));
        check("isFailure", ApiData.isFailure(Json.parse("{\"success\":false,\"message\":\"x\"}")));

        System.out.println("== CsvExporter ==");
        check("escape: pemisah dan kutip", CsvExporter.escapeForTest("a;b\"c").equals("\"a;b\"\"c\""));
        check("escape: cegah rumus Excel", CsvExporter.escapeForTest("=1+1").equals("'=1+1"));
        check("escape: angka negatif tidak diubah", CsvExporter.escapeForTest("-5").equals("-5"));

        System.out.println("== ApiClient + MockServer ==");
        HttpServer server = MockServer.start(0);
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            ApiClient api = new ApiClient(AppConfig.inMemory(url));

<<<<<<< HEAD
            Object health = api.health();
            check("GET /api/v1/health membaca status backend",
                    "ok".equals(ApiData.findMap(health, "data").get("status")));

            try {
                api.currentUser();
                check("GET /api/v1/auth/me tanpa login ditolak 401", false);
            } catch (ApiException e) {
                check("GET /api/v1/auth/me tanpa login ditolak 401", e.isUnauthorized());
=======
            try {
                api.get("/api/residents");
                check("tanpa login ditolak 401", false);
            } catch (ApiException e) {
                check("tanpa login ditolak 401", e.isUnauthorized());
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
            }

            try {
                api.login("admin@rtrw.test", "salah");
                check("password salah ditolak", false);
            } catch (ApiException e) {
                check("password salah ditolak dengan pesan server", e.isUnauthorized()
                        && e.getMessage().contains("salah"));
            }

            api.login("admin@rtrw.test", "admin123");
            check("login berhasil: nama dan peran terbaca", api.userName().contains("Sari")
                    && api.userRole().equals("Bendahara"));

<<<<<<< HEAD
            Object currentUser = api.currentUser();
            check("GET /api/v1/auth/me membaca profil sesi",
                    ApiData.findMap(currentUser, "user").get("name").equals("Bu Sari (Bendahara)"));

            Object dashboard = api.dashboardSummary();
            Map<String, Object> dashboardData = ApiData.findMap(dashboard, "data");
            check("GET /api/v1/dashboard/summary membaca statistik",
                    ApiData.findMap(dashboardData, "stats").get("residents") instanceof Number);

            Object announcementResponse = api.announcements();
            check("GET /api/v1/announcements membaca data.items",
                    ApiData.rows(announcementResponse).size() == 1
                            && "Kerja bakti Minggu pagi".equals(ApiData.rows(announcementResponse).get(0).get("title")));

            Object notificationResponse = api.notifications();
            Map<String, Object> notificationData = ApiData.findMap(notificationResponse, "data");
            check("GET /api/v1/notifications membaca items dan unreadCount",
                    ApiData.rows(notificationResponse).size() == 2
                            && Long.valueOf(1).equals(notificationData.get("unreadCount")));

=======
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
            List<Map<String, Object>> residents = ApiData.rows(api.get("/api/residents"));
            check("GET /api/residents mengembalikan data", residents.size() >= 5);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("nik", "3525019999990001");
            body.put("nama", "Warga Uji");
            body.put("no_kk", "3525010101100009");
            api.post("/api/residents", body);
            check("POST /api/residents menambah data",
                    ApiData.rows(api.get("/api/residents")).size() == residents.size() + 1);

            try {
                api.post("/api/residents", body);
                check("NIK ganda ditolak", false);
            } catch (ApiException e) {
                check("NIK ganda ditolak (422) dengan pesan server", e.statusCode() == 422
                        && e.getMessage().contains("terdaftar"));
            }

            Map<String, Object> verify = new LinkedHashMap<>();
            verify.put("id", 2L);
            verify.put("status", "diterima");
            api.post("/api/payments/verify", verify);
            boolean updated = false;
            for (Map<String, Object> p : ApiData.rows(api.get("/api/payments"))) {
                if (((Number) p.get("id")).longValue() == 2L) {
                    updated = "Diterima".equals(p.get("status"));
                }
            }
            check("POST /api/payments/verify mengubah status", updated);

            try {
                api.get("/api/tidak-ada");
                check("404 dilaporkan jelas", false);
            } catch (ApiException e) {
                check("404 dilaporkan jelas", e.statusCode() == 404);
            }

<<<<<<< HEAD
            api.logoutServer();
            try {
                api.currentUser();
                check("setelah logout endpoint auth/me menolak sesi", false);
            } catch (ApiException e) {
                check("setelah logout endpoint auth/me menolak sesi", e.isUnauthorized());
=======
            api.logout();
            try {
                api.get("/api/residents");
                check("setelah logout token terhapus", false);
            } catch (ApiException e) {
                check("setelah logout token terhapus", e.isUnauthorized());
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
            }
        } finally {
            server.stop(0);
        }

        System.out.println("== Server mati ==");
        ApiClient offline = new ApiClient(AppConfig.inMemory("http://127.0.0.1:1"));
        try {
            offline.login("a", "b");
            check("server tidak terjangkau", false);
        } catch (ApiException e) {
            check("server tidak terjangkau memberi pesan ramah", e.statusCode() == 0
                    && e.getMessage().contains("Tidak dapat terhubung"));
        }

        System.out.println();
        System.out.println((total - failed) + "/" + total + " pengujian lulus");
        System.exit(failed == 0 ? 0 : 1);
    }
}
