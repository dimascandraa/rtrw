package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Membaca konfigurasi dari file config.properties (di folder desktop/).
 * File itu berisi URL API untuk server pengembangan.
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

    /** URL dasar API, tanpa garis miring di akhir. */
    public static String baseUrl() {
        String url = PROPS.getProperty(
                "api.base.url",
                "http://127.0.0.1:8000/backend/public/api/v1").trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
