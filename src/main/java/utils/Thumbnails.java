package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for thumbnails.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Thumbnails {

    /**
     * Default thumbnail path (used when the video's thumbnail file cannot be found.
     */
    public static final String DEFAULT_THUMBNAIL = "/img/default_thumbnail.png";

    /**
     * Thumbnails constructor.
     */
    private Thumbnails() {

    }

    /**
     * Download the video thumbnail.
     *
     * @param srcUrl  Source URL to get the thumbnail from
     * @param dstFile Destination file where to copy the thumbnail to
     * @throws IOException An error occurred while downloading the thumbnail
     */
    public static void downloadThumbnail(final String srcUrl, final File dstFile)
            throws IOException {
        if (dstFile.getParentFile().exists() || dstFile.getParentFile().mkdirs()) {
            try (InputStream in = new URL(srcUrl).openStream()) {
                Files.copy(in, Paths.get(dstFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
