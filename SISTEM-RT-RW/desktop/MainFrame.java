package ui;

import app.AppConfig;
import model.User;
import util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

/** Jendela utama: header, menu samping, dan area konten (CardLayout). */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    public MainFrame(User user) {
        super("Sistem Informasi RT/RW" + (AppConfig.mockMode() ? " [MOCK]" : ""));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        add(buildHeader(user), BorderLayout.NORTH);
        add(buildSidebar(user), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildHeader(User user) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel who = new JLabel("Halo, " + user.getName() + "  (" + user.getRole() + ")");
        who.setFont(who.getFont().deriveFont(Font.BOLD));
        header.add(who, BorderLayout.WEST);

        JButton logout = new JButton("Keluar");
        logout.addActionListener(e -> logout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.add(logout);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar(User user) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        boolean first = true;
        for (String name : menuFor(user)) {
            content.add(new PlaceholderPanel(name), name); // TODO: ganti dengan panel asli per modul

            JButton b = new JButton(name);
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            b.addActionListener(e -> cards.show(content, name));
            side.add(b);
            side.add(Box.createVerticalStrut(6));

            if (first) { cards.show(content, name); first = false; }
        }
        side.setPreferredSize(new Dimension(180, 0));
        return side;
    }

    /**
     * Menu yang tampil menurut role.
     * TODO: sesuaikan dengan tab "Role/Hak Akses" di spreadsheet. Sementara semua role melihat semua menu.
     */
    private List<String> menuFor(User user) {
        return Arrays.asList("Dashboard", "Data Warga", "Iuran", "Pembayaran", "Pengumuman", "Laporan");
    }

    private void logout() {
        int ok = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?", "Keluar",
                JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            SessionManager.clear();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
