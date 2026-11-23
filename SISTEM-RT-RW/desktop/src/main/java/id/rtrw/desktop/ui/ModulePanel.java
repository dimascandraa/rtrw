package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/** Beranda dan halaman katalog modul sesuai rancangan UI/UX. */
final class ModulePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final List<ModuleInfo> MODULES = createModules();

    private final ApiClient api;

    static List<ModuleInfo> modules() {
        return MODULES;
    }

    static ModuleInfo module(String id) {
        for (ModuleInfo module : MODULES) {
            if (module.id.equals(id)) {
                return module;
            }
        }
        return null;
    }

    static boolean hasAccessibleFeature(ApiClient api, String moduleId) {
        ModuleInfo module = module(moduleId);
        if (module == null) {
            return false;
        }
        for (FeatureInfo feature : module.features) {
            if (isFeatureAccessible(api, feature.target)) {
                return true;
            }
        }
        return false;
    }

    static boolean isFeatureAccessible(ApiClient api, String target) {
        if (target == null) {
            return false;
        }
        if (target.startsWith("resource-")) {
            String resource = target.substring("resource-".length());
            String permission = resourcePermission(resource);
            return permission == null || api.hasPermission(permission);
        }
        switch (target) {
            case "dashboard-stats":
                return api.hasPermissions("warga.read", "laporan_keuangan.read");
            case "announcement-list":
                return api.hasPermission("pengumuman.read");
            case "announcement-management":
                return api.hasPermission("pengumuman.read")
                        && (api.hasPermission("pengumuman.create")
                        || api.hasPermission("pengumuman.update")
                        || api.hasPermission("pengumuman.delete"));
            case "activity-management":
                return api.hasPermission("kegiatan.read")
                        && (api.hasPermission("kegiatan.create")
                        || api.hasPermission("kegiatan.update")
                        || api.hasPermission("kegiatan.delete"));
            case "payment-verification":
                return api.hasPermissions("pembayaran.read", "pembayaran.update");
            case "report-export":
                return api.hasPermission("laporan_keuangan.read");
            case "notification-list":
            case "account-settings":
            case "account-registration":
                return true;
            case "registrations-pending":
                return hasRole(api, "super_admin", "admin_rw", "ketua_rw", "ketua_rt");
            case "letters-pending":
                return hasRole(api, "super_admin", "admin_rw", "ketua_rw", "ketua_rt", "sekretaris")
                        && api.hasPermissions("pengajuan_surat.read", "pengajuan_surat.approve");
            case "letter-request":
                return "warga".equals(api.userRoleCode()) && api.hasPermission("pengajuan_surat.create");
            default:
                return false;
        }
    }

    private static String resourcePermission(String resource) {
        switch (resource) {
            case "warga":
            case "survei":
            case "kontak-darurat":
                return "warga.read";
            case "mutasi":
                return "mutasi_warga.read";
            case "kategori-iuran":
                return "kategori_iuran.read";
            case "keluarga":
                return "keluarga.read";
            case "tagihan":
                return "tagihan.read";
            case "pembayaran":
                return "pembayaran.read";
            case "transaksi-kas":
                return "laporan_keuangan.read";
            case "jenis-surat":
                return "pengajuan_surat.create";
            case "pengajuan-surat":
                return "pengajuan_surat.read";
            case "pengumuman":
                return "pengumuman.read";
            case "periode-tagihan":
                return "tagihan.read";
            case "kegiatan":
                return "kegiatan.read";
            case "pengaduan":
                return "pengaduan.read";
            case "pengguna":
            case "peran":
                return "pengguna.read";
            case "audit-log":
                return "laporan_aktivitas.read";
            case "sesi":
                return null;
            case "wilayah":
                return "wilayah.read";
            case "pengaturan":
            case "cadangan-data":
                return "pengaturan.read";
            default:
                return "resource-unknown";
        }
    }

    private static boolean hasRole(ApiClient api, String... allowedRoles) {
        String role = api.userRoleCode();
        for (String allowed : allowedRoles) {
            if (allowed.equals(role)) {
                return true;
            }
        }
        return false;
    }

    ModulePanel(ApiClient api, ModuleInfo module, BiConsumer<String, String> openFeature) {
        this.api = api;
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(24, 28, 16, 28));
        add(buildHeader(module), BorderLayout.NORTH);
        add(buildCards(module, openFeature), BorderLayout.CENTER);
    }

    private JPanel buildHeader(ModuleInfo module) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        if (module == null) {
            JLabel title = UiKit.label("Selamat datang, " + api.userName(), 24f, true, Theme.text());
            JLabel subtitle = UiKit.label(
                    "Pilih menu untuk mulai mengelola lingkungan RT/RW Anda.", 14f, false, Theme.muted());
            int modules = 0;
            int features = 0;
            for (ModuleInfo item : MODULES) {
                if (hasAccessibleFeature(api, item.id)) {
                    modules++;
                    for (FeatureInfo feature : item.features) {
                        if (isFeatureAccessible(api, feature.target)) {
                            features++;
                        }
                    }
                }
            }
            JLabel count = UiKit.label(modules + " modul  \u00b7  " + features + " fitur tersedia",
                    12f, true, Theme.primary());
            header.add(title);
            header.add(Box.createVerticalStrut(4));
            header.add(subtitle);
            header.add(Box.createVerticalStrut(8));
            header.add(count);
        } else {
            header.add(UiKit.label(module.title, 24f, true, Theme.text()));
            header.add(Box.createVerticalStrut(4));
            header.add(UiKit.label(module.description, 14f, false, Theme.muted()));
        }
        return header;
    }

    private JScrollPane buildCards(ModuleInfo module, BiConsumer<String, String> openFeature) {
        JPanel grid = new CardGrid();
        if (module == null) {
            for (ModuleInfo item : MODULES) {
                if (hasAccessibleFeature(api, item.id)) {
                    int available = 0;
                    for (FeatureInfo feature : item.features) {
                        if (isFeatureAccessible(api, feature.target)) {
                            available++;
                        }
                    }
                    grid.add(card(item.shortName, item.title, item.description,
                            available + " fitur tersedia",
                            "Buka modul", () -> openFeature.accept(item.id, null)));
                }
            }
        } else {
            for (FeatureInfo feature : module.features) {
                if (feature.target != null && !isFeatureAccessible(api, feature.target)) {
                    continue;
                }
                grid.add(card(String.format("%02d", module.features.indexOf(feature) + 1),
                        feature.title, feature.description,
                        feature.target == null ? "Endpoint belum tersedia" : "Tersedia di desktop",
                        "Lihat tampilan", () -> {
                            if (feature.target == null) {
                                JOptionPane.showMessageDialog(this,
                                        "Backend belum menyediakan endpoint operasional untuk fitur ini.",
                                        feature.title, JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                openFeature.accept(module.id, feature.target);
                            }
                        }));
            }
        }

        JScrollPane scroll = new JScrollPane(grid,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel card(String mark, String title, String description, String detail,
            String action, Runnable onClick) {
        JPanel panel = UiKit.card();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(230, 190));

        JPanel iconBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconBox.setOpaque(false);
        JLabel badge = new JLabel(mark, JLabel.CENTER);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 14f));
        badge.setForeground(Theme.primary());
        badge.setOpaque(true);
        badge.setBackground(Theme.header());
        badge.setPreferredSize(new Dimension(38, 38));
        iconBox.add(badge);
        panel.add(iconBox);
        panel.add(Box.createVerticalStrut(12));

        JLabel heading = UiKit.label(title, 15f, true, Theme.text());
        heading.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(6));

        JLabel copy = UiKit.label("<html><div style='width:220px'>" + escape(description) + "</div></html>",
                12f, false, Theme.muted());
        copy.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(copy);
        panel.add(Box.createVerticalStrut(8));

        JLabel footer = UiKit.label(detail, 11f, false, Theme.muted());
        footer.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(footer);
        panel.add(Box.createVerticalGlue());

        JButton button = new FlatButton(action, FlatButton.Kind.NEUTRAL);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.addActionListener(e -> onClick.run());
        panel.add(button);
        return panel;
    }

    private static final class CardGrid extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        CardGrid() {
            super(new GridLayout(0, 3, 14, 14));
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 18);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static FeatureInfo feature(String title, String description, String tables, String target) {
        return new FeatureInfo(title, description, tables, target);
    }

    private static ModuleInfo module(String id, String title, String shortName, String description,
            FeatureInfo... features) {
        List<FeatureInfo> items = new ArrayList<>();
        Collections.addAll(items, features);
        return new ModuleInfo(id, title, shortName, description, Collections.unmodifiableList(items));
    }

    private static List<ModuleInfo> createModules() {
        List<ModuleInfo> items = new ArrayList<>();
        items.add(module("reports", "Dashboard & Laporan", "D", "Ringkasan data dan laporan lingkungan.",
                feature("Dashboard statistik interaktif",
                        "Pantau ringkasan warga dan kondisi tagihan lingkungan.",
                        "kas, tagihan, pembayaran, warga", "dashboard-stats"),
                feature("Laporan PDF/Excel",
                        "Ekspor laporan kas, iuran, tunggakan, dan rekap pembayaran.",
                        "kas, tagihan, pembayaran", "report-export")));
        items.add(module("residents", "Manajemen Data Warga", "M", "Data warga, keluarga, dan mutasi.",
                feature("Data warga & kepala keluarga",
                        "Lihat profil warga, data keluarga, dan kepala keluarga sesuai wilayah.",
                        "warga, keluarga", "resource-warga"),
                feature("Data keluarga",
                        "Kelola data keluarga dan alamat rumah sesuai wilayah.",
                        "keluarga", "resource-keluarga"),
                feature("Mutasi dan riwayat warga",
                        "Lihat riwayat warga pindah masuk/keluar dan perubahan status.",
                        "mutasi_warga, warga", "resource-mutasi")));
        items.add(module("finance", "Keuangan RT/RW", "K", "Iuran, tagihan, pembayaran, dan kas.",
                feature("Kategori dan aturan iuran",
                        "Lihat kategori iuran yang dikonfigurasi untuk wilayah Anda.",
                        "kategori_iuran, aturan_iuran", "resource-kategori-iuran"),
                feature("Tagihan otomatis",
                        "Lihat tagihan per keluarga, periode, jumlah, dan status pembayarannya.",
                        "periode_tagihan, tagihan", "resource-tagihan"),
                feature("Pembayaran QRIS/transfer/tunai",
                        "Telusuri pembayaran warga melalui QRIS, transfer, atau tunai.",
                        "pembayaran, tagihan", "resource-pembayaran"),
                feature("Verifikasi bukti pembayaran",
                        "Periksa bukti transfer dan tindak lanjuti pembayaran yang menunggu verifikasi.",
                        "pembayaran, bukti_pembayaran", "payment-verification"),
                feature("Riwayat pembayaran & kuitansi",
                        "Telusuri pembayaran terverifikasi dan kuitansi digital.",
                        "pembayaran, kuitansi", "resource-pembayaran"),
                feature("Kas masuk dan pengeluaran",
                        "Lihat transaksi kas masuk dan pengeluaran.",
                        "transaksi_kas", "resource-transaksi-kas"),
                feature("Pengingat iuran/tunggakan",
                        "Pantau pengingat tagihan yang mendekati jatuh tempo atau belum dibayar.",
                        "tagihan, notifikasi", "notification-list")));
        items.add(module("letters", "Administrasi Surat", "A", "Pengajuan dan persetujuan surat.",
                feature("Pengajuan surat online",
                        "Pilih jenis surat aktif dan ajukan keperluan untuk wilayah Anda.",
                        "jenis_surat, pengajuan_surat", "letter-request"),
                feature("Status pengajuan surat",
                        "Lihat status pengajuan surat yang dapat diakses akun Anda.",
                        "pengajuan_surat", "resource-pengajuan-surat"),
                feature("Verifikasi dan persetujuan surat",
                        "Tinjau kelengkapan data, catat keputusan, dan pantau status penerbitan.",
                        "pengajuan_surat, persetujuan_surat", "letters-pending")));
        items.add(module("services", "Layanan Warga", "L", "Penilaian warga atas layanan pengurus.",
                feature("Survei kepuasan warga",
                        "Lihat ringkasan penilaian dan masukan warga atas layanan.",
                        "survei, jawaban_survei", "resource-survei")));
        items.add(module("communications", "Informasi & Komunikasi", "I", "Pengumuman dan notifikasi.",
                feature("Pengumuman RT/RW",
                        "Lihat informasi yang diterbitkan untuk warga atau wilayah tertentu.",
                        "pengumuman, target_pengumuman", "announcement-list"),
                feature("Notifikasi aplikasi",
                        "Pantau pemberitahuan tagihan, pembayaran, dan informasi lingkungan.",
                        "notifikasi, log_notifikasi", "notification-list"),
                feature("Kelola pengumuman",
                        "Buat, terbitkan, ubah, atau arsipkan informasi untuk wilayah Anda.",
                        "pengumuman, target_pengumuman", "announcement-management")));
        items.add(module("activities", "Sosial & Kegiatan", "S", "Agenda kerja bakti dan kegiatan sosial.",
                feature("Jadwal kegiatan/kerja bakti",
                        "Lihat agenda dan informasi kegiatan warga.",
                        "kegiatan, kehadiran_kegiatan", "resource-kegiatan"),
                feature("Kelola kegiatan",
                        "Buat, ubah, atau arsipkan jadwal kegiatan pada wilayah Anda.",
                        "kegiatan", "activity-management")));
        items.add(module("safety", "Keamanan & Lingkungan", "K", "Laporan kejadian dan kontak darurat.",
                feature("Laporan keluhan/kejadian",
                        "Lihat laporan lingkungan dan status penanganannya.",
                        "pengaduan, tanggapan_pengaduan", "resource-pengaduan"),
                feature("Kontak darurat/direktori layanan",
                        "Lihat kontak pengurus dan layanan darurat.",
                        "kontak_darurat", "resource-kontak-darurat")));
        items.add(module("access", "Registrasi & Akses", "R", "Pendaftaran, verifikasi, dan hak akses.",
                feature("Registrasi warga",
                        "Kirim pendaftaran akun warga untuk ditinjau pengurus.",
                        "pengguna", "account-registration"),
                feature("Verifikasi akun oleh Admin",
                        "Tinjau pendaftaran dan hubungkan akun dengan data warga serta wilayahnya.",
                        "pengguna, warga, penugasan_pengguna", "registrations-pending"),
                feature("Manajemen peran dan hak akses",
                        "Lihat akun pengguna dan peran yang telah diberikan.",
                        "peran, izin_akses, penugasan_pengguna", "resource-pengguna"),
                feature("Daftar peran",
                        "Lihat peran yang telah dikonfigurasi untuk organisasi.",
                        "peran, izin_akses", "resource-peran")));
        items.add(module("account-security", "Keamanan Akun", "K", "Riwayat aktivitas dan sesi login.",
                feature("Riwayat aktivitas/audit log",
                        "Lihat jejak aktivitas penting untuk membantu pengawasan dan audit.",
                        "log_audit", "resource-audit-log"),
                feature("Manajemen sesi/perangkat",
                        "Periksa perangkat yang pernah masuk dan cabut sesi yang tidak digunakan.",
                        "sesi_pengguna", "resource-sesi")));
        items.add(module("platform", "Manajemen Platform", "M",
                "Multi-lingkungan serta ekspor dan backup data.",
                feature("Multi-RT/RW",
                        "Lihat struktur RW/RT sesuai izin akun.",
                        "organisasi, rw, rt, pengaturan", "resource-wilayah"),
                feature("Pengaturan organisasi",
                        "Lihat konfigurasi organisasi yang tersedia.",
                        "pengaturan", "resource-pengaturan"),
                feature("Ekspor dan backup data",
                        "Lihat riwayat pencadangan data dan status ekspor.",
                        "cadangan_data", "resource-cadangan-data")));
        return Collections.unmodifiableList(items);
    }

    static final class ModuleInfo {
        final String id;
        final String title;
        final String shortName;
        final String description;
        final List<FeatureInfo> features;

        ModuleInfo(String id, String title, String shortName, String description, List<FeatureInfo> features) {
            this.id = id;
            this.title = title;
            this.shortName = shortName;
            this.description = description;
            this.features = features;
        }
    }

    private static final class FeatureInfo {
        final String title;
        final String description;
        final String tables;
        final String target;

        FeatureInfo(String title, String description, String tables, String target) {
            this.title = title;
            this.description = description;
            this.tables = tables;
            this.target = target;
        }
    }
}
