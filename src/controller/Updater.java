package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for Subscription Updater.
 * @author Alkisum
 * @version 1.0
 * @since 19/04/15
 */
public class Updater implements Initializable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Updater.class);

    /**
     * Frame dimensions.
     */
    public static final int WIDTH = 800, HEIGHT = 600;

    public ListView<String> channelList;
    public SplitPane split;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        try {
            LOGGER.error("Initialise test");
            Database.create();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Initialize channel list
        ObservableList<String> items = FXCollections.observableArrayList(
                "Single", "Double", "Suite", "Family App");
        channelList.setItems(items);

    }

    /**
     * Triggered when Manage Button is clicked.
     * @param event Event
     */
    public final void onSettingsButtonClicked(final Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                   getClass().getResource("../view/manager.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Channel Manager");
            stage.setScene(new Scene(loader.load(),
                    Manager.WIDTH, Manager.HEIGHT));
            // Disable manager button
            Button btn = (Button) event.getSource();
            btn.setDisable(true);
            // Enable manager button
            stage.setOnCloseRequest(we -> btn.setDisable(false));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
