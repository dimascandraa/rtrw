package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

final class AccountSettingsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ApiClient api;
    private final SessionListener listener;
    private final Runnable profileUpdated;
    private final JLabel status = UiKit.label(" ", 12f, false, Theme.muted());

    AccountSettingsPanel(ApiClient api, SessionListener listener, Runnable profileUpdated) {
        this.api = api;
        this.listener = listener;
        this.profileUpdated = profileUpdated;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(UiKit.label("Profil & Keamanan", 24f, true, Theme.text()));
        add(Box.createVerticalStrut(6));
        add(UiKit.label("Perbarui informasi akun atau ganti kata sandi Anda.",
                13f, false, Theme.muted()));
        add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        FlatButton profile = new FlatButton("Ubah Profil", FlatButton.Kind.PRIMARY);
        FlatButton password = new FlatButton("Ganti Kata Sandi", FlatButton.Kind.NEUTRAL);
        profile.addActionListener(e -> editProfile());
        password.addActionListener(e -> changePassword());
        buttons.add(profile);
        buttons.add(password);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.setMaximumSize(new java.awt.Dimension(520, profile.getPreferredSize().height));
        add(buttons);
        add(Box.createVerticalStrut(14));
        status.setAlignmentX(LEFT_ALIGNMENT);
        add(status);
    }

    private void editProfile() {
        JTextField name = new JTextField(api.userName());
        JTextField email = new JTextField(api.userEmail());
        JTextField phone = new JTextField(api.userPhone());
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        addField(form, "Nama lengkap", name);
        addField(form, "Email", email);
        addField(form, "Nomor telepon", phone);
        int answer = JOptionPane.showConfirmDialog(this, form, "Ubah Profil",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name.getText().trim());
        body.put("email", email.getText().trim());
        body.put("phone", phone.getText().trim());
        runRequest(() -> api.updateProfile(body), "Menyimpan profil...", profileUpdated);
    }

    private void changePassword() {
        JPasswordField current = new JPasswordField();
        JPasswordField next = new JPasswordField();
        JPasswordField confirmation = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        addField(form, "Kata sandi saat ini", current);
        addField(form, "Kata sandi baru (minimal 8 karakter)", next);
        addField(form, "Ulangi kata sandi baru", confirmation);
        int answer = JOptionPane.showConfirmDialog(this, form, "Ganti Kata Sandi",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        char[] currentChars = current.getPassword();
        char[] nextChars = next.getPassword();
        char[] confirmChars = confirmation.getPassword();
        String currentValue = new String(currentChars);
        String nextValue = new String(nextChars);
        String confirmValue = new String(confirmChars);
        java.util.Arrays.fill(currentChars, '\0');
        java.util.Arrays.fill(nextChars, '\0');
        java.util.Arrays.fill(confirmChars, '\0');
        if (nextValue.length() < 8 || !nextValue.equals(confirmValue)) {
            setStatus("Kata sandi baru minimal 8 karakter dan harus sama dengan konfirmasi.", true);
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("currentPassword", currentValue);
        body.put("newPassword", nextValue);
        runRequest(() -> api.changePassword(body), "Memperbarui kata sandi...", null);
    }

    private void runRequest(DataTablePanel.ApiRequest request, String pending, Runnable onSuccess) {
        setStatus(pending, false);
        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return request.execute();
            }

            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Perubahan berhasil disimpan.", false);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof ApiException && ((ApiException) cause).isUnauthorized()) {
                        listener.onSessionExpired(cause.getMessage());
                    } else {
                        setStatus(DataTablePanel.errorText(cause), true);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setStatus("Permintaan dibatalkan.", true);
                }
            }
        }.execute();
    }

    private void setStatus(String text, boolean error) {
        status.setForeground(error ? Theme.danger() : Theme.muted());
        status.setText(text);
    }

    private static void addField(JPanel form, String label, JTextField field) {
        form.add(UiKit.label(label, 12f, true, Theme.text()));
        UiKit.styleField(field);
        form.add(field);
    }
}
