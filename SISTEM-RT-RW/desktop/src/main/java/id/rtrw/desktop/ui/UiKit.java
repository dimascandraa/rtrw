package id.rtrw.desktop.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Font;

/** Pembantu kecil untuk membuat komponen dengan gaya tema. */
final class UiKit {

    private UiKit() {
    }

    static JLabel label(String text, float size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    static void styleField(JTextComponent field) {
        field.setFont(field.getFont().deriveFont(14f));
        field.setForeground(Theme.text());
        field.setCaretColor(Theme.text());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border()),
                new EmptyBorder(7, 10, 7, 10)));
    }

    /** Panel permukaan (kartu) dengan garis tepi tipis. */
    static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border()),
                new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }
}
