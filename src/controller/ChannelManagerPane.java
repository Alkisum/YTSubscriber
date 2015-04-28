package controller;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import model.Channel;

/**
 * Pane extending HBox and containing Channel attributes.
 * @author Alkisum
 * @version 1.0
 * @since 20/04/15.
 */
public class ChannelManagerPane extends HBox {

    /**
     * Manager instance.
     */
    private Manager mManager;

    /**
     * Channel instance.
     */
    private Channel mChannel;

    /**
     * ChannelManagerPane constructor.
     * @param manager Manager instance
     * @param channel Channel instance
     */
    public ChannelManagerPane(final Manager manager, final Channel channel) {
        mManager = manager;
        mChannel = channel;
        setGUI();
    }

    /**
     * Set the GUI.
     */
    private void setGUI() {

        // Checkbox
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(mChannel.isChecked());
        checkBox.setOnAction(
                (event) -> mChannel.setChecked(checkBox.isSelected()));

        // Name and URL in GridPane for column constraints (same width)
        GridPane gridPane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(30);
        gridPane.getColumnConstraints().add(columnConstraints);
        Text textName = new Text(mChannel.getName());
        Text textURL = new Text(mChannel.getURL());
        gridPane.add(textName, 0, 0);
        gridPane.add(textURL, 1, 0);

        // Edit button
        Button buttonEdit = new Button("Edit");
        Image imageEdit = new Image(getClass().getResourceAsStream(
                "../view/icons/ic_edit_black_18dp.png"));
        buttonEdit.setGraphic(new ImageView(imageEdit));
        buttonEdit.setOnAction(
                (event) -> mManager.onEditChannelClicked(event, mChannel));

        // Delete button
        Button buttonDelete = new Button("Delete");
        Image imageDelete = new Image(getClass().getResourceAsStream(
                "../view/icons/ic_delete_black_18dp.png"));
        buttonDelete.setGraphic(new ImageView(imageDelete));
        buttonDelete.setOnAction(
                (event) -> mManager.onDeleteChannelClicked(mChannel));

        // HBox settings
        setSpacing(5);
        setHgrow(gridPane, Priority.ALWAYS);
        getChildren().addAll(checkBox, gridPane, buttonEdit, buttonDelete);
    }
}
