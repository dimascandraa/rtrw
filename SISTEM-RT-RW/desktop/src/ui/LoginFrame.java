package ui;

import api.ApiException;
import api.AuthApi;
import app.AppConfig;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JButton loginButton = new JButton("Masuk");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        super("Sistem Informasi RT/RW - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUi();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Sistem Informasi RT/RW", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(title, c);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; form.add(new JLabel("Username"), c);
        c.gridx = 1; form.add(usernameField, c);
        c.gridy = 2; c.gridx = 0; form.add(new JLabel("Password"), c);
        c.gridx = 1; form.add(passwordField, c);

        c.gridy = 3; c.gridx = 0; c.gridwidth = 2;
        form.add(loginButton, c);

        statusLabel.setForeground(Color.RED);
        c.gridy = 4;
        form.add(statusLabel, c);

        if (AppConfig.mockMode()) {
            JLabel hint = new JLabel("<html><center>MODE MOCK aktif<br>admin / admin123</center></html>",
                    SwingConstants.CENTER);
            hint.setForeground(Color.GRAY);
            c.gridy = 5;
            form.add(hint, c);
        }

        setContentPane(form);
        getRootPane().setDefaultButton(loginButton); // tombol Enter = Masuk
        loginButton.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password wajib diisi");
            return;
        }

        setBusy(true);
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return AuthApi.login(username, password); // jalan di thread terpisah
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    new MainFrame(user).setVisible(true);
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    showError(cause instanceof ApiException
                            ? cause.getMessage()
                            : "Terjadi kesalahan: " + cause);
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        loginButton.setText(busy ? "Memproses..." : "Masuk");
        if (busy) statusLabel.setText(" ");
    }

    private void showError(String msg) {
        statusLabel.setText("<html><center>" + msg + "</center></html>");
        pack();
    }
}
