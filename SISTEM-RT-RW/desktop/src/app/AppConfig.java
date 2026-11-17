package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Membaca konfigurasi dari file config.properties (di folder desktop/).
 * File itu TIDAK di-commit. Salin dari config.example.properties.
 */
public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = new FileInputStream("config.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
            System.out.println("[Config] config.properties tidak ditemukan, memakai nilai bawaan.");
        }
    }

    private AppConfig() {}

    /** URL dasar backend, tanpa garis miring di akhir. */
    public static String baseUrl() {
        String url = PROPS.getProperty("api.base.url", "http://localhost/SISTEM-RT-RW/backend").trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** true = pakai data tiruan, tidak memanggil backend. */
    public static boolean mockMode() {
        return Boolean.parseBoolean(PROPS.getProperty("mock.mode", "true").trim());
    }
}
