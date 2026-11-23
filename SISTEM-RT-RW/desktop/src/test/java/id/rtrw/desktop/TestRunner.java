package id.rtrw.desktop;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.api.Json;
import id.rtrw.desktop.config.AppConfig;
import id.rtrw.desktop.dev.MockServer;
import id.rtrw.desktop.util.CsvExporter;
import id.rtrw.desktop.util.XlsxExporter;

import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

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
        check("unreadCount: nilai numerik di dalam data",
                Integer.valueOf(2).equals(ApiData.findInteger(
                        Json.parse("{\"data\":{\"items\":[],\"unreadCount\":2}}"), "unreadCount")));
        check("display: angka bulat tanpa .0", ApiData.display(Double.valueOf(50000)).equals("50000"));
        check("isFailure", ApiData.isFailure(Json.parse("{\"success\":false,\"message\":\"x\"}")));
        check("message: membaca pesan error bersarang",
                "Akses ditolak".equals(ApiData.message(Json.parse(
                        "{\"error\":{\"code\":\"forbidden\",\"message\":\"Akses ditolak\"}}"))));

        System.out.println("== CsvExporter ==");
        check("escape: pemisah dan kutip", CsvExporter.escapeForTest("a;b\"c").equals("\"a;b\"\"c\""));
        check("escape: cegah rumus Excel", CsvExporter.escapeForTest("=1+1").equals("'=1+1"));
        check("escape: angka negatif tidak diubah", CsvExporter.escapeForTest("-5").equals("-5"));

        System.out.println("== XlsxExporter ==");
        Path workbook = Files.createTempFile("rtrw-report-", ".xlsx");
        try {
            Map<String, Object> reportRow = new LinkedHashMap<>();
            reportRow.put("keterangan", "Kas & iuran");
            reportRow.put("total_nominal", 1500.50);
            List<XlsxExporter.Sheet> sheets = new ArrayList<>();
            sheets.add(new XlsxExporter.Sheet("Kas", java.util.Collections.singletonList(reportRow)));
            XlsxExporter.write(workbook, sheets);
            try (ZipFile zip = new ZipFile(workbook.toFile())) {
                check("workbook XLSX memiliki struktur OOXML",
                        zip.getEntry("xl/workbook.xml") != null
                                && zip.getEntry("xl/worksheets/sheet1.xml") != null);
                try (InputStream input = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml"))) {
                    String worksheet = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                    check("XLSX meng-escape teks dan menulis angka",
                            worksheet.contains("Kas &amp; iuran") && worksheet.contains("1500.5"));
                }
            }
        } finally {
            Files.deleteIfExists(workbook);
        }

        System.out.println("== ApiClient + MockServer ==");
        check("peran Warga tidak diblokir desktop secara default",
                !AppConfig.inMemory("http://127.0.0.1").isRoleBlocked("Warga"));
        HttpServer server = MockServer.start(0);
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            ApiClient api = new ApiClient(AppConfig.inMemory(url));

            Object health = api.health();
            check("GET /api/v1/health membaca status backend",
                    "ok".equals(ApiData.findMap(health, "data").get("status")));

            try {
                api.currentUser();
                check("GET /api/v1/auth/me tanpa login ditolak 401", false);
            } catch (ApiException e) {
                check("GET /api/v1/auth/me tanpa login ditolak 401", e.isUnauthorized());
            }

            Map<String, Object> registration = new LinkedHashMap<>();
            registration.put("name", "Warga Baru");
            registration.put("email", "baru@rtrw.test");
            registration.put("phone", "08123456780");
            registration.put("password", "wargabaru123");
            check("POST /api/v1/auth/register dapat digunakan sebelum login",
                    "menunggu_verifikasi".equals(ApiData.findMap(
                            api.registerAccount(registration), "data").get("status")));

            try {
                api.login("admin@rtrw.test", "salah");
                check("password salah ditolak", false);
            } catch (ApiException e) {
                check("password salah ditolak dengan pesan server", e.isUnauthorized()
                        && e.getMessage().contains("salah"));
            }

            api.login("admin@rtrw.test", "admin123");
            check("login berhasil: nama dan peran terbaca", api.userName().contains("Sari")
                    && api.userRole().equals("Admin RW") && "admin_rw".equals(api.userRoleCode()));
            check("izin pengguna dibaca dari respons auth/me", api.hasPermission("pengajuan_surat.approve"));

            Object currentUser = api.currentUser();
            check("GET /api/v1/auth/me membaca profil sesi",
                    ApiData.findMap(currentUser, "user").get("name").equals("Bu Sari (Admin RW)"));

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

            List<Map<String, Object>> residents = ApiData.rows(api.resource("warga"));
            check("GET /api/v1/resources/warga membaca data.items", residents.size() >= 5);
            Object residentPage = api.resource("warga", 2, 2, "");
            Map<String, Object> pagination = ApiData.findMap(residentPage, "pagination");
            check("resource mendukung offset dan mendeteksi halaman berikutnya",
                    ApiData.rows(residentPage).size() == 2
                            && pagination.get("offset") instanceof Number
                            && ((Number) pagination.get("offset")).intValue() == 2
                            && Boolean.TRUE.equals(pagination.get("hasMore")));
            check("resource mengirim pencarian yang di-encode",
                    ApiData.rows(api.resource("warga", 100, 0, "Budi Santoso")).size() == 1);
            check("GET /api/v1/resources/keluarga membaca keluarga",
                    ApiData.rows(api.resource("keluarga")).size() == 1);
            check("GET /api/v1/resources/periode-tagihan membaca periode",
                    ApiData.rows(api.resource("periode-tagihan")).size() == 1);
            List<Map<String, Object>> pending = ApiData.rows(api.pendingRegistrations());
            check("GET /api/v1/registrations/pending membaca pendaftaran",
                    pending.size() == 2 && "Calon Warga".equals(pending.get(0).get("nama")));
            api.approveRegistration(7L);
            check("POST /api/v1/registrations/{id}/approve memperbarui pendaftaran",
                    "aktif".equals(ApiData.rows(api.resource("pengguna")).get(0).get("status")));

            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("name", "Bu Sari Baru");
            profile.put("email", "admin@rtrw.test");
            profile.put("phone", "0811111111");
            api.updateProfile(profile);
            check("PATCH /api/v1/auth/profile memperbarui profil",
                    api.userName().equals("Bu Sari Baru"));

            Map<String, Object> password = new LinkedHashMap<>();
            password.put("currentPassword", "admin123");
            password.put("newPassword", "adminbaru123");
            check("POST /api/v1/auth/password memperbarui kata sandi",
                    "Kata sandi berhasil diperbarui.".equals(ApiData.findMap(
                            api.changePassword(password), "data").get("message")));

            Map<String, Object> decision = new LinkedHashMap<>();
            decision.put("keputusan", "setuju");
            decision.put("catatan", "Lengkap");
            check("POST /api/v1/letters/{id}/decision mengirim keputusan",
                    "Pengajuan surat diperbarui.".equals(ApiData.findMap(
                            api.decideLetter(4L, decision), "data").get("message")));

            Map<String, Object> family = new LinkedHashMap<>();
            family.put("rtId", 3L);
            family.put("familyNumber", "3525010101100099");
            family.put("address", "Jalan Uji");
            check("POST /api/v1/families membuat keluarga",
                    ApiData.findMap(api.createFamily(family), "data").get("id") instanceof Number);
            check("PATCH /api/v1/families/{id} memperbarui keluarga",
                    api.updateFamily(1L, family) != null);
            check("DELETE /api/v1/families/{id} mengarsipkan keluarga",
                    api.deleteFamily(2L) != null);

            Map<String, Object> newResident = new LinkedHashMap<>();
            newResident.put("familyId", 1L);
            newResident.put("nik", "3525010101900099");
            newResident.put("name", "Warga Uji");
            newResident.put("sex", "L");
            check("POST /api/v1/residents membuat warga",
                    ApiData.findMap(api.createResident(newResident), "data").get("id") instanceof Number);
            Map<String, Object> residentUpdate = new LinkedHashMap<>();
            residentUpdate.put("name", "Warga Uji Baru");
            check("PATCH /api/v1/residents/{id} memperbarui warga",
                    api.updateResident(9L, residentUpdate) != null);
            check("DELETE /api/v1/residents/{id} mengarsipkan warga",
                    api.deleteResident(9L) != null);

            Map<String, Object> newInvoice = new LinkedHashMap<>();
            newInvoice.put("familyId", 1L);
            newInvoice.put("periodId", 1L);
            newInvoice.put("categoryId", 1L);
            newInvoice.put("amount", "75000");
            newInvoice.put("dueDate", "2026-11-25");
            check("POST /api/v1/invoices membuat tagihan",
                    ApiData.findMap(api.createInvoice(newInvoice), "data").get("id") instanceof Number);
            check("PATCH /api/v1/invoices/{id} memperbarui tagihan",
                    api.updateInvoice(7L, newInvoice) != null);
            check("DELETE /api/v1/invoices/{id} membatalkan tagihan",
                    api.deleteInvoice(7L) != null);

            Map<String, Object> announcement = new LinkedHashMap<>();
            announcement.put("title", "Pengumuman Uji");
            announcement.put("body", "Isi pengumuman");
            check("POST /api/v1/announcements membuat pengumuman",
                    ApiData.findMap(api.createAnnouncement(announcement), "data").get("id") instanceof Number);
            check("PATCH /api/v1/announcements/{id} memperbarui pengumuman",
                    api.updateAnnouncement(4L, announcement) != null);
            check("DELETE /api/v1/announcements/{id} mengarsipkan pengumuman",
                    api.deleteAnnouncement(4L) != null);

            Map<String, Object> activity = new LinkedHashMap<>();
            activity.put("title", "Kegiatan Uji");
            activity.put("startAt", "2026-11-20 07:00");
            check("POST /api/v1/activities membuat kegiatan",
                    ApiData.findMap(api.createActivity(activity), "data").get("id") instanceof Number);
            check("PATCH /api/v1/activities/{id} memperbarui kegiatan",
                    api.updateActivity(2L, activity) != null);
            check("DELETE /api/v1/activities/{id} mengarsipkan kegiatan",
                    api.deleteActivity(2L) != null);

            ApiClient financeApi = new ApiClient(AppConfig.inMemory(url));
            financeApi.login("bendahara@rtrw.test", "bendahara123");
            Map<String, Object> paymentDecision = new LinkedHashMap<>();
            paymentDecision.put("keputusan", "setuju");
            paymentDecision.put("catatan", "Bukti sesuai");
            check("POST /api/v1/payments/{id}/decision memproses pembayaran",
                    financeApi.decidePayment(2L, paymentDecision) != null
                            && financeApi.hasPermission("pembayaran.update"));
            financeApi.logout();

            try {
                api.get("/api/v1/tidak-ada");
                check("404 dilaporkan jelas", false);
            } catch (ApiException e) {
                check("404 dilaporkan jelas", e.statusCode() == 404);
            }
            try {
                api.resource("tidak-ada");
                check("resource yang tidak dikenal dilaporkan 404", false);
            } catch (ApiException e) {
                check("resource yang tidak dikenal dilaporkan 404", e.statusCode() == 404);
            }

            api.logoutServer();
            try {
                api.currentUser();
                check("setelah logout endpoint auth/me menolak sesi", false);
            } catch (ApiException e) {
                check("setelah logout endpoint auth/me menolak sesi", e.isUnauthorized());
            }

            ApiClient residentApi = new ApiClient(AppConfig.inMemory(url));
            residentApi.login("warga@rtrw.test", "warga123");
            residentApi.currentUser();
            check("akun Warga dapat masuk dengan izin pengajuan",
                    "warga".equals(residentApi.userRoleCode())
                            && residentApi.hasPermission("pengajuan_surat.create"));
            check("GET /api/v1/resources/jenis-surat membaca jenis surat",
                    ApiData.rows(residentApi.resource("jenis-surat")).size() == 2);
            check("GET /api/v1/resources/pengajuan-surat membaca status pengajuan",
                    ApiData.rows(residentApi.resource("pengajuan-surat")).size() == 1);
            Map<String, Object> letter = new LinkedHashMap<>();
            letter.put("jenisSuratId", 1L);
            letter.put("keperluan", "Administrasi");
            check("POST /api/v1/letters membuat pengajuan",
                    ApiData.findMap(residentApi.createLetterRequest(letter), "data").get("id") instanceof Number);
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
