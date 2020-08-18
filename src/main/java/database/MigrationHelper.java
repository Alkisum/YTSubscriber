package database;

import config.Config;
import javafx.concurrent.Task;
import javafx.stage.Window;
import utils.ExceptionHandler;
import utils.Logger;
import view.dialog.ProgressDialog;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Helper class for database migration.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public class MigrationHelper {

    /**
     * Listener to get notified when the migration is finished.
     */
    private final Listener listener;

    /**
     * Parent window.
     */
    private final Window parentWindow;

    /**
     * Queue of migration tasks.
     */
    private final Queue<Task<?>> migrationTasks = new LinkedList<>();

    /**
     * Current schema version.
     */
    private int currentSchemaVersion;

    /**
     * Task to migrate database to schema version 6 (not implemented yet).
     */
    private final Task<Void> migrateToSchemaVersion6 = new Task<>() {
        @Override
        protected Void call() throws Exception {
            int seconds = 10;
            for (int i = 0; i < seconds; i++) {
                updateProgress(i + 1, seconds);
                updateMessage("Migrating " + i + " seconds...");
                Thread.sleep(1000);
            }
            currentSchemaVersion = 6;
            return null;
        }
    };

    /**
     * Migration Helper constructor.
     *
     * @param listener     Listener to get notified when the migration is finished
     * @param parentWindow Parent window
     */
    public MigrationHelper(final Listener listener, final Window parentWindow) {
        this.listener = listener;
        this.parentWindow = parentWindow;
        this.currentSchemaVersion = this.readSchemaVersion();
    }

    /**
     * Add migration tasks to queue.
     *
     * @return true if there is pending migration, false otherwise
     */
    public final boolean hasPendingMigration() {
        // if (currentSchemaVersion < 6) {
        //     migrationTasks.add(migrateToSchemaVersion6);
        // }
        return !migrationTasks.isEmpty();
    }

    /**
     * Show progress dialog and run migration tasks.
     */
    public final void migrate() {
        Task<?> currentUpdateTask = migrationTasks.poll();
        if (currentUpdateTask == null) {
            listener.onMigrationFinished();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog();

        currentUpdateTask.setOnSucceeded(t -> {
            this.updateSchemaVersion();
            progressDialog.dismiss();
            migrate();
        });

        currentUpdateTask.setOnFailed(t -> {
            progressDialog.dismiss();
            try {
                throw currentUpdateTask.getException();
            } catch (Throwable throwable) {
                ExceptionHandler.handle(MigrationHelper.class, throwable);
            }
            listener.onMigrationFinished();
        });

        progressDialog.show(currentUpdateTask, getX(), getY());

        new Thread(currentUpdateTask).start();
    }

    /**
     * Read schema version from configuration file.
     *
     * @return schema version
     */
    private int readSchemaVersion() {
        int schemaVersion = 0;
        try {
            String s = Config.getValue(Config.PROP_SCHEMA_VERSION_KEY);
            if (s != null) {
                schemaVersion = Integer.parseInt(s);
            }
        } catch (IOException e) {
            Logger.get(MigrationHelper.class).error(e);
        }
        return schemaVersion;
    }

    /**
     * Update schema version in config to current version.
     */
    private void updateSchemaVersion() {
        try {
            Config.setValue(Config.PROP_SCHEMA_VERSION_KEY, String.valueOf(currentSchemaVersion));
        } catch (IOException e) {
            ExceptionHandler.handle(MigrationHelper.class, e);
        }
    }

    /**
     * @return X position based on parent window.
     */
    private double getX() {
        return parentWindow.getX() + parentWindow.getWidth() / 2.0 - ProgressDialog.WIDTH / 2.0;
    }

    /**
     * @return Y position based on parent window.
     */
    private double getY() {
        return parentWindow.getY() + parentWindow.getHeight() / 2.0 - ProgressDialog.HEIGHT / 2.0;
    }

    /**
     * Listener to get notified when the migration is finished.
     */
    public interface Listener {

        /**
         * Called when the migration is finished (successfully or not).
         */
        void onMigrationFinished();
    }
}
