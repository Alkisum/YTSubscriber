package view;

import config.Config;

import java.io.IOException;

/**
 * Class handling themes.
 *
 * @author Alkisum
 * @version 4.1
 * @since 2.0
 */
public final class Theme {

    /**
     * Identifier for Classic theme.
     */
    public static final String CLASSIC = "classic";

    /**
     * Classic CSS for Video window.
     */
    private static final String CLASSIC_VIDEO_CSS = "/classic_video.css";

    /**
     * Classic CSS for Channel window.
     */
    private static final String CLASSIC_CHANNEL_CSS = "/classic_channel.css";

    /**
     * Classic CSS for About dialog.
     */
    private static final String CLASSIC_ABOUT_CSS = "/classic_about.css";

    /**
     * Classic CSS for Progress dialog.
     */
    private static final String CLASSIC_PROGRESS_CSS = "/classic_progress.css";

    /**
     * Identifier for Dark theme.
     */
    public static final String DARK = "dark";

    /**
     * Dark CSS for Video window.
     */
    private static final String DARK_VIDEO_CSS = "/dark_video.css";

    /**
     * Dark CSS for Channel window.
     */
    private static final String DARK_CHANNEL_CSS = "/dark_channel.css";

    /**
     * Dark CSS for About dialog.
     */
    private static final String DARK_ABOUT_CSS = "/dark_about.css";

    /**
     * Dark CSS for Progress dialog.
     */
    private static final String DARK_PROGRESS_CSS = "/dark_progress.css";

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
        return CLASSIC.equals(theme) || DARK.equals(theme);
    }

    /**
     * @param theme Theme
     * @return Video view CSS according to the given theme
     */
    public static String getVideoCss(final String theme) {
        switch (theme) {
            case DARK:
                return DARK_VIDEO_CSS;
            case CLASSIC:
            default:
                return CLASSIC_VIDEO_CSS;
        }
    }

    /**
     * @param theme Theme
     * @return Channel view CSS according to the given theme
     */
    public static String getChannelCss(final String theme) {
        switch (theme) {
            case DARK:
                return DARK_CHANNEL_CSS;
            case CLASSIC:
            default:
                return CLASSIC_CHANNEL_CSS;
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
