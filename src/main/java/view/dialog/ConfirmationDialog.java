package view.dialog;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class to show a confirmation dialog.
 *
 * @author Alkisum
 * @version 2.0
 * @since 28/11/15.
 */
public final class ConfirmationDialog {

    /**
     * ConfirmationDialog constructor.
     */
    private ConfirmationDialog() {

    }

    /**
     * Show the confirmation dialog.
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @param task    Task to run when the dialog is confirmed
     */
    public static void show(final String title, final String message,
                            final Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            task.run();
        }
    }
}
