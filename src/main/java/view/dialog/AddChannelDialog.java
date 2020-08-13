package view.dialog;

import controller.ChannelController;
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
 * Dialog to add a channel.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
public final class AddChannelDialog {

    /**
     * AddChannelDialog constructor.
     */
    private AddChannelDialog() {

    }

    /**
     * Show the dialog.
     *
     * @param channelController ChannelController instance
     * @param btn               Button that triggered the dialog
     */
    public static void show(final ChannelController channelController, final Button btn) {
        Dialog<Channel> dialog = new Dialog<>();
        dialog.setTitle("Add Channel");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField name = new TextField();
        TextField urlId = new TextField();
        urlId.setTooltip(new Tooltip("Identifier following \"https://www.youtube.com/channel/\""));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("ID:"), 0, 1);
        grid.add(urlId, 1, 1);

        Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        applyButton.setDisable(true);

        name.textProperty().addListener((observable, oldValue, newValue) ->
                applyButton.setDisable(newValue.trim().isEmpty() || urlId.getText().isEmpty()));
        urlId.textProperty().addListener((observable, oldValue, newValue) ->
                applyButton.setDisable(newValue.trim().isEmpty() || name.getText().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            channelController.enableButton(btn);
            if (dialogButton == ButtonType.OK) {
                return new Channel(name.getText(), urlId.getText());
            }
            return null;
        });

        Optional<Channel> result = dialog.showAndWait();
        result.ifPresent(channelController::onChannelAdded);
    }
}
