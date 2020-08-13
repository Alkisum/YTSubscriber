package controller;

import tasks.OpmlReader;
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
import utils.Channels;
import view.Icon;
import view.Theme;
import view.dialog.AddChannelDialog;
import view.dialog.ConfirmationDialog;
import view.dialog.EditChannelDialog;
import view.dialog.ExceptionDialog;
import view.pane.ChannelPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for channel window.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
public class ChannelController implements Initializable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ChannelController.class);

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
    private ProgressBar progressBar;

    /**
     * Progress message.
     */
    @FXML
    private Label progressMessage;

    /**
     * List of channels.
     */
    private List<Channel> channelList;

    @Override
    public final void initialize(final URL location, final ResourceBundle resources) {
        channelList = Channels.getAllOrderByName();
        showChannel(false);
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
        stage.getScene().getStylesheets().add(
                getClass().getResource(Theme.getChannelCss(theme)).toExternalForm());
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
        showChannel(false);
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
        progressMessage.textProperty().bind(opmlReader.messageProperty());
        progressBar.progressProperty().bind(opmlReader.progressProperty());
        progressBar.setVisible(true);

        new Thread(opmlReader).start();
        opmlReader.setOnSucceeded(t -> {
            progressMessage.textProperty().unbind();
            progressBar.progressProperty().unbind();
            progressMessage.setText("");
            progressBar.setProgress(0);
            progressBar.setVisible(false);
            showChannel(true);
        });

        opmlReader.setOnFailed(t -> {
            try {
                progressMessage.textProperty().unbind();
                progressBar.progressProperty().unbind();
                progressMessage.setText("");
                progressBar.setProgress(0);
                progressBar.setVisible(false);
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
                new Task<>() {
                    @Override
                    protected Void call() throws IOException {
                        Channels.delete(getSelectedChannels());
                        showChannel(true);
                        return null;
                    }
                });
    }

    /**
     * Triggered when the user clicks on the enable / disable subscription button.
     *
     * @param channel Channel to update
     */
    public final void onSetChannelSubscriptionClicked(final Channel channel) {
        channel.setSubscribed(!channel.isSubscribed());
        Channels.update(channel);
        showChannel(true);
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
                new Task<>() {
                    @Override
                    protected Void call() throws IOException {
                        Channels.delete(channel);
                        showChannel(true);
                        return null;
                    }
                });
    }

    /**
     * Triggered when a channel is added.
     *
     * @param channel Channel with created information
     */
    public final void onChannelAdded(final Channel channel) {
        Channels.create(channel);
        showChannel(true);
    }

    /**
     * Triggered when a channel is edited.
     *
     * @param channel Channel with edited information
     */
    public final void onChannelEdited(final Channel channel) {
        Channels.update(channel);
        showChannel(true);
    }

    /**
     * Show the channels in the list.
     *
     * @param refresh Get the channels from database
     */
    private void showChannel(final boolean refresh) {
        if (refresh) {
            channelList = Channels.getAllOrderByName();
            checkBoxAll.setSelected(false);
        }
        scrollPaneChannel.setContent(new ChannelPane(this, channelList));
    }

    /**
     * Get selected channels.
     *
     * @return Array of selected channels
     */
    private Channel[] getSelectedChannels() {
        return channelList.stream().filter(Channel::isChecked).toArray(Channel[]::new);
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
