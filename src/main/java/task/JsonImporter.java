package task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import javafx.concurrent.Task;
import model.Channel;
import model.Video;
import utils.ChannelDeserializer;
import utils.Channels;
import utils.Json;
import utils.Thumbnails;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Task importing channels and videos from JSON.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public class JsonImporter extends Task<Void> {

    /**
     * File to import from.
     */
    private final File file;

    /**
     * JsonImporter constructor.
     *
     * @param file File to import from
     */
    public JsonImporter(final File file) {
        this.file = file;
    }

    @Override
    protected final Void call() throws IOException {
        // Read JSON file
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(Json.EXCLUSION_STRATEGY)
                .registerTypeAdapter(Channel.class, new ChannelDeserializer())
                .create();
        JsonReader jsonReader = new JsonReader(new FileReader(file));
        Channel[] channels = gson.fromJson(jsonReader, Channel[].class);

        // Delete all existing channels and videos
        Channels.deleteAll();

        // Import channels and videos read from JSON file
        for (int i = 0; i < channels.length; i++) {
            Channel channel = channels[i];
            updateProgress(i + 1, channels.length);
            updateMessage("Importing videos of " + channel.getName());
            Channels.save(channel);
            for (Video video : channel.getVideos()) {
                Thumbnails.downloadThumbnail(video.getThumbnailUrl(), video.getThumbnailFile());
            }
        }

        return null;
    }
}
