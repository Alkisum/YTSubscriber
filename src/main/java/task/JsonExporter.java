package task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.concurrent.Task;
import utils.Channels;
import utils.Json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Task exporting channels and videos to JSON.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public class JsonExporter extends Task<Void> {

    /**
     * File to export to.
     */
    private final File file;

    /**
     * JsonExporter constructor.
     *
     * @param file File to export to
     */
    public JsonExporter(final File file) {
        this.file = file;
    }

    @Override
    protected final Void call() throws IOException {
        updateProgress(-1, -1);
        updateMessage("Exporting to " + file.getAbsolutePath());

        Gson gson = new GsonBuilder()
                .setExclusionStrategies(Json.EXCLUSION_STRATEGY)
                .setPrettyPrinting()
                .create();

        try (FileWriter fileWriter = new FileWriter(file)) {
            gson.toJson(Channels.getAll(), fileWriter);
        }
        return null;
    }
}
