package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.config.AppConfig;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/** Halaman login: email/telepon dan kata sandi, ke POST /api/login. */
public final class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppConfig config;
    private final ApiClient api;
    private final String notice;

    private final JTextField serverField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel message = new JLabel(" ");
    private final FlatButton loginButton = new FlatButton("Masuk", FlatButton.Kind.PRIMARY);

    public LoginFrame(AppConfig config, ApiClient api, String notice) {
        super("Masuk - Sistem Informasi RT/RW");
        this.config = config;
        this.api = api;
        this.notice = notice;
        serverField.setText(config.baseUrl());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 580);
        setMinimumSize(new Dimension(820, 540));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(new BrandPanel());
        root.add(buildForm());
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);

        if (notice != null) {
            showMessage(notice, false);
        }
    }

    // ------------------------------------------------------------------ susunan

    private JPanel buildForm() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.bg());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        top.setOpaque(false);
        FlatButton theme = new FlatButton(Theme.isDark() ? "Mode Terang" : "Mode Gelap", FlatButton.Kind.GHOST);
        theme.addActionListener(e -> toggleTheme());
        top.add(theme);
        right.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 56, 24, 56));

        form.add(left(UiKit.label("Masuk", 28f, true, Theme.text())));
        form.add(Box.createVerticalStrut(4));
        form.add(left(UiKit.label("Gunakan akun pengurus RT/RW Anda.", 13f, false, Theme.muted())));
        form.add(Box.createVerticalStrut(22));

        form.add(left(UiKit.label("Alamat server", 12f, true, Theme.text())));
        form.add(Box.createVerticalStrut(4));
        UiKit.styleField(serverField);
        form.add(left(serverField));
        form.add(Box.createVerticalStrut(12));

        form.add(left(UiKit.label("Email atau nomor telepon", 12f, true, Theme.text())));
        form.add(Box.createVerticalStrut(4));
        UiKit.styleField(idField);
        form.add(left(idField));
        form.add(Box.createVerticalStrut(12));

        form.add(left(UiKit.label("Kata sandi", 12f, true, Theme.text())));
        form.add(Box.createVerticalStrut(4));
        UiKit.styleField(passwordField);
        form.add(left(passwordField));
        form.add(Box.createVerticalStrut(10));

        message.setFont(message.getFont().deriveFont(12f));
        form.add(left(message));
        form.add(Box.createVerticalStrut(10));

        loginButton.setPreferredSize(new Dimension(100, 40));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.addActionListener(e -> doLogin());
        form.add(left(loginButton));
        form.add(Box.createVerticalStrut(14));
        form.add(left(UiKit.label("Lupa kata sandi? Hubungi Admin RT/RW.", 12f, false, Theme.muted())));

        JPanel centered = new JPanel(new BorderLayout());
        centered.setOpaque(false);
        centered.add(form, BorderLayout.CENTER);
        right.add(centered, BorderLayout.CENTER);
        return right;
    }

    private static <T extends javax.swing.JComponent> T left(T component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        if (component instanceof JTextField) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }
        return component;
    }

    private void showMessage(String text, boolean error) {
        message.setForeground(error ? Theme.danger() : Theme.muted());
        message.setText("<html>" + text.replace("<", "&lt;") + "</html>");
    }

    // ------------------------------------------------------------------ aksi

    private void toggleTheme() {
        Theme.toggle();
        LoginFrame next = new LoginFrame(config, api, notice);
        next.serverField.setText(serverField.getText());
        next.idField.setText(idField.getText());
        next.setBounds(getBounds());
        next.setVisible(true);
        dispose();
    }

    private void doLogin() {
        String identifier = idField.getText().trim();
        char[] chars = passwordField.getPassword();
        String password = new String(chars);
        Arrays.fill(chars, '\0');
        String server = serverField.getText().trim();

        if (server.isEmpty()) {
            showMessage("Alamat server wajib diisi.", true);
            return;
        }
        if (identifier.isEmpty() || password.isEmpty()) {
            showMessage("Isi email/telepon dan kata sandi.", true);
            return;
        }

        api.setBaseUrl(server);
        loginButton.setEnabled(false);
        showMessage("Menghubungi server...", false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                api.login(identifier, password);
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    get();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    showMessage(cause instanceof ApiException ? cause.getMessage()
                            : "Terjadi kesalahan: " + cause, true);
                    return;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (config.isRoleBlocked(api.userRole())) {
                    String role = api.userRole();
                    api.logout();
                    showMessage("Peran \"" + role + "\" memakai aplikasi Mobile. "
                            + "Aplikasi desktop khusus pengurus RT/RW.", true);
                    return;
                }
                config.save(); // simpan alamat server untuk pemakaian berikutnya
                new MainFrame(config, api, "beranda").setVisible(true);
                dispose();
            }
        }.execute();
    }

    // ------------------------------------------------------------------ panel merek

    /** Panel kiri bergradasi hijau berisi identitas aplikasi. */
    private static final class BrandPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        BrandPanel() {
            setBorder(BorderFactory.createEmptyBorder(40, 44, 40, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Theme.sidebar(), getWidth(), getHeight(), Theme.primary()));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval(-80, getHeight() - 220, 320, 320);
            g2.fillOval(getWidth() - 140, -90, 260, 260);

            int x = 44;
            g2.setColor(Theme.accent());
            g2.setFont(getFont().deriveFont(Font.BOLD, 44f));
            g2.drawString("RT/RW", x, 150);

            g2.setColor(Color.WHITE);
            g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
            g2.drawString("Sistem Informasi", x, 196);
            g2.drawString("RT/RW", x, 226);

            g2.setColor(new Color(255, 255, 255, 215));
            g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            g2.drawString("Kelola data warga, iuran, surat, dan", x, 272);
            g2.drawString("pengumuman lingkungan dalam satu", x, 294);
            g2.drawString("platform: Web, Mobile, dan Desktop.", x, 316);

            g2.setColor(new Color(255, 255, 255, 150));
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g2.drawString("Aplikasi desktop untuk pengurus", x, getHeight() - 44);
            g2.dispose();
        }
    }
}
