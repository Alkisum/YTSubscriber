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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 * @version 2.4
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
    public static final int SCHEMA_VERSION = 4;

    /**
     * Database file.
     */
    private static final File DB_FILE = new File(
            Config.USER_DIR + "subscriptions.db");

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
    public static Queue<Task<?>> init() throws ClassNotFoundException,
            SQLException, ExceptionHandler {
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
        if (DB_FILE.getParentFile().exists()
                || DB_FILE.getParentFile().mkdirs()) {
            if (!DB_FILE.exists()) {
                try {
                    Config.setValue(Config.PROP_SCHEMA_VERSION,
                            String.valueOf(SCHEMA_VERSION));
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
    private static void createTables() throws ClassNotFoundException,
            SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate(Channel.getCreateTableSql());
            stmt.executeUpdate(Video.getCreateTableSql());
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
            ResultSet rs = md.getColumns(
                    null, null, "Channel", "channel_subscribed");
            if (!rs.next()) {
                updateToVersion2(stmt);
            }

            // Video duration (since v2.2)
            rs = md.getColumns(null, null, "Video", "video_duration");
            if (!rs.next()) {
                updateToVersion3(stmt);
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
                tasks.add(DatabaseUpdater.getUpdateChannelUrlTask());
                tasks.add(DatabaseUpdater.getUpdateVideoUrlTask());
                tasks.add(DatabaseUpdater.getUpdateVideoTimeTask());
                tasks.add(DatabaseUpdater.getRefreshTablesTask());
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
    private static void updateToVersion2(final Statement stmt)
            throws SQLException {
        String sql = "ALTER TABLE Channel ADD COLUMN channel_subscribed"
                + " INTEGER DEFAULT 1;";
        stmt.executeUpdate(sql);
    }

    /**
     * Update database to version 3:
     * - Add column "video_duration".
     *
     * @param stmt Statement
     * @throws SQLException Exception while executing statement
     */
    private static void updateToVersion3(final Statement stmt)
            throws SQLException {
        String sql = "ALTER TABLE Video ADD COLUMN video_duration"
                + " INTEGER DEFAULT 0;";
        stmt.executeUpdate(sql);
    }

    /**
     * Update database to version 3:
     * - Add column "channel_yt_id"
     * - Add column "video_yt_id"
     * - Add column "video_time".
     *
     * @param stmt Statement
     * @throws SQLException Exception while executing statement
     */
    private static void updateToVersion4(final Statement stmt)
            throws SQLException {
        String sql = "ALTER TABLE Channel ADD COLUMN channel_yt_id"
                + " TEXT DEFAULT '';";
        sql += "ALTER TABLE Video ADD COLUMN video_yt_id TEXT DEFAULT '';";
        sql += "ALTER TABLE Video ADD COLUMN video_time INTEGER DEFAULT 0;";
        stmt.executeUpdate(sql);
    }

    /**
     * @param id Channel id
     * @return The channel's name with the given id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static String getChannelNameById(final int id)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT channel_name FROM Channel WHERE channel_id=?;")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("channel_name");
            }
            return "";
        }
    }

    /**
     * @return All channels
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Channel> getAllChannels()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Channel "
                     + "ORDER BY channel_name COLLATE NOCASE;")) {
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
     * Insert channel to database.
     *
     * @param name Channel name
     * @param ytId Channel YT ID
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the insert
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void insertChannel(final String name, final String ytId)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "INSERT INTO Channel (channel_name, "
                             + "channel_subscribed, channel_yt_id) "
                             + "VALUES (?, ?, ?);")) {
            stmt.setString(1, name);
            stmt.setBoolean(2, false);
            stmt.setString(3, ytId);
            stmt.executeUpdate();
        }
    }

    /**
     * Update channel in database.
     *
     * @param id   Channel id
     * @param name Channel name
     * @param ytId Channel YT ID
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void updateChannel(final int id, final String name,
                                     final String ytId)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Channel SET channel_name=?, channel_yt_id=? "
                             + "WHERE channel_id=?;")) {
            stmt.setString(1, name);
            stmt.setString(2, ytId);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Update the channel subscribed flag in database.
     *
     * @param id         Channel id
     * @param subscribed Channel subscribed flag
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void updateChannelSubscription(final int id,
                                                 final boolean subscribed)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Channel SET channel_subscribed=? "
                             + "WHERE channel_id=?;")) {
            stmt.setBoolean(1, subscribed);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Update the given channels' YT ID.
     *
     * @param channels List of channels to update
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static void updateChannelYtId(final List<Channel> channels)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Channel SET channel_yt_id=? "
                             + "WHERE channel_id=?;")) {
            c.setAutoCommit(false);
            for (Channel channel : channels) {
                stmt.setString(1, channel.getYtId());
                stmt.setInt(2, channel.getId());
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * Delete channel from database.
     *
     * @param id Channel id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void deleteChannel(final int id)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        deleteVideosByChannel(id);
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM Channel WHERE channel_id=?;")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a list of channels from database.
     *
     * @param idList List of channel id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void deleteChannels(final List<Integer> idList)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        deleteVideosByChannels(idList);
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM Channel WHERE channel_id=?;")) {
            c.setAutoCommit(false);
            for (Integer id : idList) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * Delete the videos attached to channel with the given id.
     *
     * @param id Channel id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private static void deleteVideosByChannel(final int id)
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        // Delete videos' thumbnails
        getAllVideosByChannel(id).forEach(Database::deleteThumbnail);
        // Delete videos from database
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM Video WHERE video_channel_id=?;")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete the videos attached to channels with the given ids.
     *
     * @param idList List of channel id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private static void deleteVideosByChannels(final List<Integer> idList)
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        // Delete videos from database
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM Video WHERE video_channel_id=?;")) {
            c.setAutoCommit(false);
            for (Integer id : idList) {
                // Delete videos' thumbnails
                getAllVideosByChannel(id).forEach(Database::deleteThumbnail);
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * @return All the videos
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
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
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Video> getAllVideosByChannel(final int channelId)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT * FROM Video "
                     + "WHERE video_channel_id=? ORDER BY video_time DESC;")) {
            stmt.setInt(1, channelId);
            return buildVideoListFromResultSet(stmt.executeQuery());
        }
    }

    /**
     * @return The videos that have not been watched
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Video> getUnwatchedVideos()
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Video, Channel "
                     + "WHERE video_channel_id=channel_id "
                     + "AND video_watched=0 "
                     + "AND channel_subscribed=1 "
                     + "ORDER BY video_time DESC;")) {
            return buildVideoListFromResultSet(rs);
        }
    }

    /**
     * Count the number of unwatched videos.
     *
     * @return Number of unwatched videos
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static int countUnwatchedVideos()
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) "
                     + "AS unwatched FROM Video, Channel "
                     + "WHERE video_channel_id=channel_id "
                     + "AND video_watched=0 "
                     + "AND channel_subscribed=1;")) {
            ResultSet rs = stmt.executeQuery();
            return rs.getInt("unwatched");
        }
    }

    /**
     * Count the number of unwatched videos by channel.
     *
     * @param channelId Channel id
     * @return Number of unwatched videos
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static int countUnwatchedVideosByChannel(final int channelId)
            throws SQLException, ClassNotFoundException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) "
                     + "AS unwatched FROM Video WHERE video_channel_id=? AND "
                     + "video_watched=0;")) {
            stmt.setInt(1, channelId);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt("unwatched");
        }
    }

    /**
     * Update the given video's watch state.
     *
     * @param video   Video to update
     * @param watched True if the video has been watched, false otherwise
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void updateVideoWatchState(final Video video,
                                             final boolean watched)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Video SET video_watched=? WHERE video_id=?;")) {
            stmt.setBoolean(1, watched);
            stmt.setInt(2, video.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Update all the videos watch state.
     *
     * @param watched True if the videos have been watched, false otherwise
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void updateVideoWatchState(final boolean watched)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Video SET video_watched=?;")) {
            stmt.setBoolean(1, watched);
            stmt.executeUpdate();
        }
    }

    /**
     * Update the given videos' duration.
     *
     * @param videos List of videos to update
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
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
                stmt.setInt(2, video.getId());
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
     * @throws SQLException           Exception while executing the update
     *                                statement
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
                stmt.setInt(2, video.getId());
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
     * @throws SQLException           Exception while executing the update
     *                                statement
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
                stmt.setInt(2, video.getId());
                stmt.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     * Check if the video already exists in the database.
     *
     * @param ytId Video YT ID
     * @return True if the video exists, false if it does not
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static boolean videoExists(final String ytId)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT 1 FROM Video "
                     + "WHERE video_yt_id=? LIMIT 1;")) {
            stmt.setString(1, ytId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Insert the videos into the database.
     *
     * @param videos List of videos to insert
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the insert
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     * @throws IOException            Exception while downloading the thumbnail
     */
    public static void insertVideos(final List<Video> videos)
            throws SQLException, ClassNotFoundException, IOException,
            ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "INSERT INTO Video (video_title, video_time, "
                             + "video_thumbnail_url, video_watched, "
                             + "video_channel_id, video_duration, video_yt_id) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?);")) {
            c.setAutoCommit(false);
            for (Video video : videos) {
                stmt.setString(1, video.getTitle());
                stmt.setLong(2, video.getTime());
                stmt.setString(3, video.getThumbnailUrl());
                stmt.setBoolean(4, false);
                stmt.setInt(5, video.getChannelId());
                stmt.setLong(6, video.getDuration());
                stmt.setString(7, video.getYtId());
                stmt.executeUpdate();

                // Download thumbnail
                int id = stmt.getGeneratedKeys().getInt(1);
                String path = Video.THUMBNAIL_PATH + id + Video.THUMBNAIL_EXT;
                downloadThumbnail(video.getThumbnailUrl(), path);
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
                            new File(Video.THUMBNAIL_PATH + id
                                    + Video.THUMBNAIL_EXT),
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
     * Download the video thumbnail.
     *
     * @param src Source URL to get the thumbnail from
     * @param dst Destination path where to copy the thumbnail to
     * @throws IOException Exception while downloading the thumbnail
     */
    private static void downloadThumbnail(final String src, final String dst)
            throws IOException {
        File thumbnail = new File(dst);
        if (thumbnail.getParentFile().exists()
                || thumbnail.getParentFile().mkdirs()) {
            try (InputStream in = new URL(src).openStream()) {
                Files.copy(in, Paths.get(thumbnail.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Delete video thumbnail.
     *
     * @param video Video's thumbnail
     * @return true if the thumbnail file has been deleted, false otherwise
     */
    private static boolean deleteThumbnail(final Video video) {
        return video.getThumbnail().exists() && video.getThumbnail().delete();
    }

    /**
     * Delete video from database.
     *
     * @param video Video to delete
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void deleteVideo(final Video video)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM Video WHERE video_id=?;")) {
            stmt.setInt(1, video.getId());
            stmt.executeUpdate();
            deleteThumbnail(video);
        }
    }

    /**
     * Get channel URL stored in the database that belongs to the channel with
     * the given id. Must be used only with schema version lower than 4.
     *
     * @param id Channel id
     * @return Channel URL
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static String getChannelUrlFromDb(final int id) throws SQLException,
            ExceptionHandler, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT channel_url FROM Channel WHERE channel_id=?;")) {
            stmt.setInt(1, id);
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
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static String getVideoUrlFromDb(final int id) throws SQLException,
            ExceptionHandler, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT video_url FROM Video WHERE video_id=?;")) {
            stmt.setInt(1, id);
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
     * @throws SQLException           Exception while executing the delete
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    static String getVideoDateFromDb(final int id) throws SQLException,
            ExceptionHandler, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "SELECT video_date FROM Video WHERE video_id=?;")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("video_date");
            }
            return "";
        }
    }
}
