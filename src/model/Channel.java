package model;

/**
 * Class defining channel.
 * @author Alkisum
 * @version 1.0
 * @since 19/04/15.
 */
public class Channel {

    /**
     * Channel id.
     */
    private int mId;

    /**
     * Channel name.
     */
    private String mName;

    /**
     * Channel URL.
     */
    private String mURL;

    /**
     * The channel is selected ini the manager list.
     */
    private boolean mChecked;

    /**
     * Channel Constructor.
     * @param id Channel id
     * @param name Channel name
     * @param url Channel URL
     */
    public Channel(final int id, final String name, final String url) {
        mId = id;
        mName = name;
        mURL = url;
        mChecked = false;
    }

    /**
     * @return Channel id
     */
    public final int getId() {
        return mId;
    }

    /**
     * @return Channel name
     */
    public final String getName() {
        return mName;
    }

    /**
     * @return Channel URL
     */
    public final String getURL() {
        return mURL;
    }

    /**
     * @return Channel is selected in manager list
     */
    public final boolean isChecked() {
        return mChecked;
    }

    /**
     * @param checked Select the channel in manager list
     */
    public final void setChecked(final boolean checked) {
        mChecked = checked;
    }
}
