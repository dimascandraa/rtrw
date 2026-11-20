package id.rtrw.desktop.api;

/** Kesalahan saat memanggil Backend API. statusCode 0 berarti belum sampai ke server. */
public class ApiException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }

    /** True bila server menolak kredensial atau sesi sudah tidak berlaku. */
    public boolean isUnauthorized() {
        return statusCode == 401;
    }
}
