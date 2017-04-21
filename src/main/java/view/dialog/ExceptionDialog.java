package view.dialog;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Dialog to show Exceptions.
 *
 * @author Alkisum
 * @version 2.4
 * @since 1.0
 */
public final class ExceptionDialog {

    /**
     * Dialog width.
     */
    private static final int WIDTH = 600;

    /**
     * ExceptionDialog constructor.
     */
    private ExceptionDialog() {

    }

    /**
     * Show Exception dialog.
     *
     * @param exception Exception to show
     */
    public static void show(final Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        buildAlert(exception.getMessage(), stackTrace).showAndWait();
    }

    /**
     * Show Exception dialog.
     *
     * @param throwable Throwable to show
     */
    public static void show(final Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();

        buildAlert(throwable.getMessage(), stackTrace).showAndWait();
    }

    /**
     * Build the Alert dialog.
     *
     * @param message    Message to show
     * @param stackTrace StackTrace to show
     * @return Built alert
     */
    private static Alert buildAlert(final String message,
                                    final String stackTrace) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        expContent.setVgap(10.0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setPrefWidth(WIDTH);

        alert.getDialogPane().expandedProperty().addListener((l) ->
                Platform.runLater(() -> {
                    alert.getDialogPane().requestLayout();
                    Stage stage = (Stage)
                            alert.getDialogPane().getScene().getWindow();
                    stage.sizeToScene();
                }));

        return alert;
    }
}
