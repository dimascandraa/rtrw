package ui;

import model.User;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Ringkasan beranda yang mengikuti data dari endpoint API. */
public class DashboardPanel extends JPanel {
    private final JLabel title = new JLabel("Beranda");
    private final JLabel status = new JLabel(" ");
    private final JPanel statistics = new JPanel(new GridLayout(1, 4, 10, 0));
    private final JTextArea announcements = textArea();
    private final JTextArea activities = textArea();
    private final JTextArea notifications = textArea();

    public DashboardPanel() {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JPanel heading = new JPanel(new BorderLayout());
        heading.add(title, BorderLayout.NORTH);
        heading.add(status, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);
        add(statistics, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Pengumuman", new JScrollPane(announcements));
        tabs.addTab("Aktivitas", new JScrollPane(activities));
        tabs.addTab("Notifikasi", new JScrollPane(notifications));
        tabs.setPreferredSize(new java.awt.Dimension(0, 230));
        add(tabs, BorderLayout.SOUTH);
    }

    public void showLoading() {
        status.setText("Mengambil ringkasan dari API...");
        statistics.removeAll();
        announcements.setText("");
        activities.setText("");
        notifications.setText("");
        statistics.revalidate();
        statistics.repaint();
    }

    public void showError(String message) {
        status.setText("Gagal memuat data: " + message);
        statistics.removeAll();
        announcements.setText("");
        activities.setText("");
        notifications.setText("");
        statistics.revalidate();
        statistics.repaint();
    }

    public void showData(Map<String, Object> home, User user) {
        Map<String, Object> summary = map(home.get("summary"));
        Map<String, Object> organization = map(summary.get("organization"));
        Map<String, Object> stats = map(summary.get("stats"));
        Map<String, Object> period = map(summary.get("period"));
        String organizationName = value(organization.get("name"), user.getOrganizationName());
        String region = user.getRegion().isEmpty()
                ? regionName(organization)
                : user.getRegion();
        title.setText(organizationName.isEmpty()
                ? "Beranda · " + region
                : "Beranda · " + organizationName + " · " + region);
        status.setText("Terhubung ke API · Data dari backend");

        statistics.removeAll();
        statistics.add(statistic("Warga terdaftar", count(stats.get("residents"))));
        statistics.add(statistic("Kepala keluarga", count(stats.get("families"))));
        statistics.add(statistic("Tagihan belum lunas", count(stats.get("unpaidInvoices"))));
        statistics.add(statistic("Saldo kas", currency(stats.get("cashBalance"))));
        statistics.revalidate();
        statistics.repaint();

        announcements.setText(formatItems(list(home.get("announcements")), "body", null));
        activities.setText(formatItems(list(summary.get("activities")), "detail", "actor"));
        notifications.setText("Belum dibaca: " + value(home.get("unreadCount"), "0") + "\n\n"
                + formatItems(list(home.get("notifications")), "message", null));
        announcements.setCaretPosition(0);
        activities.setCaretPosition(0);
        notifications.setCaretPosition(0);
        String periodName = value(period.get("name"), "");
        if (!periodName.isEmpty()) {
            status.setText("Terhubung ke API · Data dari backend · Periode " + periodName);
        }
    }

    private static JPanel statistic(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(4, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel caption = new JLabel(label);
        JLabel number = new JLabel(value);
        number.setFont(number.getFont().deriveFont(Font.BOLD, 20f));
        card.add(caption, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        return card;
    }

    private static JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return area;
    }

    private static String formatItems(List<?> items, String bodyKey, String fallbackKey) {
        if (items.isEmpty()) {
            return "Belum ada data.";
        }
        StringBuilder text = new StringBuilder();
        for (Object item : items) {
            Map<String, Object> values = map(item);
            if (text.length() > 0) {
                text.append("\n\n");
            }
            text.append(value(values.get("title"), "Informasi"));
            String body = value(values.get(bodyKey), "");
            if (body.isEmpty() && fallbackKey != null) {
                body = value(values.get(fallbackKey), "");
            }
            if (!body.isEmpty()) {
                text.append('\n').append(body);
            }
            String date = value(values.get("publishedAt"), value(values.get("createdAt"), ""));
            if (!date.isEmpty()) {
                text.append('\n').append(date);
            }
        }
        return text.toString();
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            return java.util.Collections.emptyMap();
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() instanceof String) {
                result.put((String) entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static List<?> list(Object value) {
        return value instanceof List ? (List<?>) value : java.util.Collections.emptyList();
    }

    private static String value(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            double number = ((Number) value).doubleValue();
            if (number == Math.rint(number)) {
                return Long.toString((long) number);
            }
        }
        return String.valueOf(value);
    }

    private static String currency(Object value) {
        if (!(value instanceof Number)) {
            return "—";
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(((Number) value).doubleValue());
    }

    private static String count(Object value) {
        return value instanceof Number
                ? String.valueOf(((Number) value).intValue())
                : "—";
    }

    private static String regionName(Map<String, Object> organization) {
        String rt = value(organization.get("rtNumber"), "");
        String rw = value(organization.get("rwNumber"), "");
        if (!rt.isEmpty() && !rw.isEmpty()) {
            return "RT " + rt + " / RW " + rw;
        }
        if (!rw.isEmpty()) {
            return "RW " + rw;
        }
        return "Semua wilayah";
    }
}
