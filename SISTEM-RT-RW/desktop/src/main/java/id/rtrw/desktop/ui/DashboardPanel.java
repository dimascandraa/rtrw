package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;
import id.rtrw.desktop.api.ApiException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Beranda: ringkasan jumlah warga dan kondisi tagihan. Angka dihitung dari
 * GET /api/residents dan GET /api/invoices (kolom "status" pada tagihan).
 */
final class DashboardPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final ApiClient api;
    private final SessionListener listener;

    private final JLabel residents = value();
    private final JLabel invoices = value();
    private final JLabel paid = value();
    private final JLabel unpaid = value();
    private final PercentBar progress = new PercentBar();
    private final JLabel progressText = UiKit.label(" ", 13f, false, Theme.muted());
    private final JLabel note = UiKit.label(" ", 12f, false, Theme.danger());
    private boolean loading;

    DashboardPanel(ApiClient api, SessionListener listener) {
        this.api = api;
        this.listener = listener;

        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(24, 28, 16, 28));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel welcome = UiKit.label("Selamat datang, " + api.userName(), 24f, true, Theme.text());
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        String role = api.userRole().isEmpty() ? "" : "Peran: " + api.userRole() + "   |   ";
        JLabel info = UiKit.label(role + "Server: " + api.baseUrl(), 13f, false, Theme.muted());
        info.setAlignmentX(LEFT_ALIGNMENT);
        header.add(welcome);
        header.add(Box.createVerticalStrut(2));
        header.add(info);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(LEFT_ALIGNMENT);
        cards.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 120));
        cards.add(statCard("Warga Terdata", residents, Theme.primary()));
        cards.add(statCard("Total Tagihan", invoices, Theme.text()));
        cards.add(statCard("Lunas", paid, Theme.success()));
        cards.add(statCard("Belum Lunas / Menunggak", unpaid, Theme.danger()));
        center.add(cards);
        center.add(Box.createVerticalStrut(18));

        JPanel progressCard = UiKit.card();
        progressCard.setLayout(new BorderLayout(0, 8));
        progressCard.setAlignmentX(LEFT_ALIGNMENT);
        progressCard.add(UiKit.label("Tingkat pelunasan tagihan", 14f, true, Theme.text()), BorderLayout.NORTH);
        progressCard.add(progress, BorderLayout.CENTER);
        progressCard.add(progressText, BorderLayout.SOUTH);
        progressCard.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 110));
        center.add(progressCard);
        center.add(Box.createVerticalStrut(12));
        note.setAlignmentX(LEFT_ALIGNMENT);
        center.add(note);

        add(center, BorderLayout.CENTER);
    }

    /** Bar persen datar berwarna tema. */
    private static final class PercentBar extends javax.swing.JComponent {
        private static final long serialVersionUID = 1L;

        private int value;

        PercentBar() {
            setPreferredSize(new java.awt.Dimension(100, 16));
        }

        void setValue(int percent) {
            this.value = Math.max(0, Math.min(100, percent));
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Theme.border());
            g2.fillRoundRect(0, 0, w, h, h, h);
            if (value > 0) {
                g2.setColor(Theme.success());
                g2.fillRoundRect(0, 0, Math.max(h, w * value / 100), h, h, h);
            }
            g2.dispose();
        }
    }

    private static JLabel value() {
        return UiKit.label("-", 32f, true, Theme.text());
    }

    private static JPanel statCard(String title, JLabel valueLabel, java.awt.Color color) {
        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(0, 6));
        card.add(UiKit.label(title, 13f, false, Theme.muted()), BorderLayout.NORTH);
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Hasil pemuatan data beranda. */
    private static final class Stats {
        Integer residents;
        Integer invoices;
        Integer paid;
        Integer unpaid;
        String error;
        ApiException unauthorized;

        void fail(ApiException e) {
            if (e.isUnauthorized()) {
                unauthorized = e;
            }
            error = e.getMessage();
        }
    }

    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        note.setText(" ");
        new SwingWorker<Stats, Void>() {
            @Override
            protected Stats doInBackground() {
                Stats s = new Stats();
                try {
                    s.residents = ApiData.rows(api.get("/api/residents")).size();
                } catch (ApiException e) {
                    s.fail(e);
                }
                try {
                    List<Map<String, Object>> list = ApiData.rows(api.get("/api/invoices"));
                    int ok = 0;
                    for (Map<String, Object> row : list) {
                        if (isPaid(row)) {
                            ok++;
                        }
                    }
                    s.invoices = list.size();
                    s.paid = ok;
                    s.unpaid = list.size() - ok;
                } catch (ApiException e) {
                    s.fail(e);
                }
                return s;
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    show(get());
                } catch (ExecutionException e) {
                    note.setText("Gagal memuat ringkasan: " + e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void show(Stats s) {
        residents.setText(s.residents == null ? "-" : s.residents.toString());
        invoices.setText(s.invoices == null ? "-" : s.invoices.toString());
        paid.setText(s.paid == null ? "-" : s.paid.toString());
        unpaid.setText(s.unpaid == null ? "-" : s.unpaid.toString());

        if (s.invoices != null && s.invoices > 0) {
            int pct = Math.round(100f * s.paid / s.invoices);
            progress.setValue(pct);
            progressText.setText(s.paid + " dari " + s.invoices + " tagihan sudah lunas (" + pct + "%)");
        } else {
            progress.setValue(0);
            progressText.setText("Belum ada data tagihan.");
        }
        if (s.unauthorized != null) {
            SwingUtilities.invokeLater(() -> listener.onSessionExpired(s.unauthorized.getMessage()));
        } else if (s.error != null) {
            note.setText("Sebagian data tidak dapat dimuat: " + s.error);
        }
    }

    /** Tagihan dianggap lunas bila kolom status memuat "lunas" tanpa "belum". */
    static boolean isPaid(Map<String, Object> row) {
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e.getKey().toLowerCase(Locale.ROOT).contains("status") && e.getValue() != null) {
                String s = e.getValue().toString().toLowerCase(Locale.ROOT);
                return s.contains("lunas") && !s.contains("belum");
            }
        }
        return false;
    }
}
