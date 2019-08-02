package controller.tasks;

import config.Config;
import database.Database;
import javafx.application.Platform;
import javafx.concurrent.Task;
import model.Channel;
import model.Video;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import view.dialog.ErrorDialog;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to retrieve and read RSS Feeds.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class RssReader extends Task<Void> {

    /**
     * Format of the published date written in the feed.
     */
    private static final SimpleDateFormat PUBLISHED_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'+00:00'");

    /**
     * List of channels to read.
     */
    private final List<Channel> mChannels;

    /**
     * List of not found channels.
     */
    private final List<Channel> mNotFoundChannels = new ArrayList<>();

    /**
     * RssReader constructor.
     *
     * @param channels List of channels
     */
    public RssReader(final List<Channel> channels) {
        mChannels = channels;
    }

    @Override
    protected final Void call() throws Exception {

        List<Video> videos = new ArrayList<>();
        boolean durationError = false;

        updateMessage("Initializing...");

        for (int c = 0; c < mChannels.size(); c++) {

            Channel channel = mChannels.get(c);

            // Create a YT ID list to check whether there are videos in the
            // database that have been watched and not in the feed anymore
            List<String> ytIds = new ArrayList<>();

            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc;
            try {
                doc = dBuilder.parse(channel.getUrl());
            } catch (IOException e) {
                mNotFoundChannels.add(channel);
                // Jump to next channel
                continue;
            }


            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("entry");

            updateProgress(c + 1, mChannels.size());
            updateMessage("Reading " + channel.getName() + " feed...");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node nodeEntry = nodeList.item(i);

                if (nodeEntry.getNodeType() == Node.ELEMENT_NODE) {

                    Element eltEntry = (Element) nodeEntry;

                    // YT ID
                    String ytId = eltEntry.getElementsByTagName("yt:videoId")
                            .item(0).getTextContent();
                    if (ytId == null) {
                        continue;
                    }

                    // Title
                    String title = eltEntry.getElementsByTagName("title")
                            .item(0).getTextContent();

                    // URL
                    Node nodeUrl = eltEntry.getElementsByTagName("link")
                            .item(0);
                    String url = null;
                    if (nodeUrl.getNodeType() == Node.ELEMENT_NODE) {
                        Element eltUrl = (Element) nodeUrl;
                        url = eltUrl.getAttribute("href");
                    }

                    // Date
                    String date = eltEntry.getElementsByTagName("published")
                            .item(0).getTextContent();
                    Date parsedDate = PUBLISHED_FORMAT.parse(date);

                    // Thumbnail
                    Node nodeMedia = eltEntry.getElementsByTagName(
                            "media:group").item(0);
                    String thumbnail = null;
                    if (nodeMedia.getNodeType() == Node.ELEMENT_NODE) {
                        Element eltMedia = (Element) nodeMedia;
                        Node nodeThumbnail = eltMedia.getElementsByTagName(
                                "media:thumbnail").item(0);
                        if (nodeThumbnail.getNodeType() == Node.ELEMENT_NODE) {
                            Element eltThumbnail = (Element) nodeThumbnail;
                            thumbnail = eltThumbnail.getAttribute("url");
                        }
                    }

                    if (!Database.videoExists(ytId)) {

                        // Duration
                        long duration = 0;
                        if (url != null && Config.getValue(
                                Config.PROP_API_KEY) != null) {
                            try {
                                duration = Video.retrieveDuration(ytId);
                            } catch (Exception e) {
                                durationError = true;
                            }
                        }

                        videos.add(new Video(
                                title,
                                parsedDate.getTime(),
                                thumbnail,
                                channel.getId(),
                                duration,
                                ytId));
                    }

                    ytIds.add(ytId);
                }
            }
            channel.clean(ytIds);
        }
        if (!videos.isEmpty()) {
            updateProgress(-1, 0);
            updateMessage("Downloading thumbnails...");
            Database.insertVideos(videos);
        }

        // Errors occurred when reading durations
        if (durationError) {
            Platform.runLater(() -> ErrorDialog.show("Duration error",
                    "An error occurred when reading the duration from videos.")
            );
        }
        return null;
    }

    /**
     * @return Channels not found while reading the feeds
     */
    public final List<Channel> getNotFoundChannels() {
        return mNotFoundChannels;
    }
}
