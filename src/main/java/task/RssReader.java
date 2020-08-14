package task;

import config.Config;
import javafx.application.Platform;
import javafx.concurrent.Task;
import model.Channel;
import model.Video;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Channels;
import utils.Videos;
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
 * @version 4.1
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
    private final List<Channel> channels;

    /**
     * List of not found channels.
     */
    private final List<Channel> notFoundChannels = new ArrayList<>();

    /**
     * RssReader constructor.
     *
     * @param channels List of channels
     */
    public RssReader(final List<Channel> channels) {
        this.channels = channels;
    }

    @Override
    protected final Void call() throws Exception {
        boolean durationError = false;

        updateMessage("Initializing...");

        for (int c = 0; c < channels.size(); c++) {

            Channel channel = channels.get(c);

            // Create a YT ID list to check whether there are videos in the database that have been
            // watched and not in the feed anymore
            List<String> ytIds = new ArrayList<>();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc;
            try {
                doc = dBuilder.parse(channel.getUrl());
            } catch (IOException e) {
                notFoundChannels.add(channel);
                // Jump to next channel
                continue;
            }

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("entry");

            updateProgress(c + 1, channels.size());
            updateMessage("Reading " + channel.getName() + " feed...");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node nodeEntry = nodeList.item(i);

                if (nodeEntry.getNodeType() == Node.ELEMENT_NODE) {

                    Element eltEntry = (Element) nodeEntry;

                    // YT ID
                    String ytId = getText(eltEntry, "yt:videoId");
                    if (ytId == null) {
                        continue;
                    }

                    // Title
                    String title = getText(eltEntry, "title");

                    // URL
                    Node nodeUrl = getNode(eltEntry, "link");
                    String url = null;
                    if (nodeUrl.getNodeType() == Node.ELEMENT_NODE) {
                        Element eltUrl = (Element) nodeUrl;
                        url = eltUrl.getAttribute("href");
                    }

                    // Date
                    String date = getText(eltEntry, "published");
                    Date parsedDate = PUBLISHED_FORMAT.parse(date);

                    // Thumbnail
                    Node nodeMedia = getNode(eltEntry, "media:group");
                    String thumbnail = null;
                    if (nodeMedia.getNodeType() == Node.ELEMENT_NODE) {
                        Element eltMedia = (Element) nodeMedia;
                        Node nodeThumbnail = getNode(eltMedia, "media:thumbnail");
                        if (nodeThumbnail.getNodeType() == Node.ELEMENT_NODE) {
                            Element eltThumbnail = (Element) nodeThumbnail;
                            thumbnail = eltThumbnail.getAttribute("url");
                        }
                    }

                    if (!Videos.exists(ytId)) {
                        // Duration
                        long duration = 0;
                        if (url != null && Config.getValue(Config.PROP_API_KEY) != null) {
                            try {
                                duration = Videos.retrieveDuration(ytId);
                            } catch (IOException e) {
                                durationError = true;
                            }
                        }

                        // Save video
                        Videos.create(new Video(title, parsedDate.getTime(), thumbnail, duration,
                                ytId, channel));
                    }

                    ytIds.add(ytId);
                }
            }
            Channels.clean(channel, ytIds);
        }

        // Errors occurred when reading durations
        if (durationError) {
            Platform.runLater(() -> ErrorDialog.show("Duration error",
                    "An error occurred while reading the duration from videos.")
            );
        }
        return null;
    }

    /**
     * @return Channels not found while reading the feeds
     */
    public final List<Channel> getNotFoundChannels() {
        return notFoundChannels;
    }

    /**
     * Get the first node found with the given tag name in the given element.
     *
     * @param element Element to search
     * @param tagName Tag name to search
     * @return Node found
     */
    private Node getNode(final Element element, final String tagName) {
        return element.getElementsByTagName(tagName).item(0);
    }

    /**
     * Get the text of the first node found with the given tag name in the given element.
     *
     * @param element Element to search
     * @param tagName Tag name to search
     * @return Text found
     */
    private String getText(final Element element, final String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }
}
