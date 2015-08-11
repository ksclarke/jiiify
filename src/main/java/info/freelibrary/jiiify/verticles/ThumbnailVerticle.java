
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.ImageSize;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

public class ThumbnailVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(messageHandler -> {
            final JsonObject ingestMessage = messageHandler.body();
            final String id = ingestMessage.getString(ID_KEY);
            final File file = new File(ingestMessage.getString(FILE_PATH_KEY));
            final String filePath = file.getAbsolutePath();
            final FileSystem fileSystem = vertx.fileSystem();
            final String fsPath = PathUtils.getObjectPath(vertx, id);
            final String prefix = getConfiguration().getServicePrefix();

            fileSystem.mkdirs(fsPath, mkdirsHandler -> {
                if (mkdirsHandler.succeeded()) {
                    // FIXME: Pull thumbnail size from config rather than hard-coded
                    try {
                        final int thumbnailSize = 150;
                        final ImageRegion region = ImageUtils.getCenter(file);
                        final ImageSize size = new ImageSize(thumbnailSize);
                        final ImageRequest request = new ImageRequest(id, prefix, region, size);
                        final JsonObject infoMessage = new JsonObject().put(ID_KEY, id);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Thumbnail creation request sent: {}", request.toString());
                        }

                        infoMessage.put(FILE_PATH_KEY, filePath).put(IIIF_PATH_KEY, request.toString());
                        sendMessage(infoMessage, ImageWorkerVerticle.class.getName(), 0);
                        sendMessage(infoMessage, ManifestVerticle.class.getName(), 0);
                        messageHandler.reply(SUCCESS_RESPONSE);
                    } catch (IOException | ConfigurationException details) {
                        LOGGER.error(details, MessageCodes.EXC_000, details.getMessage());
                        messageHandler.reply(FAILURE_RESPONSE);
                    }
                } else {
                    LOGGER.error("Unable to create object directory: {}", fsPath);
                    messageHandler.reply(FAILURE_RESPONSE);
                }
            });
        });

        aFuture.complete();
    }

}
