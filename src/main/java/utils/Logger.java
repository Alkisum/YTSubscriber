package utils;

import org.apache.logging.log4j.LogManager;

/**
 * Utility class to get the Log4j logger.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public final class Logger {

    /**
     * Logger constructor.
     */
    private Logger() {

    }

    /**
     * @param clazz Class to get the logger for
     * @return Logger for given class
     */
    public static org.apache.logging.log4j.Logger get(final Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
}
