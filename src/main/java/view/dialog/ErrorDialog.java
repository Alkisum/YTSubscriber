package view.dialog;

import javafx.scene.control.Alert;

/**
 * Dialog showing error messages.
 *
 * @author Alkisum
 * @version 2.0
 * @since 29/11/15.
 */
public final class ErrorDialog {

    /**
     * ErrorDialog constructor.
     */
    private ErrorDialog() {

    }

    /**
     * Show the error dialog.
     *
     * @param title   Dialog title
     * @param message Dialog message
     */
    public static void show(final String title, final String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
