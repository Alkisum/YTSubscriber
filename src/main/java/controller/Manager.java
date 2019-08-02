package controller;

import controller.tasks.OpmlReader;
import view.dialog.AddChannelDialog;
import view.pane.ChannelPane;
import view.dialog.ConfirmationDialog;
import view.dialog.EditChannelDialog;
import view.dialog.ExceptionDialog;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.Icon;
import view.Theme;

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
 * @version 3.0
 * @since 1.0
 */
public class Manager implements Initializable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Manager.class);

    /**
     * Frame dimensions.
     */
    static final int WIDTH = 1024, HEIGHT = 768;

    /**
     * Stage instance.
     */
    private Stage stage;

    /**
     * Button to import channels.
     */
    @FXML
    private Button buttonImportChannels;

    /**
     * Button to add channel.
     */
    @FXML
    private Button buttonAddChannel;

    /**
     * Button to delete selected channels.
     */
    @FXML
    private Button buttonDeleteSelection;

    /**
     * CheckBox to select or deselect all the channels.
     */
    @FXML
    private CheckBox checkBoxAll;

    /**
     * ScrollPane displaying channel list.
     */
    @FXML
    private ScrollPane scrollPaneChannel;

    /**
     * Progress bar.
     */
    @FXML
    private ProgressBar progressBarManager;

    /**
     * Progress message.
     */
    @FXML
    private Label progressMessageManager;

    /**
     * List of channels.
     */
    private List<Channel> channelList;

    @Override
    public final void initialize(final URL location,
                                 final ResourceBundle resources) {
        try {
            channelList = collectChannels();
            showChannel(false);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Read the theme set in the config file.
     */
    private void initTheme() {
        try {
            setCss(Theme.getTheme());
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
        stage.getScene().getStylesheets().clear();
        stage.getScene().getStylesheets().add(getClass().getResource(
                Theme.getManagerCss(theme)).toExternalForm());
    }

    /**
     * Set the buttons graphics.
     */
    private void setButtonGraphics() {
        buttonImportChannels.setGraphic(new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.DOWNLOAD)))));
        buttonAddChannel.setGraphic(new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.ADD)))));
        buttonDeleteSelection.setGraphic(new ImageView(new Image(
                getClass().getResourceAsStream(Icon.getIcon(Icon.DELETE)))));
    }

    /**
     * Select all the channels.
     */
    @FXML
    public final void onSelectAll() {
        for (Channel channel : channelList) {
            channel.setChecked(checkBoxAll.isSelected());
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
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return;
        }
        OpmlReader opmlReader = new OpmlReader(selectedFile);
        progressMessageManager.textProperty().bind(
                opmlReader.messageProperty());
        progressBarManager.progressProperty().bind(
                opmlReader.progressProperty());
        progressBarManager.setVisible(true);

        new Thread(opmlReader).start();
        opmlReader.setOnSucceeded(t -> {
            try {
                progressMessageManager.textProperty().unbind();
                progressBarManager.progressProperty().unbind();
                progressMessageManager.setText("");
                progressBarManager.setProgress(0);
                progressBarManager.setVisible(false);
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
                progressMessageManager.textProperty().unbind();
                progressBarManager.progressProperty().unbind();
                progressMessageManager.setText("");
                progressBarManager.setProgress(0);
                progressBarManager.setVisible(false);
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
                    protected Object call() {
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
        EditChannelDialog.show(this, btn, channel);
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
                    protected Object call() {
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
     * @param name Channel name
     * @param ytId Channel YT id
     */
    public final void onChannelAdded(final String name, final String ytId) {
        try {
            Database.insertChannel(name, ytId);
            showChannel(true);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when a channel is edited.
     *
     * @param id   Channel id
     * @param name Channel name
     * @param ytId Channel YT id
     */
    public final void onChannelEdited(final int id, final String name,
                                      final String ytId) {
        try {
            Database.updateChannel(id, name, ytId);
            showChannel(true);
        } catch (SQLException | ClassNotFoundException | ExceptionHandler e) {
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
            channelList = Database.getAllChannels();
            checkBoxAll.setSelected(false);
        }
        scrollPaneChannel.setContent(
                new ChannelPane(this, channelList));
    }

    /**
     * Get selected channels.
     *
     * @return List of channel id
     */
    private List<Integer> getSelection() {
        return channelList.stream().filter(Channel::isChecked)
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
    final void setStage(final Stage stage) {
        this.stage = stage;
        initTheme();
    }
}
