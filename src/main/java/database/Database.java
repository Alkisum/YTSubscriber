package database;

import config.Config;
import exception.Error;
import exception.ExceptionHandler;
import javafx.concurrent.Task;
import model.Channel;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;
import view.dialog.ExceptionDialog;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class handling database operation.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public final class Database {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Database.class);

    /**
     * Database schema version.
     */
    public static final int SCHEMA_VERSION = 5;

    /**
     * Database file.
     */
    public static final File DB_FILE = new File(Config.USER_DIR + "subscriptions.db");

    /**
     * Database URL.
     */
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    /**
     * Driver.
     */
    private static final String DRIVER = "org.sqlite.JDBC";


    /**
     * Database constructor.
     */
    private Database() {

    }

    /**
     * Initialize database by creating tables (if they don't exist) and by
     * updating the tables (if necessary).
     *
     * @return Tasks to update the database, null if no update is necessary
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           SQL Exception
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static Queue<Task<?>> init()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        createTables();
        return updateTables();
    }

    /**
     * @return Database connection.
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while trying to connect to
     *                                database
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static Connection getConnection()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        if (DB_FILE.getParentFile().exists() || DB_FILE.getParentFile().mkdirs()) {
            if (!DB_FILE.exists()) {
                try {
                    Config.setValue(Config.PROP_SCHEMA_VERSION, String.valueOf(SCHEMA_VERSION));
                } catch (IOException e) {
                    ExceptionDialog.show(e);
                    LOGGER.error(e);
                    e.printStackTrace();
                }
            }
            Class.forName(DRIVER);
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            return DriverManager.getConnection(DB_URL, config.toProperties());
        } else {
            throw new ExceptionHandler(Error.DB_CONNECTION);
        }

    }

    /**
     * Create tables in database.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private static void createTables()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate(getChannelCreateTableSql());
            stmt.executeUpdate(getVideoCreateTableSql());
        }
    }

    /**
     * Updates table in database. Will be used when upgrading to a newer version
     * with changes in the database structure.
     *
     * @return Tasks to update the database, null if no update is necessary
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private static Queue<Task<?>> updateTables()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        Queue<Task<?>> tasks = new LinkedList<>();
        try (Connection c = getConnection();
             Statement stmt = c.createStatement()) {
            DatabaseMetaData md = c.getMetaData();

            // Channel subscribed (since v1.2)
            ResultSet rs = md.getColumns(null, null, "Channel", "channel_subscribed");
            if (!rs.next()) {
                updateToVersion2(stmt);
            }

            // Video duration (since v2.2)
            rs = md.getColumns(null, null, "Video", "video_duration");
            if (!rs.next()) {
                updateToVersion3(stmt);
                LOGGER.debug("getUpdateVideoDurationTask");
                tasks.add(DatabaseUpdater.getUpdateVideoDurationTask());
            }

            // Use schema version (since v2.4)
            Integer schemaVersion = null;
            try {
                String s = Config.getValue(Config.PROP_SCHEMA_VERSION);
                if (s != null) {
                    schemaVersion = Integer.parseInt(s);
                }
            } catch (IOException e) {
                LOGGER.info(e);
            }

            // Channel and video YT ids (since v2.4 - schema version 4)
            if (schemaVersion == null || schemaVersion < 4) {
                updateToVersion4(stmt);
                LOGGER.debug("getUpdateChannelUrlTask");
                tasks.add(DatabaseUpdater.getUpdateChannelUrlTask());
                LOGGER.debug("getUpdateVideoUrlTask");
                tasks.add(DatabaseUpdater.getUpdateVideoUrlTask());
                LOGGER.debug("getUpdateVideoTimeTask");
                tasks.add(DatabaseUpdater.getUpdateVideoTimeTask());
                LOGGER.debug("getRefreshTablesTask");
                tasks.add(DatabaseUpdater.getRefreshTablesTask());
            }

            // Migration to ObjectBox
            if (schemaVersion == null || schemaVersion < 5) {
                LOGGER.debug("getMigrateToObjectBoxTask");
                tasks.add(DatabaseUpdater.getMigrateToObjectBoxTask());
            }

            if (tasks.isEmpty()) {
                return null;
            } else {
                return tasks;
            }
        }
    }

    /**
     * Update database to version 2:
     * - Add column "channel_subscribed".
     *
     * @param stmt Statement
     * @throws SQLException Exception while executing statement
     */
    private static void updateToVersion2(final Statement stmt) throws SQLException {
        String sql = "ALTER TABLE Channel ADD COLUMN channel_subscribed INTEGER DEFAULT 1;";
        stmt.executeUpdate(sql);
    }

    /**
     * Update database to version 3:
     * - Add column "video_duration".
     *
     * @param stmt Statement
     * @throws SQLException Exception while executing statement
     */
    private static void updateToVersion3(final Statement stmt) throws SQLException {
        String sql = "ALTER TABLE Video ADD COLUMN video_duration INTEGER DEFAULT 0;";
        stmt.executeUpdate(sql);
    }

    /**
     * Update database to version 4:
     * - Add column "channel_yt_id"
     * - Add column "video_yt_id"
     * - Add column "video_time".
     *
     * @param stmt Statement
     * @throws SQLException Exception while executing statement
     */
    private static void updateToVersion4(final Statement stmt) throws SQLException {
        String sql = "ALTER TABLE Channel ADD COLUMN channel_yt_id TEXT DEFAULT '';";
        sql += "ALTER TABLE Video ADD COLUMN video_yt_id TEXT DEFAULT '';";
        sql += "ALTER TABLE Video ADD COLUMN video_time INTEGER DEFAULT 0;";
        stmt.executeUpdate(sql);
    }

    /**
     * @return All channels
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Channel> getAllChannels()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Channel;")) {
            List<Channel> channels = new ArrayList<>();
            while (rs.next()) {
                channels.add(
                        new Channel(
                                rs.getInt("channel_id"),
                                rs.getString("channel_name"),
                                rs.getBoolean("channel_subscribed"),
                                rs.getString("channel_yt_id")
                        )
                );
            }
            return channels;
        }
    }

    /**
     * Update the given channels' YT ID.
     *
     * @param channels List of channels to update
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static void updateChannelYtId(final List<Channel> channels)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Channel SET channel_yt_id=? WHERE channel_id=?;")) {
            c.setAutoCommit(false);
            for (Channel channel : channels) {
                stmt.setString(1, channel.getYtId());
                stmt.setLong(2, channel.getId());
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * @return All the videos
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Video> getAllVideos()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Video;")) {
            return buildVideoListFromResultSet(rs);
        }
    }

    /**
     * @param channelId Channel id to get the videos from
     * @return All the videos of the given channel
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Video> getAllVideosByChannel(final long channelId)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT * FROM Video "
                     + "WHERE video_channel_id=? ORDER BY video_time DESC;")) {
            stmt.setLong(1, channelId);
            return buildVideoListFromResultSet(stmt.executeQuery());
        }
    }

    /**
     * Update the given videos' duration.
     *
     * @param videos List of videos to update
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static void updateVideoDuration(final List<Video> videos)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Video SET video_duration=? WHERE video_id=?;")) {
            c.setAutoCommit(false);
            for (Video video : videos) {
                stmt.setLong(1, video.getDuration());
                stmt.setLong(2, video.getId());
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * Update the given videos' YT ID.
     *
     * @param videos List of videos to update
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static void updateVideoYtId(final List<Video> videos)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Video SET video_yt_id=? WHERE video_id=?;")) {
            c.setAutoCommit(false);
            for (Video video : videos) {
                stmt.setString(1, video.getYtId());
                stmt.setLong(2, video.getId());
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * Update the given videos' time.
     *
     * @param videos List of videos to update
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static void updateVideoTime(final List<Video> videos)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Video SET video_time=? WHERE video_id=?;")) {
            c.setAutoCommit(false);
            for (Video video : videos) {
                stmt.setLong(1, video.getTime());
                stmt.setLong(2, video.getId());
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * Build a list of videos from the given result set.
     *
     * @param rs Result set
     * @return List of videos
     * @throws SQLException Exception while executing the select statement
     */
    private static List<Video> buildVideoListFromResultSet(final ResultSet rs)
            throws SQLException {
        List<Video> videos = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("video_id");
            videos.add(
                    new Video(
                            id,
                            rs.getString("video_title"),
                            rs.getLong("video_time"),
                            rs.getString("video_thumbnail_url"),
                            rs.getBoolean("video_watched"),
                            rs.getInt("video_channel_id"),
                            rs.getInt("video_duration"),
                            rs.getString("video_yt_id")
                    )
            );
        }
        return videos;
    }

    /**
     * Get channel URL stored in the database that belongs to the channel with
     * the given id. Must be used only with schema version lower than 4.
     *
     * @param id Channel id
     * @return Channel URL
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static String getChannelUrlFromDb(final long id) throws SQLException,
            ExceptionHandler, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT channel_url FROM Channel WHERE channel_id=?;")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("channel_url");
            }
            return "";
        }
    }

    /**
     * Get video URL stored in the database that belongs to the video with
     * the given id. Must be used only with schema version lower than 4 because
     * the URL column does not exit after this version.
     *
     * @param id Video id
     * @return Video URL
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static String getVideoUrlFromDb(final long id) throws SQLException,
            ExceptionHandler, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT video_url FROM Video WHERE video_id=?;")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("video_url");
            }
            return "";
        }
    }

    /**
     * Get video date stored in the database that belongs to the video with
     * the given id. Must be used only with schema version lower than 4 because
     * the date column does not exit after this version.
     *
     * @param id Video id
     * @return Video URL
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static String getVideoDateFromDb(final long id) throws SQLException,
            ExceptionHandler, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT video_date FROM Video WHERE video_id=?;")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("video_date");
            }
            return "";
        }
    }

    /**
     * Refresh table structure with a new one.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the sql statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void refreshChannel() throws SQLException, ExceptionHandler,
            ClassNotFoundException {
        try (Connection c = Database.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = OFF");
            stmt.executeUpdate("ALTER TABLE Channel RENAME TO Channel_tmp;");
            stmt.executeUpdate(getChannelCreateTableSql());
            stmt.executeUpdate("INSERT INTO Channel(" + getChannelColumnNames() + ")"
                    + " SELECT " + getChannelColumnNames() + " FROM Channel_tmp;");
            stmt.executeUpdate("DROP TABLE Channel_tmp;");
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
        }
    }

    /**
     * @return SQL command to create the Channel table
     */
    private static String getChannelCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS Channel"
                + "(channel_id         INTEGER  PRIMARY KEY AUTOINCREMENT,"
                + " channel_name       TEXT NOT NULL,"
                + " channel_yt_id      TEXT NOT NULL,"
                + " channel_subscribed INTEGER  NOT NULL);";
    }

    /**
     * @return Column names separated by commas
     */
    private static String getChannelColumnNames() {
        return "channel_id, channel_name, channel_yt_id, channel_subscribed";
    }

    /**
     * Refresh table structure with a new one.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the sql statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void refreshVideo() throws SQLException, ExceptionHandler,
            ClassNotFoundException {
        try (Connection c = Database.getConnection();
             Statement stmt = c.createStatement()) {

            stmt.executeUpdate("ALTER TABLE Video RENAME TO Video_tmp;");
            stmt.executeUpdate(getVideoCreateTableSql());
            stmt.executeUpdate("INSERT INTO Video(" + getVideoColumnNames() + ")"
                    + " SELECT " + getVideoColumnNames() + " FROM Video_tmp;");
            stmt.executeUpdate("DROP TABLE Video_tmp;");
        }
    }

    /**
     * @return SQL command to create the Video table
     */
    private static String getVideoCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS Video"
                + "(video_id            INTEGER  PRIMARY KEY AUTOINCREMENT,"
                + " video_title         TEXT NOT NULL,"
                + " video_time          TEXT NOT NULL,"
                + " video_thumbnail_url TEXT NOT NULL,"
                + " video_watched       INTEGER  NOT NULL,"
                + " video_channel_id    INTEGER  NOT NULL,"
                + " video_duration      INTEGER  NOT NULL,"
                + " video_yt_id         TEXT NOT NULL,"
                + " FOREIGN KEY(video_channel_id)"
                + " REFERENCES Channel(channel_id));";
    }

    /**
     * @return Column names separated by commas
     */
    private static String getVideoColumnNames() {
        return "video_id, video_title, video_time, video_thumbnail_url, video_watched, "
                +  "video_channel_id, video_duration, video_yt_id";
    }
}
