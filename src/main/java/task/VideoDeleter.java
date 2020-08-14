package task;

import javafx.concurrent.Task;
import model.Video;
import utils.Videos;

import java.io.IOException;

/**
 * Class to delete a list of videos.
 *
 * @author Alkisum
 * @version 4.1
 * @since 2.4
 */
public class VideoDeleter extends Task<Void> {

    /**
     * Listener used when videos are deleted. Can be null if no post action is necessary.
     */
    private final Listener listener;

    /**
     * Videos to delete.
     */
    private final Video[] videos;

    /**
     * VideoDeleter constructor.
     *
     * @param listener Listener used when videos are deleted. Can be null if no post action is
     *                 necessary
     * @param videos   List of videos to delete
     */
    public VideoDeleter(final Listener listener, final Video... videos) {
        this.listener = listener;
        this.videos = videos;
    }

    @Override
    protected final Void call() throws IOException {
        for (int i = 0; i < videos.length; i++) {
            Video video = videos[i];
            updateProgress(i + 1, videos.length);
            updateMessage("Deleting " + video.getTitle() + "...");
            Videos.delete(video);
        }
        if (listener != null) {
            listener.onVideosDeleted();
        }
        return null;
    }

    /**
     * Listener used when videos are deleted.
     */
    public interface Listener {

        /**
         * Called when videos are deleted.
         */
        void onVideosDeleted();
    }
}
