package controller.tasks;

import database.Database;
import javafx.concurrent.Task;
import model.Video;

import java.util.List;

/**
 * Class to delete a list of videos.
 *
 * @author Alkisum
 * @version 2.4
 * @since 2.4
 */
public class VideoDeleter extends Task<Void> {

    /**
     * List of videos to delete.
     */
    private final List<Video> mVideos;

    /**
     * VideoDeleter constructor.
     *
     * @param videos List of videos
     */
    public VideoDeleter(final List<Video> videos) {
        mVideos = videos;
    }

    @Override
    protected final Void call() throws Exception {

        for (int i = 0; i < mVideos.size(); i++) {

            Video video = mVideos.get(i);

            updateProgress(i + 1, mVideos.size());
            updateMessage("Deleting " + video.getTitle() + "...");

            Database.deleteVideo(video);
        }

        return null;
    }
}
