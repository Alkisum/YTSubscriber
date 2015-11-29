package controller.ui;

import controller.Manager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Pane extending GridPane and containing Channel attributes.
 *
 * @author Alkisum
 * @version 1.0
 * @since 20/04/15.
 */
public class ChannelPane extends GridPane {

    /**
     * Manager instance.
     */
    private final Manager mManager;

    /**
     * List of channels.
     */
    private final List<Channel> mChannels;

    /**
     * ChannelPane constructor.
     *
     * @param manager  Manager instance
     * @param channels List of channel to populate the GridPane
     */
    public ChannelPane(final Manager manager,
                       final List<Channel> channels) {
        mManager = manager;
        mChannels = new ArrayList<>(channels);
        setGUI();
    }

    /**
     * Set the GUI.
     */
    private void setGUI() {

        setHgap(10);
        setVgap(5);
        setPadding(new Insets(5, 10, 5, 10));

        for (int row = 0; row < mChannels.size(); row++) {

            Channel channel = mChannels.get(row);

            // CheckBox
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(channel.isChecked());
            checkBox.setOnAction(
                    event -> channel.setChecked(checkBox.isSelected()));
            add(checkBox, 0, row);

            // Name
            Label labelName = new Label(channel.getName());
            add(labelName, 1, row);

            // URL
            Label labelURL = new Label(channel.getUrl());
            add(labelURL, 2, row);

            // Edit button
            Button buttonEdit = new Button();
            Image imageEdit = new Image(getClass().getResourceAsStream(
                    "/view/icons/ic_edit_grey_18dp.png"));
            buttonEdit.setGraphic(new ImageView(imageEdit));
            buttonEdit.setOnAction(
                    (event) -> mManager.onEditChannelClicked(event, channel));
            add(buttonEdit, 3, row);

            // Delete button
            Button buttonDelete = new Button();
            Image imageDelete = new Image(getClass().getResourceAsStream(
                    "/view/icons/ic_delete_grey_18dp.png"));
            buttonDelete.setGraphic(new ImageView(imageDelete));
            buttonDelete.setOnAction(
                    (event) -> mManager.onDeleteChannelClicked(channel));
            add(buttonDelete, 4, row);
        }
        ColumnConstraints checkBoxConstraint = new ColumnConstraints();
        checkBoxConstraint.setHgrow(Priority.NEVER);
        ColumnConstraints nameConstraint = new ColumnConstraints();
        nameConstraint.setHgrow(Priority.NEVER);
        nameConstraint.setMinWidth(Double.NEGATIVE_INFINITY);
        ColumnConstraints urlConstraint = new ColumnConstraints();
        urlConstraint.setHgrow(Priority.ALWAYS);
        ColumnConstraints editConstraint = new ColumnConstraints();
        editConstraint.setHgrow(Priority.NEVER);
        ColumnConstraints deleteConstraint = new ColumnConstraints();
        deleteConstraint.setHgrow(Priority.NEVER);

        getColumnConstraints().addAll(checkBoxConstraint, nameConstraint,
                urlConstraint, editConstraint, deleteConstraint);
    }
}
