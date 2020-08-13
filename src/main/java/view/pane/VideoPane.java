package view.pane;

import config.Config;
import controller.VideoController;
import tasks.VideoDeleter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;
import utils.Thumbnails;
import utils.Videos;
import view.Icon;
import view.dialog.ConfirmationDialog;
import view.dialog.ErrorDialog;
import view.dialog.ExceptionDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class extending GridPane to show videos in video window.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
public class VideoPane extends GridPane implements VideoDeleter.OnVideosDeletedListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(VideoPane.class);

    /**
     * Play icon path.
     */
    private static final String PLAY = "/img/play.png";

    /**
     * Number of rows for each video pane.
     */
    private static final int ROW_COUNT = 4;

    /**
     * List of videos shown.
     */
    private final List<Video> videos;

    /**
     * VideoController instance.
     */
    private final VideoController videoController;

    /**
     * Progress message.
     */
    private final Label progressMessage;

    /**
     * Progress bar.
     */
    private final ProgressBar progressBar;

    /**
     * Video pane constructor.
     *
     * @param videos          List of videos
     * @param videoController VideoController instance
     * @param progressMessage Progress message from video window
     * @param progressBar     Progress bar from video window
     */
    public VideoPane(final List<Video> videos, final VideoController videoController,
                     final Label progressMessage,
                     final ProgressBar progressBar) {
        this.videos = new ArrayList<>(videos);
        this.videoController = videoController;
        this.progressMessage = progressMessage;
        this.progressBar = progressBar;
        setGUI();
    }

    /**
     * Set the general GridPane attributes and call a task to add the videos.
     */
    private void setGUI() {

        setHgap(10);
        setVgap(5);
        setPadding(new Insets(5, 10, 5, 10));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int row = 0;
                for (int i = 0; i < videos.size(); i++) {
                    updateProgress(i + 1, videos.size());
                    updateMessage("Getting " + videos.get(i).getTitle() + "...");
                    addVideo(videos.get(i), row);
                    row += ROW_COUNT;
                    if (i > 100) {
                        // Do not list more than a 100 videos
                        return null;
                    }
                }
                return null;
            }
        };

        progressMessage.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setVisible(true);

        new Thread(task).start();

        task.setOnSucceeded(t -> {
            progressMessage.textProperty().unbind();
            progressBar.progressProperty().unbind();
            progressMessage.setText("");
            progressBar.setProgress(0);
            progressBar.setVisible(false);
        });

        task.setOnFailed(t -> {
            try {
                progressMessage.textProperty().unbind();
                progressBar.progressProperty().unbind();
                progressMessage.setText("");
                progressBar.setProgress(0);
                progressBar.setVisible(false);
                throw task.getException();
            } catch (Throwable throwable) {
                ExceptionDialog.show(throwable);
                LOGGER.error(throwable);
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Add the video information to the pane.
     *
     * @param video Video to add
     * @param pRow  First row where to add information
     */
    private void addVideo(final Video video, final int pRow) {

        // Play icon
        ImageView play = new ImageView(new Image(getClass().getResourceAsStream(PLAY)));
        play.setStyle("-fx-cursor: hand;");
        setRowSpan(play, 3);
        play.setVisible(false);
        play.setOnMouseExited(event -> play.setVisible(false));
        play.setOnMouseClicked(event -> playVideo(video));

        // Thumbnail
        ImageView thumbnail;
        if (!video.getThumbnailFile().exists()) {
            Image image = new Image(getClass().getResourceAsStream(Thumbnails.DEFAULT_THUMBNAIL));
            thumbnail = new ImageView(image);
        } else {
            thumbnail = new ImageView(new Image(video.getThumbnailFile().toURI().toString()));
            thumbnail.setFitWidth(80);
            thumbnail.setPreserveRatio(true);
            thumbnail.setSmooth(true);
            thumbnail.setCache(true);
        }
        thumbnail.setStyle("-fx-cursor: hand;");
        thumbnail.setOnMouseEntered(event -> play.setVisible(true));
        setRowSpan(thumbnail, 3);

        // Title
        Label title = new Label(video.getTitle());
        title.setStyle("-fx-font-weight: bold");
        title.setAlignment(Pos.CENTER_LEFT);
        setColumnSpan(title, 5);
        setHgrow(title, Priority.ALWAYS);
        setVgrow(title, Priority.ALWAYS);

        // Channel name
        Label channelName = new Label("by " + video.getChannel().getTarget().getName());
        channelName.setAlignment(Pos.CENTER_LEFT);
        setColumnSpan(channelName, 5);
        setHgrow(channelName, Priority.ALWAYS);
        setVgrow(channelName, Priority.ALWAYS);
        channelName.setStyle("-fx-cursor: hand;");
        channelName.setOnMouseClicked(
                event -> videoController.selectChannel(video.getChannel().getTargetId()));

        // Date
        Label date = new Label(new PrettyTime().format(new Date(video.getTime())));
        date.setStyle("-fx-font-style: italic");
        date.setAlignment(Pos.CENTER_LEFT);
        setHgrow(date, Priority.ALWAYS);
        setVgrow(date, Priority.ALWAYS);

        // Duration
        Label duration;
        if (video.getDuration() < 1) {
            duration = new Label("");
        } else {
            duration = new Label(video.getFormatDuration());
        }
        duration.setStyle("-fx-font-weight: bold");
        GridPane.setHalignment(duration, HPos.RIGHT);

        // YouTube
        ImageView youtube = new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.YOUTUBE))));
        Tooltip.install(youtube, new Tooltip("Watch video on YouTube"));
        youtube.setStyle("-fx-cursor: hand;");
        youtube.setOnMouseClicked(event -> videoController.getApplication().getHostServices()
                .showDocument(video.getUrl()));

        // Watch
        Image image;
        Tooltip tooltip;
        if (video.isWatched()) {
            image = new Image(getClass().getResourceAsStream(Icon.getIcon(Icon.WATCHED)));
            tooltip = new Tooltip("Set video to unwatched");
        } else {
            image = new Image(getClass().getResourceAsStream(Icon.getIcon(Icon.UNWATCHED)));
            tooltip = new Tooltip("Set video to watched");
        }
        ImageView watched = new ImageView(image);
        Tooltip.install(watched, tooltip);
        watched.setStyle("-fx-cursor: hand;");
        watched.setOnMouseClicked(event -> updateVideoWatchedState(video, watched));

        // Delete video
        ImageView delete = new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.DELETE))));
        Tooltip.install(delete, new Tooltip("Delete video"));
        delete.setStyle("-fx-cursor: hand;");
        delete.setOnMouseClicked(event -> ConfirmationDialog.show(
                "Delete video",
                "Are you sure you want to delete the video " + video.getTitle() + "?",
                new VideoDeleter(this, video)));

        // Separator
        Separator separator = new Separator(Orientation.HORIZONTAL);
        setColumnSpan(separator, 6);

        Platform.runLater(() -> {
            int row = pRow;
            add(thumbnail, 0, row);
            add(play, 0, row);
            add(title, 1, row++);
            add(channelName, 1, row++);
            add(date, 1, row);
            add(duration, 2, row);
            add(youtube, 3, row);
            add(watched, 4, row);
            add(delete, 5, row++);
            add(separator, 0, row);
        });
    }

    /**
     * Play the video with Streamlink.
     *
     * @param video Video to play
     */
    private static void playVideo(final Video video) {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                executeStreamlink(video);
                return null;
            }
        }).start();
    }

    /**
     * Execute Streamlink to read the given video.
     *
     * @param video Video to play
     */
    private static void executeStreamlink(final Video video) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            List<String> command = new ArrayList<>();
            command.add("streamlink");
            if (Config.getValue(Config.PROP_MEDIA_PLAYER) != null) {
                String mediaPlayer = Config.getValue(Config.PROP_MEDIA_PLAYER);
                command.add("-p");
                command.add(mediaPlayer);
            }
            command.add(video.getUrl());
            command.add("best");
            pb.command(command);
            pb.start();
        } catch (IOException e) {
            Platform.runLater(() -> {
                ErrorDialog.show("Streamlink not found",
                        "Streamlink cannot be found on your system."
                                + "\nPlease, make sure Streamlink is installed."
                                + "\n(https://streamlink.github.io/)");
                LOGGER.error(e);
                e.printStackTrace();
            });
        }
    }

    /**
     * Update the video watched state?
     *
     * @param video     Video to update
     * @param imageView Icon showing watched state
     */
    private void updateVideoWatchedState(final Video video,
                                         final ImageView imageView) {
        boolean watched = !video.isWatched();
        video.setWatched(watched);
        if (watched) {
            imageView.setImage(new Image(
                    getClass().getResourceAsStream(Icon.getIcon(Icon.WATCHED))));
        } else {
            imageView.setImage(new Image(
                    getClass().getResourceAsStream(Icon.getIcon(Icon.UNWATCHED))));
        }
        Videos.update(video);
        videoController.refreshChannelList();
    }

    @Override
    public final void onVideosDeleted() {
        videoController.refreshVideoList();
        videoController.refreshChannelList();
    }
}
