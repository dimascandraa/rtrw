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
            JLabel count = UiKit.label("11 modul  \u00b7  26 fitur", 12f, true, Theme.primary());
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
                grid.add(card(item.shortName, item.title, item.description,
                        item.features.size() + " fitur",
                        "Buka modul", () -> openFeature.accept(item.id, null)));
            }
        } else {
            for (FeatureInfo feature : module.features) {
                grid.add(card(String.format("%02d", module.features.indexOf(feature) + 1),
                        feature.title, feature.description,
                        feature.target == null ? "Tahap integrasi berikutnya" : "Tersedia di desktop",
                        "Lihat tampilan", () -> {
                            if (feature.target == null) {
                                JOptionPane.showMessageDialog(this,
                                        "Halaman operasional fitur ini belum tersedia di desktop karena "
                                                + "endpoint API belum terintegrasi.",
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
<<<<<<< HEAD
                        "kas, tagihan, pembayaran", null)));
        items.add(module("residents", "Manajemen Data Warga", "M", "Data warga, keluarga, dan mutasi.",
                feature("Data warga & kepala keluarga",
                        "Kelola profil warga, anggota keluarga, dan kepala keluarga per wilayah.",
                        "warga, keluarga", null),
=======
                        "kas, tagihan, pembayaran", "report-list")));
        items.add(module("residents", "Manajemen Data Warga", "M", "Data warga, keluarga, dan mutasi.",
                feature("Data warga & kepala keluarga",
                        "Kelola profil warga, anggota keluarga, dan kepala keluarga per wilayah.",
                        "warga, keluarga", "resident-list"),
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
                feature("Mutasi dan riwayat warga",
                        "Catat warga pindah masuk, pindah keluar, kelahiran, dan perubahan status.",
                        "mutasi_warga, warga", null)));
        items.add(module("finance", "Keuangan RT/RW", "K", "Iuran, tagihan, pembayaran, dan kas.",
                feature("Kategori dan aturan iuran",
                        "Atur jenis iuran, nominal, siklus, jatuh tempo, dan cakupan RT.",
                        "kategori_iuran, aturan_iuran", null),
                feature("Tagihan otomatis",
                        "Lihat tagihan per keluarga, periode, jumlah, dan status pembayarannya.",
<<<<<<< HEAD
                        "periode_tagihan, tagihan", null),
                feature("Pembayaran QRIS/transfer/tunai",
                        "Telusuri pembayaran warga melalui QRIS, transfer, atau tunai.",
                        "pembayaran, tagihan", null),
                feature("Verifikasi bukti pembayaran",
                        "Periksa bukti transfer dan tindak lanjuti pembayaran yang menunggu verifikasi.",
                        "pembayaran, bukti_pembayaran", null),
=======
                        "periode_tagihan, tagihan", "invoice-list"),
                feature("Pembayaran QRIS/transfer/tunai",
                        "Telusuri pembayaran warga melalui QRIS, transfer, atau tunai.",
                        "pembayaran, tagihan", "payment-list"),
                feature("Verifikasi bukti pembayaran",
                        "Periksa bukti transfer dan tindak lanjuti pembayaran yang menunggu verifikasi.",
                        "pembayaran, bukti_pembayaran", "payment-verification"),
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
                feature("Riwayat pembayaran & kuitansi",
                        "Telusuri pembayaran terverifikasi dan kuitansi digital.",
                        "pembayaran, kuitansi", null),
                feature("Kas masuk dan pengeluaran",
                        "Catat arus kas lingkungan dan lihat saldo berdasarkan transaksi.",
                        "transaksi_kas", null),
                feature("Pengingat iuran/tunggakan",
                        "Pantau pengingat tagihan yang mendekati jatuh tempo atau belum dibayar.",
                        "tagihan, notifikasi", null)));
        items.add(module("letters", "Administrasi Surat", "A", "Pengajuan dan persetujuan surat.",
                feature("Pengajuan surat online",
                        "Ajukan surat keterangan domisili, pengantar, dan surat keterangan usaha.",
                        "jenis_surat, pengajuan_surat", null),
                feature("Verifikasi dan persetujuan surat",
                        "Tinjau kelengkapan data, catat keputusan, dan pantau status penerbitan.",
                        "pengajuan_surat, persetujuan_surat", null)));
        items.add(module("services", "Layanan Warga", "L", "Penilaian warga atas layanan pengurus.",
                feature("Survei kepuasan warga",
                        "Lihat ringkasan penilaian dan masukan warga atas layanan.",
                        "survei, jawaban_survei", null)));
        items.add(module("communications", "Informasi & Komunikasi", "I", "Pengumuman dan notifikasi.",
                feature("Pengumuman RT/RW",
                        "Lihat informasi yang diterbitkan untuk warga atau wilayah tertentu.",
                        "pengumuman, target_pengumuman", "announcement-list"),
                feature("Notifikasi aplikasi",
                        "Pantau pemberitahuan tagihan, pembayaran, dan informasi lingkungan.",
<<<<<<< HEAD
                        "notifikasi, log_notifikasi", "notification-list")));
=======
                        "notifikasi, log_notifikasi", null)));
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b
        items.add(module("activities", "Sosial & Kegiatan", "S", "Agenda kerja bakti dan kegiatan sosial.",
                feature("Jadwal kegiatan/kerja bakti",
                        "Bagikan agenda warga dan catat konfirmasi kehadiran kegiatan.",
                        "kegiatan, kehadiran_kegiatan", null)));
        items.add(module("safety", "Keamanan & Lingkungan", "K", "Laporan kejadian dan kontak darurat.",
                feature("Laporan keluhan/kejadian",
                        "Sampaikan laporan lingkungan dan tindak lanjuti progres penanganannya.",
                        "pengaduan, tanggapan_pengaduan", null),
                feature("Kontak darurat/direktori layanan",
                        "Simpan kontak pengurus, keamanan, ambulans, polisi, dan layanan lainnya.",
                        "kontak_darurat", null)));
        items.add(module("access", "Registrasi & Akses", "R", "Pendaftaran, verifikasi, dan hak akses.",
                feature("Registrasi warga",
                        "Daftarkan akun warga dan kirim kode verifikasi melalui email atau telepon.",
                        "pengguna, kode_verifikasi", null),
                feature("Verifikasi akun oleh Admin",
                        "Tinjau pendaftaran dan hubungkan akun dengan data warga serta wilayahnya.",
                        "pengguna, warga, penugasan_pengguna", null),
                feature("Manajemen peran dan hak akses",
                        "Atur peran, penugasan wilayah, dan izin aksi setiap pengguna.",
                        "peran, izin_akses, penugasan_pengguna", null)));
        items.add(module("account-security", "Keamanan Akun", "K", "Riwayat aktivitas dan sesi login.",
                feature("Riwayat aktivitas/audit log",
                        "Lihat jejak aktivitas penting untuk membantu pengawasan dan audit.",
                        "log_audit", null),
                feature("Manajemen sesi/perangkat",
                        "Periksa perangkat yang pernah masuk dan cabut sesi yang tidak digunakan.",
                        "sesi_pengguna", null)));
        items.add(module("platform", "Manajemen Platform", "M",
                "Multi-lingkungan serta ekspor dan backup data.",
                feature("Multi-RT/RW",
                        "Kelola organisasi, RW, RT, dan pengaturan lingkungan dalam satu platform.",
                        "organisasi, rw, rt, pengaturan", null),
                feature("Ekspor dan backup data",
                        "Lihat riwayat pencadangan data dan status ekspor.",
                        "cadangan_data", null)));
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
