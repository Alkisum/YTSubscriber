package database;

import javafx.concurrent.Task;
import model.Video;

import java.util.List;

/**
 * Utility class to update the database.
 *
 * @author Alkisum
 * @version 2.2
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
    private static final Task<Void> UPDATE_VIDEO_DURATION = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            List<Video> videos = Database.getAllVideos();
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                updateProgress(i + 1, videos.size());
                updateMessage("Updating video duration of "
                        + video.getTitle() + "...");
                long duration = Video.retrieveDuration(video.getUrl());
                video.setDuration(duration);
            }
            Database.updateVideoDuration(videos);
            return null;
        }
    };
}
