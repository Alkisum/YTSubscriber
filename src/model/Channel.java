package model;

import config.Config;
import database.Database;
import exception.ExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Class defining channel.
 *
 * @author Alkisum
 * @version 1.0
 * @since 19/04/15.
 */
public class Channel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Channel.class);

    /**
     * Channel id.
     */
    private final int mId;

    /**
     * Channel name.
     */
    private final String mName;

    /**
     * Channel URL.
     */
    private final String mUrl;

    /**
     * Channel subscribed flag.
     */
    private final boolean mSubscribed;

    /**
     * The channel is selected ini the manager list.
     */
    private boolean mChecked;

    /**
     * Channel constructor.
     *
     * @param id         Channel id
     * @param name       Channel name
     * @param url        Channel URL
     * @param subscribed Channel subscribed flag
     */
    public Channel(final int id, final String name, final String url,
                   final boolean subscribed) {
        mId = id;
        mName = name;
        mUrl = url;
        mSubscribed = subscribed;
        mChecked = false;
    }

    @Override
    public final String toString() {
        try {
            return mName
                    + " (" + Database.countUnwatchedVideosByChannel(mId) + ")";
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Clean the channel from the videos watched and not existing anymore
     * in the RSS Feeds.
     *
     * @param urlList List of video URL existing in the RSS Feed.
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing a statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public final void clean(final List<String> urlList)
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        List<Video> videoList = Database.getAllVideosByChannel(mId);
        for (Video video : videoList) {
            if (!urlList.contains(video.getUrl()) && video.isWatched()) {
                Database.deleteVideo(video);
            }
        }
    }

    /**
     * @return Channel id
     */
    public final int getId() {
        return mId;
    }

    /**
     * @return Channel name
     */
    public final String getName() {
        return mName;
    }

    /**
     * @return Channel URL
     */
    public final String getUrl() {
        return mUrl;
    }

    /**
     * @return Channel subscribed flag
     */
    public final boolean isSubscribed() {
        return mSubscribed;
    }

    /**
     * @return Channel is selected in manager list
     */
    public final boolean isChecked() {
        return mChecked;
    }

    /**
     * @param checked Select the channel in manager list
     */
    public final void setChecked(final boolean checked) {
        mChecked = checked;
    }

    /**
     * Read the properties file to get the feed URL (without id).
     *
     * @return Feed base URL
     * @throws IOException The properties file has not been found
     */
    public static String getBaseUrl() throws IOException {
        Config.createFile();
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(Config.CONFIG_FILE_PATH)) {
            prop.load(input);
            return prop.getProperty(Config.PROP_CHANNEL_URL_KEY);
        }
    }
}
