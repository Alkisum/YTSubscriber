package controller.tasks;

import database.Database;
import javafx.concurrent.Task;
import model.Video;

import java.util.List;

/**
 * Class to delete a list of videos.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.4
 */
public class VideoDeleter extends Task<Void> {

    /**
     * List of videos to delete.
     */
    private final List<Video> videos;

    /**
     * VideoDeleter constructor.
     *
     * @param videos List of videos
     */
    public VideoDeleter(final List<Video> videos) {
        this.videos = videos;
    }

    @Override
    protected final Void call() throws Exception {

        for (int i = 0; i < videos.size(); i++) {

            Video video = videos.get(i);

            updateProgress(i + 1, videos.size());
            updateMessage("Deleting " + video.getTitle() + "...");

            Database.deleteVideo(video);
        }

        return null;
    }
}
