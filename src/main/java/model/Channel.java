package model;

import config.Config;
import database.Database;
import exception.ExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Class defining channel.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class Channel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Channel.class);

    /**
     * Default base URL for channels.
     */
    public static final String BASE_URL =
            "https://www.youtube.com/feeds/videos.xml?channel_id=";

    /**
     * Channel id.
     */
    private final int id;

    /**
     * Channel name.
     */
    private final String name;

    /**
     * Channel subscribed flag.
     */
    private final boolean subscribed;

    /**
     * The channel is selected ini the manager list.
     */
    private boolean checked;

    /**
     * YT id.
     */
    private String ytId;

    /**
     * Channel constructor.
     *
     * @param id         Channel id
     * @param name       Channel name
     * @param subscribed Channel subscribed flag
     * @param ytId       YT id
     */
    public Channel(final int id, final String name, final boolean subscribed,
                   final String ytId) {
        this.id = id;
        this.name = name;
        this.subscribed = subscribed;
        checked = false;
        this.ytId = ytId;
    }

    @Override
    public final String toString() {
        try {
            return name
                    + " (" + Database.countUnwatchedVideosByChannel(id) + ")";
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
     * @param ytIdList List of video YT id existing in the RSS Feed.
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing a statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public final void clean(final List<String> ytIdList)
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        List<Video> videoList = Database.getAllVideosByChannel(id);
        for (Video video : videoList) {
            if (!ytIdList.contains(video.getYtId()) && video.isWatched()) {
                Database.deleteVideo(video);
            }
        }
    }

    /**
     * @return Channel id
     */
    public final int getId() {
        return id;
    }

    /**
     * @return Channel name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return Channel URL
     */
    public final String getUrl() {
        return getBaseUrl() + ytId;
    }

    /**
     * @return Channel subscribed flag
     */
    public final boolean isSubscribed() {
        return subscribed;
    }

    /**
     * @return Channel is selected in manager list
     */
    public final boolean isChecked() {
        return checked;
    }

    /**
     * @param checked Select the channel in manager list
     */
    public final void setChecked(final boolean checked) {
        this.checked = checked;
    }

    /**
     * @return YT id
     */
    public final String getYtId() {
        return ytId;
    }

    /**
     * @param ytId YT id to set
     */
    public final void setYtId(final String ytId) {
        this.ytId = ytId;
    }

    /**
     * Get the feed URL (without id) stored in the configuration file.
     *
     * @return Feed base URL
     */
    public static String getBaseUrl() {
        try {
            String baseUrl = Config.getValue(Config.PROP_CHANNEL_URL_KEY);
            if (baseUrl == null) {
                return BASE_URL;
            }
            return baseUrl;
        } catch (IOException e) {
            return BASE_URL;
        }
    }

    /**
     * @return SQL command to create the Channel table
     */
    public static String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS Channel"
                + "(channel_id         INTEGER  PRIMARY KEY AUTOINCREMENT,"
                + " channel_name       TEXT NOT NULL,"
                + " channel_yt_id      TEXT NOT NULL,"
                + " channel_subscribed INTEGER  NOT NULL);";
    }

    /**
     * @return Column names separated by commas
     */
    private static String getColumnNames() {
        return "channel_id, channel_name, channel_yt_id, channel_subscribed";
    }

    /**
     * Refresh table structure with a new one.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the sql
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void refresh() throws SQLException, ExceptionHandler,
            ClassNotFoundException {
        try (Connection c = Database.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = OFF");
            stmt.executeUpdate("ALTER TABLE Channel RENAME TO Channel_tmp;");
            stmt.executeUpdate(getCreateTableSql());
            stmt.executeUpdate("INSERT INTO Channel(" + getColumnNames() + ")"
                    + " SELECT " + getColumnNames()
                    + " FROM Channel_tmp;");
            stmt.executeUpdate("DROP TABLE Channel_tmp;");
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
        }
    }
}
