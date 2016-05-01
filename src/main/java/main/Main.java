package main;

import config.Config;
import controller.Updater;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.dialog.ExceptionDialog;

import java.io.IOException;

/**
 * Main class.
 *
 * @author Alkisum
 * @version 2.2
 * @since 1.0
 */
public class Main extends Application {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * Minimum width of the window.
     */
    private static final double MIN_WIDTH = 600;

    /**
     * Minimum height of the window.
     */
    private static final double MIN_HEIGHT = 400;

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

        setWindow(scene);

        updater.updateDatabase();

        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            double width = scene.getWindow().getWidth();
            double height = scene.getWindow().getHeight();
            double x = scene.getWindow().getX();
            double y = scene.getWindow().getY();
            try {
                Config.setValue(Config.PROP_WIDTH_KEY, String.valueOf(width));
                Config.setValue(Config.PROP_HEIGHT_KEY, String.valueOf(height));
                Config.setValue(Config.PROP_X_KEY, String.valueOf(x));
                Config.setValue(Config.PROP_Y_KEY, String.valueOf(y));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Set window position and dimension with values stored in configuration
     * file.
     *
     * @param scene Scene with window to set
     */
    private void setWindow(final Scene scene) {
        double maxWidth = Screen.getPrimary().getBounds().getWidth();
        double maxHeight = Screen.getPrimary().getBounds().getHeight();
        double minX = Screen.getPrimary().getBounds().getMinX();
        double maxX = Screen.getPrimary().getBounds().getMaxX();
        double minY = Screen.getPrimary().getBounds().getMinY();
        double maxY = Screen.getPrimary().getBounds().getMaxY();
        try {
            String widthValue = Config.getValue(Config.PROP_WIDTH_KEY);
            String heightValue = Config.getValue(Config.PROP_HEIGHT_KEY);
            String xValue = Config.getValue(Config.PROP_X_KEY);
            String yValue = Config.getValue(Config.PROP_Y_KEY);
            if (widthValue != null && heightValue != null
                    && xValue != null && yValue != null) {
                double width = Double.parseDouble(widthValue);
                if (width < MIN_WIDTH) {
                    width = MIN_WIDTH;
                } else if (width > maxWidth) {
                    width = maxWidth;
                }
                double height = Double.parseDouble(heightValue);
                if (height < MIN_HEIGHT) {
                    height = MIN_HEIGHT;
                } else if (height > maxHeight) {
                    height = maxHeight;
                }
                double x = Double.parseDouble(xValue);
                if (x < minX) {
                    x = minX;
                } else if (x > maxX) {
                    x = maxX;
                }
                double y = Double.parseDouble(yValue);
                if (y < minY) {
                    y = minY;
                } else if (y > maxY) {
                    y = maxY;
                }

                scene.getWindow().setWidth(width);
                scene.getWindow().setHeight(height);
                scene.getWindow().setX(x);
                scene.getWindow().setY(y);
            }
        } catch (NumberFormatException | IOException e) {
            ExceptionDialog.show(e);
            LOGGER.error(e);
            e.printStackTrace();
            scene.getWindow().setWidth(Updater.WIDTH);
            scene.getWindow().setHeight(Updater.HEIGHT);
            scene.getWindow().centerOnScreen();
        }
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
