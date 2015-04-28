package controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Channel;
import model.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
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
    public static final int WIDTH = 600, HEIGHT = 400;

    /**
     * Vertical Box containing the list of channels.
     */
    public VBox mVBoxChannel;

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
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(e);
            showExceptionDialog(e);
        }
    }

    /**
     * Triggered when the user clicks on the add channel button.
     *
     * @param actionEvent Event that triggered the method
     */
    public final void onAddChannelClicked(final ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();
        btn.setDisable(true);
        AddChannelDialog.show(this, btn);
    }

    /**
     * Triggered when the user clicks on the delete selection button.
     */
    public final void onDeleteSelectionClicked() {
        try {
            Database.deleteChannels(getSelection());
            showChannel(true);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(e);
            showExceptionDialog(e);
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
        try {
            Database.deleteChannel(channel.getId());
            showChannel(true);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(e);
            showExceptionDialog(e);
        }
    }

    /**
     * Triggered when a channel is added.
     *
     * @param name Channel name
     * @param url  Channel URL
     */
    public final void onChannelAdded(final String name, final String url) {
        // Add channel to database
        try {
            Database.insertChannel(name, url);
            showChannel(true);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(e);
            showExceptionDialog(e);
        }
    }

    /**
     * Triggered when a channel is edited.
     *
     * @param id   Channel id
     * @param name Channel name
     * @param url  Channel URL
     */
    public final void onChannelEdited(final int id, final String name,
                                      final String url) {
        // Update channel in database
        try {
            Database.updateChannel(id, name, url);
            showChannel(true);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(e);
            showExceptionDialog(e);
        }
    }

    /**
     * Show the channels in the list.
     *
     * @param refresh Get the channels from database
     * @throws ClassNotFoundException Exception while trying to use JDBC driver
     * @throws SQLException           Exception while executing the select
     *                                statement
     */
    private void showChannel(final boolean refresh)
            throws ClassNotFoundException, SQLException {
        mVBoxChannel.getChildren().clear();
        if (refresh) {
            mChannelList = Database.getAllChannels();
        }
        for (Channel channel : mChannelList) {
            mVBoxChannel.getChildren().add(
                    new ChannelManagerPane(this, channel));
        }
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
     */
    private List<Channel> collectChannels() throws ClassNotFoundException,
            SQLException {
        return Database.getAllChannels();
    }

    /**
     * Select all the channels.
     *
     * @param actionEvent Event that triggered the method
     */
    public final void onSelectAll(final ActionEvent actionEvent) {
        CheckBox checkBox = (CheckBox) actionEvent.getSource();
        for (Channel channel : mChannelList) {
            channel.setChecked(checkBox.isSelected());
        }
        try {
            showChannel(false);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(e);
            showExceptionDialog(e);
        }
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
     * Show a dialog displaying the exception that occurred.
     *
     * @param exception Exception to display
     */
    private void showExceptionDialog(final Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(exception.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setPrefWidth(WIDTH);

        alert.showAndWait();
    }
}
