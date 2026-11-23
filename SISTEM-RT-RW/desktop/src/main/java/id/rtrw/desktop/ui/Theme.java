package id.rtrw.desktop.ui;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Color;
import java.awt.Font;
import java.util.prefs.Preferences;

/**
 * Tema terang dan gelap (Daftar Fitur F-027). Warna utama hijau pinus dengan
 * aksen kuning, mengikuti dokumentasi UI/UX. Pilihan tema diingat antar sesi.
 */
public final class Theme {

    private static final String PREF_KEY = "darkMode";
    private static boolean dark;

    private Theme() {
    }

    public static void init() {
        try {
            dark = Preferences.userNodeForPackage(Theme.class).getBoolean(PREF_KEY, false);
        } catch (RuntimeException e) {
            dark = false;
        }
        apply();
    }

    public static boolean isDark() {
        return dark;
    }

    public static void toggle() {
        dark = !dark;
        try {
            Preferences.userNodeForPackage(Theme.class).putBoolean(PREF_KEY, dark);
        } catch (RuntimeException e) {
            // preferensi tidak tersimpan: tema tetap berlaku untuk sesi ini
        }
        apply();
    }

    public static Font font(float size, boolean bold) {
        Font font = new Font("Plus Jakarta Sans", bold ? Font.BOLD : Font.PLAIN, Math.round(size));
        if ("Dialog".equalsIgnoreCase(font.getFamily())) {
            font = new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, Math.round(size));
        }
        return font.deriveFont(bold ? Font.BOLD : Font.PLAIN, size);
    }

    private static void apply() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            // tetap memakai tampilan bawaan Java
        }
        Font regular = font(14f, false);
        Font bold = font(14f, true);
        UIManager.put("defaultFont", regular);
        UIManager.put("Button.font", bold);
        UIManager.put("ToggleButton.font", bold);
        UIManager.put("CheckBox.font", regular);
        UIManager.put("ComboBox.font", regular);
        UIManager.put("Label.font", regular);
        UIManager.put("Menu.font", regular);
        UIManager.put("MenuItem.font", regular);
        UIManager.put("OptionPane.buttonFont", bold);
        UIManager.put("OptionPane.messageFont", regular);
        UIManager.put("Panel.font", regular);
        UIManager.put("PasswordField.font", regular);
        UIManager.put("Table.font", regular);
        UIManager.put("TableHeader.font", bold);
        UIManager.put("TextArea.font", regular);
        UIManager.put("TextField.font", regular);
        UIManager.put("control", bg());
        UIManager.put("info", surface());
        UIManager.put("nimbusBase", primary());
        UIManager.put("nimbusBlueGrey", dark ? rgb(0x354640) : rgb(0xAAB5B1));
        UIManager.put("nimbusLightBackground", surface());
        UIManager.put("nimbusSelectionBackground", primary());
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusOrange", primary()); // warna isi progress bar
        UIManager.put("nimbusFocus", accent());
        UIManager.put("nimbusDisabledText", muted());
        UIManager.put("text", text());
        UIManager.put("Table.alternateRowColor", stripe());
    }

    private static Color rgb(int value) {
        return new Color(value);
    }

    public static Color bg() {
        return dark ? rgb(0x0E1714) : rgb(0xF2F3F2);
    }

    public static Color surface() {
        return dark ? rgb(0x182320) : Color.WHITE;
    }

    public static Color stripe() {
        return dark ? rgb(0x1D2A26) : rgb(0xFAFBFA);
    }

    public static Color header() {
        return dark ? rgb(0x243631) : rgb(0xE2E9E7);
    }

    public static Color text() {
        return dark ? rgb(0xF3F4EF) : rgb(0x10201C);
    }

    public static Color muted() {
        return dark ? rgb(0xA8BBC1) : rgb(0x64737B);
    }

    public static Color border() {
        return dark ? rgb(0x2B3C37) : rgb(0xD9DEDC);
    }

    public static Color primary() {
        return dark ? rgb(0x66A394) : rgb(0x31594F);
    }

    public static Color accent() {
        return rgb(0xDEAD43);
    }

    public static Color sidebar() {
        return dark ? rgb(0x17221F) : Color.WHITE;
    }

    public static Color sidebarText() {
        return dark ? rgb(0xA8BBC1) : rgb(0x49565D);
    }

    public static Color sidebarSelected() {
        return dark ? rgb(0x243631) : rgb(0xE1E9E6);
    }

    public static Color sidebarHover() {
        return dark ? rgb(0x1E2D28) : rgb(0xF0F3F2);
    }

    public static Color brand() {
        return rgb(0x214B40);
    }

    public static Color primaryText() {
        return dark ? rgb(0x10201C) : Color.WHITE;
    }

    public static Color success() {
        return dark ? rgb(0x66C49B) : rgb(0x24845C);
    }

    public static Color warning() {
        return dark ? rgb(0xE5BE62) : rgb(0xB17B19);
    }

    public static Color danger() {
        return dark ? rgb(0xF27C72) : rgb(0xBE3F3A);
    }
}
