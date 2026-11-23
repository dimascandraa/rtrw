package id.rtrw.desktop.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Tombol datar berwarna tema (digambar sendiri supaya konsisten di terang dan gelap). */
public final class FlatButton extends JButton {
    private static final long serialVersionUID = 1L;

    public enum Kind { PRIMARY, NEUTRAL, DANGER, GHOST }

    private final Kind kind;
    private boolean hover;

    public FlatButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Theme.font(13f, true));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 32, fm.getHeight() + 16);
    }

    private static Color mix(Color a, Color b, float ratio) {
        float r = 1f - ratio;
        return new Color(
                Math.round(a.getRed() * r + b.getRed() * ratio),
                Math.round(a.getGreen() * r + b.getGreen() * ratio),
                Math.round(a.getBlue() * r + b.getBlue() * ratio));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        Color fill;
        Color fg;
        Color line = null;

        if (!isEnabled()) {
            fill = Theme.border();
            fg = Theme.muted();
        } else if (kind == Kind.PRIMARY) {
            fill = hover ? mix(Theme.primary(), Color.WHITE, 0.14f) : Theme.primary();
            fg = Theme.primaryText();
        } else if (kind == Kind.DANGER) {
            fill = hover ? mix(Theme.danger(), Color.WHITE, 0.14f) : Theme.danger();
            fg = Color.WHITE;
        } else if (kind == Kind.GHOST) {
            fill = hover ? mix(Theme.surface(), Theme.primary(), 0.15f) : null;
            fg = Theme.text();
        } else {
            fill = hover ? mix(Theme.surface(), Theme.primary(), 0.12f) : Theme.surface();
            fg = Theme.text();
            line = Theme.border();
        }

        if (fill != null) {
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }
        if (line != null) {
            g2.setColor(line);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }

        g2.setFont(getFont());
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
