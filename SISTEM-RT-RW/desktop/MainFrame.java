package ui;

import api.ApiException;
import api.AuthApi;
import api.DashboardApi;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Jendela utama; beranda dan data ringkasnya dimuat dari REST API. */
public class MainFrame extends JFrame {
    private final User user;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final DashboardPanel dashboard = new DashboardPanel();
    private final JButton logoutButton = new JButton("Keluar");

    public MainFrame(User user) {
        super("Sistem Informasi RT/RW");
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        content.add(dashboard, "Dashboard");
        content.add(new PlaceholderPanel("Data Warga"), "Data Warga");
        content.add(new PlaceholderPanel("Iuran"), "Iuran");
        content.add(new PlaceholderPanel("Pembayaran"), "Pembayaran");
        content.add(new PlaceholderPanel("Pengumuman"), "Pengumuman");
        content.add(new PlaceholderPanel("Laporan"), "Laporan");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        refreshDashboard();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        String identity = "Halo, " + user.getName() + " (" + user.getRole() + ")";
        if (!user.getRegion().isEmpty()) {
            identity += " · " + user.getRegion();
        }
        header.add(new JLabel(identity), BorderLayout.WEST);

        logoutButton.addActionListener(event -> logout());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.add(logoutButton);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        addNavigationButton(sidebar, "Dashboard");
        addNavigationButton(sidebar, "Data Warga");
        addNavigationButton(sidebar, "Iuran");
        addNavigationButton(sidebar, "Pembayaran");
        addNavigationButton(sidebar, "Pengumuman");
        addNavigationButton(sidebar, "Laporan");
        sidebar.add(Box.createVerticalGlue());
        sidebar.setPreferredSize(new Dimension(180, 0));
        return sidebar;
    }

    private void addNavigationButton(JPanel sidebar, String name) {
        JButton button = new JButton(name);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.addActionListener(event -> {
            cards.show(content, name);
            if ("Dashboard".equals(name)) {
                refreshDashboard();
            }
        });
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private void refreshDashboard() {
        dashboard.showLoading();
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return DashboardApi.loadHome();
            }

            @Override
            protected void done() {
                try {
                    dashboard.showData(get(), user);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    dashboard.showError("Pemuatan dashboard dibatalkan.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    dashboard.showError(cause instanceof ApiException
                            ? cause.getMessage()
                            : "Tidak dapat memuat dashboard: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void logout() {
        int confirmed = JOptionPane.showConfirmDialog(
                this, "Yakin ingin keluar?", "Keluar", JOptionPane.YES_NO_OPTION);
        if (confirmed != JOptionPane.YES_OPTION) {
            return;
        }

        logoutButton.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                AuthApi.logout();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    new LoginFrame().setVisible(true);
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logoutFailed("Proses keluar dibatalkan.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    logoutFailed(cause instanceof ApiException
                            ? cause.getMessage()
                            : "Tidak dapat keluar: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void logoutFailed(String message) {
        logoutButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, message, "Gagal keluar", JOptionPane.ERROR_MESSAGE);
    }
}
