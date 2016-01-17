package controller;

import controller.tasks.OpmlReader;
import controller.ui.AddChannelDialog;
import controller.ui.ChannelPane;
import controller.ui.ConfirmationDialog;
import controller.ui.EditChannelDialog;
import controller.ui.ExceptionDialog;
import database.Database;
import exception.ExceptionHandler;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Channel Manager.
 *
 * @author Alkisum
 * @version 1.0
 * @since 19/04/15
 */
public class Manager implements Initializable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Manager.class);

    /**
     * Frame dimensions.
     */
    public static final int WIDTH = 800, HEIGHT = 600;

    /**
     * Stage instance.
     */
    private Stage mStage;

    /**
     * CheckBox to select or deselect all the channels.
     */
    @FXML
    private CheckBox mCheckBoxAll;

    /**
     * ScrollPane displaying channel list.
     */
    @FXML
    private ScrollPane mScrollPaneChannel;

    /**
     * Progress bar.
     */
    @FXML
    private ProgressBar mProgressBar;

    /**
     * Progress message.
     */
    @FXML
    private Label mProgressMessage;

    /**
     * List of channels.
     */
    private List<Channel> mChannelList;

    @Override
    public final void initialize(final URL location,
                                 final ResourceBundle resources) {
        try {
            mChannelList = collectChannels();
            showChannel(false);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Select all the channels.
     */
    @FXML
    public final void onSelectAll() {
        for (Channel channel : mChannelList) {
            channel.setChecked(mCheckBoxAll.isSelected());
        }
        try {
            showChannel(false);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Called when the Import button is clicked.
     */
    @FXML
    public final void onImportClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open OPML File");
        File selectedFile = fileChooser.showOpenDialog(mStage);

        if (selectedFile == null) {
            return;
        }
        OpmlReader opmlReader = new OpmlReader(selectedFile);
        mProgressMessage.textProperty().bind(opmlReader.messageProperty());
        mProgressBar.progressProperty().bind(opmlReader.progressProperty());
        mProgressBar.setVisible(true);

        new Thread(opmlReader).start();
        opmlReader.setOnSucceeded(t -> {
            try {
                mProgressMessage.textProperty().unbind();
                mProgressBar.progressProperty().unbind();
                mProgressMessage.setText("");
                mProgressBar.setProgress(0);
                mProgressBar.setVisible(false);
                showChannel(true);
            } catch (ClassNotFoundException | SQLException
                    | ExceptionHandler e) {
                ExceptionDialog.show(e);
                LOGGER.error(e);
                e.printStackTrace();
            }
        });

        opmlReader.setOnFailed(t -> {
            try {
                mProgressMessage.textProperty().unbind();
                mProgressBar.progressProperty().unbind();
                mProgressMessage.setText("");
                mProgressBar.setProgress(0);
                mProgressBar.setVisible(false);
                throw opmlReader.getException();
            } catch (Throwable throwable) {
                ExceptionDialog.show(throwable);
                LOGGER.error(throwable);
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Triggered when the user clicks on the add channel button.
     *
     * @param actionEvent Event that triggered the method
     */
    @FXML
    public final void onAddChannelClicked(final ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();
        btn.setDisable(true);
        AddChannelDialog.show(this, btn);
    }

    /**
     * Triggered when the user clicks on the delete selection button.
     */
    @FXML
    public final void onDeleteSelectionClicked() {
        ConfirmationDialog.show(
                "Delete selection",
                "Are you sure you want to delete the selected channels?",
                new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Database.deleteChannels(getSelection());
                            showChannel(true);
                        } catch (SQLException | ClassNotFoundException
                                | ExceptionHandler e) {
                            ExceptionDialog.show(e);
                            LOGGER.error(e);
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }

    /**
     * Triggered when the user clicks on the enable / disable subscription
     * button.
     *
     * @param channel Channel to update
     */
    public final void onSetChannelSubscriptionClicked(final Channel channel) {
        try {
            Database.updateChannelSubscription(channel.getId(),
                    !channel.isSubscribed());
            showChannel(true);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the user clicks on the edit channel button.
     *
     * @param actionEvent Event that triggered the method
     * @param channel     Channel to edit
     */
    public final void onEditChannelClicked(final ActionEvent actionEvent,
                                           final Channel channel) {
        Button btn = (Button) actionEvent.getSource();
        btn.setDisable(true);
        try {
            EditChannelDialog.show(this, btn, channel);
        } catch (IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the user clicks on the delete channel button.
     *
     * @param channel Channel to delete
     */
    public final void onDeleteChannelClicked(final Channel channel) {
        ConfirmationDialog.show(
                "Delete channel",
                "Are you sure you want to delete the channel?",
                new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Database.deleteChannel(channel.getId());
                            showChannel(true);
                        } catch (SQLException | ClassNotFoundException
                                | ExceptionHandler e) {
                            ExceptionDialog.show(e);
                            LOGGER.error(e);
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }

    /**
     * Triggered when a channel is added.
     *
     * @param name  Channel name
     * @param urlId Channel id in URL
     */
    public final void onChannelAdded(final String name, final String urlId) {
        try {
            Database.insertChannel(name, Channel.getBaseUrl() + urlId, true);
            showChannel(true);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler
                | IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when a channel is edited.
     *
     * @param id    Channel id
     * @param name  Channel name
     * @param urlId Channel id in URL
     */
    public final void onChannelEdited(final int id, final String name,
                                      final String urlId) {
        try {
            Database.updateChannel(id, name, Channel.getBaseUrl() + urlId);
            showChannel(true);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler
                | IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Show the channels in the list.
     *
     * @param refresh Get the channels from database
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private void showChannel(final boolean refresh)
            throws ClassNotFoundException, SQLException, ExceptionHandler {
        if (refresh) {
            mChannelList = Database.getAllChannels();
            mCheckBoxAll.setSelected(false);
        }
        mScrollPaneChannel.setContent(
                new ChannelPane(this, mChannelList));
    }

    /**
     * Get selected channels.
     *
     * @return List of channel id
     */
    private List<Integer> getSelection() {
        return mChannelList.stream().filter(Channel::isChecked)
                .map(Channel::getId).collect(Collectors.toList());
    }

    /**
     * Collect all the channels stored in the database.
     *
     * @return List of channels
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     * @throws ExceptionHandler       Exception while accessing config directory
     */
    private List<Channel> collectChannels() throws ClassNotFoundException,
            SQLException, ExceptionHandler {
        return Database.getAllChannels();
    }

    /**
     * Enable the given button, called from dialogs opened with this button.
     *
     * @param btn Button to enable
     */
    public final void enableButton(final Button btn) {
        btn.setDisable(false);
    }

    /**
     * Set the stage.
     *
     * @param stage Stage to set
     */
    public final void setStage(final Stage stage) {
        mStage = stage;
    }
}
