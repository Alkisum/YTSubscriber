package exception;

/**
 * @author Alkisum
 * @version 1.0
 * @since 27/04/15.
 */
public class ExceptionHandler extends Exception {

    /**
     * Error code.
     */
    private int code;

    /**
     * Error message.
     */
    private String message;

    /**
     * ExceptionHandler constructor.
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
