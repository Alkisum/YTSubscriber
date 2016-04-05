package controller;

import controller.tasks.RssReader;
import view.dialog.AboutDialog;
import view.dialog.ConfirmationDialog;
import view.dialog.ErrorDialog;
import view.dialog.ExceptionDialog;
import view.pane.VideoPane;
import database.Database;
import exception.ExceptionHandler;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Channel;
import model.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.Icon;
import view.Theme;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Subscription Updater.
 *
 * @author Alkisum
 * @version 2.0
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
     * Scene instance.
     */
    private Scene mScene;

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
     * RadioMenuItem for classic theme.
     */
    @FXML
    private RadioMenuItem mRadioMenuItemThemeClassic;

    /**
     * RadioMenuItem for dark theme.
     */
    @FXML
    private RadioMenuItem mRadioMenuItemThemeDark;

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

    /**
     * Button to check for new videos available.
     */
    @FXML
    private Button mButtonRefresh;

    @Override
    public final void initialize(final URL location,
                                 final ResourceBundle resources) {
        try {
            // Create database tables if they do not exist yet
            Database.init();
        } catch (ClassNotFoundException | SQLException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }

        setGui();
    }

    /**
     * Set GUI components, add listeners.
     */
    private void setGui() {
        // Initialize channel list
        refreshChannelList();
        // Initialize video list
        mPostRefreshId = UNWATCHED_VIDEOS_ID;
        refreshVideoList();

        mButtonSubscriptions.setText(
                "Subscriptions (" + mVideosShown.size() + ")");

        mListViewChannel.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        mPostRefreshId = newValue.getId();
                        refreshVideoList();
                    }
                });
    }

    /**
     * Read and set the theme set in the config file.
     *
     * @param scene Scene
     */
    public final void initTheme(final Scene scene) {
        mScene = scene;
        try {
            String theme = Theme.getTheme();
            setCss(theme);
            // Set the RadioMenuItem
            switch (theme) {
                case Theme.CLASSIC:
                    mRadioMenuItemThemeClassic.setSelected(true);
                    break;
                case Theme.DARK:
                    mRadioMenuItemThemeDark.setSelected(true);
                    break;
                default:
                    break;
            }
            // Set the refresh button image
            setButtonGraphics();
        } catch (IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Set CSS to scene.
     *
     * @param theme Theme
     */
    private void setCss(final String theme) {
        mScene.getStylesheets().clear();
        mScene.getStylesheets().add(getClass().getResource(
                Theme.getUpdaterCss(theme)).toExternalForm());
    }

    /**
     * Set the buttons graphics.
     */
    private void setButtonGraphics() {
        mButtonRefresh.setGraphic(new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.REFRESH)))));
    }

    /**
     * Triggered when the subscriptions button is clicked.
     */
    @FXML
    public final void onSubscriptionsClicked() {
        mPostRefreshId = UNWATCHED_VIDEOS_ID;
        refreshVideoList();
        mListViewChannel.getSelectionModel().clearSelection();
    }

    /**
     * Triggered when Manage menu item is clicked.
     *
     * @param actionEvent Event that triggered the method
     */
    @FXML
    public final void onManageClicked(final ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/manager.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Channel Manager");
            stage.getIcons().add(new Image(
                    getClass().getResourceAsStream("/icons/app.png")));
            stage.setScene(new Scene(loader.load(),
                    Manager.WIDTH, Manager.HEIGHT));
            Manager manager = loader.getController();
            manager.setStage(stage);
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

        mButtonRefresh.setDisable(true);

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
            refreshVideoList();

            List<Channel> notFoundChannels =
                    finalRssReaderOnSuccess.getNotFoundChannels();
            if (!notFoundChannels.isEmpty()) {
                String message = "";
                for (Channel channel : notFoundChannels) {
                    message += channel.getName() + "\n";
                }
                ErrorDialog.show("Not found channels", message);
            }

            mButtonRefresh.setDisable(false);
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

            mButtonRefresh.setDisable(false);
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
                new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Database.updateVideoWatchState(true);
                            refreshChannelList();
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
     * Triggered when the unwatch all menu item is clicked.
     */
    @FXML
    public final void onUnwatchAllClicked() {
        ConfirmationDialog.show(
                "Unwatch all videos",
                "Are you sure you want to set all the videos to unwatched?",
                new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Database.updateVideoWatchState(false);
                            refreshChannelList();
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
    public final void refreshVideoList() {
        try {
            if (mPostRefreshId == UNWATCHED_VIDEOS_ID) {
                mVideosShown = Database.getUnwatchedVideos();
            } else {
                mVideosShown = Database.getAllVideosByChannel(mPostRefreshId);
            }
            mScrollPaneVideo.setContent(new VideoPane(mVideosShown, this,
                    mProgressMessage, mProgressBar));
        } catch (ClassNotFoundException | SQLException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
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
        } catch (ParseException | IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }
}
