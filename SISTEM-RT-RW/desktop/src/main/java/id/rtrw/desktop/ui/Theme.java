package id.rtrw.desktop.ui;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Color;
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

    private static void apply() {
        UIManager.put("control", bg());
        UIManager.put("info", surface());
        UIManager.put("nimbusBase", dark ? rgb(0x2F6F55) : rgb(0x1F6B4A));
        UIManager.put("nimbusBlueGrey", dark ? rgb(0x3A4A42) : rgb(0xAAB8B0));
        UIManager.put("nimbusLightBackground", surface());
        UIManager.put("nimbusSelectionBackground", primary());
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusOrange", primary()); // warna isi progress bar
        UIManager.put("nimbusFocus", accent());
        UIManager.put("nimbusDisabledText", muted());
        UIManager.put("text", text());
        UIManager.put("Table.alternateRowColor", stripe());
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            // tetap memakai tampilan bawaan Java
        }
    }

    private static Color rgb(int value) {
        return new Color(value);
    }

    public static Color bg() {
        return dark ? rgb(0x121A16) : rgb(0xF3F6F4);
    }

    public static Color surface() {
        return dark ? rgb(0x1B2620) : Color.WHITE;
    }

    public static Color stripe() {
        return dark ? rgb(0x202D26) : rgb(0xF6F9F7);
    }

    public static Color header() {
        return dark ? rgb(0x26352D) : rgb(0xE6EEE9);
    }

    public static Color text() {
        return dark ? rgb(0xE6EEE9) : rgb(0x1F2937);
    }

    public static Color muted() {
        return dark ? rgb(0x9DB0A6) : rgb(0x6B7280);
    }

    public static Color border() {
        return dark ? rgb(0x2C3A33) : rgb(0xDCE3DF);
    }

    public static Color primary() {
        return dark ? rgb(0x3E9B72) : rgb(0x1F6B4A);
    }

    public static Color accent() {
        return rgb(0xF2B705);
    }

    public static Color sidebar() {
        return dark ? rgb(0x0C1411) : rgb(0x1B4D38);
    }

    public static Color sidebarText() {
        return rgb(0xE8F1EC);
    }

    public static Color success() {
        return dark ? rgb(0x5BC08E) : rgb(0x1E8E57);
    }

    public static Color warning() {
        return dark ? rgb(0xF2C94C) : rgb(0xB7791F);
    }

    public static Color danger() {
        return dark ? rgb(0xF2766B) : rgb(0xC53030);
    }
}
