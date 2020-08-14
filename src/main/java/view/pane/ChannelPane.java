package view.pane;

import controller.ChannelController;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.Channel;
import view.Icon;

import java.util.ArrayList;
import java.util.List;

/**
 * Pane extending GridPane and containing Channel attributes.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
public class ChannelPane extends GridPane {

    /**
     * ChannelController instance.
     */
    private final ChannelController channelController;

    /**
     * List of channels.
     */
    private final List<Channel> channels;

    /**
     * ChannelPane constructor.
     *
     * @param channelController ChannelController instance
     * @param channels          List of channel to populate the GridPane
     */
    public ChannelPane(final ChannelController channelController, final List<Channel> channels) {
        this.channelController = channelController;
        this.channels = new ArrayList<>(channels);
        setGUI();
    }

    /**
     * Set the GUI.
     */
    private void setGUI() {

        setHgap(10);
        setVgap(5);
        setPadding(new Insets(5, 10, 5, 10));

        for (int row = 0; row < channels.size(); row++) {

            Channel channel = channels.get(row);

            // CheckBox
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(channel.isChecked());
            checkBox.setOnAction(event -> channel.setChecked(checkBox.isSelected()));
            add(checkBox, 0, row);

            // Name
            Label labelName = new Label(channel.getName());
            add(labelName, 1, row);

            // URL
            Label labelURL = new Label(channel.getUrl());
            add(labelURL, 2, row);

            // Subscribed button
            Button buttonSubscribed = new Button();
            Image imageSubscribed;
            Tooltip tooltip;
            if (channel.isSubscribed()) {
                tooltip = new Tooltip("Disable subscription");
                imageSubscribed = Icon.get(Icon.SUB_ON);
            } else {
                tooltip = new Tooltip("Enable subscription");
                imageSubscribed = Icon.get(Icon.SUB_OFF);
            }
            buttonSubscribed.setTooltip(tooltip);
            buttonSubscribed.setGraphic(new ImageView(imageSubscribed));
            buttonSubscribed.setOnAction(
                    (event) -> channelController.onSetChannelSubscriptionClicked(channel));
            add(buttonSubscribed, 3, row);

            // Edit button
            Button buttonEdit = new Button();
            buttonEdit.setTooltip(new Tooltip("Edit channel"));
            buttonEdit.setGraphic(new ImageView(Icon.get(Icon.EDIT)));
            buttonEdit.setOnAction(
                    (event) -> channelController.onEditChannelClicked(event, channel));
            add(buttonEdit, 4, row);

            // Delete button
            Button buttonDelete = new Button();
            buttonDelete.setTooltip(new Tooltip("Delete channel"));
            buttonDelete.setGraphic(new ImageView(Icon.get(Icon.DELETE)));
            buttonDelete.setOnAction((event) -> channelController.onDeleteChannelClicked(channel));
            add(buttonDelete, 5, row);
        }

        ColumnConstraints checkBoxConstraint = new ColumnConstraints();
        checkBoxConstraint.setHgrow(Priority.NEVER);
        ColumnConstraints nameConstraint = new ColumnConstraints();
        nameConstraint.setHgrow(Priority.NEVER);
        nameConstraint.setMinWidth(Double.NEGATIVE_INFINITY);
        ColumnConstraints urlConstraint = new ColumnConstraints();
        urlConstraint.setHgrow(Priority.ALWAYS);
        ColumnConstraints subscribedConstraint = new ColumnConstraints();
        subscribedConstraint.setHgrow(Priority.NEVER);
        ColumnConstraints editConstraint = new ColumnConstraints();
        editConstraint.setHgrow(Priority.NEVER);
        ColumnConstraints deleteConstraint = new ColumnConstraints();
        deleteConstraint.setHgrow(Priority.NEVER);

        getColumnConstraints().addAll(checkBoxConstraint, nameConstraint,
                urlConstraint, subscribedConstraint, editConstraint,
                deleteConstraint);
    }
}
