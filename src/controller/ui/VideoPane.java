package controller.ui;

import controller.Updater;
import database.Database;
import exception.ExceptionHandler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class extending GridPane to show videos in Updater.
 *
 * @author Alkisum
 * @version 1.0
 * @since 19/11/15.
 */
public class VideoPane extends GridPane {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(VideoPane.class);

    /**
     * Play icon path.
     */
    private static final String PLAY = "/view/img/play.png";

    /**
     * Default thumbnail path (used when the video's thumbnail file cannot be
     * found.
     */
    private static final String DEFAULT_THUMBNAIL =
            "/view/img/default_thumbnail.png";

    /**
     * Icon for watched videos.
     */
    private static final String WATCHED =
            "/view/icons/ic_visibility_off_grey_18dp.png";

    /**
     * Icon for unwatched videos.
     */
    private static final String UNWATCHED =
            "/view/icons/ic_visibility_grey_18dp.png";

    /**
     * Icon for YouTube link.
     */
    private static final String YOUTUBE =
            "/view/icons/ic_youtube_grey_18dp.png";

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

        Task<List<Video>> task = new Task<List<Video>>() {
            @Override
            protected List<Video> call() throws Exception {
                int row = 0;
                for (int i = 0; i < mVideos.size(); i++) {
                    updateProgress(i + 1, mVideos.size());
                    updateMessage("Getting " + mVideos.get(i).getTitle()
                            + "...");
                    addVideo(mVideos.get(i), row);
                    row += ROW_COUNT;
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
     * @throws ParseException         Exception while parsing the published date
     */
    private void addVideo(final Video video, final int pRow)
            throws SQLException, ExceptionHandler, ClassNotFoundException,
            ParseException {

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
        setColumnSpan(title, 3);
        setHgrow(title, Priority.ALWAYS);
        setVgrow(title, Priority.ALWAYS);

        // Channel name
        Label channelName = new Label("by " + Database.getChannelNameById(
                video.getChannelId()));
        channelName.setAlignment(Pos.CENTER_LEFT);
        setColumnSpan(channelName, 3);
        setHgrow(channelName, Priority.ALWAYS);
        setVgrow(channelName, Priority.ALWAYS);

        // Date
        Label date = new Label(new PrettyTime().format(
                Video.DATE_FORMAT.parse(video.getDate())));
        date.setStyle("-fx-font-style: italic");
        date.setAlignment(Pos.CENTER_LEFT);
        setHgrow(date, Priority.ALWAYS);
        setVgrow(date, Priority.ALWAYS);

        // YouTube
        ImageView youtube = new ImageView(new Image(
                getClass().getResourceAsStream(YOUTUBE)));
        setVgrow(youtube, Priority.ALWAYS);
        youtube.setStyle("-fx-cursor: hand;");
        youtube.setOnMouseClicked(
                event -> mUpdater.getApplication().getHostServices()
                        .showDocument(video.getUrl()));

        // Watch
        Image image;
        if (video.isWatched()) {
            image = new Image(getClass().getResourceAsStream(WATCHED));
        } else {
            image = new Image(getClass().getResourceAsStream(UNWATCHED));
        }
        ImageView watched = new ImageView(image);
        watched.setStyle("-fx-cursor: hand;");
        watched.setOnMouseClicked(
                event -> updateVideoWatchedState(video, watched));

        // Separator
        Separator separator = new Separator(Orientation.HORIZONTAL);
        setColumnSpan(separator, 4);
        setHgrow(separator, Priority.ALWAYS);

        Platform.runLater(() -> {
            int row = pRow;
            add(thumbnail, 0, row);
            add(play, 0, row);
            add(title, 1, row++);
            add(channelName, 1, row++);
            add(date, 1, row);
            add(youtube, 2, row);
            add(watched, 3, row++);
            add(separator, 0, row++);
        });
    }

    /**
     * Play the video with Livestreamer.
     *
     * @param video Video to play
     */
    private static void playVideo(final Video video) {
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
                            + "\nPlease, make sure Livestreamer is installed."
                            + "\n(http://docs.livestreamer.io/)");
            LOGGER.error(e);
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
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
            watched.setImage(new Image(
                    getClass().getResourceAsStream(UNWATCHED)));
        } else {
            watched.setImage(new Image(
                    getClass().getResourceAsStream(WATCHED)));
        }
        video.setWatched(!video.isWatched());
        mUpdater.refreshChannelList();
    }
}
