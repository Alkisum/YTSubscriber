package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import model.Channel;
import model.Video;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * JSON deserializer for Channel.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public class ChannelDeserializer implements JsonDeserializer<Channel> {

    @Override
    public final Channel deserialize(final JsonElement json, final Type type,
                                     final JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject channelJson = json.getAsJsonObject();
        Channel channel = new Channel();

        channel.setName(channelJson.get("name").getAsString());
        channel.setSubscribed(channelJson.get("subscribed").getAsBoolean());
        channel.setYtId(channelJson.get("ytId").getAsString());

        JsonArray addressesJson = channelJson.getAsJsonArray("videos");
        Type addressListType = new TypeToken<ArrayList<Video>>() {
        }.getType();
        ArrayList<Video> videos = context.deserialize(addressesJson, addressListType);
        channel.getVideos().addAll(videos);

        return channel;
    }
}
