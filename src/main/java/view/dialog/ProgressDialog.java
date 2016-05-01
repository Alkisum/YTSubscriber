package view.dialog;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.Theme;

import java.io.IOException;

/**
 * Dialog to show task progress.
 *
 * @author Alkisum
 * @version 2.2
 * @since 2.2
 */
public class ProgressDialog {

    /**
     * Stage instance.
     */
    private final Stage dialogStage;

    /**
     * Progress bar.
     */
    private final ProgressBar mBar;

    /**
     * Progress message.
     */
    private final Label mMessage;

    /**
     * ProgressDialog's width.
     */
    public static final int WIDTH = 600;

    /**
     * ProgressDialog's height, only used as an estimation to center the dialog
     * in its parent.
     */
    public static final int HEIGHT = 100;

    /**
     * ProgressDialog constructor.
     *
     * @throws IOException An exception occurred while getting the theme
     */
    public ProgressDialog() throws IOException {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Update");

        mBar = new ProgressBar();
        mBar.setPrefWidth(WIDTH);
        mBar.setProgress(-1);
        mMessage = new Label("");

        final VBox hb = new VBox();
        hb.setPadding(new Insets(5, 5, 5, 5));
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.getChildren().addAll(mMessage, mBar);

        Scene scene = new Scene(hb);
        scene.getStylesheets().add(ProgressDialog.class.getResource(
                Theme.getProgressCss(Theme.getTheme())).toExternalForm());
        dialogStage.setScene(scene);
    }

    /**
     * Show the ProgressDialog.
     *
     * @param task Task to bind the progress dialog to
     * @param x    ProgressDialog's X position
     * @param y    ProgressDialog's Y position
     */
    public final void show(final Task<?> task, final double x, final double y) {
        mBar.progressProperty().bind(task.progressProperty());
        mMessage.textProperty().bind(task.messageProperty());
        dialogStage.getScene().getWindow().setX(x);
        dialogStage.getScene().getWindow().setY(y);
        dialogStage.show();
    }

    /**
     * Dismiss the ProgressDialog.
     */
    public final void dismiss() {
        mBar.progressProperty().unbind();
        mMessage.textProperty().unbind();
        dialogStage.close();
    }
}