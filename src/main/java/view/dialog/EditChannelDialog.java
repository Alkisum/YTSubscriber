package view.dialog;

import controller.Manager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import model.Channel;

import java.util.Optional;

/**
 * Dialog to edit a channel.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public final class EditChannelDialog {

    /**
     * EditChannelDialog constructor.
     */
    private EditChannelDialog() {

    }

    /**
     * Show the dialog.
     *
     * @param manager Manager instance
     * @param btn     Button that triggered the dialog
     * @param channel Channel to edit
     */
    public static void show(final Manager manager, final Button btn,
                            final Channel channel) {
        Dialog<Channel> dialog = new Dialog<>();
        dialog.setTitle("Edit Channel");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField name = new TextField(channel.getName());
        TextField urlId = new TextField(channel.getYtId());
        urlId.setTooltip(new Tooltip("Identifier following \"https://www.youtube.com/channel/\""));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("ID:"), 0, 1);
        grid.add(urlId, 1, 1);

        Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.OK);

        name.textProperty().addListener((observable, oldValue, newValue) ->
                applyButton.setDisable(newValue.trim().isEmpty() || urlId.getText().isEmpty()));
        urlId.textProperty().addListener((observable, oldValue, newValue) ->
                applyButton.setDisable(newValue.trim().isEmpty() || name.getText().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            manager.enableButton(btn);
            if (dialogButton == ButtonType.OK) {
                channel.setName(name.getText());
                channel.setYtId(urlId.getText());
                return channel;
            }
            return null;
        });

        Optional<Channel> result = dialog.showAndWait();
        result.ifPresent(manager::onChannelEdited);
    }
}
