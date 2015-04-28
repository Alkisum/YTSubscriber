package model;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
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
     * Database URL.
     */
    private static final String DB_URL = "jdbc:sqlite:subscriptions.db";

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
     * Create database.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           SQL
     */
    public static void create() throws ClassNotFoundException, SQLException {
        createTables();
    }

    /**
     * @return Database connection.
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while trying to connect to
     *                                database
     */
    private static Connection getConnection()
            throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return DriverManager.getConnection(DB_URL, config.toProperties());
    }

    /**
     * Create tables in database.
     *
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing statement
     */
    private static void createTables()
            throws ClassNotFoundException, SQLException {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement()) {
            // Check if tables already exist
            String sql = "CREATE TABLE IF NOT EXISTS Channel"
                    + "(channel_id   INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " channel_name TEXT            NOT NULL,"
                    + " channel_url  TEXT            NOT NULL);";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS Video"
                    + "(video_id         INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " video_title      TEXT            NOT NULL,"
                    + " video_url        TEXT            NOT NULL,"
                    + " video_date       TEXT            NOT NULL,"
                    + " video_watched    INT             NOT NULL,"
                    + " video_channel_id INT             NOT NULL,"
                    + " FOREIGN KEY(video_channel_id)"
                    + " REFERENCES Channel(channel_id));";
            stmt.executeUpdate(sql);

        }
    }

    /**
     * @return All channels
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException Exception while executing the select statement
     */
    public static List<Channel> getAllChannels() throws ClassNotFoundException,
            SQLException {
        try (Connection c = getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Channel;")) {
            List<Channel> channelList = new ArrayList<>();
            while (rs.next()) {
                channelList.add(
                        new Channel(
                                rs.getInt("channel_id"),
                                rs.getString("channel_name"),
                                rs.getString("channel_url")
                        )
                );
            }
            return channelList;
        }
    }

    /**
     * Insert channel to database.
     *
     * @param name Channel name
     * @param url  Channel
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException Exception while executing the insert statement
     */
    public static void insertChannel(final String name, final String url)
            throws ClassNotFoundException, SQLException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "INSERT INTO Channel (channel_name, channel_url)"
                             + " VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, url);
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
     * @throws SQLException Exception while executing the update statement
     */
    public static void updateChannel(final int id, final String name,
                                     final String url)
            throws ClassNotFoundException, SQLException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "UPDATE Channel SET channel_name=?, channel_url=?"
                             + "WHERE channel_id=?")) {
            stmt.setString(1, name);
            stmt.setString(2, url);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete channel from database.
     *
     * @param id Channel id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException Exception while executing the delete statement
     */
    public static void deleteChannel(final int id)
            throws ClassNotFoundException, SQLException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM Channel WHERE channel_id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a list of channels from database.
     *
     * @param idList List of channel id
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException Exception while executing the delete statement
     */
    public static void deleteChannels(final List<Integer> idList)
            throws ClassNotFoundException, SQLException {
        try (Connection c = getConnection();
             PreparedStatement stmt = c.prepareStatement(
                     "DELETE FROM AAAAChannel WHERE channel_id=?")) {
            c.setAutoCommit(false);
            for (Integer id : idList) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            c.commit();
        }
    }
}
