/**
 *
 */

package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.CONFIG_KEY;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.OVERWRITE_KEY;
import static info.freelibrary.jiiify.Constants.SHARED_DATA_KEY;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
//import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.FileUtils;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * A verticle that watches a folder for image file changes and then fires off messages about them to the image tiler.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class WatchFolderVerticle extends AbstractJiiifyVerticle {

    private final Map<WatchKey, Path> myKeys = new HashMap<>();

    private WatchService myWatcher;

    private boolean isChanged;

    // FIXME: delay between adding new folder and getting alerts for files added to it

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        final Configuration config = (Configuration) vertx.sharedData().getLocalMap(SHARED_DATA_KEY).get(CONFIG_KEY);
        final Path watchDir = Paths.get(config.getWatchFolder().getAbsolutePath());

        myWatcher = FileSystems.getDefault().newWatchService();
        recursivelyRegister(watchDir);

        // Get contacted every five seconds so we can check our watch folder [TODO: Make time configurable]
        vertx.setPeriodic(5000, aLong -> {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.DBG_005);
            }

            checkWatchFolder();
        });

        // FIXME: Blocking for now
        // Check the watch folder on startup for new items which need to be ingested
        // Is this a good idea for large collections? We should at least make it non-blocking
        // TODO: Get some measurements on how long this takes for large collections
        final FileExtFileFilter extFilter = new FileExtFileFilter(ImageFormat.getExtensions());
        final File[] files = FileUtils.listFiles(config.getWatchFolder(), extFilter, true);

        for (final File file : files) {
            final JsonObject json = new JsonObject().put(OVERWRITE_KEY, false);
            final String filePath = file.getAbsolutePath();

            // Sends an ingest request which will be ignored if it exists already
            sendMessage(json.put(FILE_PATH_KEY, filePath), ImageIngestVerticle.class.getName(), INGEST_TIMEOUT);
        }

        aFuture.complete();
    }

    /**
     * Check our watch folder for any new additions or modifications.
     */
    private void checkWatchFolder() {
        boolean hadChangeEvents = false;
        WatchKey watchKey;

        // Check for new additions to our watch folder directory structure
        while ((watchKey = myWatcher.poll()) != null) {
            hadChangeEvents = true;
            isChanged = true;

            // Handle the change event (either new subfolder or image file)
            handleChangeEvent(watchKey);
        }

        // FIXME: Work through the class' logic and confirm this is kosher
        if (isChanged && !hadChangeEvents) {
            vertx.eventBus().send("reload", new JsonObject());
            isChanged = false;
        }
    }

    /**
     * Process any change events from our <code>WatchService</code>.
     *
     * @param aWatchKey The <code>WatchKey</code> returned by our <code>WatchService</code> polling.
     */
    private void handleChangeEvent(final WatchKey aWatchKey) {
        final Path dir = myKeys.get(aWatchKey);

        if (dir != null) {
            // Cycle through our WatchKey events to look for images to be ingested
            for (final WatchEvent<?> watchEvent : aWatchKey.pollEvents()) {
                final Kind<?> kind = watchEvent.kind();

                // Ignore events that may have been lost or discarded
                if (kind == OVERFLOW) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                final Path name = ((WatchEvent<Path>) watchEvent).context();
                final Path child = dir.resolve(name);

                // Check the watch folder event
                if (kind == ENTRY_MODIFY && !Files.isDirectory(child, NOFOLLOW_LINKS) && ImageFormat.isSupportedFormat(
                        FileUtils.getExt(child.toString()))) {
                    final String childPath = child.toAbsolutePath().toString();
                    final JsonObject json = new JsonObject().put(OVERWRITE_KEY, true);

                    LOGGER.info(MessageCodes.INFO_001, childPath);

                    json.put(FILE_PATH_KEY, childPath);

                    // Notify our tiling verticle that we have an image that needs to be (re)tiled
                    sendMessage(json, ImageIngestVerticle.class.getName(), INGEST_TIMEOUT);
                } else if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            recursivelyRegister(child);
                        } else if (LOGGER.isDebugEnabled() && ImageFormat.isSupportedFormat(FileUtils.getExt(child
                                .toString()))) {
                            LOGGER.debug(MessageCodes.DBG_004, child);
                        }
                    } catch (final IOException details) {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            LOGGER.error(MessageCodes.EXC_030, child);
                        } else if (ImageFormat.isSupportedFormat(FileUtils.getExt(child.toString()))) {
                            LOGGER.error(MessageCodes.EXC_029, child);
                        }
                    }
                }
                // else should there be a configurable option to remove images on delete from this folder?
            }

            // Reset the WatchKey after its events have been processed
            if (aWatchKey.isValid() && !aWatchKey.reset()) {
                myKeys.remove(aWatchKey);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.DBG_003, dir);
                }

                if (myKeys.isEmpty()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageCodes.DBG_002);
                    }

                    return;
                }
            }
        } else {
            LOGGER.error(MessageCodes.EXC_031);
        }
    }

    /**
     * Registers the supplied directory and all subdirectories as watch directories.
     *
     * @param aWatchPath A path at which to watch for new files to ingest
     * @throws IOException If there is trouble adding the directory to be watched
     */
    private void recursivelyRegister(final Path aWatchPath) throws IOException {
        Files.walkFileTree(aWatchPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path aDirPath, final BasicFileAttributes aAttrs)
                    throws IOException {
                register(aDirPath);
                return CONTINUE;
            }
        });
    }

    /**
     * Registers a particular directory as a watched directory.
     *
     * @param aDirPath A directory to watch for ingests
     * @throws IOException If there is trouble registering the supplied directory as a watched directory
     */
    private void register(final Path aDirPath) throws IOException {
        final WatchKey watchKey = aDirPath.register(myWatcher, ENTRY_CREATE, ENTRY_MODIFY);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_001, aDirPath);
        }

        myKeys.put(watchKey, aDirPath);
    }

}
