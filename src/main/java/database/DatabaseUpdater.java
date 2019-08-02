package database;

import config.Config;
import javafx.concurrent.Task;
import model.Channel;
import model.Video;

import java.util.List;

/**
 * Utility class to update the database.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.2
 */
final class DatabaseUpdater {

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
                updateMessage("Updating video duration of "
                        + video.getTitle() + "...");
                long duration = 0;
                if (Config.getValue(Config.PROP_API_KEY) != null) {
                    duration = Video.retrieveDuration(video.getYtId());
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
                updateMessage("Updating channel URL of "
                        + channel.getName() + "...");
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
                updateMessage("Updating video URL of "
                        + video.getTitle() + "...");
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
                updateMessage("Updating video time of "
                        + video.getTitle() + "...");
                String date = Database.getVideoDateFromDb(video.getId());
                long time = Video.DATE_FORMAT.parse(date).getTime();
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
            Video.refresh();
            Channel.refresh();
            return null;
        }
    };
}
