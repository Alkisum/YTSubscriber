package controller;

import database.MigrationHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import kotlin.collections.ArrayDeque;
import model.Channel;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import task.RssReader;
import task.VideoDeleter;
import utils.Channels;
import utils.Videos;
import view.Icon;
import view.Theme;
import view.dialog.AboutDialog;
import view.dialog.ConfirmationDialog;
import view.dialog.ErrorDialog;
import view.dialog.ExceptionDialog;
import view.pane.VideoPane;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Controller for video window.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
public class VideoController implements MigrationHelper.Listener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(VideoController.class);

    /**
     * Frame width.
     */
    public static final int WIDTH = 1024;

    /**
     * Frame height.
     */
    public static final int HEIGHT = 768;

    /**
     * Identifier for list of unwatched videos.
     */
    private static final int UNWATCHED_VIDEOS_ID = -1;

    /**
     * Application instance.
     */
    private Application application;

    /**
     * Scene instance.
     */
    private Scene scene;

    /**
     * List of videos shown in Video's scroll pane.
     */
    private List<Video> videosShown = new ArrayDeque<>();

    /**
     * Identifier to refresh the videos after calling {@link VideoController#onRefreshClicked()}.
     * The identifier is either a channel id or -1 for unwatched videos.
     */
    private long postRefreshId;

    /**
     * RadioMenuItem for classic theme.
     */
    @FXML
    private RadioMenuItem radioMenuItemThemeClassic;

    /**
     * RadioMenuItem for dark theme.
     */
    @FXML
    private RadioMenuItem radioMenuItemThemeDark;

    /**
     * Scroll pane containing the videos.
     */
    @FXML
    private ScrollPane scrollPaneVideo;

    /**
     * Progress message.
     */
    @FXML
    private Label progressMessage;

    /**
     * Progress bar.
     */
    @FXML
    private ProgressBar progressBar;

    /**
     * ListView containing the channel names.
     */
    @FXML
    private ListView<Channel> listViewChannel;

    /**
     * Button to show all the unwatched videos.
     */
    @FXML
    private Button buttonSubscriptions;

    /**
     * Button to check for new videos available.
     */
    @FXML
    private Button buttonRefresh;

    @Override
    public final void onMigrationFinished() {
        this.init();
    }

    /**
     * Set GUI components, populate lists, add listeners.
     */
    public void init() {
        // Initialize channel list
        refreshChannelList();

        // Initialize video list
        postRefreshId = UNWATCHED_VIDEOS_ID;
        refreshVideoList();

        buttonSubscriptions.setText("Subscriptions (" + videosShown.size() + ")");

        listViewChannel.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        postRefreshId = newValue.getId();
                        refreshVideoList();
                    }
                });
    }

    /**
     * Read and set the theme set in the config file.
     *
     * @param pScene Scene
     */
    public final void initTheme(final Scene pScene) {
        this.scene = pScene;
        String theme = Theme.getTheme();
        setCss(theme);
        // Set the RadioMenuItem
        switch (theme) {
            case Theme.CLASSIC:
                radioMenuItemThemeClassic.setSelected(true);
                break;
            case Theme.DARK:
                radioMenuItemThemeDark.setSelected(true);
                break;
            default:
                break;
        }
        // Set the refresh button image
        setButtonGraphics();
    }

    /**
     * Set CSS to scene.
     *
     * @param theme Theme
     */
    private void setCss(final String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                getClass().getResource(Theme.getVideoCss(theme)).toExternalForm());
    }

    /**
     * Set the buttons graphics.
     */
    private void setButtonGraphics() {
        buttonRefresh.setGraphic(new ImageView(Icon.get(Icon.REFRESH)));
    }

    /**
     * Triggered when the subscriptions button is clicked.
     */
    @FXML
    public final void onSubscriptionsClicked() {
        postRefreshId = UNWATCHED_VIDEOS_ID;
        refreshVideoList();
        listViewChannel.getSelectionModel().clearSelection();
    }

    /**
     * Triggered when Manage menu item is clicked.
     *
     * @param actionEvent Event that triggered the method
     */
    @FXML
    public final void onManageClicked(final ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/channel.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Channel Manager");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app.png")));
            stage.setScene(
                    new Scene(loader.load(), ChannelController.WIDTH, ChannelController.HEIGHT));
            ChannelController channelController = loader.getController();
            channelController.setStage(stage);
            // Disable manager button
            MenuItem menuItem = (MenuItem) actionEvent.getSource();
            menuItem.setDisable(true);
            // Enable manager button and refresh lists
            stage.setOnCloseRequest(we -> {
                menuItem.setDisable(false);
                refreshChannelList();
                refreshVideoList();
            });
            stage.show();
        } catch (IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when Refresh Button is clicked.
     */
    @FXML
    public final void onRefreshClicked() {
        RssReader rssReader = new RssReader(Channels.getAllOrderByName());

        buttonRefresh.setDisable(true);

        progressMessage.textProperty().bind(rssReader.messageProperty());
        progressBar.progressProperty().bind(rssReader.progressProperty());
        progressBar.setVisible(true);

        new Thread(rssReader).start();

        final RssReader finalRssReaderOnSuccess = rssReader;
        rssReader.setOnSucceeded(t -> {
            progressMessage.textProperty().unbind();
            progressBar.progressProperty().unbind();
            progressMessage.setText("");
            progressBar.setProgress(0);
            progressBar.setVisible(false);

            refreshChannelList();
            refreshVideoList();

            List<Channel> notFoundChannels = finalRssReaderOnSuccess.getNotFoundChannels();
            if (!notFoundChannels.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (Channel channel : notFoundChannels) {
                    message.append(channel.getName()).append("\n");
                }
                ErrorDialog.show("Not found channels", message.toString());
            }

            buttonRefresh.setDisable(false);
        });

        final RssReader finalRssReaderOnFailed = rssReader;
        rssReader.setOnFailed(t -> {
            try {
                progressMessage.textProperty().unbind();
                progressBar.progressProperty().unbind();
                progressMessage.setText("");
                progressBar.setProgress(0);
                progressBar.setVisible(false);
                throw finalRssReaderOnFailed.getException();
            } catch (Throwable throwable) {
                ExceptionDialog.show(throwable);
                LOGGER.error(throwable);
                throwable.printStackTrace();
            }

            buttonRefresh.setDisable(false);
        });
    }

    /**
     * Triggered when the watch all menu item is clicked.
     */
    @FXML
    public final void onWatchAllClicked() {
        ConfirmationDialog.show(
                "Watch all videos",
                "Are you sure you want to set all the videos to watched?",
                new Task<>() {
                    @Override
                    protected Void call() {
                        updateAllVideosWatchedState(true);
                        return null;
                    }
                }
        );
    }

    /**
     * Triggered when the unwatch all menu item is clicked.
     */
    @FXML
    public final void onUnwatchAllClicked() {
        ConfirmationDialog.show(
                "Unwatch all videos",
                "Are you sure you want to set all the videos to unwatched?",
                new Task<>() {
                    @Override
                    protected Void call() {
                        updateAllVideosWatchedState(false);
                        return null;
                    }
                }
        );
    }

    /**
     * Update all videos watched state.
     *
     * @param watched Watched state to set for all videos
     */
    private void updateAllVideosWatchedState(final boolean watched) {
        List<Video> videos = Videos.getAll();
        for (Video video : videos) {
            video.setWatched(watched);
        }
        Videos.update(videos.toArray(new Video[0]));
        refreshChannelList();
        refreshVideoList();
    }

    /**
     * Triggered when the delete all menu item is clicked.
     */
    @FXML
    public final void onDeleteAllClicked() {
        ConfirmationDialog.show(
                "Delete all videos",
                "Are you sure you want to delete all the videos?",
                new Task<>() {
                    @Override
                    protected Void call() {
                        deleteAllVideos();
                        return null;
                    }
                }
        );
    }

    /**
     * Delete all videos and their thumbnails.
     */
    private void deleteAllVideos() {
        VideoDeleter videoDeleter = new VideoDeleter(null, Videos.getAll().toArray(new Video[0]));

        progressMessage.textProperty().bind(videoDeleter.messageProperty());
        progressBar.progressProperty().bind(videoDeleter.progressProperty());
        progressBar.setVisible(true);

        new Thread(videoDeleter).start();

        videoDeleter.setOnSucceeded(t -> {
            progressMessage.textProperty().unbind();
            progressBar.progressProperty().unbind();
            progressMessage.setText("");
            progressBar.setProgress(0);
            progressBar.setVisible(false);
            refreshChannelList();
            refreshVideoList();
        });

        final VideoDeleter finalVideoDeleterOnFailed = videoDeleter;
        videoDeleter.setOnFailed(t -> {
            try {
                progressMessage.textProperty().unbind();
                progressBar.progressProperty().unbind();
                progressMessage.setText("");
                progressBar.setProgress(0);
                progressBar.setVisible(false);
                throw finalVideoDeleterOnFailed.getException();
            } catch (Throwable throwable) {
                ExceptionDialog.show(throwable);
                LOGGER.error(throwable);
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Retrieve channels from database and populate the list.
     */
    public final void refreshChannelList() {
        ObservableList<Channel> items = FXCollections.observableArrayList();
        items.addAll(Channels.getAllOrderByName());
        listViewChannel.setItems(items);
        buttonSubscriptions.setText("Subscriptions (" + Videos.countUnwatchedVideos() + ")");
    }

    /**
     * Refresh the video list.
     */
    public final void refreshVideoList() {
        if (postRefreshId == UNWATCHED_VIDEOS_ID) {
            videosShown = Videos.getUnwatchedVideos();
        } else {
            videosShown = Videos.getVideosByChannelId(postRefreshId);
        }
        scrollPaneVideo.setContent(new VideoPane(videosShown, this, progressMessage, progressBar));
    }

    /**
     * Select the channel in the list view with the given id.
     *
     * @param channelId Channel id to select
     */
    public final void selectChannel(final long channelId) {
        int index = getIndexFromListView(channelId);
        listViewChannel.scrollTo(index);
        listViewChannel.getSelectionModel().select(index);
    }

    /**
     * Find the channel with the given id in the list view and return its index.
     *
     * @param channelId Channel id to find
     * @return Index of the channel in the list view
     */
    private int getIndexFromListView(final long channelId) {
        List<Channel> channels = listViewChannel.getItems();
        for (int i = 0; i < channels.size(); i++) {
            if (channelId == channels.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return Application instance
     */
    public final Application getApplication() {
        return application;
    }

    /**
     * @param application Application instance to set
     */
    public final void setApplication(final Application application) {
        this.application = application;
    }

    /**
     * Triggered when the Exit item is clicked in the Menu.
     */
    public final void onExitClicked() {
        Platform.exit();
    }

    /**
     * Triggered when the classic theme is selected.
     */
    public final void onClassicThemeSelected() {
        try {
            setCss(Theme.CLASSIC);
            Theme.setTheme(Theme.CLASSIC);
            setButtonGraphics();
            refreshVideoList();
        } catch (IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the dark theme is selected.
     */
    public final void onDarkThemeSelected() {
        try {
            setCss(Theme.DARK);
            Theme.setTheme(Theme.DARK);
            setButtonGraphics();
            refreshVideoList();
        } catch (IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the About item from menu is clicked.
     */
    public final void onAboutClicked() {
        try {
            AboutDialog.show();
        } catch (ParseException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }
}
