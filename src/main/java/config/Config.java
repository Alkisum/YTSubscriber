package config;

import model.Channel;
import model.Video;
import view.Theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Class defining the application configuration.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
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
    private static final String CONFIG_FILE_PATH = USER_DIR
            + "config.properties";

    /**
     * Key for video URL in properties file.
     */
    public static final String PROP_VIDEO_URL_KEY = "videoUrl";

    /**
     * Default value for video URL in properties file.
     */
    private static final String PROP_VIDEO_URL_VALUE = Video.BASE_URL;

    /**
     * Key for channel URL in properties file.
     */
    public static final String PROP_CHANNEL_URL_KEY = "channelUrl";

    /**
     * Default value for channel URL in properties file.
     */
    private static final String PROP_CHANNEL_URL_VALUE = Channel.BASE_URL;

    /**
     * Key for theme in properties file.
     */
    public static final String PROP_THEME_KEY = "theme";

    /**
     * Default value for theme in properties file.
     */
    private static final String PROP_THEME_VALUE = Theme.CLASSIC;

    /**
     * Key for window's width in properties file.
     */
    public static final String PROP_WIDTH_KEY = "windowWidth";

    /**
     * Key for window's height in properties file.
     */
    public static final String PROP_HEIGHT_KEY = "windowHeight";

    /**
     * Key for window's X position in properties file.
     */
    public static final String PROP_X_KEY = "windowX";

    /**
     * Key for window's Y position in properties file.
     */
    public static final String PROP_Y_KEY = "windowY";

    /**
     * Key for schema version in properties file.
     */
    public static final String PROP_SCHEMA_VERSION = "schemaVersion";

    /**
     * Key for API key in properties file.
     */
    public static final String PROP_API_KEY = "apiKey";

    /**
     * Key for media player to use when playing a video with Streamlink.
     */
    public static final String PROP_MEDIA_PLAYER = "mediaPlayer";

    /**
     * Config constructor.
     */
    private Config() {

    }

    /**
     * Set default values to config file if the key does not exist yet.
     *
     * @throws IOException An exception occurred while reading or writing the
     *                     file
     */
    public static void setDefaultValues() throws IOException {
        if (getValue(PROP_VIDEO_URL_KEY) == null) {
            setValue(PROP_VIDEO_URL_KEY, PROP_VIDEO_URL_VALUE);
        }
        if (getValue(PROP_CHANNEL_URL_KEY) == null) {
            setValue(PROP_CHANNEL_URL_KEY, PROP_CHANNEL_URL_VALUE);
        }
        if (getValue(PROP_THEME_KEY) == null) {
            setValue(PROP_THEME_KEY, PROP_THEME_VALUE);
        }
    }

    /**
     * Check if the configuration file exists.
     *
     * @return true if the configuration file exists, false otherwise
     */
    private static boolean configFileExists() {
        File configFile = new File(CONFIG_FILE_PATH);
        return configFile.exists();
    }

    /**
     * Create the configuration file if it does not exist yet.
     *
     * @return true if the configuration file has been created with the keys,
     * false otherwise
     * @throws IOException An exception occurred while creating the file
     */
    private static boolean createFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.createNewFile()) {
            Properties prop = new Properties();
            try (OutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
                prop.setProperty(PROP_VIDEO_URL_KEY, PROP_VIDEO_URL_VALUE);
                prop.setProperty(PROP_CHANNEL_URL_KEY, PROP_CHANNEL_URL_VALUE);
                prop.setProperty(PROP_THEME_KEY, PROP_THEME_VALUE);
                prop.store(output, null);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the value for the given key.
     *
     * @param key Key
     * @return Value
     * @throws IOException An exception occurred while getting the value
     */
    public static String getValue(final String key) throws IOException {
        if (configFileExists() || createFile()) {
            Properties prop = new Properties();
            try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
                prop.load(input);
                return prop.getProperty(key);
            }
        }
        return null;
    }

    /**
     * Set the value for the given key.
     *
     * @param key   Key
     * @param value Value
     * @throws IOException An exception occurred while setting the value
     */
    public static void setValue(final String key, final String value)
            throws IOException {
        if (configFileExists() || createFile()) {
            Properties prop = new Properties();
            try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
                prop.load(input);
            }
            try (OutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
                prop.put(key, value);
                prop.store(output, null);
            }
        }
    }
}
