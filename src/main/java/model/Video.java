package model;

import config.Config;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;
import utils.Videos;

import java.io.File;
import java.io.IOException;

/**
 * Class defining video.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
@Entity
public class Video {

    /**
     * Video id.
     */
    @Id
    private long id;

    /**
     * Video title.
     */
    private String title;

    /**
     * Video published time.
     */
    private long time;

    /**
     * Thumbnail URL.
     */
    private String thumbnailUrl;

    /**
     * Video has been watched.
     */
    private boolean watched;

    /**
     * Video duration in seconds.
     */
    private long duration;

    /**
     * ID used by YT to identify the video.
     */
    private String ytId;

    /**
     * Video's channel.
     */
    private ToOne<Channel> channel;

    /**
     * Video constructor.
     */
    public Video() {

    }

    /**
     * Video constructor.
     *
     * @param title        Video title
     * @param time         Video published time
     * @param thumbnailUrl Thumbnail URL
     * @param duration     Video duration in seconds
     * @param ytId         YT id
     * @param channel      Video's channel
     */
    public Video(final String title, final long time, final String thumbnailUrl,
                 final long duration, final String ytId, final Channel channel) {
        this.title = title;
        this.time = time;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.ytId = ytId;
        this.channel.setTarget(channel);
    }

    /**
     * Video constructor.
     *
     * @param id           Video id
     * @param title        Video title
     * @param time         Video published time
     * @param thumbnailUrl Thumbnail URL
     * @param watched      True if the video has been watched, false otherwise
     * @param channelId    Channel's video id
     * @param duration     Video duration in seconds
     * @param ytId         YT id
     */
    public Video(final long id, final String title, final long time, final String thumbnailUrl,
                 final boolean watched, final int channelId, final long duration,
                 final String ytId) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.thumbnailUrl = thumbnailUrl;
        this.watched = watched;
        this.duration = duration;
        this.ytId = ytId;
        this.channel.setTargetId(channelId);
    }

    /**
     * @return Video id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id Video id to set
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * @return Video title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Video URL
     */
    public String getUrl() {
        return getBaseUrl() + ytId;
    }

    /**
     * @return Video published time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time Video published time to set
     */
    public void setTime(final long time) {
        this.time = time;
    }

    /**
     * @return Thumbnail URL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @return Thumbnail file
     */
    public File getThumbnailFile() {
        return new File(Videos.THUMBNAIL_PATH + id + Videos.THUMBNAIL_EXT);
    }

    /**
     * @return Video has been watched.
     */
    public boolean isWatched() {
        return watched;
    }

    /**
     * @param watched True if the video has been watched, false otherwise
     */
    public void setWatched(final boolean watched) {
        this.watched = watched;
    }

    /**
     * @return Video duration in seconds
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration Video duration in seconds to set
     */
    public void setDuration(final long duration) {
        this.duration = duration;
    }

    /**
     * @return YT id
     */
    public String getYtId() {
        return ytId;
    }

    /**
     * @param ytId YT id to set
     */
    public void setYtId(final String ytId) {
        this.ytId = ytId;
    }

    /**
     * @return Video's channel
     */
    public ToOne<Channel> getChannel() {
        return channel;
    }

    /**
     * @param channel Video's channel to set
     */
    public void setChannel(final ToOne<Channel> channel) {
        this.channel = channel;
    }

    /**
     * @return Formatted video duration
     */
    public String getFormatDuration() {
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
     * Get the video URL (without id) stored in the configuration file.
     *
     * @return Video base URL
     */
    private static String getBaseUrl() {
        try {
            String baseUrl = Config.getValue(Config.PROP_VIDEO_URL_KEY);
            if (baseUrl == null) {
                return Videos.BASE_URL;
            }
            return baseUrl;
        } catch (IOException e) {
            return Videos.BASE_URL;
        }
    }
}
