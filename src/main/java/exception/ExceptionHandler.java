package exception;

/**
 * Exception handler.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class ExceptionHandler extends Exception {

    /**
     * Error code.
     */
    private final int code;

    /**
     * Error message.
     */
    private final String message;

    /**
     * ExceptionHandler constructor.
     *
     * @param error Error
     */
    public ExceptionHandler(final Error error) {
        code = error.getCode();
        message = error.getMessage();
    }

    /**
     * @return Error code
     */
    public final int getCode() {
        return code;
    }

    /**
     * @return Error message
     */
    public final String getMessage() {
        return message;
    }
}
