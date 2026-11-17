package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.config.AppConfig;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Jendela utama: sidebar menu di kiri dan area konten di kanan. */
public final class MainFrame extends JFrame implements SessionListener {
    private static final long serialVersionUID = 1L;

    private static final Map<String, String> MENU = new LinkedHashMap<>();

    static {
        MENU.put("beranda", "Beranda");
        MENU.put("warga", "Data Warga");
        MENU.put("tagihan", "Tagihan");
        MENU.put("pembayaran", "Pembayaran");
        MENU.put("pengumuman", "Pengumuman");
        MENU.put("laporan", "Laporan");
    }

    private final AppConfig config;
    private final ApiClient api;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> pages = new HashMap<>();
    private final Map<String, NavButton> navButtons = new HashMap<>();
    private String current;

    public MainFrame(AppConfig config, ApiClient api, String startPage) {
        super("Sistem Informasi RT/RW - Desktop");
        this.config = config;
        this.api = api;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1200, 760);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        content.setBackground(Theme.bg());
        add(content, BorderLayout.CENTER);

        show(MENU.containsKey(startPage) ? startPage : "beranda");
    }

    // ------------------------------------------------------------------ sidebar

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.sidebar());
        side.setPreferredSize(new Dimension(230, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(22, 14, 16, 14));

        JLabel logo = UiKit.label("RT/RW", 26f, true, Theme.accent());
        logo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = UiKit.label("Desktop Pengurus", 12f, false, Theme.sidebarText());
        sub.setAlignmentX(LEFT_ALIGNMENT);
        side.add(logo);
        side.add(sub);
        side.add(Box.createVerticalStrut(24));

        for (Map.Entry<String, String> item : MENU.entrySet()) {
            NavButton button = new NavButton(item.getValue());
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    show(item.getKey());
                }
            });
            navButtons.put(item.getKey(), button);
            side.add(button);
            side.add(Box.createVerticalStrut(4));
        }

        side.add(Box.createVerticalGlue());

        JLabel name = UiKit.label(api.userName(), 14f, true, Color.WHITE);
        name.setAlignmentX(LEFT_ALIGNMENT);
        String roleText = api.userRole().isEmpty() ? "Masuk" : api.userRole();
        JLabel role = UiKit.label(roleText, 12f, false, Theme.sidebarText());
        role.setAlignmentX(LEFT_ALIGNMENT);
        side.add(name);
        side.add(role);
        side.add(Box.createVerticalStrut(12));

        FlatButton theme = new FlatButton(Theme.isDark() ? "Mode Terang" : "Mode Gelap", FlatButton.Kind.NEUTRAL);
        theme.setAlignmentX(LEFT_ALIGNMENT);
        theme.setMaximumSize(new Dimension(Integer.MAX_VALUE, theme.getPreferredSize().height));
        theme.addActionListener(e -> toggleTheme());
        FlatButton logout = new FlatButton("Keluar", FlatButton.Kind.DANGER);
        logout.setAlignmentX(LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, logout.getPreferredSize().height));
        logout.addActionListener(e -> logout(null));
        side.add(theme);
        side.add(Box.createVerticalStrut(8));
        side.add(logout);
        return side;
    }

    // ------------------------------------------------------------------ halaman

    private JPanel page(String key) {
        JPanel page = pages.get(key);
        if (page != null) {
            return page;
        }
        switch (key) {
            case "warga":
                page = new ResidentsPanel(api, this);
                break;
            case "tagihan":
                page = new DataTablePanel(api, "Tagihan",
                        "Tagihan iuran per keluarga beserta status pembayarannya.", "/api/invoices", this);
                break;
            case "pembayaran":
                page = new PaymentsPanel(api, this);
                break;
            case "pengumuman":
                page = new DataTablePanel(api, "Pengumuman",
                        "Pengumuman RT/RW untuk warga.", "/api/announcements", this);
                break;
            case "laporan":
                page = new DataTablePanel(api, "Laporan",
                        "Daftar laporan kas, iuran, dan tunggakan. Gunakan Ekspor CSV untuk membuka di Excel.",
                        "/api/reports", this);
                break;
            default:
                page = new DashboardPanel(api, this);
        }
        pages.put(key, page);
        content.add(page, key);
        return page;
    }

    private void show(String key) {
        JPanel page = page(key);
        current = key;
        cards.show(content, key);
        for (Map.Entry<String, NavButton> e : navButtons.entrySet()) {
            e.getValue().setSelected(e.getKey().equals(key));
        }
        if (page instanceof Refreshable) {
            ((Refreshable) page).refresh();
        }
    }

    // ------------------------------------------------------------------ sesi dan tema

    private void toggleTheme() {
        Theme.toggle();
        MainFrame next = new MainFrame(config, api, current);
        next.setBounds(getBounds());
        next.setExtendedState(getExtendedState());
        next.setVisible(true);
        dispose();
    }

    private void logout(String message) {
        api.logout();
        LoginFrame login = new LoginFrame(config, api, message);
        login.setVisible(true);
        dispose();
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
            setPreferredSize(new Dimension(200, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
                g2.setColor(new Color(255, 255, 255, selected ? 34 : 16));
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            }
            if (selected) {
                g2.setColor(Theme.accent());
                g2.fillRoundRect(0, 8, 4, r.height - 16, 4, 4);
            }
            g2.setColor(selected ? Color.WHITE : Theme.sidebarText());
            g2.setFont(getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN, 14f));
            int y = (r.height - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(text, 18, y);
            g2.dispose();
        }
    }
}
