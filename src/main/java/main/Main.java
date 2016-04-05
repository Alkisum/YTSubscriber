package main;

import controller.Updater;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main class.
 *
 * @author Alkisum
 * @version 2.0
 * @since 19/04/15
 */
public class Main extends Application {

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/updater.fxml"));
        Scene scene = new Scene(loader.load(), Updater.WIDTH, Updater.HEIGHT);

        // Set instances to controller
        Updater updater = loader.getController();
        updater.setApplication(this);
        updater.initTheme(scene);

        primaryStage.setTitle("YTSubscriber");
        primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/icons/app.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main method.
     *
     * @param args Arguments
     */
    public static void main(final String[] args) {
        System.setProperty("prism.lcdtext", "false");
        //System.setProperty("prism.text", "t2k");
        launch(args);
    }
}
