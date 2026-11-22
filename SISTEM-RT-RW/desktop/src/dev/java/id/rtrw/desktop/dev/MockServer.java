package id.rtrw.desktop.dev;

import id.rtrw.desktop.api.Json;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Server API tiruan untuk pengembangan dan pengujian desktop SEBELUM backend
 * PHP siap. Hanya memakai JDK. Data seluruhnya dummy dan disimpan di memori.
 *
 * Jalankan: ./mock-server.sh [port]   (bawaan 8000)
 * Akun uji : admin@rtrw.test / admin123   (peran Bendahara)
 *            warga@rtrw.test / warga123   (peran Warga, ditolak oleh desktop)
 */
public final class MockServer {

    public static final String TOKEN = "mock-token-123";

    private final List<Map<String, Object>> residents = new ArrayList<>();
    private final List<Map<String, Object>> invoices = new ArrayList<>();
    private final List<Map<String, Object>> payments = new ArrayList<>();
    private final List<Map<String, Object>> announcements = new ArrayList<>();
    private final List<Map<String, Object>> notifications = new ArrayList<>();
    private final List<Map<String, Object>> reports = new ArrayList<>();

    private MockServer() {
        String[][] people = {
            {"3525010101900001", "Budi Santoso", "3525010101100001", "03", "Kepala Keluarga", "Tetap"},
            {"3525010202920002", "Siti Aminah", "3525010101100001", "03", "Istri", "Tetap"},
            {"3525010303850003", "Agus Prasetyo", "3525010101100002", "03", "Kepala Keluarga", "Tetap"},
            {"3525010404880004", "Dewi Lestari", "3525010101100003", "04", "Kepala Keluarga", "Kontrak"},
            {"3525010505950005", "Rudi Hartono", "3525010101100004", "04", "Kepala Keluarga", "Tetap"},
            {"3525010606990006", "Maya Sari", "3525010101100004", "04", "Anak", "Tetap"},
            {"3525010707800007", "Hendra Wijaya", "3525010101100005", "05", "Kepala Keluarga", "Tetap"},
            {"3525010808820008", "Ratna Dewi", "3525010101100006", "05", "Kepala Keluarga", "Kontrak"},
        };
        long id = 1;
        for (String[] p : people) {
            residents.add(row("id", id++, "nik", p[0], "nama", p[1], "no_kk", p[2], "rt", p[3],
                    "hubungan_keluarga", p[4], "status_tinggal", p[5]));
        }

        String[] heads = {"Budi Santoso", "Agus Prasetyo", "Dewi Lestari", "Rudi Hartono", "Hendra Wijaya", "Ratna Dewi"};
        String[] invoiceStatus = {"Lunas", "Lunas", "Belum bayar", "Menunggak", "Lunas", "Belum bayar"};
        for (int i = 0; i < heads.length; i++) {
            invoices.add(row("id", (long) (i + 1), "no_tagihan", String.format("INV-2026-11-%03d", i + 1),
                    "keluarga", heads[i], "periode", "November 2026", "total", 50000L + 10000L * (i % 2),
                    "jatuh_tempo", "2026-11-20", "status", invoiceStatus[i]));
        }

        payments.add(row("id", 1L, "id_tagihan", 1L, "nama", "Budi Santoso", "metode", "QRIS", "jumlah", 50000L,
                "tanggal", "2026-11-05", "status", "Diterima"));
        payments.add(row("id", 2L, "id_tagihan", 2L, "nama", "Agus Prasetyo", "metode", "Transfer", "jumlah", 60000L,
                "tanggal", "2026-11-08", "status", "Menunggu verifikasi"));
        payments.add(row("id", 3L, "id_tagihan", 5L, "nama", "Hendra Wijaya", "metode", "Transfer", "jumlah", 50000L,
                "tanggal", "2026-11-09", "status", "Menunggu verifikasi"));
        payments.add(row("id", 4L, "id_tagihan", 4L, "nama", "Rudi Hartono", "metode", "Tunai", "jumlah", 30000L,
                "tanggal", "2026-11-10", "status", "Sebagian"));

        announcements.add(row("id", 1L, "judul", "Kerja bakti Minggu pagi", "target", "Semua warga",
                "tanggal", "2026-11-12", "status", "Terkirim"));
        announcements.add(row("id", 2L, "judul", "Pengumpulan iuran November", "target", "RT 03",
                "tanggal", "2026-11-10", "status", "Terkirim"));
        announcements.add(row("id", 3L, "judul", "Rapat pengurus bulanan", "target", "Pengurus",
                "tanggal", "2026-11-14", "status", "Draft"));

        notifications.add(row("id", 1L, "title", "Pembayaran terverifikasi",
                "message", "Pembayaran iuran November telah diterima.",
                "type", "pembayaran", "readAt", null, "createdAt", "2026-11-10 09:00:00"));
        notifications.add(row("id", 2L, "title", "Rapat pengurus",
                "message", "Rapat pengurus akan dilaksanakan Jumat malam.",
                "type", "informasi", "readAt", "2026-11-11 08:00:00", "createdAt", "2026-11-09 16:00:00"));

        reports.add(row("id", 1L, "nama_laporan", "Laporan kas bulanan", "periode", "Oktober 2026",
                "format", "PDF/Excel", "status", "Tersedia"));
        reports.add(row("id", 2L, "nama_laporan", "Rekap iuran", "periode", "November 2026",
                "format", "Excel", "status", "Tersedia"));
        reports.add(row("id", 3L, "nama_laporan", "Daftar tunggakan", "periode", "November 2026",
                "format", "PDF", "status", "Diproses"));
    }

    private static Map<String, Object> row(Object... keyValues) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put((String) keyValues[i], keyValues[i + 1]);
        }
        return m;
    }

    public static HttpServer start(int port) throws IOException {
        MockServer mock = new MockServer();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/", mock::handle);
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8000;
        HttpServer server = start(port);
        System.out.println("Mock API berjalan di http://127.0.0.1:" + server.getAddress().getPort());
        System.out.println("Akun uji: admin@rtrw.test / admin123  (Ctrl+C untuk berhenti)");
    }

    // ------------------------------------------------------------------ penanganan

    @SuppressWarnings("unchecked")
    private synchronized void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();
            String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> body = raw.trim().isEmpty() ? new LinkedHashMap<String, Object>()
                    : (Map<String, Object>) Json.parse(raw);

            if (path.equals("/api/v1/health") && method.equals("GET")) {
                send(ex, 200, row("data", row("status", "ok")));
                return;
            }
            if ((path.equals("/api/login") || path.equals("/api/v1/auth/login")) && method.equals("POST")) {
                login(ex, body);
                return;
            }
            if (path.equals("/api/v1/auth/logout") && method.equals("POST")) {
                send(ex, 200, ok(row("message", "Anda telah keluar")));
                return;
            }
            if (!("Bearer " + TOKEN).equals(ex.getRequestHeaders().getFirst("Authorization"))) {
                send(ex, 401, fail("Sesi tidak valid. Silakan login."));
                return;
            }

            if (path.equals("/api/v1/auth/me") && method.equals("GET")) {
                send(ex, 200, ok(row("user", row("id", 1L, "name", "Bu Sari (Bendahara)",
                        "email", "admin@rtrw.test", "role", "Bendahara"))));
                return;
            }
            if (path.equals("/api/v1/dashboard/summary") && method.equals("GET")) {
                send(ex, 200, ok(row("organization", row("name", "RT/RW Uji"),
                        "stats", row("residents", residents.size(), "families", 6,
                                "unpaidInvoices", 3, "cashBalance", 125000L),
                        "period", row("paidFamilies", 3, "unpaidInvoices", 3),
                        "activities", new ArrayList<Map<String, Object>>())));
                return;
            }
            if (path.equals("/api/v1/announcements") && method.equals("GET")) {
                List<Map<String, Object>> items = new ArrayList<>();
                items.add(row("id", 1L, "title", "Kerja bakti Minggu pagi",
                        "body", "Kerja bakti dimulai pukul 07.00.", "publishedAt", "2026-11-12 08:00:00"));
                send(ex, 200, ok(row("items", items)));
                return;
            }
            if (path.equals("/api/v1/notifications") && method.equals("GET")) {
                send(ex, 200, ok(row("items", notifications, "unreadCount", 1)));
                return;
            }

            switch (path) {
                case "/api/residents":
                    if (method.equals("GET")) {
                        send(ex, 200, ok(residents));
                    } else {
                        addResident(ex, body);
                    }
                    break;
                case "/api/invoices":
                    send(ex, 200, ok(invoices));
                    break;
                case "/api/payments":
                    send(ex, 200, ok(payments));
                    break;
                case "/api/payments/verify":
                    verify(ex, body);
                    break;
                case "/api/announcements":
                    send(ex, 200, ok(announcements));
                    break;
                case "/api/reports":
                    send(ex, 200, ok(reports));
                    break;
                default:
                    send(ex, 404, fail("Endpoint tidak ditemukan"));
            }
        } catch (RuntimeException e) {
            send(ex, 500, fail("Kesalahan server tiruan: " + e.getMessage()));
        }
    }

    private void login(HttpExchange ex, Map<String, Object> body) throws IOException {
        Object id = body.containsKey("identity") ? body.get("identity")
                : body.containsKey("email") ? body.get("email") : body.get("telepon");
        Object pw = body.get("password");
        if ("admin@rtrw.test".equals(id) && "admin123".equals(pw)) {
            Map<String, Object> data = row("token", TOKEN,
                    "user", row("id", 1L, "nama", "Bu Sari (Bendahara)", "email", "admin@rtrw.test", "peran", "Bendahara"));
            send(ex, 200, row("success", Boolean.TRUE, "message", "Login berhasil", "data", data));
        } else if ("warga@rtrw.test".equals(id) && "warga123".equals(pw)) {
            Map<String, Object> data = row("token", TOKEN,
                    "user", row("id", 2L, "nama", "Budi Santoso", "email", "warga@rtrw.test", "peran", "Warga"));
            send(ex, 200, row("success", Boolean.TRUE, "message", "Login berhasil", "data", data));
        } else {
            send(ex, 401, fail("Email atau kata sandi salah"));
        }
    }

    private void addResident(HttpExchange ex, Map<String, Object> body) throws IOException {
        String nik = String.valueOf(body.get("nik"));
        String nama = String.valueOf(body.get("nama"));
        if (!nik.matches("\\d{16}") || nama.trim().isEmpty() || "null".equals(nama)) {
            send(ex, 422, fail("NIK (16 digit) dan nama wajib diisi"));
            return;
        }
        for (Map<String, Object> r : residents) {
            if (nik.equals(r.get("nik"))) {
                send(ex, 422, fail("NIK sudah terdaftar"));
                return;
            }
        }
        long id = residents.size() + 1L;
        Map<String, Object> r = row("id", id, "nik", nik, "nama", nama, "no_kk", body.get("no_kk"), "rt", "03",
                "hubungan_keluarga", "Kepala Keluarga", "status_tinggal", "Tetap");
        residents.add(r);
        send(ex, 201, row("success", Boolean.TRUE, "message", "Warga ditambahkan", "data", r));
    }

    private void verify(HttpExchange ex, Map<String, Object> body) throws IOException {
        Object id = body.get("id");
        String status = String.valueOf(body.get("status"));
        if (!(id instanceof Number) || !(status.equals("diterima") || status.equals("ditolak"))) {
            send(ex, 422, fail("id dan status (diterima/ditolak) wajib diisi"));
            return;
        }
        for (Map<String, Object> p : payments) {
            if (((Number) p.get("id")).longValue() == ((Number) id).longValue()) {
                p.put("status", status.equals("diterima") ? "Diterima" : "Ditolak");
                send(ex, 200, row("success", Boolean.TRUE, "message", "Pembayaran diperbarui", "data", p));
                return;
            }
        }
        send(ex, 404, fail("Pembayaran tidak ditemukan"));
    }

    private static Map<String, Object> ok(Object data) {
        return row("success", Boolean.TRUE, "message", "OK", "data", data);
    }

    private static Map<String, Object> fail(String message) {
        return row("success", Boolean.FALSE, "message", message);
    }

    private static void send(HttpExchange ex, int status, Object json) throws IOException {
        byte[] bytes = Json.stringify(json).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
