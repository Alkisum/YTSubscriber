package view;

import config.Config;

import java.io.IOException;

/**
 * Class handling themes.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.0
 */
public final class Theme {

    /**
     * Identifier for Classic theme.
     */
    public static final String CLASSIC = "classic";

    /**
     * Classic CSS for Updater.
     */
    private static final String CLASSIC_UPDATER_CSS = "/updater_classic.css";

    /**
     * Classic CSS for Manager.
     */
    private static final String CLASSIC_MANAGER_CSS = "/manager_classic.css";

    /**
     * Classic CSS for About dialog.
     */
    private static final String CLASSIC_ABOUT_CSS = "/about_classic.css";

    /**
     * Classic CSS for Progress dialog.
     */
    private static final String CLASSIC_PROGRESS_CSS = "/progress_classic.css";

    /**
     * Identifier for Dark theme.
     */
    public static final String DARK = "dark";

    /**
     * Dark CSS for Updater.
     */
    private static final String DARK_UPDATER_CSS = "/updater_dark.css";

    /**
     * Dark CSS for Manager.
     */
    private static final String DARK_MANAGER_CSS = "/manager_dark.css";

    /**
     * Dark CSS for About dialog.
     */
    private static final String DARK_ABOUT_CSS = "/about_dark.css";

    /**
     * Dark CSS for Progress dialog.
     */
    private static final String DARK_PROGRESS_CSS = "/progress_dark.css";

    /**
     * Theme constructor.
     */
    private Theme() {

    }

    /**
     * Get the theme written in the configuration file.
     *
     * @return Theme id
     * @throws IOException An exception occurred while getting the theme
     */
    public static String getTheme() throws IOException {
        String theme = Config.getValue(Config.PROP_THEME_KEY);
        if (theme == null || !isValid(theme)) {
            return CLASSIC;
        }
        return theme;
    }

    /**
     * Set the theme in the configuration file.
     *
     * @param theme Theme to store
     * @throws IOException An exception occurred while setting the theme
     */
    public static void setTheme(final String theme) throws IOException {
        if (!isValid(theme)) {
            return;
        }
        Config.setValue(Config.PROP_THEME_KEY, theme);
    }

    /**
     * Check if the given theme is valid.
     *
     * @param theme Theme to check
     * @return true if the theme is valid, false otherwise
     */
    private static boolean isValid(final String theme) {
        return theme.equals(CLASSIC) || theme.equals(DARK);
    }

    /**
     * @param theme Theme
     * @return Updater CSS according to the given theme
     */
    public static String getUpdaterCss(final String theme) {
        switch (theme) {
            case DARK:
                return DARK_UPDATER_CSS;
            case CLASSIC:
            default:
                return CLASSIC_UPDATER_CSS;
        }
    }

    /**
     * @param theme Theme
     * @return Manager CSS according to the given theme
     */
    public static String getManagerCss(final String theme) {
        switch (theme) {
            case DARK:
                return DARK_MANAGER_CSS;
            case CLASSIC:
            default:
                return CLASSIC_MANAGER_CSS;
        }
    }

    /**
     * @param theme Theme
     * @return About CSS according to the given theme
     */
    public static String getAboutCss(final String theme) {
        switch (theme) {
            case DARK:
                return DARK_ABOUT_CSS;
            case CLASSIC:
            default:
                return CLASSIC_ABOUT_CSS;
        }
    }

    /**
     * @param theme Theme
     * @return Progress CSS according to the given theme
     */
    public static String getProgressCss(final String theme) {
        switch (theme) {
            case DARK:
                return DARK_PROGRESS_CSS;
            case CLASSIC:
            default:
                return CLASSIC_PROGRESS_CSS;
        }
    }
}
