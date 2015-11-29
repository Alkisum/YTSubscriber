package model;

import config.Config;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Class defining video.
 *
 * @author Alkisum
 * @version 1.0
 * @since 14/05/15.
 */
public class Video {

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
     * Video URL.
     */
    private final String mUrl;

    /**
     * Video published date.
     */
    private final String mDate;

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
     * Video constructor.
     *
     * @param title        Video title
     * @param url          Video URL
     * @param date         Video published date
     * @param thumbnailUrl Thumbnail URL
     * @param channelId    Channel's video id
     */
    public Video(final String title, final String url,
                 final String date, final String thumbnailUrl,
                 final int channelId) {
        mTitle = title;
        mUrl = url;
        mDate = date;
        mThumbnailUrl = thumbnailUrl;
        mChannelId = channelId;
    }

    /**
     * Video constructor.
     *
     * @param id           Video id
     * @param title        Video title
     * @param url          Video URL
     * @param date         Video published date
     * @param thumbnailUrl Thumbnail URL
     * @param thumbnail    Thumbnail fail
     * @param watched      True if the video has been watched, false otherwise
     * @param channelId    Channel's video id
     */
    public Video(final int id, final String title, final String url,
                 final String date, final String thumbnailUrl,
                 final File thumbnail, final boolean watched,
                 final int channelId) {
        mId = id;
        mTitle = title;
        mUrl = url;
        mDate = date;
        mThumbnailUrl = thumbnailUrl;
        mThumbnail = thumbnail;
        mWatched = watched;
        mChannelId = channelId;
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
        return mUrl;
    }

    /**
     * @return Video published date
     */
    public final String getDate() {
        return mDate;
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
}
