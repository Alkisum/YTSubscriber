package exception;

/**
 * Enum class to enumerate the errors.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public enum Error {

    /**
     * Database: Connection.
     */
    DB_CONNECTION(0, "Cannot connect to the database.");

    /**
     * Error code.
     */
    private final int code;

    /**
     * Error message.
     */
    private final String message;

    /**
     * Error constructor.
     *
     * @param pCode    Exception code
     * @param pMessage Exception message
     */
    Error(final int pCode, final String pMessage) {
        code = pCode;
        message = pMessage;
    }

    /**
     * @return Error code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return Error message
     */
    public String getMessage() {
        return message;
    }
}
