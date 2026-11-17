package app;

import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Tidak dapat menggunakan tampilan sistem: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
