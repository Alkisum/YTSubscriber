package model;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;
import io.objectbox.relation.ToMany;
import utils.Channels;

/**
 * Class defining channel.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
@Entity
public class Channel {

    /**
     * Channel id.
     */
    @Id
    private long id;

    /**
     * Channel name.
     */
    private String name;

    /**
     * Channel subscribed flag.
     */
    private boolean subscribed;

    /**
     * YT id.
     */
    private String ytId;

    /**
     * List of videos attached to the channel.
     */
    @Backlink
    private ToMany<Video> videos;

    /**
     * The channel is selected in the manager list.
     */
    @Transient
    private boolean checked;

    /**
     * Channel constructor.
     */
    public Channel() {

    }

    /**
     * Channel constructor.
     *
     * @param name Channel name
     * @param ytId YT id
     */
    public Channel(final String name, final String ytId) {
        this.id = 0;
        this.name = name;
        this.subscribed = false;
        this.ytId = ytId;
        this.checked = false;
    }

    /**
     * Channel constructor.
     *
     * @param id         Channel id
     * @param name       Channel name
     * @param subscribed Channel subscribed flag
     * @param ytId       YT id
     */
    public Channel(final int id, final String name, final boolean subscribed, final String ytId) {
        this.id = id;
        this.name = name;
        this.subscribed = subscribed;
        this.ytId = ytId;
        this.checked = false;
    }

    /**
     * @return Channel id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id Channel id to set
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * @return Channel name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Channel name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Channel subscribed flag
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * @param subscribed Channel subscribed flag to set
     */
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    /**
     * @return YT id
     */
    public String getYtId() {
        return ytId;
    }

    /**
     * @param ytId YT id to set
     */
    public void setYtId(final String ytId) {
        this.ytId = ytId;
    }

    /**
     * @return List of videos attached to the channel
     */
    public ToMany<Video> getVideos() {
        return videos;
    }

    /**
     * @param videos List of videos to attach to the channel
     */
    public void setVideos(final ToMany<Video> videos) {
        this.videos = videos;
    }

    /**
     * @return Channel is selected in manager list
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * @param checked Select the channel in manager list
     */
    public void setChecked(final boolean checked) {
        this.checked = checked;
    }

    /**
     * @return Channel URL
     */
    public String getUrl() {
        return Channels.getBaseUrl() + ytId;
    }

    @Override
    public final String toString() {
        return name + " (" + Channels.countUnwatchedVideos(id) + ")";
    }
}
