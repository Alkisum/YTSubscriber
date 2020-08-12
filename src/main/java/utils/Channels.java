package utils;

import config.Config;
import database.ObjectBox;
import io.objectbox.Box;
import model.Channel;
import model.Channel_;
import model.Video;
import model.Video_;

import java.io.IOException;
import java.util.List;

/**
 * Utility class for channels.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Channels {

    /**
     * Default base URL for channels.
     */
    public static final String BASE_URL = "https://www.youtube.com/feeds/videos.xml?channel_id=";

    /**
     * Box storing channels.
     */
    private static final Box<Channel> CHANNEL_BOX = ObjectBox.get().boxFor(Channel.class);

    /**
     * Box storing videos.
     */
    private static final Box<Video> VIDEO_BOX = ObjectBox.get().boxFor(Video.class);

    /**
     * Channels constructor.
     */
    private Channels() {

    }

    /**
     * @return All channels order by name.
     */
    public static List<Channel> getAllOrderByName() {
        return CHANNEL_BOX.query().order(Channel_.name).build().find();
    }

    /**
     * Create channels.
     *
     * @param channels Channels to create
     */
    public static void create(final Channel... channels) {
        CHANNEL_BOX.put(channels);
    }

    /**
     * Update channels.
     *
     * @param channels Channels to update
     */
    public static void update(final Channel... channels) {
        CHANNEL_BOX.put(channels);
    }

    /**
     * Delete channels.
     *
     * @param channels Channels to delete
     * @throws IOException An error occurred while deleting the video thumbnail file
     */
    public static void delete(final Channel... channels) throws IOException {
        for (Channel channel : channels) {
            Videos.delete(channel.getVideos().toArray(new Video[0]));
            CHANNEL_BOX.remove(channel);
        }
    }

    /**
     * Count the number of unwatched videos in the channel identified by the given id.
     *
     * @param channelId Channel id
     * @return Number of unwatched videos
     */
    public static long countUnwatchedVideos(final long channelId) {
        return VIDEO_BOX.query()
                .equal(Video_.channelId, channelId)
                .equal(Video_.watched, false)
                .build().count();
    }


    /**
     * Clean the channel from the videos watched and not existing anymore in the RSS Feeds.
     *
     * @param channel  Channel to clean
     * @param ytIdList List of video YT id existing in the RSS Feed.
     * @throws IOException An error occurred while deleting the video thumbnail file
     */
    public static void clean(final Channel channel, final List<String> ytIdList)
            throws IOException {
        for (Video video : channel.getVideos()) {
            if (!ytIdList.contains(video.getYtId()) && video.isWatched()) {
                Videos.delete(video);
            }
        }
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
}
