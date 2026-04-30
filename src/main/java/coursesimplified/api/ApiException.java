package coursesimplified.api;

public class ApiException extends RuntimeException {
    private final int httpStatus;

    public ApiException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = -1;
    }

    public int getHttpStatus() { return httpStatus; }
}
