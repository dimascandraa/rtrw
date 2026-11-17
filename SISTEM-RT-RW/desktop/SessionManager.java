package util;

import model.User;

/** Menyimpan data pengguna selama aplikasi berjalan; cookie sesi dikelola ApiClient. */
public final class SessionManager {
    private static User user;

    private SessionManager() {}

    public static void start(User user) {
        SessionManager.user = user;
    }

    public static void clear() {
        user = null;
    }

    public static boolean isLoggedIn() { return user != null; }
    public static User getUser() { return user; }
}
