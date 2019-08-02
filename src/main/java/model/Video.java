package model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import config.Config;
import database.Database;
import exception.ExceptionHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
 * @version 3.0
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
    private int id;

    /**
     * Video title.
     */
    private final String title;

    /**
     * Video published time.
     */
    private long time;

    /**
     * Thumbnail URL.
     */
    private final String thumbnailUrl;

    /**
     * Thumbnail file (local).
     */
    private File thumbnail;

    /**
     * Video has been watched.
     */
    private boolean watched;

    /**
     * Channel's video id.
     */
    private final int channelId;

    /**
     * Video duration in seconds.
     */
    private long duration;

    /**
     * ID used by YT to identify the video.
     */
    private String ytId;

    /**
     * OkHttpClient instance.
     */
    private static final OkHttpClient CLIENT = new OkHttpClient();

    /**
     * Video constructor.
     *
     * @param title        Video title
     * @param time         Video published time
     * @param thumbnailUrl Thumbnail URL
     * @param channelId    Channel's video id
     * @param duration     Video duration in seconds
     * @param ytId         YT id
     */
    public Video(final String title, final long time,
                 final String thumbnailUrl, final int channelId,
                 final long duration, final String ytId) {
        this.title = title;
        this.time = time;
        this.thumbnailUrl = thumbnailUrl;
        this.channelId = channelId;
        this.duration = duration;
        this.ytId = ytId;
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
     * @param duration     Video duration in seconds
     * @param ytId         YT id
     */
    public Video(final int id, final String title, final long time,
                 final String thumbnailUrl, final File thumbnail,
                 final boolean watched, final int channelId,
                 final long duration, final String ytId) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnail = thumbnail;
        this.watched = watched;
        this.channelId = channelId;
        this.duration = duration;
        this.ytId = ytId;
    }

    /**
     * @return Video id
     */
    public final int getId() {
        return id;
    }

    /**
     * @return Video title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * @return Video URL
     */
    public final String getUrl() {
        return getBaseUrl() + ytId;
    }

    /**
     * @return Video published time
     */
    public final long getTime() {
        return time;
    }

    /**
     * @param time Video published time to set
     */
    public final void setTime(final long time) {
        this.time = time;
    }

    /**
     * @return Thumbnail URL
     */
    public final String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @return Thumbnail file
     */
    public final File getThumbnail() {
        return thumbnail;
    }

    /**
     * @return Video has been watched.
     */
    public final boolean isWatched() {
        return watched;
    }

    /**
     * @param watched True if the video has been watched, false otherwise
     */
    public final void setWatched(final boolean watched) {
        this.watched = watched;
    }

    /**
     * @return Channel's video id
     */
    public final int getChannelId() {
        return channelId;
    }

    /**
     * @return Video duration in seconds
     */
    public final long getDuration() {
        return duration;
    }

    /**
     * @param duration Video duration in seconds to set
     */
    public final void setDuration(final long duration) {
        this.duration = duration;
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
     * @return Formatted video duration
     */
    public final String getFormatDuration() {
        if (duration >= 3600) {
            return String.format("%d:%02d:%02d",
                    duration / 3600,
                    (duration % 3600) / 60,
                    (duration % 60));
        } else {
            return String.format("%d:%02d",
                    (duration % 3600) / 60,
                    (duration % 60));
        }
    }

    /**
     * Retrieve duration using YouTube API
     * (API key must be set in the configuration file in order to use the API).
     *
     * @param videoId Video ID to retrieve the duration for
     * @return Video duration in seconds
     * @throws IOException An error occurred when reading the API key from the
     *                     configuration file
     */
    public static long retrieveDuration(final String videoId)
            throws IOException {
        // Build HTTP request
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/youtube/v3/videos?id="
                        + videoId + "&part=contentDetails&key="
                        + Config.getValue(Config.PROP_API_KEY))
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            // Get response body
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return 0;
            }
            String body = responseBody.string();

            // Read duration from response body
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
            String durationString = jsonObject.getAsJsonArray("items")
                    .get(0).getAsJsonObject()
                    .get("contentDetails").getAsJsonObject()
                    .get("duration").getAsString();

            // Parse duration and return it in seconds
            return Duration.parse(durationString).toSeconds();
        }
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
