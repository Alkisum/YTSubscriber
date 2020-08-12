package database;

import config.Config;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import javafx.concurrent.Task;
import model.Channel;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Videos;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility class to update the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.2
 */
final class DatabaseUpdater {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(DatabaseUpdater.class);

    /**
     * DatabaseUpdater constructor.
     */
    private DatabaseUpdater() {

    }

    /**
     * @return Task that updates video duration in database.
     */
    static Task<Void> getUpdateVideoDurationTask() {
        return UPDATE_VIDEO_DURATION;
    }

    /**
     * Task that updates video duration in database.
     */
    private static final Task<Void> UPDATE_VIDEO_DURATION = new Task<>() {
        @Override
        protected Void call() throws Exception {
            List<Video> videos = Database.getAllVideos();
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                updateProgress(i + 1, videos.size());
                updateMessage("Updating video duration of " + video.getTitle() + "...");
                long duration = 0;
                if (Config.getValue(Config.PROP_API_KEY) != null) {
                    duration = Videos.retrieveDuration(video.getYtId());
                }
                video.setDuration(duration);
            }
            Database.updateVideoDuration(videos);
            return null;
        }
    };

    /**
     * @return Task that updates channel URL in database.
     */
    static Task<Void> getUpdateChannelUrlTask() {
        return UPDATE_CHANNEL_URL;
    }

    /**
     * Task that updates channel URL in database.
     */
    private static final Task<Void> UPDATE_CHANNEL_URL = new Task<>() {
        @Override
        protected Void call() throws Exception {
            List<Channel> channels = Database.getAllChannels();
            for (int i = 0; i < channels.size(); i++) {
                Channel channel = channels.get(i);
                updateProgress(i + 1, channels.size());
                updateMessage("Updating channel URL of " + channel.getName() + "...");
                String url = Database.getChannelUrlFromDb(channel.getId());
                String id = url.substring(url.lastIndexOf("=") + 1);
                channel.setYtId(id);
            }
            Database.updateChannelYtId(channels);
            return null;
        }
    };

    /**
     * @return Task that updates video URL in database.
     */
    static Task<Void> getUpdateVideoUrlTask() {
        return UPDATE_VIDEO_URL;
    }

    /**
     * Task that updates video URL in database.
     */
    private static final Task<Void> UPDATE_VIDEO_URL = new Task<>() {
        @Override
        protected Void call() throws Exception {
            List<Video> videos = Database.getAllVideos();
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                updateProgress(i + 1, videos.size());
                updateMessage("Updating video URL of " + video.getTitle() + "...");
                String url = Database.getVideoUrlFromDb(video.getId());
                String id = url.substring(url.lastIndexOf("=") + 1);
                video.setYtId(id);
            }
            Database.updateVideoYtId(videos);
            return null;
        }
    };

    /**
     * @return Task that updates video time in database.
     */
    static Task<Void> getUpdateVideoTimeTask() {
        return UPDATE_VIDEO_TIME;
    }

    /**
     * Task that updates video time in database.
     */
    private static final Task<Void> UPDATE_VIDEO_TIME = new Task<>() {
        @Override
        protected Void call() throws Exception {
            List<Video> videos = Database.getAllVideos();
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                updateProgress(i + 1, videos.size());
                updateMessage("Updating video time of " + video.getTitle() + "...");
                String date = Database.getVideoDateFromDb(video.getId());
                long time = Videos.DATE_FORMAT.parse(date).getTime();
                video.setTime(time);
            }
            Database.updateVideoTime(videos);
            return null;
        }
    };

    /**
     * @return Task that refreshes the tables (remove unused columns).
     */
    static Task<Void> getRefreshTablesTask() {
        return REFRESH_TABLES;
    }

    /**
     * Task that refreshes the tables (remove unused columns).
     */
    private static final Task<Void> REFRESH_TABLES = new Task<>() {
        @Override
        protected Void call() throws Exception {
            Database.refreshVideo();
            Database.refreshChannel();
            return null;
        }
    };

    /**
     * @return Task migrating the data from SQLite to ObjectBox.
     */
    static Task<Void> getMigrateToObjectBoxTask() {
        return MIGRATE_TO_OBJECT_BOX;
    }

    /**
     * Task migrating the data from SQLite to ObjectBox.
     */
    private static final Task<Void> MIGRATE_TO_OBJECT_BOX = new Task<>() {
        @Override
        protected Void call() throws Exception {
            BoxStore store = ObjectBox.get();
            Box<Channel> channelBox = store.boxFor(Channel.class);
            Box<Video> videoBox = store.boxFor(Video.class);
            channelBox.removeAll();
            videoBox.removeAll();

            File thumbnailDir = new File(Videos.THUMBNAIL_PATH);
            File thumbnailDirTmp = new File(Config.USER_DIR + "tmp");
            if (!thumbnailDir.renameTo(thumbnailDirTmp) || !thumbnailDir.mkdir()) {
                throw new IOException("Video thumbnails cannot not be migrated.");
            }

            List<Channel> channels = Database.getAllChannels();
            for (int i = 0; i < channels.size(); i++) {
                Channel channel = channels.get(i);
                LOGGER.debug(channel.getName() + "\n");
                updateProgress(i + 1, channels.size());
                updateMessage("Migrating " + channel.getName() + "...");
                List<Video> videos = Database.getAllVideosByChannel(channel.getId());
                channel.setId(0);
                long storedChannelId = channelBox.put(channel);

                for (Video video : videos) {
                    LOGGER.debug("\t" + video.getTitle());
                    long oldId = video.getId();
                    video.setId(0);
                    video.getChannel().setTargetId(storedChannelId);
                    long storedVideoId = videoBox.put(video);
                    LOGGER.debug("\toldId: " + oldId + "\t newId: " + storedVideoId + "\n");
                    File thumbnail = new File(thumbnailDirTmp, oldId + Videos.THUMBNAIL_EXT);
                    boolean renamed = thumbnail.renameTo(
                            new File(thumbnailDir, storedVideoId + Videos.THUMBNAIL_EXT));
                    if (!renamed) {
                        LOGGER.error("Thumbnail of \"" + video.getTitle() + "\" cannot be renamed");
                    }
                }
            }
            if (!thumbnailDirTmp.delete()) {
                LOGGER.warn(thumbnailDirTmp.getAbsolutePath()
                        + " cannot be deleted after thumbnail migration");
            }
            return null;
        }
    };
}
