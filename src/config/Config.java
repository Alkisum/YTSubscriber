package config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

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
    public static final String CONFIG_FILE_PATH = USER_DIR
            + "config.properties";

    /**
     * Key for channel URL in properties file.
     */
    public static final String PROP_CHANNEL_URL_KEY = "channelUrl";

    /**
     * Default value for channel URL in properties file.
     */
    private static final String PROP_CHANNEL_URL_VALUE =
            "https://www.youtube.com/feeds/videos.xml?channel_id=";

    /**
     * Config constructor.
     */
    private Config() {

    }

    /**
     * Create the configuration file if it does not exist yet.
     *
     * @throws IOException An exception occurred while creating the file
     */
    public static void createFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists() && configFile.createNewFile()) {
            Properties prop = new Properties();
            try (OutputStream output = new FileOutputStream(
                    Config.CONFIG_FILE_PATH)) {
                prop.setProperty(PROP_CHANNEL_URL_KEY, PROP_CHANNEL_URL_VALUE);
                prop.store(output, null);
            }
        }
    }
}
