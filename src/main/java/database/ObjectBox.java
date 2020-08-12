package database;

import config.Config;
import io.objectbox.BoxStore;
import model.MyObjectBox;

import java.io.File;

/**
 * Singleton class providing ObjectBox database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class ObjectBox {

    /**
     * ObjectBox database.
     */
    private static BoxStore boxStore;

    /**
     * ObjectBox constructor.
     */
    private ObjectBox() {

    }

    /**
     * Initialize ObjectBox database.
     */
    private static void init() {
        boxStore = MyObjectBox.builder()
                .baseDirectory(new File(Config.USER_DIR))
                .name("subscriptions")
                .build();
    }

    /**
     * @return Initialized ObjectBox database.
     */
    public static BoxStore get() {
        if (boxStore == null) {
            init();
        }
        return boxStore;
    }

    /**
     * Close ObjectBox database.
     */
    public static void close() {
        if (boxStore != null && !boxStore.isClosed()) {
            boxStore.closeThreadResources();
        }
    }
}
