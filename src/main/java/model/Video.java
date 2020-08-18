package model;

import config.Config;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;
import utils.JsonIgnore;
import utils.Videos;

import java.io.File;
import java.io.IOException;

/**
 * Class defining video.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
@Entity
public class Video {

    /**
     * Video id.
     */
    @Id
    @JsonIgnore
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
     * Time the video should be started.
     */
    private String startTime;

    /**
     * Video's channel.
     */
    @JsonIgnore
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
        String url = getBaseUrl() + ytId;
        if (startTime != null && !startTime.isEmpty()) {
            url += "&t=" + startTime;
        }
        return url;
    }

    /**
     * @return Video published time
     */
    public long getTime() {
        return time;
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
     * @param duration Video duration in seconds
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
     * @return Time the video should be started
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime Time the video should be started
     */
    public void setStartTime(final String startTime) {
        this.startTime = startTime;
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
