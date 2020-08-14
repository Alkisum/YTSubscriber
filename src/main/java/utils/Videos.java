package utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import config.Config;
import database.ObjectBox;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import model.Channel_;
import model.Video;
import model.Video_;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

/**
 * Utility class for videos.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Videos {

    /**
     * Default base URL for videos.
     */
    public static final String BASE_URL = "https://www.youtube.com/watch?v=";

    /**
     * Path of directory containing the thumbnails.
     */
    public static final String THUMBNAIL_PATH = Config.USER_DIR + "thumbnail" + File.separator;

    /**
     * Thumbnail file extension.
     */
    public static final String THUMBNAIL_EXT = ".jpg";

    /**
     * Box storing videos.
     */
    private static final Box<Video> VIDEO_BOX = ObjectBox.get().boxFor(Video.class);

    /**
     * OkHttpClient instance.
     */
    private static final OkHttpClient CLIENT = new OkHttpClient();

    /**
     * Videos constructor.
     */
    private Videos() {

    }

    /**
     * Retrieve duration using YouTube API
     * (API key must be set in the configuration file in order to use the API).
     *
     * @param ytId Video ID to retrieve the duration for
     * @return Video duration in seconds
     * @throws IOException An error occurred while reading the API key from the configuration file
     *                     or while fetching the duration
     */
    public static long retrieveDuration(final String ytId) throws IOException {
        // Build HTTP request
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/youtube/v3/videos?id=" + ytId
                        + "&part=contentDetails&key=" + Config.getValue(Config.PROP_API_KEY))
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
     * Check if a video exists from its YT id.
     *
     * @param ytId YT id
     * @return true if a video exists, false otherwise
     */
    public static boolean exists(final String ytId) {
        return VIDEO_BOX.query().equal(Video_.ytId, ytId).build().count() > 0;
    }

    /**
     * Create videos.
     *
     * @param videos Videos to create
     * @throws IOException An error occurred while downloading the thumbnail
     */
    public static void create(final Video... videos) throws IOException {
        VIDEO_BOX.put(videos);
        for (Video video : videos) {
            Thumbnails.downloadThumbnail(video.getThumbnailUrl(), video.getThumbnailFile());
        }
    }

    /**
     * Update videos.
     *
     * @param videos Videos to update
     */
    public static void update(final Video... videos) {
        VIDEO_BOX.put(videos);
    }

    /**
     * Delete videos and their thumbnail.
     *
     * @param videos Videos to delete
     * @throws IOException An error occurred while deleting the video thumbnail file
     */
    public static void delete(final Video... videos) throws IOException {
        VIDEO_BOX.remove(videos);
        for (Video video : videos) {
            Files.deleteIfExists(video.getThumbnailFile().toPath());
        }
    }

    /**
     * Count the number of unwatched videos in subscribed channels.
     *
     * @return Number of unwatched videos.
     */
    public static long countUnwatchedVideos() {
        QueryBuilder<Video> builder = VIDEO_BOX.query().equal(Video_.watched, false);
        builder.link(Video_.channel).equal(Channel_.subscribed, true);
        return builder.build().count();
    }

    /**
     * @return List of unwatched videos of subscribed channels, order by time (desc).
     */
    public static List<Video> getUnwatchedVideos() {
        QueryBuilder<Video> builder = VIDEO_BOX.query().equal(Video_.watched, false);
        builder.link(Video_.channel).equal(Channel_.subscribed, true);
        return builder.orderDesc(Video_.time).build().find();
    }

    /**
     * Get the videos of the channel identified by the given id.
     *
     * @param channelId Channel id to get the videos from
     * @return List of videos
     */
    public static List<Video> getVideosByChannelId(final long channelId) {
        return VIDEO_BOX.query().equal(Video_.channelId, channelId)
                .orderDesc(Video_.time).build().find();
    }

    /**
     * @return All videos
     */
    public static List<Video> getAll() {
        return VIDEO_BOX.getAll();
    }
}
