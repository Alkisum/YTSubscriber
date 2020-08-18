package utils;

import view.dialog.ExceptionDialog;

/**
 * Utility class to handle exceptions.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public final class ExceptionHandler {

    /**
     * ExceptionHandler constructor.
     */
    private ExceptionHandler() {

    }

    /**
     * Handle given throwable.
     *
     * @param clazz     Class to use when logging exception
     * @param throwable Throwable to handle
     */
    public static void handle(final Class<?> clazz, final Throwable throwable) {
        ExceptionDialog.show(throwable);
        Logger.get(clazz).error(throwable);
        throwable.printStackTrace();
    }
}
