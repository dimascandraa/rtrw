package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;
import id.rtrw.desktop.api.ApiException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

final class LetterRequestPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final class LetterType {
        final long id;
        final String name;

        LetterType(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final ApiClient api;
    private final SessionListener listener;
    private final JComboBox<LetterType> type = new JComboBox<>();
    private final JTextArea purpose = new JTextArea(5, 36);
    private final JLabel status = UiKit.label("Memuat jenis surat...", 12f, false, Theme.muted());
    private final FlatButton submit = new FlatButton("Kirim Pengajuan", FlatButton.Kind.PRIMARY);
    private boolean loading;

    LetterRequestPanel(ApiClient api, SessionListener listener) {
        this.api = api;
        this.listener = listener;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(UiKit.label("Pengajuan Surat Online", 24f, true, Theme.text()));
        add(Box.createVerticalStrut(6));
        add(UiKit.label("Pilih jenis surat aktif dan tuliskan keperluannya.",
                13f, false, Theme.muted()));
        add(Box.createVerticalStrut(20));

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(UiKit.label("Jenis surat", 12f, true, Theme.text()));
        type.setFont(type.getFont().deriveFont(14f));
        type.setForeground(Theme.text());
        type.setBackground(Theme.surface());
        form.add(type);
        form.add(UiKit.label("Keperluan (maksimal 255 karakter)", 12f, true, Theme.text()));
        purpose.setLineWrap(true);
        purpose.setWrapStyleWord(true);
        UiKit.styleField(purpose);
        form.add(new JScrollPane(purpose));
        form.setAlignmentX(LEFT_ALIGNMENT);
        form.setMaximumSize(new java.awt.Dimension(560, 260));
        add(form);
        add(Box.createVerticalStrut(12));

        status.setAlignmentX(LEFT_ALIGNMENT);
        add(status);
        add(Box.createVerticalStrut(8));
        submit.setAlignmentX(LEFT_ALIGNMENT);
        submit.setEnabled(false);
        submit.addActionListener(e -> submitRequest());
        add(submit);
    }

    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        submit.setEnabled(false);
        setStatus("Memuat jenis surat...", false);

        new SwingWorker<List<LetterType>, Void>() {
            @Override
            protected List<LetterType> doInBackground() throws Exception {
                List<LetterType> items = new ArrayList<>();
                for (Map<String, Object> row : ApiData.rows(api.resource("jenis-surat"))) {
                    Object id = ApiData.firstValue(row, "id");
                    Object name = ApiData.firstValue(row, "nama", "nama_jenis_surat", "name", "judul");
                    if (id instanceof Number && name != null) {
                        items.add(new LetterType(((Number) id).longValue(), name.toString()));
                    }
                }
                return items;
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    List<LetterType> items = get();
                    type.removeAllItems();
                    for (LetterType item : items) {
                        type.addItem(item);
                    }
                    submit.setEnabled(!items.isEmpty());
                    setStatus(items.isEmpty() ? "Belum ada jenis surat aktif di wilayah Anda." : " ",
                            items.isEmpty());
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    setStatus(DataTablePanel.errorText(cause), true);
                    expireSession(cause);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setStatus("Pemuatan dibatalkan.", true);
                }
            }
        }.execute();
    }

    private void submitRequest() {
        LetterType selected = (LetterType) type.getSelectedItem();
        String text = purpose.getText().trim();
        if (selected == null || text.isEmpty() || text.length() > 255) {
            setStatus("Pilih jenis surat dan isi keperluan (maksimal 255 karakter).", true);
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jenisSuratId", selected.id);
        body.put("keperluan", text);
        submit.setEnabled(false);
        setStatus("Mengirim pengajuan...", false);

        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return api.createLetterRequest(body);
            }

            @Override
            protected void done() {
                submit.setEnabled(true);
                try {
                    Object response = get();
                    Object id = ApiData.findMap(response, "data").get("id");
                    purpose.setText("");
                    setStatus(id instanceof Number
                            ? "Pengajuan surat terkirim. Nomor pengajuan: " + id + "."
                            : "Pengajuan surat berhasil dikirim.", false);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    setStatus(DataTablePanel.errorText(cause), true);
                    expireSession(cause);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setStatus("Pengiriman dibatalkan.", true);
                }
            }
        }.execute();
    }

    private void expireSession(Throwable cause) {
        if (cause instanceof ApiException && ((ApiException) cause).isUnauthorized()) {
            listener.onSessionExpired(cause.getMessage());
        }
    }

    private void setStatus(String text, boolean error) {
        status.setForeground(error ? Theme.danger() : Theme.muted());
        status.setText("<html>" + escape(text) + "</html>");
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
