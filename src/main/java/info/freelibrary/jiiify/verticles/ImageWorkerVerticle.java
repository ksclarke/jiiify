
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.MessageCodes.EXC_000;

import java.io.IOException;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.image.ImageObject;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

public class ImageWorkerVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();

            try {
                final ImageRequest request = new ImageRequest(json.getString(IIIF_PATH_KEY));
                final String id = request.getID();
                final PairtreeObject ptObj = getConfig().getDataDir(id).getObject(id);

                // FIXME: FILE_PATH_KEY can be s3 too?
                final FileSystem fileSystem = getVertx().fileSystem();
                final Buffer imageBuffer = fileSystem.readFileBlocking(json.getString(FILE_PATH_KEY));
                final ImageObject image = ImageUtils.getImage(imageBuffer);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating derivative image for: {}", id);
                }

                if (!request.getRegion().isFullImage()) {
                    image.extractRegion(request.getRegion());
                }

                if (!request.getSize().isFullSize()) {
                    image.resize(request.getSize());
                }

                if (request.getRotation().isRotated()) {
                    image.rotate(request.getRotation());
                }

                if (!request.getQuality().equals(ImageQuality.DEFAULT)) {
                    image.adjustQuality(request.getQuality());
                }

                ptObj.put(request.getPath(), image.toBuffer(request.getFormat().getExtension()), handler -> {
                    if (handler.succeeded()) {
                        message.reply(SUCCESS_RESPONSE);
                    } else {
                        LOGGER.error(handler.cause(), EXC_000, handler.cause().getMessage());
                        message.reply(FAILURE_RESPONSE);
                    }
                });
            } catch (final Exception details) {
                LOGGER.error(details, EXC_000, details.getMessage());
                message.reply(FAILURE_RESPONSE);
            } catch (final OutOfMemoryError details) {
                LOGGER.error(details, "OutOfMemoryError: {}", json.getString(IIIF_PATH_KEY));
                message.reply(FAILURE_RESPONSE);
            }
        });

        aFuture.complete();
    }

}
