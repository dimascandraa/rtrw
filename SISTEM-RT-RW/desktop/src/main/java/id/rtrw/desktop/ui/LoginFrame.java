package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiException;
import id.rtrw.desktop.config.AppConfig;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.concurrent.ExecutionException;

/** Halaman login: email/telepon dan kata sandi, ke POST /api/v1/auth/login. */
public final class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String REMEMBERED_LOGIN_KEY = "rememberedLoginIdentifier";

    private final AppConfig config;
    private final ApiClient api;
    private final String notice;

    private final JTextField serverField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JCheckBox rememberLogin = new JCheckBox("Ingat saya");
    private final JLabel message = new JLabel(" ");
    private final FlatButton loginButton = new FlatButton("Masuk", FlatButton.Kind.PRIMARY);

    public LoginFrame(AppConfig config, ApiClient api, String notice) {
        super("Masuk - Sistem Informasi RT/RW");
        this.config = config;
        this.api = api;
        this.notice = notice;
        serverField.setText(config.baseUrl());
        loadRememberedLogin();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 580);
        setMinimumSize(new Dimension(820, 540));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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
        FlatButton theme = new FlatButton(Theme.isDark() ? "\u2600 Terang" : "\u263E Gelap",
                FlatButton.Kind.GHOST);
        theme.addActionListener(e -> toggleTheme());
        top.add(theme);
        right.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 0, 0, 0));
        form.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        form.add(left(UiKit.label("Masuk", 34f, true, Theme.text())));
        form.add(Box.createVerticalStrut(4));
        form.add(left(UiKit.label("Gunakan email atau nomor telepon yang terdaftar.",
                16f, false, Theme.muted())));
        form.add(Box.createVerticalStrut(28));

        JPanel serverSettings = new JPanel();
        serverSettings.setOpaque(false);
        serverSettings.setLayout(new BoxLayout(serverSettings, BoxLayout.Y_AXIS));
        serverSettings.setAlignmentX(LEFT_ALIGNMENT);
        serverSettings.add(left(UiKit.label("Alamat server", 13f, true, Theme.text())));
        serverSettings.add(Box.createVerticalStrut(6));
        UiKit.styleField(serverField);
        serverSettings.add(left(serverField));
        serverSettings.add(Box.createVerticalStrut(18));
        serverSettings.setVisible(false);
        form.add(serverSettings);

        form.add(left(UiKit.label("Email atau telepon", 14f, true, Theme.text())));
        form.add(Box.createVerticalStrut(8));
        UiKit.styleField(idField);
        idField.setFont(Theme.font(16f, false));
        form.add(left(idField));
        form.add(Box.createVerticalStrut(20));

        form.add(left(UiKit.label("Kata sandi", 14f, true, Theme.text())));
        form.add(Box.createVerticalStrut(8));
        UiKit.styleField(passwordField);
        passwordField.setFont(Theme.font(16f, false));
        form.add(left(passwordField));

        rememberLogin.setOpaque(false);
        rememberLogin.setForeground(Theme.text());
        rememberLogin.setFont(Theme.font(13f, false));
        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.setAlignmentX(LEFT_ALIGNMENT);
        options.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        options.add(rememberLogin, BorderLayout.WEST);
        FlatButton forgotPassword = new FlatButton("Lupa kata sandi?", FlatButton.Kind.GHOST);
        forgotPassword.setFont(Theme.font(13f, false));
        forgotPassword.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Untuk mengatur ulang kata sandi, silakan hubungi Admin RT/RW.",
                "Lupa kata sandi", JOptionPane.INFORMATION_MESSAGE));
        options.add(forgotPassword, BorderLayout.EAST);
        form.add(Box.createVerticalStrut(12));
        form.add(options);
        form.add(Box.createVerticalStrut(8));
        message.setFont(Theme.font(12f, false));
        form.add(left(message));
        form.add(Box.createVerticalStrut(10));

        loginButton.setPreferredSize(new Dimension(100, 60));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        loginButton.addActionListener(e -> doLogin());
        form.add(left(loginButton));
        form.add(Box.createVerticalStrut(18));
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setOpaque(false);
        registerRow.setAlignmentX(LEFT_ALIGNMENT);
        registerRow.add(UiKit.label("Belum punya akun?", 13f, false, Theme.muted()));
        FlatButton registerButton = new FlatButton("Daftar akun warga", FlatButton.Kind.GHOST);
        registerButton.setFont(Theme.font(13f, true));
        registerButton.addActionListener(e -> openRegistration());
        registerRow.add(registerButton);
        form.add(registerRow);

        FlatButton serverToggle = new FlatButton("Ubah alamat server", FlatButton.Kind.GHOST);
        serverToggle.setFont(Theme.font(11f, false));
        serverToggle.setAlignmentX(CENTER_ALIGNMENT);
        serverToggle.addActionListener(e -> {
            serverSettings.setVisible(!serverSettings.isVisible());
            serverToggle.setText(serverSettings.isVisible() ? "Sembunyikan alamat server" : "Ubah alamat server");
            form.revalidate();
            form.repaint();
        });
        form.add(Box.createVerticalStrut(8));
        form.add(serverToggle);

        JPanel centered = new JPanel(new GridBagLayout());
        centered.setOpaque(false);
        GridBagConstraints formConstraints = new GridBagConstraints();
        formConstraints.gridx = 0;
        formConstraints.gridy = 0;
        formConstraints.fill = GridBagConstraints.HORIZONTAL;
        formConstraints.weightx = 1;
        formConstraints.insets = new Insets(0, 40, 0, 40);
        centered.add(form, formConstraints);
        right.add(centered, BorderLayout.CENTER);
        return right;
    }

    private static <T extends javax.swing.JComponent> T left(T component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        if (component instanceof JTextField) {
            Dimension preferred = component.getPreferredSize();
            component.setPreferredSize(new Dimension(preferred.width, 52));
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
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
        next.rememberLogin.setSelected(rememberLogin.isSelected());
        next.setBounds(getBounds());
        next.setExtendedState(getExtendedState());
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
                api.currentUser();
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
                    try {
                        api.logoutServer();
                    } catch (ApiException ex) {
                        api.logout();
                    }
                    showMessage("Peran \"" + role + "\" tidak diizinkan menggunakan aplikasi desktop.", true);
                    return;
                }
                saveRememberedLogin(identifier);
                try {
                    config.save();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Login berhasil, tetapi alamat server tidak dapat disimpan. "
                                    + "Alamat ini hanya berlaku sampai aplikasi ditutup.\n\n"
                                    + ex.getMessage(),
                            "Konfigurasi tidak tersimpan", JOptionPane.WARNING_MESSAGE);
                }
                new MainFrame(config, api, "beranda").setVisible(true);
                dispose();
            }
        }.execute();
    }

    private void loadRememberedLogin() {
        try {
            String remembered = Preferences.userNodeForPackage(LoginFrame.class)
                    .get(REMEMBERED_LOGIN_KEY, "");
            if (!remembered.isEmpty()) {
                idField.setText(remembered);
                rememberLogin.setSelected(true);
            }
        } catch (RuntimeException ex) {
            rememberLogin.setEnabled(false);
            rememberLogin.setToolTipText("Preferensi tidak dapat diakses: " + ex.getMessage());
        }
    }

    private void saveRememberedLogin(String identifier) {
        try {
            Preferences preferences = Preferences.userNodeForPackage(LoginFrame.class);
            if (rememberLogin.isSelected()) {
                preferences.put(REMEMBERED_LOGIN_KEY, identifier);
            } else {
                preferences.remove(REMEMBERED_LOGIN_KEY);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Login berhasil, tetapi pilihan \"Ingat saya\" tidak dapat disimpan.\n" + ex.getMessage(),
                    "Preferensi tidak tersimpan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openRegistration() {
        String server = serverField.getText().trim();
        if (server.isEmpty()) {
            showMessage("Alamat server wajib diisi sebelum mendaftar.", true);
            return;
        }
        api.setBaseUrl(server);

        JDialog dialog = new JDialog(this, "Pendaftaran Akun Warga", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new AccountRegistrationPanel(api));
        dialog.setMinimumSize(new Dimension(620, 430));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ------------------------------------------------------------------ panel merek

    /** Panel kiri menampilkan identitas dan pesan utama sesuai rancangan UI/UX. */
    private static final class BrandPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.brand());
            g2.fillRect(0, 0, getWidth(), getHeight());

            int x = 60;
            g2.setColor(Theme.accent());
            g2.fillRoundRect(x, 62, 50, 50, 12, 12);
            g2.setColor(Theme.brand());
            g2.setFont(Theme.font(16f, true));
            g2.drawString("RW", x + 10, 93);

            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(17f, true));
            g2.drawString("Sistem Informasi RT/RW", x + 65, 94);

            float sloganSize = Math.min(74f, Math.max(42f, getHeight() * 0.081f));
            int lineHeight = Math.round(sloganSize * 1.04f);
            int sloganY = Math.round(getHeight() * 0.375f);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(sloganSize, true));
            g2.drawString("Satu", x, sloganY);
            g2.drawString("lingkungan,", x, sloganY + lineHeight);
            g2.drawString("satu data.", x, sloganY + lineHeight * 2);

            float descriptionSize = Math.min(19f, Math.max(14f, getWidth() * 0.02f));
            int descriptionY = sloganY + lineHeight * 2 + Math.round(sloganSize * 0.55f);
            int descriptionLineHeight = Math.round(descriptionSize * 1.55f);
            g2.setColor(new Color(0xC6DDD8));
            g2.setFont(Theme.font(descriptionSize, false));
            if (getWidth() < 650) {
                g2.drawString("Kelola data warga, iuran, surat,", x, descriptionY);
                g2.drawString("dan kegiatan lingkungan dalam", x, descriptionY + descriptionLineHeight);
                g2.drawString("satu tempat.", x, descriptionY + descriptionLineHeight * 2);
            } else {
                g2.drawString("Kelola data warga, iuran, surat, dan kegiatan lingkungan", x, descriptionY);
                g2.drawString("dalam satu tempat.", x, descriptionY + descriptionLineHeight);
            }

            int baseline = getHeight() - Math.max(36, Math.round(getHeight() * 0.059f));
            float barScale = Math.min(1f, getHeight() / 909f);
            int[] widths = {55, 55, 55, 55, 55};
            int[] heights = {56, 91, 112, 74, 101};
            Color[] colors = {
                new Color(0x3B6C61), new Color(0x4A8377), Theme.accent(),
                new Color(0x4A8377), new Color(0x3B6C61)
            };
            int barX = x;
            for (int i = 0; i < widths.length; i++) {
                int barHeight = Math.round(heights[i] * barScale);
                g2.setColor(colors[i]);
                g2.fillRoundRect(barX, baseline - barHeight, widths[i], barHeight, 10, 10);
                g2.fillRect(barX, baseline - 8, widths[i], 8);
                barX += widths[i] + 8;
            }
            g2.dispose();
        }
    }
}
