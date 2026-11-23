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
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
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
    private final JTextField applicantName = new JTextField();
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

        JPanel form = UiKit.card();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));

        form.add(UiKit.label("Nama pemohon", 13f, true, Theme.text()));
        form.add(Box.createVerticalStrut(6));
        applicantName.setText(api.userName());
        applicantName.setEditable(false);
        applicantName.setToolTipText("Nama diambil dari akun warga yang sedang masuk.");
        UiKit.styleField(applicantName);
        applicantName.setFont(Theme.font(14f, false));
        form.add(left(applicantName));
        form.add(Box.createVerticalStrut(4));
        form.add(UiKit.label("Nama mengikuti akun warga yang sedang masuk.", 11f, false, Theme.muted()));
        form.add(Box.createVerticalStrut(18));

        form.add(UiKit.label("Surat yang dipilih", 13f, true, Theme.text()));
        form.add(Box.createVerticalStrut(6));
        type.setFont(Theme.font(14f, false));
        type.setForeground(Theme.text());
        type.setBackground(Theme.surface());
        type.setPreferredSize(new Dimension(320, 42));
        type.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        type.setEnabled(false);
        form.add(type);
        form.add(Box.createVerticalStrut(18));
        form.add(UiKit.label("Keperluan (maksimal 255 karakter)", 13f, true, Theme.text()));
        form.add(Box.createVerticalStrut(6));
        purpose.setFont(Theme.font(14f, false));
        purpose.setLineWrap(true);
        purpose.setWrapStyleWord(true);
        UiKit.styleField(purpose);
        JScrollPane purposeScroll = new JScrollPane(purpose);
        purposeScroll.setPreferredSize(new Dimension(560, 130));
        purposeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        purposeScroll.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.border(), 1, true));
        form.add(purposeScroll);
        form.add(Box.createVerticalStrut(14));

        status.setAlignmentX(LEFT_ALIGNMENT);
        form.add(status);
        form.add(Box.createVerticalStrut(8));
        submit.setAlignmentX(LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submit.setEnabled(false);
        submit.addActionListener(e -> submitRequest());
        form.add(submit);
        add(form);
    }

    private static <T extends javax.swing.JComponent> T left(T component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        if (component instanceof JTextField) {
            Dimension preferred = component.getPreferredSize();
            component.setPreferredSize(new Dimension(preferred.width, 42));
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
        return component;
    }

    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        submit.setEnabled(false);
        type.setEnabled(false);
        setStatus("Memuat jenis surat...", false);

        new SwingWorker<List<LetterType>, Void>() {
            @Override
            protected List<LetterType> doInBackground() throws Exception {
                List<LetterType> items = new ArrayList<>();
                for (Map<String, Object> row : ApiData.rows(api.resource("jenis-surat"))) {
                    Long id = positiveId(ApiData.firstValue(row, "id", "jenisSuratId", "jenis_surat_id"));
                    Object name = ApiData.firstValue(row, "nama", "nama_jenis_surat", "jenis_surat",
                            "name", "judul", "label");
                    if (id != null && name != null && !name.toString().trim().isEmpty()) {
                        items.add(new LetterType(id, name.toString().trim()));
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
                    type.addItem(new LetterType(0, "Pilih jenis surat..."));
                    for (LetterType item : items) {
                        type.addItem(item);
                    }
                    type.setEnabled(!items.isEmpty());
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
        if (selected == null || selected.id < 1 || text.isEmpty() || text.length() > 255) {
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

    private static Long positiveId(Object value) {
        if (value instanceof Number) {
            long id = ((Number) value).longValue();
            return id > 0 ? id : null;
        }
        if (value instanceof String) {
            try {
                long id = Long.parseLong((String) value);
                return id > 0 ? id : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
