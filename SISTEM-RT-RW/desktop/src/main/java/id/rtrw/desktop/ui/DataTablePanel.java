package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.util.CsvExporter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Halaman tabel generik: memanggil satu endpoint GET, menampilkan hasilnya,
 * dan menyediakan pencarian, urut kolom, ekspor CSV, cetak, serta detail baris.
 *
 * Kolom dibuat otomatis dari kunci JSON, jadi tampilan tetap jalan walaupun
 * daftar kolom di backend berubah.
 */
class DataTablePanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final String[] HIDDEN_KEY_PARTS = {"password", "kata_sandi", "token", "hash", "secret"};
    private static final Map<String, String> ACRONYMS = new HashMap<>();

    static {
        for (String a : new String[] {"nik", "kk", "rt", "rw", "id", "qris", "ip", "hp", "pdf"}) {
            ACRONYMS.put(a, a.toUpperCase(Locale.ROOT));
        }
    }

    protected final ApiClient api;
    private final String title;
    private final String endpoint;
    private final SessionListener listener;

    private final DefaultTableModel model = new DefaultTableModel() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getPreferredSize().width < getParent().getWidth();
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField search = new JTextField(20);
    private final JLabel statusLabel = new JLabel(" ");
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final FlatButton reloadButton = new FlatButton("Muat Ulang", FlatButton.Kind.NEUTRAL);

    private List<Map<String, Object>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private boolean loading;

    DataTablePanel(ApiClient api, String title, String subtitle, String endpoint, SessionListener listener) {
        this.api = api;
        this.title = title;
        this.endpoint = endpoint;
        this.listener = listener;

        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(24, 28, 16, 28));

        add(buildHeader(subtitle), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        statusLabel.setForeground(Theme.muted());
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        add(statusLabel, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------ susunan

    private JPanel buildHeader(String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = UiKit.label(title, 24f, true, Theme.text());
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subLabel = UiKit.label(subtitle, 13f, false, Theme.muted());
        subLabel.setAlignmentX(LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(subLabel);
        header.add(Box.createVerticalStrut(14));

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(UiKit.label("Cari:", 13f, true, Theme.text()));
        UiKit.styleField(search);
        left.add(search);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        actions.setOpaque(false);
        reloadButton.addActionListener(e -> refresh());
        FlatButton exportButton = new FlatButton("Ekspor CSV", FlatButton.Kind.NEUTRAL);
        exportButton.addActionListener(e -> exportCsv());
        FlatButton printButton = new FlatButton("Cetak", FlatButton.Kind.NEUTRAL);
        printButton.addActionListener(e -> printTable());
        actions.add(reloadButton);
        actions.add(exportButton);
        actions.add(printButton);

        bar.add(left, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        header.add(bar);
        return header;
    }

    private JScrollPane buildTable() {
        table.setRowSorter(sorter);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.border());
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFont(table.getFont().deriveFont(13f));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showDetail(selectedRow());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.border()));
        scroll.getViewport().setBackground(Theme.surface());
        return scroll;
    }

    /** Menambah tombol aksi di sebelah kiri tombol bawaan (dipakai subkelas). */
    protected final void addAction(FlatButton button) {
        actions.add(button, 0);
    }

    // ------------------------------------------------------------------ data

    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        reloadButton.setEnabled(false);
        setStatus("Memuat data...", false);

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return ApiData.rows(api.get(endpoint));
            }

            @Override
            protected void done() {
                loading = false;
                reloadButton.setEnabled(true);
                try {
                    setRows(get());
                    setStatus(rows.size() + " data  |  diperbarui "
                            + new SimpleDateFormat("HH:mm:ss").format(new Date()), false);
                } catch (ExecutionException ex) {
                    setRows(new ArrayList<Map<String, Object>>());
                    setStatus(errorText(ex.getCause()), true);
                    handleSessionError(ex.getCause());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void setRows(List<Map<String, Object>> newRows) {
        this.rows = newRows;
        Set<String> keys = new LinkedHashSet<>();
        for (Map<String, Object> row : newRows) {
            for (String key : row.keySet()) {
                if (!isHidden(key)) {
                    keys.add(key);
                }
            }
        }
        this.columns = new ArrayList<>(keys);

        String[] headers = new String[columns.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = prettify(columns.get(i));
        }
        Object[][] data = new Object[newRows.size()][columns.size()];
        for (int r = 0; r < newRows.size(); r++) {
            for (int c = 0; c < columns.size(); c++) {
                data[r][c] = ApiData.display(newRows.get(r).get(columns.get(c)));
            }
        }
        model.setDataVector(data, headers);
        for (int c = 0; c < columns.size(); c++) {
            sorter.setComparator(c, SMART_COMPARATOR);
        }
        fitColumns();
        applyFilter();
    }

    private void fitColumns() {
        FontMetrics fm = table.getFontMetrics(table.getFont());
        int sample = Math.min(rows.size(), 60);
        for (int c = 0; c < table.getColumnCount(); c++) {
            int width = fm.stringWidth(String.valueOf(table.getColumnName(c))) + 44;
            for (int r = 0; r < sample; r++) {
                Object value = model.getValueAt(r, c);
                if (value != null) {
                    width = Math.max(width, fm.stringWidth(value.toString()) + 28);
                }
            }
            width = Math.max(70, Math.min(width, 340));
            TableColumn column = table.getColumnModel().getColumn(c);
            column.setPreferredWidth(width);
        }
    }

    private void applyFilter() {
        String text = search.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }

    /** Baris data yang sedang dipilih di tabel, atau null. */
    protected final Map<String, Object> selectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow < rows.size() ? rows.get(modelRow) : null;
    }

    // ------------------------------------------------------------------ aksi

    private void exportCsv() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor.", "Ekspor CSV",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Ekspor ke CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("Berkas CSV (*.csv)", "csv"));
        String slug = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        chooser.setSelectedFile(new File(slug + "-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "Berkas \"" + file.getName() + "\" sudah ada. Timpa?", "Ekspor CSV",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        List<String> headers = new ArrayList<>();
        for (int c = 0; c < table.getColumnCount(); c++) {
            headers.add(table.getColumnName(c));
        }
        List<List<String>> out = new ArrayList<>();
        for (int r = 0; r < table.getRowCount(); r++) { // urutan dan filter sesuai tampilan
            List<String> line = new ArrayList<>();
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object v = table.getValueAt(r, c);
                line.add(v == null ? "" : v.toString());
            }
            out.add(line);
        }
        try {
            CsvExporter.write(file.toPath(), headers, out);
            setStatus("Diekspor: " + file.getAbsolutePath(), false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan berkas:\n" + ex.getMessage(),
                    "Ekspor CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printTable() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak.", "Cetak",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            table.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(title), new MessageFormat("Halaman {0}"));
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak:\n" + ex.getMessage(), "Cetak",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetail(Map<String, Object> row) {
        if (row == null) {
            return;
        }
        int width = 0;
        for (String key : row.keySet()) {
            width = Math.max(width, prettify(key).length());
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (isHidden(e.getKey())) {
                continue;
            }
            String label = prettify(e.getKey());
            sb.append(label);
            for (int i = label.length(); i < width; i++) {
                sb.append(' ');
            }
            sb.append(" : ").append(ApiData.display(e.getValue())).append('\n');
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new java.awt.Dimension(520, 320));
        JOptionPane.showMessageDialog(this, scroll, "Detail " + title, JOptionPane.PLAIN_MESSAGE);
    }

    // ------------------------------------------------------------------ bantuan subkelas

    protected final void setStatus(String text, boolean error) {
        statusLabel.setForeground(error ? Theme.danger() : Theme.muted());
        statusLabel.setText(text);
    }

    /** POST di latar belakang. onSuccess dijalankan di EDT bila berhasil. */
    protected final void postAsync(String path, Object body, Runnable onSuccess) {
        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return api.post(path, body);
            }

            @Override
            protected void done() {
                try {
                    get();
                    onSuccess.run();
                } catch (ExecutionException ex) {
                    if (!handleSessionError(ex.getCause())) {
                        JOptionPane.showMessageDialog(DataTablePanel.this, errorText(ex.getCause()),
                                "Gagal", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    protected static String errorText(Throwable cause) {
        if (cause instanceof ApiException) {
            return cause.getMessage();
        }
        return "Terjadi kesalahan: " + (cause == null ? "tidak diketahui" : cause);
    }

    /** True bila kesalahan berupa sesi berakhir dan pengguna sudah diarahkan ke login. */
    protected final boolean handleSessionError(Throwable cause) {
        if (cause instanceof ApiException && ((ApiException) cause).isUnauthorized()) {
            SwingUtilities.invokeLater(() -> listener.onSessionExpired(cause.getMessage()));
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ format

    private static boolean isHidden(String key) {
        String k = key.toLowerCase(Locale.ROOT);
        for (String part : HIDDEN_KEY_PARTS) {
            if (k.contains(part)) {
                return true;
            }
        }
        return false;
    }

    static String prettify(String key) {
        StringBuilder sb = new StringBuilder();
        for (String word : key.split("[_\\s]+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            String acronym = ACRONYMS.get(word.toLowerCase(Locale.ROOT));
            if (acronym != null) {
                sb.append(acronym);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private static final Comparator<Object> SMART_COMPARATOR = (a, b) -> {
        String x = String.valueOf(a);
        String y = String.valueOf(b);
        try {
            return Double.compare(Double.parseDouble(x), Double.parseDouble(y));
        } catch (NumberFormatException ex) {
            return x.compareToIgnoreCase(y);
        }
    };

    private static Color statusColor(String columnKey, Object value) {
        if (value == null || !columnKey.toLowerCase(Locale.ROOT).contains("status")) {
            return null;
        }
        String s = value.toString().toLowerCase(Locale.ROOT);
        if (containsAny(s, "tolak", "tunggak", "gagal", "batal", "belum", "kedaluwarsa")) {
            return Theme.danger();
        }
        if (containsAny(s, "menunggu", "pending", "proses", "sebagian", "draft", "diajukan")) {
            return Theme.warning();
        }
        if (containsAny(s, "lunas", "selesai", "setuju", "terima", "verifikasi", "aktif", "terkirim",
                "berhasil", "tersedia")) {
            return Theme.success();
        }
        return null;
    }

    private static boolean containsAny(String s, String... parts) {
        for (String p : parts) {
            if (s.contains(p)) {
                return true;
            }
        }
        return false;
    }

    /** Header datar berwarna tema dengan penanda urutan kolom. */
    private final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focused, int row, int column) {
            super.getTableCellRendererComponent(t, value, false, false, row, column);
            String text = value == null ? "" : value.toString();
            List<? extends javax.swing.RowSorter.SortKey> keys = sorter.getSortKeys();
            if (!keys.isEmpty() && keys.get(0).getColumn() == t.convertColumnIndexToModel(column)) {
                text += keys.get(0).getSortOrder() == javax.swing.SortOrder.ASCENDING ? "  ▲" : "  ▼";
            }
            setText(text);
            setOpaque(true);
            setBackground(Theme.header());
            setForeground(Theme.text());
            setFont(t.getFont().deriveFont(Font.BOLD, 13f));
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                    new EmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    /** Sel bergaris-garis, status berwarna (hijau selesai, kuning menunggu, merah ditolak/menunggak). */
    private final class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focused, int row, int column) {
            super.getTableCellRendererComponent(t, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            int modelCol = t.convertColumnIndexToModel(column);
            String key = modelCol < columns.size() ? columns.get(modelCol) : "";
            Font base = t.getFont();
            if (selected) {
                setBackground(Theme.primary());
                setForeground(Color.WHITE);
                setFont(base);
            } else {
                setBackground(row % 2 == 0 ? Theme.surface() : Theme.stripe());
                Color status = statusColor(key, value);
                setForeground(status != null ? status : Theme.text());
                setFont(status != null ? base.deriveFont(Font.BOLD) : base);
            }
            return this;
        }
    }
}
