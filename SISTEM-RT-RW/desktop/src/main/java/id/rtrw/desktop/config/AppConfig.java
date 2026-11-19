package id.rtrw.desktop.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * Konfigurasi aplikasi dari berkas config.properties (di folder tempat aplikasi
 * dijalankan, atau path lain lewat -Drtrw.config=...).
 *
 * Berkas ini hanya untuk pengaturan non-rahasia seperti alamat server.
 * Jangan menyimpan password atau token di sini.
 */
public final class AppConfig {

    public static final String DEFAULT_BASE_URL = "http://localhost:8000";

    private final Properties props = new Properties();
    private final Path file;

    private AppConfig(Path file) {
        this.file = file;
    }

    public static AppConfig load() {
        Path path = Paths.get(System.getProperty("rtrw.config", "config.properties"));
        AppConfig config = new AppConfig(path);
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config.props.load(reader);
            } catch (IOException e) {
                // berkas tidak terbaca: pakai nilai bawaan
            }
        }
        return config;
    }

    /** Konfigurasi sementara tanpa berkas (untuk pengujian). */
    public static AppConfig inMemory(String baseUrl) {
        AppConfig config = new AppConfig(null);
        config.props.setProperty("api.base.url", baseUrl);
        return config;
    }

    /** Alamat server tanpa garis miring di akhir, contoh http://localhost:8000. */
    public String baseUrl() {
        return props.getProperty("api.base.url", DEFAULT_BASE_URL).trim();
    }

    public void setBaseUrl(String url) {
        props.setProperty("api.base.url", url == null ? DEFAULT_BASE_URL : url.trim());
    }

    public int timeoutSeconds() {
        try {
            return Math.max(3, Integer.parseInt(props.getProperty("api.timeout.seconds", "15").trim()));
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    /** Nama kolom untuk email/telepon pada body POST /api/login. */
    public String loginField() {
        return props.getProperty("api.login.field", "email").trim();
    }

    /** Peran yang diarahkan ke aplikasi Mobile dan tidak boleh memakai desktop. */
    public Set<String> blockedRoles() {
        Set<String> roles = new HashSet<>();
        for (String r : props.getProperty("desktop.blocked.roles", "warga").split(",")) {
            if (!r.trim().isEmpty()) {
                roles.add(r.trim().toLowerCase(Locale.ROOT));
            }
        }
        return roles;
    }

    public boolean isRoleBlocked(String role) {
        return role != null && blockedRoles().contains(role.trim().toLowerCase(Locale.ROOT));
    }

    public void save() throws IOException {
        if (file == null) {
            throw new IOException("Berkas konfigurasi tidak tersedia.");
        }
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            props.store(writer, "Konfigurasi Aplikasi RT/RW Desktop. Jangan simpan password atau token di sini.");
        }
    }
}
