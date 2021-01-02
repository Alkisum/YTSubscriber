package view.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import model.Video;
import utils.Videos;

import java.util.Optional;

/**
 * Dialog to set a start time to a video.
 *
 * @author Alkisum
 * @version 4.3
 * @since 4.1
 */
public final class SetStartTimeDialog {

    /**
     * SetStartTimeDialog constructor.
     */
    private SetStartTimeDialog() {

    }

    /**
     * Show the dialog.
     *
     * @param video Video to set the start time to
     */
    public static void show(final Video video) {
        Dialog<Video> dialog = new Dialog<>();
        dialog.setTitle("Set start time");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField startTimeTextField = new TextField(video.getStartTime());
        startTimeTextField.setTooltip(new Tooltip("Example: 1h35m42s"));

        grid.add(new Label("Start time:"), 0, 0);
        grid.add(startTimeTextField, 1, 0);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(startTimeTextField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String startTime = startTimeTextField.getText();
                if (startTime != null && startTime.isEmpty()) {
                    startTime = null;
                }
                video.setStartTime(startTime);
                return video;
            }
            return null;
        });

        Optional<Video> result = dialog.showAndWait();
        result.ifPresent(Videos::update);
    }
}
