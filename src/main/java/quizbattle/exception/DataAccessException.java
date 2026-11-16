package quizbattle.exception;

/**
 * Custom unchecked exception to wrap checked SQLExceptions.
 * WHY: Keeps business logic and CLI clean from database-specific exceptions.
 */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
