package ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Panel sementara untuk modul yang belum dikerjakan. Ganti dengan panel asli per fitur. */
public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(String title) {
        super(new BorderLayout());
        JLabel label = new JLabel("<html><center>" + title + "<br><small>(belum dikerjakan)</small></center></html>",
                SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 20f));
        label.setForeground(Color.GRAY);
        add(label, BorderLayout.CENTER);
    }
}
