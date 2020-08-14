package task;

import javafx.concurrent.Task;
import model.Video;
import utils.Videos;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task fetching duration of videos.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public class DurationFetcher extends Task<Void> {

    /**
     * DurationFetcher constructor.
     */
    public DurationFetcher() {

    }

    @Override
    protected final Void call() throws IOException {
        // Get all videos with no duration
        List<Video> videos = Videos.getAll().stream()
                .filter(video -> video.getDuration() == 0)
                .collect(Collectors.toList());

        StringBuilder message = new StringBuilder();
        for (int i = 0; i < videos.size(); i++) {
            Video video = videos.get(i);
            updateProgress(i + 1, videos.size());
            updateMessage("Fetching duration for " + video.getTitle() + "...");
            try {
                video.setDuration(Videos.retrieveDuration(video.getYtId()));
            } catch (IOException e) {
                message.append("Cannot fetch duration for ").append(video.getTitle()).append(": ")
                        .append(e.getMessage()).append(System.lineSeparator());
            }
        }
        Videos.update(videos.toArray(new Video[0]));

        if (message.length() > 0) {
            throw new IOException(message.toString());
        }
        return null;
    }
}
