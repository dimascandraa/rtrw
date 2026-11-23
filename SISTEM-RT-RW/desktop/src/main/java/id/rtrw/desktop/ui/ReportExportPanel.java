package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.util.XlsxExporter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

final class ReportExportPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 100;
    private static final int MAX_OFFSET = 1_000_000;

    private final ApiClient api;
    private final SessionListener listener;
    private final JTextField from = new JTextField(10);
    private final JTextField to = new JTextField(10);
    private final JLabel status = UiKit.label("Pilih rentang tanggal untuk mengambil laporan.", 12f, false, Theme.muted());
    private final FlatButton load = new FlatButton("Muat Laporan", FlatButton.Kind.NEUTRAL);
    private final FlatButton xlsx = new FlatButton("Unduh XLSX", FlatButton.Kind.PRIMARY);
    private final FlatButton pdf = new FlatButton("Cetak / Simpan PDF", FlatButton.Kind.NEUTRAL);
    private Map<String, List<Map<String, Object>>> reports = new LinkedHashMap<>();
    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean loading;

    ReportExportPanel(ApiClient api, SessionListener listener) {
        this.api = api;
        this.listener = listener;
        LocalDate today = LocalDate.now();
        from.setText(today.withDayOfMonth(1).toString());
        to.setText(today.toString());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(28, 32, 24, 32));
        add(UiKit.label("Laporan Keuangan", 24f, true, Theme.text()));
        add(Box.createVerticalStrut(6));
        add(UiKit.label("Kas, tagihan, dan pembayaran sesuai izin serta wilayah akun Anda.",
                13f, false, Theme.muted()));
        add(Box.createVerticalStrut(16));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(UiKit.label("Dari (YYYY-MM-DD)", 12f, true, Theme.text()));
        UiKit.styleField(from);
        filters.add(from);
        filters.add(UiKit.label("Sampai", 12f, true, Theme.text()));
        UiKit.styleField(to);
        filters.add(to);
        load.addActionListener(event -> refresh());
        xlsx.addActionListener(event -> exportXlsx());
        pdf.addActionListener(event -> printPdf());
        xlsx.setEnabled(false);
        pdf.setEnabled(false);
        filters.add(load);
        filters.add(xlsx);
        filters.add(pdf);
        add(filters);
        add(Box.createVerticalStrut(12));

        status.setAlignmentX(LEFT_ALIGNMENT);
        add(status);
        add(Box.createVerticalStrut(12));
        add(UiKit.label("Ekspor Excel menghasilkan workbook XLSX dengan sheet Kas, Tagihan, dan Pembayaran.",
                12f, false, Theme.muted()));
        add(Box.createVerticalStrut(4));
        add(UiKit.label("Untuk PDF, pilih printer PDF pada dialog cetak yang dibuka oleh sistem.",
                12f, false, Theme.muted()));
    }

    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        try {
            fromDate = LocalDate.parse(from.getText().trim());
            toDate = LocalDate.parse(to.getText().trim());
            if (fromDate.isAfter(toDate)) {
                throw new DateTimeParseException("Rentang tanggal tidak valid.", from.getText(), 0);
            }
        } catch (DateTimeParseException ex) {
            status.setForeground(Theme.danger());
            status.setText("Tanggal harus valid dengan format YYYY-MM-DD dan tanggal awal tidak boleh setelah tanggal akhir.");
            return;
        }
        loading = true;
        load.setEnabled(false);
        xlsx.setEnabled(false);
        pdf.setEnabled(false);
        status.setForeground(Theme.muted());
        status.setText("Mengambil seluruh halaman data laporan dari API...");

        new SwingWorker<Map<String, List<Map<String, Object>>>, Void>() {
            @Override
            protected Map<String, List<Map<String, Object>>> doInBackground() throws Exception {
                Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
                result.put("Kas", loadRows("transaksi-kas", "tanggal_transaksi"));
                result.put("Tagihan", loadRows("tagihan", "tanggal_jatuh_tempo"));
                result.put("Pembayaran", loadRows("pembayaran", "dibayar_pada"));
                return result;
            }

            private List<Map<String, Object>> loadRows(String resource, String dateColumn) throws ApiException {
                List<Map<String, Object>> result = new ArrayList<>();
                int offset = 0;
                while (offset <= MAX_OFFSET) {
                    List<Map<String, Object>> page = ApiData.rows(api.resource(resource, PAGE_SIZE, offset));
                    for (Map<String, Object> row : page) {
                        if (isInRange(row.get(dateColumn))) {
                            result.add(row);
                        }
                    }
                    if (page.size() < PAGE_SIZE) {
                        return result;
                    }
                    offset += page.size();
                }
                throw new ApiException("Ekspor dihentikan karena jumlah data melewati batas aman.", 0);
            }

            private boolean isInRange(Object value) {
                if (value == null || value.toString().length() < 10) {
                    return false;
                }
                try {
                    LocalDate date = LocalDate.parse(value.toString().substring(0, 10));
                    return !date.isBefore(fromDate) && !date.isAfter(toDate);
                } catch (DateTimeParseException ex) {
                    return false;
                }
            }

            @Override
            protected void done() {
                loading = false;
                load.setEnabled(true);
                try {
                    reports = get();
                    int count = 0;
                    for (List<Map<String, Object>> rows : reports.values()) {
                        count += rows.size();
                    }
                    status.setForeground(Theme.success());
                    status.setText(count + " baris laporan dimuat untuk " + fromDate + " s.d. " + toDate + ".");
                    xlsx.setEnabled(true);
                    pdf.setEnabled(true);
                } catch (ExecutionException ex) {
                    reports.clear();
                    status.setForeground(Theme.danger());
                    status.setText(DataTablePanel.errorText(ex.getCause()));
                    xlsx.setEnabled(false);
                    pdf.setEnabled(false);
                    if (ex.getCause() instanceof ApiException && ((ApiException) ex.getCause()).isUnauthorized()) {
                        ReportExportPanel.this.listener.onSessionExpired(ex.getCause().getMessage());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    status.setForeground(Theme.danger());
                    status.setText("Pemuatan laporan terhenti.");
                }
            }
        }.execute();
    }

    private void exportXlsx() {
        if (reports.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Muat laporan terlebih dahulu.", "Ekspor XLSX",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan laporan Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Workbook Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("laporan-rtrw-" + LocalDate.now() + ".xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selected = chooser.getSelectedFile();
        File file = selected.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx")
                ? selected : new File(selected.getParentFile(), selected.getName() + ".xlsx");
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "Berkas \"" + file.getName() + "\" sudah ada. Timpa?", "Ekspor XLSX",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        List<XlsxExporter.Sheet> sheets = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : reports.entrySet()) {
            sheets.add(new XlsxExporter.Sheet(entry.getKey(), entry.getValue()));
        }
        try {
            Path path = file.toPath();
            XlsxExporter.write(path, sheets);
            status.setForeground(Theme.success());
            status.setText("Laporan XLSX disimpan: " + path.toAbsolutePath());
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan XLSX:\n" + ex.getMessage(),
                    "Ekspor XLSX", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printPdf() {
        if (reports.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Muat laporan terlebih dahulu.", "Cetak laporan",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextArea text = new JTextArea(reportText());
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
        try {
            text.print(new MessageFormat("Laporan Keuangan RT/RW"),
                    new MessageFormat("Halaman {0}"));
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan:\n" + ex.getMessage(),
                    "Cetak laporan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String reportText() {
        StringBuilder text = new StringBuilder("LAPORAN KEUANGAN RT/RW\n")
                .append("Periode: ").append(fromDate).append(" s.d. ").append(toDate)
                .append("\nDibuat: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .append("\n\n");
        for (Map.Entry<String, List<Map<String, Object>>> sheet : reports.entrySet()) {
            text.append("=== ").append(sheet.getKey()).append(" ===\n");
            if (sheet.getValue().isEmpty()) {
                text.append("Tidak ada data pada rentang tanggal ini.\n\n");
                continue;
            }
            for (Map<String, Object> row : sheet.getValue()) {
                List<String> cells = new ArrayList<>();
                for (Map.Entry<String, Object> cell : row.entrySet()) {
                    cells.add(cell.getKey() + ": " + ApiData.display(cell.getValue()));
                }
                text.append(String.join(" | ", cells)).append('\n');
            }
            text.append('\n');
        }
        return text.toString();
    }
}
