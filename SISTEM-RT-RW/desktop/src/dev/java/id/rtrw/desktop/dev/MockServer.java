package id.rtrw.desktop.dev;

import id.rtrw.desktop.api.Json;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URLDecoder;
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
 * Akun uji : admin@rtrw.test / admin123   (peran Admin RW)
 *            warga@rtrw.test / warga123   (peran Warga)
 */
public final class MockServer {

    public static final String TOKEN = "mock-token-123";

    private final List<Map<String, Object>> residents = new ArrayList<>();
    private final List<Map<String, Object>> families = new ArrayList<>();
    private final List<Map<String, Object>> periods = new ArrayList<>();
    private final List<Map<String, Object>> categories = new ArrayList<>();
    private final List<Map<String, Object>> invoices = new ArrayList<>();
    private final List<Map<String, Object>> payments = new ArrayList<>();
    private final List<Map<String, Object>> announcements = new ArrayList<>();
    private final List<Map<String, Object>> activities = new ArrayList<>();
    private final List<Map<String, Object>> notifications = new ArrayList<>();
    private final List<Map<String, Object>> reports = new ArrayList<>();
    private final List<Map<String, Object>> registrations = new ArrayList<>();
    private final List<Map<String, Object>> letters = new ArrayList<>();
    private Map<String, Object> currentUser;

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
        families.add(row("id", 1L, "id_organisasi", 1L, "id_rt", 3L,
                "alamat", "Jalan Melati", "nomor_rumah", "01", "jenis_hunian", "milik_sendiri"));
        periods.add(row("id", 1L, "id_organisasi", 1L, "nama", "November 2026",
                "periode_mulai", "2026-11-01", "periode_akhir", "2026-11-30", "status", "terbit"));
        categories.add(row("id", 1L, "nama", "Iuran Kebersihan", "jenis_iuran", "wajib", "aktif", true));

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
        activities.add(row("id", 1L, "id_organisasi", 1L, "id_rt", 3L,
                "judul", "Kerja bakti", "deskripsi", "Kegiatan warga", "jenis", "kerja_bakti",
                "mulai_pada", "2026-11-15 07:00:00", "lokasi", "Balai RT"));

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
        registrations.add(row("id", 7L, "nama", "Calon Warga", "email", "calon@rtrw.test",
                "telepon", "08123456789", "status", "menunggu_verifikasi"));
        letters.add(row("id", 4L, "nama_pemohon", "Budi Santoso", "jenis_surat", "Surat Pengantar",
                "keperluan", "Administrasi", "status", "diajukan"));
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
        System.out.println("Akun uji: admin@rtrw.test / admin123; warga@rtrw.test / warga123 (Ctrl+C untuk berhenti)");
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
            if (path.equals("/api/v1/auth/login") && method.equals("POST")) {
                login(ex, body);
                return;
            }
            if (path.equals("/api/v1/auth/register") && method.equals("POST")) {
                register(ex, body);
                return;
            }
            if (!("Bearer " + TOKEN).equals(ex.getRequestHeaders().getFirst("Authorization"))) {
                send(ex, 401, fail("Sesi tidak valid. Silakan login."));
                return;
            }

            if (path.equals("/api/v1/auth/logout") && method.equals("POST")) {
                send(ex, 200, ok(row("message", "Anda telah keluar")));
                return;
            }
            if (path.equals("/api/v1/auth/me") && method.equals("GET")) {
                send(ex, 200, ok(row("user", currentUser)));
                return;
            }
            if (path.equals("/api/v1/auth/profile") && method.equals("PATCH")) {
                currentUser.put("name", body.get("name"));
                currentUser.put("email", body.get("email"));
                currentUser.put("phone", body.get("phone"));
                send(ex, 200, ok(row("user", currentUser)));
                return;
            }
            if (path.equals("/api/v1/auth/password") && method.equals("POST")) {
                if (!"admin123".equals(body.get("currentPassword"))
                        || !(body.get("newPassword") instanceof String)
                        || ((String) body.get("newPassword")).length() < 8) {
                    send(ex, 422, fail("Kata sandi saat ini tidak sesuai."));
                } else {
                    send(ex, 200, ok(row("message", "Kata sandi berhasil diperbarui.")));
                }
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
            if (path.equals("/api/v1/registrations/pending") && method.equals("GET")) {
                List<Map<String, Object>> pending = new ArrayList<>();
                for (Map<String, Object> registration : registrations) {
                    if ("menunggu_verifikasi".equals(registration.get("status"))) {
                        pending.add(registration);
                    }
                }
                send(ex, 200, ok(row("items", pending)));
                return;
            }
            if (path.matches("/api/v1/registrations/[1-9][0-9]*/approve") && method.equals("POST")) {
                decideRegistration(ex, path);
                return;
            }
            if (path.equals("/api/v1/letters") && method.equals("POST")) {
                createLetter(ex, body);
                return;
            }
            if (path.matches("/api/v1/letters/[1-9][0-9]*/decision") && method.equals("POST")) {
                decideLetter(ex, path, body);
                return;
            }
            if (path.equals("/api/v1/families") || path.matches("/api/v1/families/[1-9][0-9]*")) {
                writeCollection(ex, path, method, body, "families", families, false);
                return;
            }
            if (path.equals("/api/v1/residents") || path.matches("/api/v1/residents/[1-9][0-9]*")) {
                writeCollection(ex, path, method, body, "residents", residents, true);
                return;
            }
            if (path.equals("/api/v1/invoices") || path.matches("/api/v1/invoices/[1-9][0-9]*")) {
                writeCollection(ex, path, method, body, "invoices", invoices, true);
                return;
            }
            if (path.matches("/api/v1/payments/[1-9][0-9]*/decision") && method.equals("POST")) {
                decidePayment(ex, path, body);
                return;
            }
            if (path.equals("/api/v1/announcements") || path.matches("/api/v1/announcements/[1-9][0-9]*")) {
                writeCollection(ex, path, method, body, "announcements", announcements, true);
                return;
            }
            if (path.equals("/api/v1/activities") || path.matches("/api/v1/activities/[1-9][0-9]*")) {
                writeCollection(ex, path, method, body, "activities", activities, true);
                return;
            }
            if (path.startsWith("/api/v1/resources/") && method.equals("GET")) {
                String resource = path.substring("/api/v1/resources/".length());
                List<Map<String, Object>> rows = resourceRows(resource);
                if (rows == null) {
                    send(ex, 404, fail("Resource tidak ditemukan."));
                } else {
                    Map<String, String> query = queryParameters(ex.getRequestURI().getRawQuery());
                    int limit = queryInteger(query, "limit", 50);
                    int offset = queryInteger(query, "offset", 0);
                    String search = query.getOrDefault("search", "").toLowerCase(java.util.Locale.ROOT);
                    List<Map<String, Object>> filtered = new ArrayList<>();
                    for (Map<String, Object> row : rows) {
                        boolean matches = search.isEmpty();
                        for (Object value : row.values()) {
                            if (value != null && value.toString().toLowerCase(java.util.Locale.ROOT).contains(search)) {
                                matches = true;
                                break;
                            }
                        }
                        if (matches) {
                            filtered.add(row);
                        }
                    }
                    int start = Math.min(offset, filtered.size());
                    int end = Math.min(start + limit, filtered.size());
                    List<Map<String, Object>> page = new ArrayList<>(filtered.subList(start, end));
                    boolean hasMore = end < filtered.size();
                    send(ex, 200, ok(row("items", page, "pagination",
                            row("limit", limit, "offset", offset, "hasMore", hasMore))));
                }
                return;
            }

            send(ex, 404, fail("Endpoint tidak ditemukan"));
        } catch (RuntimeException e) {
            send(ex, 500, fail("Kesalahan server tiruan: " + e.getMessage()));
        }
    }

    private void login(HttpExchange ex, Map<String, Object> body) throws IOException {
        Object id = body.containsKey("identity") ? body.get("identity")
                : body.containsKey("email") ? body.get("email") : body.get("telepon");
        Object pw = body.get("password");
        if ("admin@rtrw.test".equals(id) && "admin123".equals(pw)) {
            currentUser = row("id", 1L, "name", "Bu Sari (Admin RW)", "email", "admin@rtrw.test",
                    "phone", "0811111111", "role", "Admin RW", "roleCode", "admin_rw",
                    "permissions", java.util.Arrays.asList("warga.read", "mutasi_warga.read",
                            "kategori_iuran.read", "tagihan.read", "pembayaran.read", "laporan_keuangan.read",
                            "pengajuan_surat.read", "pengajuan_surat.approve", "pengumuman.read",
                            "kegiatan.read", "pengaduan.read", "pengguna.read", "laporan_aktivitas.read",
                            "wilayah.read", "pengaturan.read",
                            "keluarga.read", "keluarga.create", "keluarga.update", "keluarga.delete",
                            "warga.create", "warga.update", "warga.delete",
                            "tagihan.create", "tagihan.update", "tagihan.delete",
                            "pengumuman.create", "pengumuman.update", "pengumuman.delete",
                            "kegiatan.create", "kegiatan.update", "kegiatan.delete"));
            Map<String, Object> data = row("user", currentUser);
            if ("desktop".equals(body.get("client"))) {
                data.put("accessToken", TOKEN);
            }
            send(ex, 200, row("success", Boolean.TRUE, "message", "Login berhasil", "data", data));
        } else if ("warga@rtrw.test".equals(id) && "warga123".equals(pw)) {
            currentUser = row("id", 2L, "name", "Budi Santoso", "email", "warga@rtrw.test",
                    "phone", "0822222222", "role", "Warga", "roleCode", "warga",
                    "permissions", java.util.Arrays.asList("warga.read", "tagihan.read", "pembayaran.read",
                            "pengajuan_surat.create", "pengajuan_surat.read", "pengumuman.read",
                            "kegiatan.read", "pengaduan.read"));
            Map<String, Object> data = row("user", currentUser);
            if ("desktop".equals(body.get("client"))) {
                data.put("accessToken", TOKEN);
            }
            send(ex, 200, row("success", Boolean.TRUE, "message", "Login berhasil", "data", data));
        } else if ("bendahara@rtrw.test".equals(id) && "bendahara123".equals(pw)) {
            currentUser = row("id", 3L, "name", "Pak Dedi (Bendahara)", "email", "bendahara@rtrw.test",
                    "phone", "0833333333", "role", "Bendahara", "roleCode", "bendahara",
                    "permissions", java.util.Arrays.asList("pembayaran.read", "pembayaran.update",
                            "tagihan.read", "laporan_keuangan.read"));
            Map<String, Object> data = row("user", currentUser);
            if ("desktop".equals(body.get("client"))) {
                data.put("accessToken", TOKEN);
            }
            send(ex, 200, row("success", Boolean.TRUE, "message", "Login berhasil", "data", data));
        } else {
            send(ex, 401, fail("Email atau kata sandi salah"));
        }
    }

    private void register(HttpExchange ex, Map<String, Object> body) throws IOException {
        Object name = body.get("name");
        Object email = body.get("email");
        Object phone = body.get("phone");
        Object password = body.get("password");
        if (!(name instanceof String) || ((String) name).trim().isEmpty()
                || !(phone instanceof String) || ((String) phone).trim().isEmpty()
                || !(password instanceof String) || ((String) password).length() < 8) {
            send(ex, 422, fail("Data pendaftaran tidak valid."));
            return;
        }
        long id = 1;
        for (Map<String, Object> registration : registrations) {
            id = Math.max(id, ((Number) registration.get("id")).longValue() + 1);
        }
        registrations.add(row("id", id, "nama", name, "email", email, "telepon", phone,
                "status", "menunggu_verifikasi"));
        send(ex, 201, ok(row("message", "Pendaftaran berhasil.", "status", "menunggu_verifikasi")));
    }

    private void decideRegistration(HttpExchange ex, String path) throws IOException {
        long id = Long.parseLong(path.substring("/api/v1/registrations/".length(),
                path.length() - "/approve".length()));
        for (Map<String, Object> registration : registrations) {
            if (((Number) registration.get("id")).longValue() == id) {
                registration.put("status", "aktif");
                send(ex, 200, ok(row("message", "Pendaftaran disetujui.", "user", registration)));
                return;
            }
        }
        send(ex, 404, fail("Pendaftaran tidak ditemukan."));
    }

    private void createLetter(HttpExchange ex, Map<String, Object> body) throws IOException {
        Object typeId = body.get("jenisSuratId");
        Object purpose = body.get("keperluan");
        if (!(typeId instanceof Number) || !(purpose instanceof String)
                || ((String) purpose).trim().isEmpty()) {
            send(ex, 422, fail("Pilih jenis surat dan isi keperluan."));
            return;
        }
        long id = 1;
        for (Map<String, Object> letter : letters) {
            id = Math.max(id, ((Number) letter.get("id")).longValue() + 1);
        }
        letters.add(row("id", id, "jenis_surat_id", typeId, "keperluan", purpose, "status", "diajukan"));
        send(ex, 201, ok(row("message", "Pengajuan surat berhasil dikirim.", "id", id)));
    }

    private void decideLetter(HttpExchange ex, String path, Map<String, Object> body) throws IOException {
        long id = Long.parseLong(path.substring("/api/v1/letters/".length(),
                path.length() - "/decision".length()));
        String decision = String.valueOf(body.get("keputusan"));
        if (!decision.equals("setuju") && !decision.equals("tolak")) {
            send(ex, 422, fail("Keputusan harus setuju atau tolak."));
            return;
        }
        for (Map<String, Object> letter : letters) {
            if (((Number) letter.get("id")).longValue() == id) {
                letter.put("status", decision.equals("setuju") ? "disetujui" : "ditolak");
                send(ex, 200, ok(row("message", "Pengajuan surat diperbarui.")));
                return;
            }
        }
        send(ex, 404, fail("Pengajuan surat tidak ditemukan."));
    }

    private void writeCollection(HttpExchange ex, String path, String method, Map<String, Object> body,
            String collection, List<Map<String, Object>> rows, boolean markDeleted) throws IOException {
        if (path.equals("/api/v1/" + collection) && method.equals("POST")) {
            long id = nextId(rows);
            Map<String, Object> record = new LinkedHashMap<>(body);
            record.put("id", id);
            if (collection.equals("residents")) {
                record.put("nama", body.get("name"));
                record.put("status", body.getOrDefault("status", "aktif"));
            } else if (collection.equals("invoices")) {
                record.put("status", "belum_dibayar");
                record.put("total_nominal", body.get("amount"));
            }
            rows.add(record);
            send(ex, 201, ok(row("id", id, "message", "Data berhasil dibuat.")));
            return;
        }
        if (method.equals("PATCH") || method.equals("DELETE")) {
            String idText = path.substring(path.lastIndexOf('/') + 1);
            long id = Long.parseLong(idText);
            for (Map<String, Object> record : rows) {
                if (((Number) record.get("id")).longValue() == id) {
                    if (method.equals("PATCH")) {
                        record.putAll(body);
                    } else if (markDeleted) {
                        record.put("status", collection.equals("invoices") ? "batal" : "nonaktif");
                    } else {
                        rows.remove(record);
                    }
                    send(ex, 200, ok(row("message", "Data berhasil diperbarui.")));
                    return;
                }
            }
        }
        send(ex, 404, fail("Data tidak ditemukan."));
    }

    private long nextId(List<Map<String, Object>> rows) {
        long id = 1;
        for (Map<String, Object> record : rows) {
            Object value = record.get("id");
            if (value instanceof Number) {
                id = Math.max(id, ((Number) value).longValue() + 1);
            }
        }
        return id;
    }

    private void decidePayment(HttpExchange ex, String path, Map<String, Object> body) throws IOException {
        long id = Long.parseLong(path.substring("/api/v1/payments/".length(),
                path.length() - "/decision".length()));
        for (Map<String, Object> payment : payments) {
            if (((Number) payment.get("id")).longValue() == id) {
                String decision = String.valueOf(body.get("keputusan"));
                if (!decision.equals("setuju") && !decision.equals("tolak")) {
                    send(ex, 422, fail("Keputusan harus setuju atau tolak."));
                    return;
                }
                payment.put("status", decision.equals("setuju") ? "terverifikasi" : "ditolak");
                send(ex, 200, ok(row("message", "Keputusan pembayaran berhasil disimpan.")));
                return;
            }
        }
        send(ex, 404, fail("Pembayaran tidak ditemukan."));
    }

    private List<Map<String, Object>> resourceRows(String resource) {
        switch (resource) {
            case "warga":
                return residents;
            case "keluarga":
                return families;
            case "periode-tagihan":
                return periods;
            case "kategori-iuran":
                return categories;
            case "tagihan":
                return invoices;
            case "pembayaran":
                return payments;
            case "pengumuman":
                return announcements;
            case "pengajuan-surat":
                return letters;
            case "kegiatan":
                return activities;
            case "pengguna":
                return registrations;
            case "audit-log":
                return reports;
            case "jenis-surat":
                return java.util.Arrays.asList(
                        row("id", 1L, "nama", "Surat Pengantar", "aktif", Boolean.TRUE),
                        row("id", 2L, "nama", "Surat Keterangan Domisili", "aktif", Boolean.TRUE));
            case "mutasi":
            case "transaksi-kas":
            case "survei":
            case "pengaduan":
            case "kontak-darurat":
            case "peran":
            case "sesi":
            case "wilayah":
            case "pengaturan":
            case "cadangan-data":
                return new ArrayList<Map<String, Object>>();
            default:
                return null;
        }
    }

    private static Map<String, Object> ok(Object data) {
        return row("success", Boolean.TRUE, "message", "OK", "data", data);
    }

    private static Map<String, Object> fail(String message) {
        return row("error", row("code", "mock_error", "message", message));
    }

    private static Map<String, String> queryParameters(String query) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) {
            return parameters;
        }
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            parameters.put(key, value);
        }
        return parameters;
    }

    private static int queryInteger(Map<String, String> query, String key, int fallback) {
        try {
            return Integer.parseInt(query.get(key));
        } catch (NumberFormatException | NullPointerException ex) {
            return fallback;
        }
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
