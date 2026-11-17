package api;

/** Error dari pemanggilan API (jaringan, status HTTP, atau format respons). */
public class ApiException extends Exception {
    private final int statusCode; // 0 jika bukan error HTTP

    public ApiException(String message) {
        this(message, 0);
    }

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }
}
