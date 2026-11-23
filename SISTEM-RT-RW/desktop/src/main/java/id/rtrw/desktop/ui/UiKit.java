package id.rtrw.desktop.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;

/** Pembantu kecil untuk membuat komponen dengan gaya tema. */
final class UiKit {

    private UiKit() {
    }

    static JLabel label(String text, float size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(size, bold));
        label.setForeground(color);
        return label;
    }

    static void styleField(JTextComponent field) {
        field.setFont(Theme.font(14f, false));
        field.setForeground(Theme.text());
        field.setBackground(Theme.surface());
        field.setCaretColor(Theme.text());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border(), 1, true),
                new EmptyBorder(10, 14, 10, 14)));
    }

    /** Panel permukaan (kartu) dengan garis tepi tipis. */
    static JPanel card() {
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.surface());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
            }

            @Override
            protected void paintBorder(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.border());
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        return panel;
    }
}
