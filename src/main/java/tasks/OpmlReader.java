package tasks;

import javafx.concurrent.Task;
import model.Channel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Channels;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Task reading the OPML file to import a list of channels to subscribe.
 *
 * @author Alkisum
 * @version 4.1
 * @since 1.0
 */
public class OpmlReader extends Task<List<Channel>> {

    /**
     * File to read.
     */
    private final File file;

    /**
     * OpmlReader constructor.
     *
     * @param file File to read
     */
    public OpmlReader(final File file) {
        this.file = file;
    }

    @Override
    protected final List<Channel> call() throws Exception {
        List<Channel> channels = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("outline");

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;

                String name = element.getAttribute("title");
                String url = element.getAttribute("xmlUrl");
                try {
                    new URL(url).toURI();
                    updateProgress(i + 1, nodeList.getLength());
                    updateMessage("Importing " + name + "...");
                    String ytId = url.substring(url.lastIndexOf("=") + 1);
                    Channels.create(new Channel(name, ytId));
                } catch (MalformedURLException | URISyntaxException e) {
                    // Nothing to do here, just don't add the channel
                }
            }
        }
        return channels;
    }
}
