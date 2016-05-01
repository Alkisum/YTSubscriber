package view;

import java.io.IOException;

/**
 * Class defining icons used in the application.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public final class Icon {

    /**
     * Index for Add icon.
     */
    public static final int ADD = 0;

    /**
     * Index for Delete icon.
     */
    public static final int DELETE = 1;

    /**
     * Index for Edit icon.
     */
    public static final int EDIT = 2;

    /**
     * Index for File Download icon.
     */
    public static final int DOWNLOAD = 3;

    /**
     * Index for Subscriptions icon.
     */
    public static final int SUB_ON = 4;

    /**
     * Index for Subscriptions off icon.
     */
    public static final int SUB_OFF = 5;

    /**
     * Index for Sync icon.
     */
    public static final int REFRESH = 6;

    /**
     * Index for Visibility icon.
     */
    public static final int UNWATCHED = 7;

    /**
     * Index for Visibility off icon.
     */
    public static final int WATCHED = 8;

    /**
     * Index for YouTube icon.
     */
    public static final int YOUTUBE = 9;

    /**
     * Array of icon path for Classic theme.
     */
    private static final String[] CLASSIC_ICONS = new String[]{
            "/icons/ic_add_dark_18dp.png",
            "/icons/ic_delete_dark_18dp.png",
            "/icons/ic_edit_dark_18dp.png",
            "/icons/ic_file_download_dark_18dp.png",
            "/icons/ic_subscriptions_dark_18dp.png",
            "/icons/ic_subscriptions_off_dark_18dp.png",
            "/icons/ic_sync_dark_18dp.png",
            "/icons/ic_visibility_dark_18dp.png",
            "/icons/ic_visibility_off_dark_18dp.png",
            "/icons/ic_youtube_dark_18dp.png",
    };

    /**
     * Array of icon path for Dark theme.
     */
    private static final String[] DARK_ICONS = new String[]{
            "/icons/ic_add_light_18dp.png",
            "/icons/ic_delete_light_18dp.png",
            "/icons/ic_edit_light_18dp.png",
            "/icons/ic_file_download_light_18dp.png",
            "/icons/ic_subscriptions_light_18dp.png",
            "/icons/ic_subscriptions_off_light_18dp.png",
            "/icons/ic_sync_light_18dp.png",
            "/icons/ic_visibility_light_18dp.png",
            "/icons/ic_visibility_off_light_18dp.png",
            "/icons/ic_youtube_light_18dp.png",
    };

    /**
     * Icon constructor.
     */
    private Icon() {

    }

    /**
     * Return the icon path at the given index according to the current theme.
     *
     * @param index Index where the icon path is stored
     * @return Icon path
     */
    public static String getIcon(final int index) {
        try {
            switch (Theme.getTheme()) {
                case Theme.CLASSIC:
                    return CLASSIC_ICONS[index];
                case Theme.DARK:
                    return DARK_ICONS[index];
                default:
                    return CLASSIC_ICONS[index];
            }
        } catch (IOException e) {
            return CLASSIC_ICONS[index];
        }
    }
}
