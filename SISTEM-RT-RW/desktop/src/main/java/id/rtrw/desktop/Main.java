package id.rtrw.desktop;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.config.AppConfig;
import id.rtrw.desktop.ui.LoginFrame;
import id.rtrw.desktop.ui.Theme;

import javax.swing.SwingUtilities;

/**
 * Titik masuk aplikasi desktop Sistem Informasi RT/RW (Java Swing).
 *
 * Opsi: --url=http://alamat-server  untuk mengganti alamat server tanpa mengubah config.properties.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            Theme.init();
            AppConfig config = AppConfig.load();
            for (String arg : args) {
                if (arg.startsWith("--url=")) {
                    config.setBaseUrl(arg.substring("--url=".length()));
                }
            }
            ApiClient api = new ApiClient(config);
            new LoginFrame(config, api, null).setVisible(true);
        });
    }
}
