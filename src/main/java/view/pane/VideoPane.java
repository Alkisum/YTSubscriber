package view.pane;

import controller.Updater;
import database.Database;
import exception.ExceptionHandler;
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
import view.Icon;
import view.dialog.ConfirmationDialog;
import view.dialog.ErrorDialog;
import view.dialog.ExceptionDialog;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class extending GridPane to show videos in Updater.
 *
 * @author Alkisum
 * @version 2.4
 * @since 1.0
 */
public class VideoPane extends GridPane {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(VideoPane.class);

    /**
     * Play icon path.
     */
    private static final String PLAY = "/img/play.png";

    /**
     * Default thumbnail path (used when the video's thumbnail file cannot be
     * found.
     */
    private static final String DEFAULT_THUMBNAIL =
            "/img/default_thumbnail.png";

    /**
     * Number of rows for each video pane.
     */
    private static final int ROW_COUNT = 4;

    /**
     * List of videos shown.
     */
    private final List<Video> mVideos;

    /**
     * Update instance.
     */
    private final Updater mUpdater;

    /**
     * Progress message.
     */
    private final Label mProgressMessage;

    /**
     * Progress bar.
     */
    private final ProgressBar mProgressBar;

    /**
     * Video pane constructor.
     *
     * @param videos          List of videos
     * @param updater         Update instance
     * @param progressMessage Progress message from Updater
     * @param progressBar     Progress bar from Updated
     */
    public VideoPane(final List<Video> videos, final Updater updater,
                     final Label progressMessage,
                     final ProgressBar progressBar) {
        mVideos = new ArrayList<>(videos);
        mUpdater = updater;
        mProgressMessage = progressMessage;
        mProgressBar = progressBar;
        setGUI();
    }

    /**
     * Set the general GridPane attributes and call a task to add the videos.
     */
    private void setGUI() {

        setHgap(10);
        setVgap(5);
        setPadding(new Insets(5, 10, 5, 10));

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int row = 0;
                for (int i = 0; i < mVideos.size(); i++) {
                    updateProgress(i + 1, mVideos.size());
                    updateMessage("Getting " + mVideos.get(i).getTitle()
                            + "...");
                    addVideo(mVideos.get(i), row);
                    row += ROW_COUNT;
                    if (i > 100) {
                        // Do not list more than a 100 videos
                        return null;
                    }
                }
                return null;
            }
        };

        mProgressMessage.textProperty().bind(task.messageProperty());
        mProgressBar.progressProperty().bind(task.progressProperty());
        mProgressBar.setVisible(true);

        new Thread(task).start();

        task.setOnSucceeded(t -> {
            mProgressMessage.textProperty().unbind();
            mProgressBar.progressProperty().unbind();
            mProgressMessage.setText("");
            mProgressBar.setProgress(0);
            mProgressBar.setVisible(false);
        });

        task.setOnFailed(t -> {
            try {
                mProgressMessage.textProperty().unbind();
                mProgressBar.progressProperty().unbind();
                mProgressMessage.setText("");
                mProgressBar.setProgress(0);
                mProgressBar.setVisible(false);
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
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private void addVideo(final Video video, final int pRow)
            throws SQLException, ExceptionHandler, ClassNotFoundException {

        // Play icon
        ImageView play = new ImageView(new Image(
                getClass().getResourceAsStream(PLAY)));
        play.setStyle("-fx-cursor: hand;");
        setRowSpan(play, 3);
        play.setVisible(false);
        play.setOnMouseExited(event -> play.setVisible(false));
        play.setOnMouseClicked(event -> playVideo(video));

        // Thumbnail
        ImageView thumbnail;
        if (!video.getThumbnail().exists()) {
            Image image = new Image(getClass().getResourceAsStream(
                    DEFAULT_THUMBNAIL));
            thumbnail = new ImageView(image);
        } else {
            thumbnail = new ImageView(
                    new Image(video.getThumbnail().toURI().toString()));
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
        Label channelName = new Label("by " + Database.getChannelNameById(
                video.getChannelId()));
        channelName.setAlignment(Pos.CENTER_LEFT);
        setColumnSpan(channelName, 5);
        setHgrow(channelName, Priority.ALWAYS);
        setVgrow(channelName, Priority.ALWAYS);

        // Date
        Label date = new Label(new PrettyTime().format(
                new Date(video.getTime())));
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
        youtube.setOnMouseClicked(
                event -> mUpdater.getApplication().getHostServices()
                        .showDocument(video.getUrl()));

        // Watch
        Image image;
        Tooltip tooltip;
        if (video.isWatched()) {
            image = new Image(getClass().getResourceAsStream(
                    Icon.getIcon(Icon.WATCHED)));
            tooltip = new Tooltip("Set video to unwatched");
        } else {
            image = new Image(getClass().getResourceAsStream(
                    Icon.getIcon(Icon.UNWATCHED)));
            tooltip = new Tooltip("Set video to watched");
        }
        ImageView watched = new ImageView(image);
        Tooltip.install(watched, tooltip);
        watched.setStyle("-fx-cursor: hand;");
        watched.setOnMouseClicked(
                event -> updateVideoWatchedState(video, watched));

        // Delete video
        ImageView delete = new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.DELETE))));
        Tooltip.install(delete, new Tooltip("Delete video"));
        delete.setStyle("-fx-cursor: hand;");
        delete.setOnMouseClicked(
                event -> ConfirmationDialog.show("Delete video",
                        "Are you sure you want to delete the video "
                                + video.getTitle() + "?",
                        deleteVideo(video)));

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
     * Play the video with Livestreamer.
     *
     * @param video Video to play
     */
    private static void playVideo(final Video video) {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(
                            "livestreamer " + video.getUrl() + " best");
                    process.waitFor();
                } catch (InterruptedException e) {
                    ExceptionDialog.show(e);
                    LOGGER.error(e);
                    e.printStackTrace();
                } catch (IOException e) {
                    ErrorDialog.show("Livestreamer not found",
                            "Livestreamer cannot be found on your system."
                                    + "\nPlease, make sure Livestreamer is "
                                    + "installed."
                                    + "\n(http://docs.livestreamer.io/)");
                    LOGGER.error(e);
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
                return null;
            }
        }).start();
    }

    /**
     * Update the video watched state?
     *
     * @param video   Video to update
     * @param watched Watched state: true if video watched, false otherwise
     */
    private void updateVideoWatchedState(final Video video,
                                         final ImageView watched) {
        try {
            Database.updateVideoWatchState(video, !video.isWatched());
        } catch (ClassNotFoundException | SQLException
                | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
        if (video.isWatched()) {
            watched.setImage(new Image(getClass().getResourceAsStream(
                    Icon.getIcon(Icon.UNWATCHED))));
        } else {
            watched.setImage(new Image(getClass().getResourceAsStream(
                    Icon.getIcon(Icon.WATCHED))));
        }
        video.setWatched(!video.isWatched());
        mUpdater.refreshChannelList();
    }

    /**
     * Delete the given video.
     *
     * @param video Video to delete
     * @return Task deleting the given video
     */
    private Task deleteVideo(final Video video) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Database.deleteVideo(video);
                    mUpdater.refreshVideoList();
                } catch (ClassNotFoundException | SQLException
                        | ExceptionHandler e) {
                    ExceptionDialog.show(e);
                    LOGGER.error(e);
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
