package controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

/**
 * Dialog to add a channel.
 * @author Alkisum
 * @version 1.0
 * @since 19/04/15.
 */
public final class AddChannelDialog {

    /**
     * AddChannelDialog constructor.
     */
    private AddChannelDialog() {

    }

    /**
     * Show the dialog.
     * @param manager Manager instance
     * @param btn Button that triggered the dialog
     */
    public static void show(final Manager manager, final Button btn) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Channel");

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        TextField url = new TextField();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("URL:"), 0, 1);
        grid.add(url, 1, 1);

        Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        applyButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            applyButton.setDisable(newValue.trim().isEmpty()
                    || url.getText().isEmpty());
        });
        url.textProperty().addListener((observable, oldValue, newValue) -> {
            applyButton.setDisable(newValue.trim().isEmpty()
                    || name.getText().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(name::requestFocus);

        // Convert the result to a username-password-pair
        // when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            manager.enableButton(btn);
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(name.getText(), url.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(channel -> manager.onChannelAdded(
                channel.getKey(), channel.getValue()));
    }
}
