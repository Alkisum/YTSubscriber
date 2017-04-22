package model;

import config.Config;
import database.Database;
import exception.ExceptionHandler;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;

/**
 * Class defining video.
 *
 * @author Alkisum
 * @version 2.4
 * @since 1.0
 */
public class Video {

    /**
     * Default base URL for videos.
     */
    public static final String BASE_URL = "https://www.youtube.com/watch?v=";

    /**
     * Date format for video published date.
     */
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Path of directory containing the thumbnails.
     */
    public static final String THUMBNAIL_PATH = Config.USER_DIR + "thumbnail"
            + File.separator;

    /**
     * Thumbnail file extension.
     */
    public static final String THUMBNAIL_EXT = ".jpg";

    /**
     * Video id.
     */
    private int mId;

    /**
     * Video title.
     */
    private final String mTitle;

    /**
     * Video published time.
     */
    private long mTime;

    /**
     * Thumbnail URL.
     */
    private final String mThumbnailUrl;

    /**
     * Thumbnail file (local).
     */
    private File mThumbnail;

    /**
     * Video has been watched.
     */
    private boolean mWatched;

    /**
     * Channel's video id.
     */
    private final int mChannelId;

    /**
     * Video duration.
     */
    private long mDuration;

    /**
     * ID used by YT to identify the video.
     */
    private String mYtId;

    /**
     * Video constructor.
     *
     * @param title        Video title
     * @param time         Video published time
     * @param thumbnailUrl Thumbnail URL
     * @param channelId    Channel's video id
     * @param duration     Video duration
     * @param ytId         YT id
     */
    public Video(final String title, final long time,
                 final String thumbnailUrl, final int channelId,
                 final long duration, final String ytId) {
        mTitle = title;
        mTime = time;
        mThumbnailUrl = thumbnailUrl;
        mChannelId = channelId;
        mDuration = duration;
        mYtId = ytId;
    }

    /**
     * Video constructor.
     *
     * @param id           Video id
     * @param title        Video title
     * @param time         Video published time
     * @param thumbnailUrl Thumbnail URL
     * @param thumbnail    Thumbnail file
     * @param watched      True if the video has been watched, false otherwise
     * @param channelId    Channel's video id
     * @param duration     Video duration
     * @param ytId         YT id
     */
    public Video(final int id, final String title, final long time,
                 final String thumbnailUrl, final File thumbnail,
                 final boolean watched, final int channelId,
                 final long duration, final String ytId) {
        mId = id;
        mTitle = title;
        mTime = time;
        mThumbnailUrl = thumbnailUrl;
        mThumbnail = thumbnail;
        mWatched = watched;
        mChannelId = channelId;
        mDuration = duration;
        mYtId = ytId;
    }

    /**
     * @return Video id
     */
    public final int getId() {
        return mId;
    }

    /**
     * @return Video title
     */
    public final String getTitle() {
        return mTitle;
    }

    /**
     * @return Video URL
     */
    public final String getUrl() {
        return getBaseUrl() + mYtId;
    }

    /**
     * @return Video published time
     */
    public final long getTime() {
        return mTime;
    }

    /**
     * @param time Video published time to set
     */
    public final void setTime(final long time) {
        mTime = time;
    }

    /**
     * @return Thumbnail URL
     */
    public final String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    /**
     * @return Thumbnail file
     */
    public final File getThumbnail() {
        return mThumbnail;
    }

    /**
     * @return Video has been watched.
     */
    public final boolean isWatched() {
        return mWatched;
    }

    /**
     * @param watched True if the video has been watched, false otherwise
     */
    public final void setWatched(final boolean watched) {
        mWatched = watched;
    }

    /**
     * @return Channel's video id
     */
    public final int getChannelId() {
        return mChannelId;
    }

    /**
     * @return Video duration
     */
    public final long getDuration() {
        return mDuration;
    }

    /**
     * @param duration Video duration to set
     */
    public final void setDuration(final long duration) {
        mDuration = duration;
    }

    /**
     * @return YT id
     */
    public final String getYtId() {
        return mYtId;
    }

    /**
     * @param ytId YT id to set
     */
    public final void setYtId(final String ytId) {
        mYtId = ytId;
    }

    /**
     * @return Formatted video duration
     */
    public final String getFormatDuration() {
        if (mDuration >= 3600) {
            return String.format("%d:%02d:%02d",
                    mDuration / 3600,
                    (mDuration % 3600) / 60,
                    (mDuration % 60));
        } else {
            return String.format("%d:%02d",
                    (mDuration % 3600) / 60,
                    (mDuration % 60));
        }
    }

    /**
     * Retrieve duration from YouTube page.
     *
     * @param url URL of YouTube page
     * @return Duration of the video
     * @throws IOException The YouTube page has not been found
     */
    public static long retrieveDuration(final String url) throws IOException {
        String duration = "";
        int i = 0;
        while (duration.equals("") && i <= 3) {
            duration = Jsoup.connect(url).get().getElementsByAttributeValue(
                    "itemprop", "duration").attr("content");
            i++;
        }
        return Duration.parse(duration).getSeconds();
    }

    /**
     * Get the video URL (without id) stored in the configuration file.
     *
     * @return Video base URL
     */
    private static String getBaseUrl() {
        try {
            String baseUrl = Config.getValue(Config.PROP_VIDEO_URL_KEY);
            if (baseUrl == null) {
                return BASE_URL;
            }
            return baseUrl;
        } catch (IOException e) {
            return BASE_URL;
        }
    }

    /**
     * @return SQL command to create the Video table
     */
    public static String getCreateTableSql() {
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
    private static String getColumnNames() {
        return "video_id, video_title, video_time, video_thumbnail_url, "
                + "video_watched, video_channel_id, video_duration, "
                + "video_yt_id";
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

            stmt.executeUpdate("ALTER TABLE Video RENAME TO Video_tmp;");
            stmt.executeUpdate(getCreateTableSql());
            stmt.executeUpdate("INSERT INTO Video(" + getColumnNames() + ")"
                    + " SELECT " + getColumnNames()
                    + " FROM Video_tmp;");
            stmt.executeUpdate("DROP TABLE Video_tmp;");
        }
    }
}
