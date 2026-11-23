package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.config.AppConfig;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

/** Jendela utama: sidebar menu di kiri dan area konten di kanan. */
public final class MainFrame extends JFrame implements SessionListener {
    private static final long serialVersionUID = 1L;

    private static final Map<String, String> MENU = new LinkedHashMap<>();
    private static final Map<String, String> RESOURCE_TITLES = new HashMap<>();

    static {
        MENU.put("beranda", "Beranda");
        for (ModulePanel.ModuleInfo module : ModulePanel.modules()) {
            MENU.put(module.id, module.title);
        }
        RESOURCE_TITLES.put("warga", "Data Warga");
        RESOURCE_TITLES.put("keluarga", "Data Keluarga");
        RESOURCE_TITLES.put("mutasi", "Mutasi Warga");
        RESOURCE_TITLES.put("kategori-iuran", "Kategori Iuran");
        RESOURCE_TITLES.put("periode-tagihan", "Periode Tagihan");
        RESOURCE_TITLES.put("tagihan", "Tagihan");
        RESOURCE_TITLES.put("pembayaran", "Pembayaran");
        RESOURCE_TITLES.put("transaksi-kas", "Transaksi Kas");
        RESOURCE_TITLES.put("jenis-surat", "Jenis Surat");
        RESOURCE_TITLES.put("pengajuan-surat", "Pengajuan Surat");
        RESOURCE_TITLES.put("survei", "Survei");
        RESOURCE_TITLES.put("kegiatan", "Kegiatan");
        RESOURCE_TITLES.put("pengaduan", "Pengaduan");
        RESOURCE_TITLES.put("kontak-darurat", "Kontak Darurat");
        RESOURCE_TITLES.put("pengguna", "Pengguna");
        RESOURCE_TITLES.put("peran", "Peran");
        RESOURCE_TITLES.put("audit-log", "Audit Log");
        RESOURCE_TITLES.put("sesi", "Sesi Perangkat");
        RESOURCE_TITLES.put("wilayah", "Wilayah RT/RW");
        RESOURCE_TITLES.put("pengaturan", "Pengaturan");
        RESOURCE_TITLES.put("cadangan-data", "Riwayat Cadangan Data");
    }

    private final AppConfig config;
    private final ApiClient api;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> pages = new HashMap<>();
    private final Map<String, NavButton> navButtons = new HashMap<>();
    private String current;
    private String activeModule = "beranda";

    public MainFrame(AppConfig config, ApiClient api, String startPage) {
        this(config, api, startPage, "beranda");
    }

    private MainFrame(AppConfig config, ApiClient api, String startPage, String startModule) {
        super("Sistem Informasi RT/RW - Desktop");
        this.config = config;
        this.api = api;
        this.activeModule = MENU.containsKey(startModule)
                && ("beranda".equals(startModule) || ModulePanel.hasAccessibleFeature(api, startModule))
                ? startModule : "beranda";

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        content.setBackground(Theme.bg());
        add(content, BorderLayout.CENTER);

        show(MENU.containsKey(startPage) || isFeaturePage(startPage) ? startPage : "beranda");
    }

    // ------------------------------------------------------------------ sidebar

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.sidebar());
        side.setPreferredSize(new Dimension(260, 0));
        JPanel branding = new JPanel(new BorderLayout(10, 0));
        branding.setOpaque(false);
        branding.setBorder(new EmptyBorder(18, 16, 16, 14));

        JPanel identity = new JPanel(new BorderLayout(10, 0));
        identity.setOpaque(false);
        JLabel logo = new BadgeLabel("RW", Theme.accent(), Theme.brand(), 40);
        identity.add(logo, BorderLayout.WEST);
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(UiKit.label("Sistem RT/RW", 15f, true, Theme.sidebarText()));
        brandText.add(UiKit.label("DESKTOP", 10f, true, Theme.accent()));
        identity.add(brandText, BorderLayout.CENTER);
        branding.add(identity, BorderLayout.CENTER);

        FlatButton theme = new FlatButton(Theme.isDark() ? "Terang" : "Gelap", FlatButton.Kind.GHOST);
        theme.setToolTipText(Theme.isDark() ? "Gunakan tema terang" : "Gunakan tema gelap");
        theme.addActionListener(e -> toggleTheme());
        branding.add(theme, BorderLayout.EAST);
        side.add(branding, BorderLayout.NORTH);

        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBorder(new EmptyBorder(4, 14, 8, 14));
        for (Map.Entry<String, String> item : MENU.entrySet()) {
            if (!"beranda".equals(item.getKey())
                    && !ModulePanel.hasAccessibleFeature(api, item.getKey())) {
                continue;
            }
            NavButton button = new NavButton(item.getValue());
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    show(item.getKey());
                }
            });
            navButtons.put(item.getKey(), button);
            navigation.add(button);
            navigation.add(Box.createVerticalStrut(4));
        }

        JScrollPane navigationScroll = new JScrollPane(navigation,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScroll.setBorder(null);
        navigationScroll.setOpaque(false);
        navigationScroll.getViewport().setOpaque(false);
        navigationScroll.getVerticalScrollBar().setUnitIncrement(18);
        side.add(navigationScroll, BorderLayout.CENTER);

        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(12, 14, 16, 14));
        JPanel identityRow = new JPanel(new BorderLayout(10, 0));
        identityRow.setOpaque(false);
        String displayName = api.userName().isEmpty() ? "Pengguna" : api.userName();
        String initial = displayName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        identityRow.add(new BadgeLabel(initial, Theme.accent(), Theme.brand(), 38), BorderLayout.WEST);
        JPanel userText = new JPanel();
        userText.setOpaque(false);
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        userText.add(UiKit.label(displayName, 13f, true, Theme.sidebarText()));
        String roleText = api.userRole().isEmpty() ? "Masuk" : api.userRole();
        userText.add(UiKit.label(roleText, 11f, false, Theme.sidebarText()));
        identityRow.add(userText, BorderLayout.CENTER);
        profile.add(identityRow);
        profile.add(Box.createVerticalStrut(12));

        JPanel profileActions = new JPanel(new GridLayout(1, 2, 8, 0));
        profileActions.setOpaque(false);
        FlatButton accountSettings = new FlatButton("Profil", FlatButton.Kind.GHOST);
        accountSettings.addActionListener(e -> {
            activeModule = "beranda";
            show("account-settings");
        });
        FlatButton logout = new FlatButton("Keluar", FlatButton.Kind.NEUTRAL);
        logout.addActionListener(e -> logout(null));
        profileActions.add(accountSettings);
        profileActions.add(logout);
        profile.add(profileActions);
        side.add(profile, BorderLayout.SOUTH);
        return side;
    }

    // ------------------------------------------------------------------ halaman

    private JPanel page(String key) {
        JPanel page = pages.get(key);
        if (page != null) {
            return page;
        }
        switch (key) {
            case "beranda":
                page = new ModulePanel(api, null, this::openFeature);
                break;
            case "reports":
            case "residents":
            case "finance":
            case "letters":
            case "services":
            case "communications":
            case "activities":
            case "safety":
            case "access":
            case "account-security":
            case "platform":
                page = new ModulePanel(api, ModulePanel.module(key), this::openFeature);
                break;
            case "announcement-list":
                page = new DataTablePanel(api, "Pengumuman",
                        "Pengumuman RT/RW untuk wilayah Anda.", api::announcements, this);
                break;
            case "notification-list":
                page = new DataTablePanel(api, "Notifikasi",
                        "Notifikasi akun Anda. Waktu dibaca kosong berarti belum dibaca.",
                        api::notifications, this);
                break;
            case "registrations-pending":
                page = new RegistrationApprovalPanel(api, this);
                break;
            case "letters-pending":
                page = new LetterDecisionPanel(api, this);
                break;
            case "payment-verification":
                page = new PaymentVerificationPanel(api, this);
                break;
            case "report-export":
                page = new ReportExportPanel(api, this);
                break;
            case "announcement-management":
                page = new ManagementTablePanel(api, ManagementTablePanel.Kind.ANNOUNCEMENTS, this);
                break;
            case "activity-management":
                page = new ManagementTablePanel(api, ManagementTablePanel.Kind.ACTIVITIES, this);
                break;
            case "letter-request":
                page = new LetterRequestPanel(api, this);
                break;
            case "account-settings":
                page = new AccountSettingsPanel(api, this, this::refreshProfile);
                break;
            case "account-registration":
                page = new AccountRegistrationPanel(api);
                break;
            case "dashboard-stats":
                page = new DashboardPanel(api, this);
                break;
            default:
                page = key.startsWith("resource-") ? resourcePage(key) : new DashboardPanel(api, this);
        }
        pages.put(key, page);
        content.add(page, key);
        return page;
    }

    private JPanel resourcePage(String pageKey) {
        String resource = pageKey.substring("resource-".length());
        String title = RESOURCE_TITLES.get(resource);
        if (title == null) {
            return new ModulePanel(api, null, this::openFeature);
        }
        switch (resource) {
            case "warga":
                return new ManagementTablePanel(api, ManagementTablePanel.Kind.RESIDENTS, this);
            case "keluarga":
                return new ManagementTablePanel(api, ManagementTablePanel.Kind.FAMILIES, this);
            case "tagihan":
                return new ManagementTablePanel(api, ManagementTablePanel.Kind.INVOICES, this);
            default:
                break;
        }
        return new DataTablePanel(api, title, "Data sesuai hak akses dan wilayah akun Anda.",
                (limit, offset, search) -> api.resource(resource, limit, offset, search), this);
    }

    private void show(String key) {
        JPanel page = page(key);
        current = key;
        if (MENU.containsKey(key)) {
            activeModule = key;
        }
        cards.show(content, key);
        for (Map.Entry<String, NavButton> e : navButtons.entrySet()) {
            e.getValue().setSelected(e.getKey().equals(activeModule));
        }
        if (page instanceof Refreshable) {
            ((Refreshable) page).refresh();
        }
    }

    private void openFeature(String moduleKey, String featureKey) {
        if (!MENU.containsKey(moduleKey)) {
            return;
        }
        if (featureKey == null) {
            show(moduleKey);
            return;
        }
        if (!ModulePanel.isFeatureAccessible(api, featureKey)) {
            return;
        }
        activeModule = moduleKey;
        show(featureKey);
    }

    private static boolean isFeaturePage(String key) {
        return key != null && (key.startsWith("resource-") || key.equals("dashboard-stats")
                || key.equals("announcement-list") || key.equals("notification-list")
                || key.equals("announcement-management") || key.equals("activity-management")
                || key.equals("payment-verification") || key.equals("report-export")
                || key.equals("registrations-pending") || key.equals("letters-pending")
                || key.equals("letter-request")
                || key.equals("account-registration")
                || key.equals("account-settings"));
    }

    // ------------------------------------------------------------------ sesi dan tema

    private void toggleTheme() {
        Theme.toggle();
        MainFrame next = new MainFrame(config, api, current, activeModule);
        next.setBounds(getBounds());
        next.setExtendedState(getExtendedState());
        next.setVisible(true);
        dispose();
    }

    private void refreshProfile() {
        MainFrame next = new MainFrame(config, api, current, activeModule);
        next.setBounds(getBounds());
        next.setExtendedState(getExtendedState());
        next.setVisible(true);
        dispose();
    }

    private void logout(String message) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    api.logoutServer();
                    return message;
                } catch (ApiException ex) {
                    api.logout();
                    String prefix = message == null ? "" : message + "\n";
                    return prefix + "Sesi lokal dihapus, tetapi server gagal mencabut sesi: " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                String notice;
                try {
                    notice = get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    notice = "Permintaan keluar dibatalkan.";
                } catch (ExecutionException ex) {
                    api.logout();
                    notice = "Sesi lokal dihapus. Gagal keluar dari server: " + ex.getCause().getMessage();
                }
                LoginFrame login = new LoginFrame(config, api, notice);
                login.setVisible(true);
                dispose();
            }
        }.execute();
    }

    @Override
    public void onSessionExpired(String message) {
        logout(message == null ? "Sesi berakhir. Silakan masuk kembali." : message);
    }

    // ------------------------------------------------------------------ tombol menu

    /** Tombol menu di sidebar dengan penanda aksen kuning pada menu aktif. */
    private static final class NavButton extends JPanel {
        private static final long serialVersionUID = 1L;

        private final String text;
        private boolean selected;
        private boolean hover;

        NavButton(String text) {
            this.text = text;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
            if (selected || hover) {
                g2.setColor(selected ? Theme.sidebarSelected() : Theme.sidebarHover());
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            }
            int badgeSize = 30;
            int badgeX = 9;
            int badgeY = (r.height - badgeSize) / 2;
            g2.setColor(selected ? Theme.primary() : Theme.header());
            g2.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 10, 10);
            g2.setColor(selected ? Theme.primaryText() : Theme.primary());
            g2.setFont(Theme.font(12f, true));
            int badgeTextY = badgeY + (badgeSize - g2.getFontMetrics().getHeight()) / 2
                    + g2.getFontMetrics().getAscent();
            String mark = text.isEmpty() ? "" : text.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
            int badgeTextX = badgeX + (badgeSize - g2.getFontMetrics().stringWidth(mark)) / 2;
            g2.drawString(mark, badgeTextX, badgeTextY);
            g2.setColor(selected ? Theme.text() : Theme.sidebarText());
            g2.setFont(Theme.font(14f, selected));
            int y = (r.height - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(text, 50, y);
            g2.dispose();
        }
    }

    private static final class BadgeLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private final java.awt.Color fill;

        BadgeLabel(String text, java.awt.Color fill, java.awt.Color foreground, int size) {
            super(text, JLabel.CENTER);
            this.fill = fill;
            setForeground(foreground);
            setFont(Theme.font(13f, true));
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
