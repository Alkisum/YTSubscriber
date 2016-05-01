package view.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import view.Theme;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dialog showing information about the application.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public final class AboutDialog {

    /**
     * Dialog dimensions.
     */
    private static final double WIDTH = 400.0, HEIGHT = 300.0;

    /**
     * Date format to parse the build date from the version number.
     */
    private static final SimpleDateFormat PARSER =
            new SimpleDateFormat("yyyyMMdd");

    /**
     * Date format to format the build date parsed from the version number.
     */
    private static final SimpleDateFormat FORMATER =
            new SimpleDateFormat("MMMM dd, yyyy");

    /**
     * AboutDialog constructor.
     */
    private AboutDialog() {

    }

    /**
     * Show the About dialog.
     *
     * @throws ParseException An error occurred while parsing the build date
     *                        from the version number
     * @throws IOException    An exception occurred while setting the theme
     */
    public static void show() throws ParseException, IOException {
        Dialog dialog = new Dialog();
        dialog.setWidth(WIDTH);
        dialog.setHeight(HEIGHT);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                AboutDialog.class.getResource(
                        Theme.getAboutCss(Theme.getTheme())).toExternalForm());
        dialog.setTitle("About");
        dialog.initStyle(StageStyle.UTILITY);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(20, 40, 20, 40));

        ImageView logo = new ImageView(new Image(
                AboutDialog.class.getResourceAsStream("/icons/app.png")));
        vBox.getChildren().add(logo);

        Package p = AboutDialog.class.getPackage();
        String title;
        String version;
        String date;
        String website;
        if (p.getImplementationTitle() != null
                && p.getImplementationVersion() != null
                && p.getImplementationVendor() != null) {
            title = p.getImplementationTitle();
            String[] fullVersion = p.getImplementationVersion().split("_");
            version = fullVersion[0];
            date = FORMATER.format(PARSER.parse(fullVersion[1]));
            website = p.getImplementationVendor();
        } else {
            title = "Title";
            version = "x.x";
            date = FORMATER.format(new Date());
            website = "Website";

        }
        String environment = System.getProperty("java.version") + "  "
                + System.getProperty("sun.arch.data.model") + "bit";

        Label titleLabel = new Label(title + " " + version);
        titleLabel.setFont(new Font(30));
        vBox.getChildren().add(titleLabel);
        vBox.getChildren().add(new Label("Built on " + date));
        vBox.getChildren().add(new Label(website));
        vBox.getChildren().add(new Label("JRE: " + environment));

        dialog.getDialogPane().setContent(vBox);
        dialog.showAndWait();
    }
}
