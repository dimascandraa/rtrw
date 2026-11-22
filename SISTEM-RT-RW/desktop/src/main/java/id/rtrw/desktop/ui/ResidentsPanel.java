package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Data Warga (F-004): daftar dari GET /api/residents dan form tambah warga
 * lewat POST /api/residents.
 */
final class ResidentsPanel extends DataTablePanel {
    private static final long serialVersionUID = 1L;

    /**
     * Isian form: {nama kolom JSON, label}. SESUAIKAN dengan dokumentasi
     * endpoint POST /api/residents di documentation/API/ bila nama kolomnya berbeda.
     */
    private static final String[][] FORM_FIELDS = {
        {"nik", "NIK (16 digit)"},
        {"nama", "Nama lengkap"},
        {"no_kk", "No. KK (16 digit)"},
        {"alamat", "Alamat"},
        {"telepon", "No. telepon"},
    };

    ResidentsPanel(ApiClient api, SessionListener listener) {
        super(api, "Data Warga", "Data warga dan kepala keluarga. Klik dua kali pada baris untuk melihat detail.",
                "/api/residents", listener);
        FlatButton add = new FlatButton("+ Tambah Warga", FlatButton.Kind.PRIMARY);
        add.addActionListener(e -> showAddDialog());
        addAction(add);
    }

    private void showAddDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tambah Warga", JDialog.ModalityType.APPLICATION_MODAL);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 8, 20));

        JTextField[] fields = new JTextField[FORM_FIELDS.length];
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < FORM_FIELDS.length; i++) {
            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            form.add(new JLabel(FORM_FIELDS[i][1]), gc);
            fields[i] = new JTextField(24);
            UiKit.styleField(fields[i]);
            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            form.add(fields[i], gc);
        }

        JLabel error = UiKit.label(" ", 12f, false, Theme.danger());
        FlatButton cancel = new FlatButton("Batal", FlatButton.Kind.NEUTRAL);
        FlatButton save = new FlatButton("Simpan", FlatButton.Kind.PRIMARY);
        cancel.addActionListener(e -> dialog.dispose());

        save.addActionListener(e -> {
            Map<String, Object> body = new LinkedHashMap<>();
            for (int i = 0; i < FORM_FIELDS.length; i++) {
                body.put(FORM_FIELDS[i][0], fields[i].getText().trim());
            }
            String problem = validate(body);
            if (problem != null) {
                error.setText(problem);
                return;
            }
            error.setText(" ");
            save.setEnabled(false);
            new SwingWorker<Object, Void>() {
                @Override
                protected Object doInBackground() throws Exception {
                    return api.post("/api/residents", body);
                }

                @Override
                protected void done() {
                    save.setEnabled(true);
                    try {
                        get();
                        dialog.dispose();
                        refresh();
                    } catch (ExecutionException ex) {
                        if (handleSessionError(ex.getCause())) {
                            dialog.dispose();
                        } else {
                            error.setText("<html>" + errorText(ex.getCause()) + "</html>");
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }.execute();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancel);
        buttons.add(save);
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setBorder(new EmptyBorder(0, 20, 16, 20));
        south.add(error, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);

        form.setOpaque(false);
        south.setOpaque(false);
        buttons.setOpaque(false);
        dialog.getContentPane().setBackground(Theme.bg());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(save);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /** Validasi dasar di sisi klien. Validasi utama tetap dilakukan backend. */
    static String validate(Map<String, Object> body) {
        String nama = String.valueOf(body.get("nama"));
        if (nama.isEmpty()) {
            return "Nama lengkap wajib diisi.";
        }
        if (!String.valueOf(body.get("nik")).matches("\\d{16}")) {
            return "NIK harus terdiri dari 16 digit angka.";
        }
        String kk = String.valueOf(body.get("no_kk"));
        if (!kk.isEmpty() && !kk.matches("\\d{16}")) {
            return "No. KK harus terdiri dari 16 digit angka.";
        }
        return null;
    }
}
