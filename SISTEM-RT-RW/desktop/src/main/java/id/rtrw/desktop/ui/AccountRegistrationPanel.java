package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

final class AccountRegistrationPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ApiClient api;
    private final JTextField name = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField phone = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JLabel status = UiKit.label(" ", 12f, false, Theme.muted());
    private final FlatButton submit = new FlatButton("Kirim Pendaftaran", FlatButton.Kind.PRIMARY);

    AccountRegistrationPanel(ApiClient api) {
        this.api = api;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.bg());
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(UiKit.label("Pendaftaran Akun Warga", 24f, true, Theme.text()));
        add(Box.createVerticalStrut(6));
        add(UiKit.label("Akun baru menunggu persetujuan dan pencocokan nomor telepon oleh pengurus.",
                13f, false, Theme.muted()));
        add(Box.createVerticalStrut(20));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setOpaque(false);
        addField(form, "Nama lengkap", name);
        addField(form, "Email (opsional)", email);
        addField(form, "Nomor telepon", phone);
        addField(form, "Kata sandi (minimal 8 karakter)", password);
        add(form);
        add(Box.createVerticalStrut(12));

        status.setAlignmentX(LEFT_ALIGNMENT);
        add(status);
        add(Box.createVerticalStrut(8));
        submit.setAlignmentX(LEFT_ALIGNMENT);
        submit.addActionListener(e -> submit());
        add(submit);
    }

    private static void addField(JPanel form, String label, JTextField field) {
        form.add(UiKit.label(label, 12f, true, Theme.text()));
        UiKit.styleField(field);
        form.add(field);
    }

    private void submit() {
        String nameValue = name.getText().trim();
        String emailValue = email.getText().trim();
        String phoneValue = phone.getText().trim();
        char[] chars = password.getPassword();
        String passwordValue = new String(chars);
        java.util.Arrays.fill(chars, '\0');

        if (nameValue.isEmpty() || phoneValue.isEmpty() || passwordValue.length() < 8) {
            status.setForeground(Theme.danger());
            status.setText("Nama, nomor telepon, dan kata sandi minimal 8 karakter wajib diisi.");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", nameValue);
        body.put("email", emailValue);
        body.put("phone", phoneValue);
        body.put("password", passwordValue);
        submit.setEnabled(false);
        status.setForeground(Theme.muted());
        status.setText("Mengirim pendaftaran...");

        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return api.registerAccount(body);
            }

            @Override
            protected void done() {
                submit.setEnabled(true);
                try {
                    get();
                    password.setText("");
                    status.setForeground(Theme.success());
                    status.setText("Pendaftaran terkirim. Tunggu persetujuan pengurus.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    status.setForeground(Theme.danger());
                    status.setText("<html>" + escape(cause instanceof ApiException
                            ? cause.getMessage() : "Gagal mengirim pendaftaran: " + cause) + "</html>");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
