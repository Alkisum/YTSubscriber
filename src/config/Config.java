package config;

import java.io.File;

/**
 * Class defining the application configuration.
 *
 * @author Alkisum
 * @version 1.0
 * @since 28/11/15.
 */
public final class Config {

    /**
     * User directory.
     */
    public static final String USER_DIR = System.getProperty("user.home")
            + File.separator + ".YTSubscriber" + File.separator;

    /**
     * Properties file name.
     */
    public static final String CONFIG_FILE = "config.properties";

    /**
     * Key for channel URL in properties file.
     */
    public static final String PROP_CHANNEL_URL = "channelUrl";

    /**
     * Config constructor.
     */
    private Config() {

    }
}
