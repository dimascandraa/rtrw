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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Beranda: ringkasan jumlah warga dan kondisi tagihan dari
 * GET /api/v1/dashboard/summary.
 */
final class DashboardPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final ApiClient api;
    private final SessionListener listener;

    private final JLabel residents = value(30f);
    private final JLabel cashBalance = value(22f);
    private final JLabel paid = value(22f);
    private final JLabel unpaid = value(22f);
    private final PaymentChart paymentChart = new PaymentChart();
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
        JLabel welcome = UiKit.label("Dashboard statistik interaktif", 24f, true, Theme.text());
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        JLabel info = UiKit.label(
                "Pantau saldo kas, tagihan, dan data warga lingkungan Anda.", 13f, false, Theme.muted());
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
        cards.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 124));
        cards.add(statCard("Saldo kas", cashBalance, Theme.primary()));
        cards.add(statCard("Tagihan lunas", paid, Theme.success()));
        cards.add(statCard("Tunggakan", unpaid, Theme.danger()));
        cards.add(statCard("Warga terdata", residents, Theme.text()));
        center.add(cards);
        center.add(Box.createVerticalStrut(18));

        JPanel progressCard = UiKit.card();
        progressCard.setLayout(new BorderLayout(0, 8));
        progressCard.setAlignmentX(LEFT_ALIGNMENT);
        progressCard.add(UiKit.label("Tagihan dan pembayaran periode aktif", 14f, true, Theme.text()),
                BorderLayout.NORTH);
        progressCard.add(paymentChart, BorderLayout.CENTER);
        progressCard.add(progressText, BorderLayout.SOUTH);
        progressCard.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 300));
        center.add(progressCard);
        center.add(Box.createVerticalStrut(12));
        note.setAlignmentX(LEFT_ALIGNMENT);
        center.add(note);

        add(center, BorderLayout.CENTER);
    }

    /** Grafik perbandingan nilai tagihan dan pembayaran yang tersedia dari API. */
    private static final class PaymentChart extends javax.swing.JComponent {
        private static final long serialVersionUID = 1L;

        private Double billed;
        private Double collected;

        PaymentChart() {
            setPreferredSize(new java.awt.Dimension(100, 190));
        }

        void setValues(Double billed, Double collected) {
            this.billed = billed;
            this.collected = collected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int baseline = getHeight() - 28;
            int chartHeight = Math.max(1, baseline - 34);
            g2.setColor(Theme.border());
            g2.drawLine(18, baseline, width - 18, baseline);

            double max = Math.max(billed == null ? 0 : billed, collected == null ? 0 : collected);
            if (max <= 0) {
                g2.setColor(Theme.muted());
                g2.setFont(Theme.font(13f, false));
                String empty = "Data pembayaran periode aktif belum tersedia.";
                FontMetrics metrics = g2.getFontMetrics();
                g2.drawString(empty, Math.max(8, (width - metrics.stringWidth(empty)) / 2),
                        Math.max(24, getHeight() / 2));
            } else {
                int barWidth = Math.min(112, Math.max(42, width / 8));
                drawBar(g2, width / 3, barWidth, chartHeight, baseline, billed, max,
                        Theme.primary(), "Tagihan");
                drawBar(g2, (width * 2) / 3, barWidth, chartHeight, baseline, collected, max,
                        Theme.success(), "Dibayar");
            }
            g2.dispose();
        }

        private static void drawBar(Graphics2D g2, int center, int width, int chartHeight, int baseline,
                Double amount, double max, Color color, String label) {
            int barHeight = amount == null ? 0 : Math.round((float) (chartHeight * amount / max));
            if (amount != null && amount > 0) {
                barHeight = Math.max(8, barHeight);
                int x = center - width / 2;
                g2.setColor(color);
                g2.fillRoundRect(x, baseline - barHeight, width, barHeight, 12, 12);
            }

            String amountText = amount == null ? "-" : formatMoney(amount);
            g2.setFont(Theme.font(12f, true));
            g2.setColor(Theme.text());
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(amountText, center - metrics.stringWidth(amountText) / 2,
                    Math.max(18, baseline - barHeight - 8));
            g2.setFont(Theme.font(12f, false));
            g2.setColor(Theme.muted());
            metrics = g2.getFontMetrics();
            g2.drawString(label, center - metrics.stringWidth(label) / 2, baseline + 20);
        }
    }

    private static JLabel value(float size) {
        return UiKit.label("-", size, true, Theme.text());
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
        Integer families;
        Integer paid;
        Integer unpaid;
        Double cashBalance;
        Double billed;
        Double collected;
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
                    Object response = api.dashboardSummary();
                    Map<String, Object> data = ApiData.findMap(response, "data");
                    Map<String, Object> stats = ApiData.findMap(data, "stats");
                    Map<String, Object> period = ApiData.findMap(data, "period");
                    s.residents = integer(stats, "residents");
                    s.families = integer(stats, "families");
                    s.cashBalance = decimal(stats, "cashBalance");
                    s.paid = integer(period, "paidFamilies");
                    s.unpaid = integer(period, "unpaidInvoices");
                    s.billed = decimal(period, "billed");
                    s.collected = decimal(period, "collected");
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
        cashBalance.setText(s.cashBalance == null ? "-" : formatMoney(s.cashBalance));
        paid.setText(s.paid == null ? "-" : s.families == null
                ? s.paid.toString() : s.paid + " dari " + s.families + " KK");
        unpaid.setText(s.billed == null || s.collected == null
                ? "-" : formatMoney(Math.max(0, s.billed - s.collected)));
        paymentChart.setValues(s.billed, s.collected);

        if (s.billed != null && s.billed > 0 && s.collected != null) {
            int pct = Math.round(100f * (float) (s.collected / s.billed));
            progressText.setText(formatMoney(s.collected) + " terkumpul dari " + formatMoney(s.billed)
                    + " (" + pct + "%)");
        } else {
            progressText.setText("Belum ada data tagihan untuk periode aktif.");
        }
        if (s.unauthorized != null) {
            SwingUtilities.invokeLater(() -> listener.onSessionExpired(s.unauthorized.getMessage()));
        } else if (s.error != null) {
            note.setText("Sebagian data tidak dapat dimuat: " + s.error);
        }
    }

    private static Integer integer(Map<String, Object> map, String... keys) {
        if (map == null) {
            return null;
        }
        Object value = ApiData.firstValue(map, keys);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Double decimal(Map<String, Object> map, String... keys) {
        if (map == null) {
            return null;
        }
        Object value = ApiData.firstValue(map, keys);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String formatMoney(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

}
