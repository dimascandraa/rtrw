package util;

import model.User;

/** Menyimpan token login dan data pengguna selama aplikasi berjalan. */
public final class SessionManager {
    private static String token;
    private static User user;

    private SessionManager() {}

    public static void start(String token, User user) {
        SessionManager.token = token;
        SessionManager.user = user;
    }

    public static void clear() {
        token = null;
        user = null;
    }

    public static boolean isLoggedIn() { return user != null; }
    public static String getToken() { return token; }
    public static User getUser() { return user; }
}
