package controller;

import controller.tasks.RssReader;
import controller.ui.ConfirmationDialog;
import controller.ui.ErrorDialog;
import controller.ui.ExceptionDialog;
import controller.ui.VideoPane;
import database.Database;
import exception.ExceptionHandler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Channel;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Subscription Updater.
 *
 * @author Alkisum
 * @version 1.0
 * @since 19/04/15
 */
public class Updater implements Initializable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Updater.class);

    /**
     * Frame dimensions.
     */
    public static final int WIDTH = 800, HEIGHT = 600;

    /**
     * Identifier for list of unwatched videos.
     */
    private static final int UNWATCHED_VIDEOS_ID = -1;

    /**
     * Application instance.
     */
    private Application mApplication;

    /**
     * List of videos shown in Video's scroll pane.
     */
    private List<Video> mVideosShown;

    /**
     * Identifier to refresh the videos after calling
     * {@link Updater#onRefreshClicked()}. The identifier is either a channel id
     * or -1 for unwatched videos.
     */
    private int mPostRefreshId;

    /**
     * Scroll pane containing the videos.
     */
    @FXML
    private ScrollPane mScrollPaneVideo;

    /**
     * Progress message.
     */
    @FXML
    private Label mProgressMessage;

    /**
     * Progress bar.
     */
    @FXML
    private ProgressBar mProgressBar;

    /**
     * ListView containing the channel names.
     */
    @FXML
    private ListView<Channel> mListViewChannel;

    /**
     * Button to show all the unwatched videos.
     */
    @FXML
    private Button mButtonSubscriptions;

    @Override
    public final void initialize(final URL location,
                                 final ResourceBundle resources) {
        try {
            // Create database tables if they do not exist yet
            Database.init();
            // Initialize channel list
            refreshChannelList();
            // Initialize video list
            mVideosShown = Database.getUnwatchedVideos();
            mPostRefreshId = UNWATCHED_VIDEOS_ID;
            mButtonSubscriptions.setText("Subscriptions ("
                    + mVideosShown.size() + ")");
            refreshVideoList();
        } catch (ClassNotFoundException | SQLException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the subscriptions button is clicked.
     */
    @FXML
    public final void onSubscriptionsClicked() {
        try {
            mVideosShown = Database.getUnwatchedVideos();
            mPostRefreshId = UNWATCHED_VIDEOS_ID;
            refreshVideoList();
            mListViewChannel.getSelectionModel().clearSelection();
        } catch (ClassNotFoundException | SQLException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when Manage Button is clicked.
     *
     * @param actionEvent Event that triggered the method
     */
    @FXML
    public final void onManageClicked(final ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/manager.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Channel Manager");
            stage.getIcons().add(new Image(
                    getClass().getResourceAsStream("/view/icons/app.png")));
            stage.setScene(new Scene(loader.load(),
                    Manager.WIDTH, Manager.HEIGHT));
            Manager manager = loader.getController();
            manager.setStage(stage);
            // Disable manager button
            Button btn = (Button) actionEvent.getSource();
            btn.setDisable(true);
            // Enable manager button and refresh lists
            stage.setOnCloseRequest(we -> {
                try {
                    btn.setDisable(false);
                    refreshChannelList();
                    mVideosShown = Database.getVideos(mVideosShown);
                    refreshVideoList();
                } catch (ClassNotFoundException | ExceptionHandler
                        | SQLException e) {
                    ExceptionDialog.show(e);
                    LOGGER.error(e);
                    e.printStackTrace();
                }
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
        RssReader rssReader = null;
        try {
            rssReader = new RssReader(Database.getAllChannels());
        } catch (ClassNotFoundException | SQLException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }

        if (rssReader == null) {
            return;
        }

        mProgressMessage.textProperty().bind(rssReader.messageProperty());
        mProgressBar.progressProperty().bind(rssReader.progressProperty());
        mProgressBar.setVisible(true);

        new Thread(rssReader).start();

        final RssReader finalRssReaderOnSuccess = rssReader;
        rssReader.setOnSucceeded(t -> {
            mProgressMessage.textProperty().unbind();
            mProgressBar.progressProperty().unbind();
            mProgressMessage.setText("");
            mProgressBar.setProgress(0);
            mProgressBar.setVisible(false);
            refreshChannelList();

            try {
                if (mPostRefreshId == UNWATCHED_VIDEOS_ID) {
                    mVideosShown = Database.getUnwatchedVideos();
                } else {
                    mVideosShown = Database.getAllVideosByChannel(
                            mPostRefreshId);
                }
                refreshVideoList();
            } catch (ClassNotFoundException | SQLException
                    | ExceptionHandler e) {
                ExceptionDialog.show(e);
                LOGGER.error(e);
                e.printStackTrace();
            }

            List<Channel> notFoundChannels =
                    finalRssReaderOnSuccess.getNotFoundChannels();
            if (!notFoundChannels.isEmpty()) {
                String message = "";
                for (Channel channel : notFoundChannels) {
                    message += channel.getName() + "\n";
                }
                ErrorDialog.show("Not found channels", message);
            }
        });

        final RssReader finalRssReader = rssReader;
        rssReader.setOnFailed(t -> {
            try {
                mProgressMessage.textProperty().unbind();
                mProgressBar.progressProperty().unbind();
                mProgressMessage.setText("");
                mProgressBar.setProgress(0);
                mProgressBar.setVisible(false);
                throw finalRssReader.getException();
            } catch (Throwable throwable) {
                ExceptionDialog.show(throwable);
                LOGGER.error(throwable);
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Triggered when the list view is clicked.
     */
    @FXML
    public final void onListViewClicked() {
        int id = mListViewChannel.getSelectionModel().getSelectedItem().getId();
        try {
            mVideosShown = Database.getAllVideosByChannel(id);
            mPostRefreshId = id;
            refreshVideoList();
        } catch (ClassNotFoundException | SQLException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the watch all button is clicked.
     */
    @FXML
    public final void onWatchAll() {
        ConfirmationDialog.show(
                "Watch all videos",
                "Are you sure you want to set all the videos to watched?",
                new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Database.updateVideoWatchState(true);
                            // Initialize channel list
                            refreshChannelList();
                            // Refresh video list
                            mVideosShown = Database.getVideos(mVideosShown);
                            refreshVideoList();
                        } catch (ClassNotFoundException | SQLException
                                | ExceptionHandler e) {
                            ExceptionDialog.show(e);
                            LOGGER.error(e);
                            e.printStackTrace();
                        }
                        return null;
                    }
                }
        );
    }

    /**
     * Triggered when the unwatch all button is clicked.
     */
    @FXML
    public final void onUnwatchAll() {
        ConfirmationDialog.show(
                "Unwatch all videos",
                "Are you sure you want to set all the videos to unwatched?",
                new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Database.updateVideoWatchState(false);
                            // Initialize channel list
                            refreshChannelList();
                            // Refresh video list
                            mVideosShown = Database.getVideos(mVideosShown);
                            refreshVideoList();
                        } catch (ClassNotFoundException | SQLException
                                | ExceptionHandler e) {
                            ExceptionDialog.show(e);
                            LOGGER.error(e);
                            e.printStackTrace();
                        }
                        return null;
                    }
                }
        );
    }

    /**
     * Retrieve channels from database and populate the list.
     */
    public final void refreshChannelList() {
        try {
            ObservableList<Channel> items = FXCollections.observableArrayList();
            List<Channel> channels = Database.getAllChannels();
            items.addAll(channels.stream().collect(Collectors.toList()));
            mListViewChannel.setItems(items);
            mButtonSubscriptions.setText("Subscriptions ("
                    + Database.countUnwatchedVideos() + ")");
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Refresh the video list.
     */
    private void refreshVideoList() {
        mScrollPaneVideo.setContent(new VideoPane(mVideosShown, this,
                mProgressMessage, mProgressBar));
    }

    /**
     * @return Application instance
     */
    public final Application getApplication() {
        return mApplication;
    }

    /**
     * @param application Application instance to set
     */
    public final void setApplication(final Application application) {
        mApplication = application;
    }
}
