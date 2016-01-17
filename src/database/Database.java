package database;

import config.Config;
import exception.Error;
import exception.ExceptionHandler;
import model.Channel;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

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
import java.util.List;

/**
 * Class handling database operation.
 *
 * @author Alkisum
 * @version 1.0
 * @since 16/04/15.
 */
public final class Database {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Database.class);

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
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           SQL Exception
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void init() throws ClassNotFoundException, SQLException,
            ExceptionHandler {
        createTables();
        updateTables();
    }

    /**
     * @return Database connection.
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while trying to connect to
     *                                database
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private static Connection getConnection()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        if (DB_FILE.getParentFile().exists()
                || DB_FILE.getParentFile().mkdirs()) {
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
            // Check if tables already exist
            String sql = "CREATE TABLE IF NOT EXISTS Channel"
                    + "(channel_id         INT  PRIMARY KEY AUTOINCREMENT,"
                    + " channel_name       TEXT NOT NULL,"
                    + " channel_url        TEXT NOT NULL,"
                    + " channel_subscribed INT  NOT NULL);";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS Video"
                    + "(video_id            INT  PRIMARY KEY AUTOINCREMENT,"
                    + " video_title         TEXT NOT NULL,"
                    + " video_url           TEXT NOT NULL,"
                    + " video_date          TEXT NOT NULL,"
                    + " video_thumbnail_url TEXT NOT NULL,"
                    + " video_watched       INT  NOT NULL,"
                    + " video_channel_id    INT  NOT NULL,"
                    + " FOREIGN KEY(video_channel_id)"
                    + " REFERENCES Channel(channel_id));";
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Updates table in database. Will be used when upgrading to a newer version
     * with changes in the database structure.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private static void updateTables()
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement()) {
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getColumns(
                    null, null, "Channel", "channel_subscribed");
            if (!rs.next()) {
                LOGGER.error("Update table");
                String sql = "ALTER TABLE Channel ADD COLUMN channel_subscribed"
                        + " INT DEFAULT 1;";
                stmt.executeUpdate(sql);
            }
        }
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
                                rs.getString("channel_url"),
                                rs.getBoolean("channel_subscribed")
                        )
                );
            }
            return channels;
        }
    }

    /**
     * Insert channel to database.
     *
     * @param name       Channel name
     * @param url        Channel URL
     * @param subscribed Channel subscribed flag
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the insert
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void insertChannel(final String name, final String url,
                                     final boolean subscribed)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "INSERT INTO Channel (channel_name, channel_url, "
                             + "channel_subscribed) VALUES (?, ?, ?);")) {
            stmt.setString(1, name);
            stmt.setString(2, url);
            stmt.setBoolean(3, subscribed);
            stmt.executeUpdate();
        }
    }

    /**
     * Update channel in database.
     *
     * @param id   Channel id
     * @param name Channel name
     * @param url  Channel URL
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the update
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static void updateChannel(final int id, final String name,
                                     final String url)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Channel SET channel_name=?, channel_url=? "
                             + "WHERE channel_id=?;")) {
            stmt.setString(1, name);
            stmt.setString(2, url);
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
     * @param videos List of videos to refresh
     * @return List of videos refreshed
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static List<Video> getVideos(final List<Video> videos)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT * FROM Video "
                     + "WHERE video_id=?;")) {
            List<Video> updatedVideos = new ArrayList<>();
            for (Video video : videos) {
                stmt.setInt(1, video.getId());
                Video updatedVideo = buildVideoFromResultSet(
                        stmt.executeQuery());
                if (updatedVideo != null) {
                    updatedVideos.add(updatedVideo);
                }
            }
            return updatedVideos;
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
                     + "WHERE video_channel_id=? ORDER BY video_date DESC;")) {
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
                     + "ORDER BY video_date DESC;")) {
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
            LOGGER.debug("Update video watched: " + watched);
            stmt.setBoolean(1, watched);
            stmt.executeUpdate();
        }
    }

    /**
     * Check if the video already exists in the database.
     *
     * @param url Video URL
     * @return True if the video exists, false if it does not
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    public static boolean videoExists(final String url)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement("SELECT 1 FROM Video "
                     + "WHERE video_url=? LIMIT 1;")) {
            stmt.setString(1, url);
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
                     "INSERT INTO Video (video_title, video_url, video_date, "
                             + "video_thumbnail_url, video_watched, "
                             + "video_channel_id) "
                             + "VALUES (?, ?, ?, ?, 0, ?);")) {
            c.setAutoCommit(false);
            for (Video video : videos) {
                stmt.setString(1, video.getTitle());
                stmt.setString(2, video.getUrl());
                stmt.setString(3, video.getDate());
                stmt.setString(4, video.getThumbnailUrl());
                stmt.setInt(5, video.getChannelId());
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
     * Build a video from the given result set.
     *
     * @param rs Result set
     * @return Video
     * @throws SQLException Exception while executing the select statement
     */
    private static Video buildVideoFromResultSet(final ResultSet rs)
            throws SQLException {
        if (rs.next()) {
            int id = rs.getInt("video_id");
            return new Video(
                    id,
                    rs.getString("video_title"),
                    rs.getString("video_url"),
                    rs.getString("video_date"),
                    rs.getString("video_thumbnail_url"),
                    new File(Video.THUMBNAIL_PATH + id + Video.THUMBNAIL_EXT),
                    rs.getBoolean("video_watched"),
                    rs.getInt("video_channel_id")
            );
        }
        return null;
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
                            rs.getString("video_url"),
                            rs.getString("video_date"),
                            rs.getString("video_thumbnail_url"),
                            new File(Video.THUMBNAIL_PATH + id
                                    + Video.THUMBNAIL_EXT),
                            rs.getBoolean("video_watched"),
                            rs.getInt("video_channel_id")
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
}
