
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_BUFFER_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_CLEANUP_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_TILE_COUNT;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_REQUEST_KEY;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.image.ImageObject;
import info.freelibrary.jiiify.image.ImmutableBytes;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

/**
 * A threaded verticle that handles image creation requests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageWorkerVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start() throws ConfigurationException, IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final FileSystem fileSystem = getVertx().fileSystem();
            final String filePath = json.getString(FILE_PATH_KEY);

            try {
                final ImageRequest request = new ImageRequest(json.getString(IIIF_PATH_KEY));
                final String id = request.getID();

                /* Check whether our image request is coming from the tile master */
                if (json.containsKey(TILE_REQUEST_KEY)) {
                    final String tileRequestKey = json.getString(TILE_REQUEST_KEY);
                    final SharedData sharedData = vertx.sharedData();

                    sharedData.getCounter(tileRequestKey, getCounter -> {
                        if (getCounter.succeeded()) {
                            getCounter.result().decrementAndGet(decrementAndGet -> {
                                if (decrementAndGet.succeeded()) {
                                    /* Change the tile count to be 1-based instead of 0-based */
                                    final long tileCount = decrementAndGet.result() + 1;
                                    final byte[] bytes = getCachedImage(sharedData, json, tileCount, filePath, id);

                                    try {
                                        final ImageObject image = ImageUtils.getImage(bytes);

                                        try {
                                            processImage(request, image, message);
                                        } catch (final Throwable details) {
                                            LOGGER.error(details, details.getMessage());
                                            image.free();
                                            message.reply(FAILURE_RESPONSE);
                                        }
                                    } catch (final IOException details) {
                                        LOGGER.error(details, details.getMessage());
                                        message.reply(FAILURE_RESPONSE);
                                    }
                                } else {
                                    final Throwable cause = decrementAndGet.cause();

                                    LOGGER.error(cause, cause.getMessage());
                                    message.reply(FAILURE_RESPONSE);
                                }
                            });
                        } else {
                            final Throwable cause = getCounter.cause();

                            LOGGER.error(cause, cause.getMessage());
                            message.reply(FAILURE_RESPONSE);
                        }
                    });
                } else {
                    final byte[] bytes = fileSystem.readFileBlocking(filePath).getBytes();
                    final ImageObject image = ImageUtils.getImage(bytes);

                    LOGGER.debug(MessageCodes.DBG_010, filePath);

                    try {
                        processImage(request, image, message);
                    } catch (final Throwable details) {
                        LOGGER.error(details, MessageCodes.EXC_049, filePath);
                        image.free();
                        message.reply(FAILURE_RESPONSE);
                    }
                }
            } catch (final Throwable details) {
                LOGGER.error(details, details.getMessage());
                message.reply(FAILURE_RESPONSE);
            }
        });
    }

    private byte[] getCachedImage(final SharedData aSharedData, final JsonObject aJson, final long aTileCount,
            final String aFilePath, final String aID) {
        final LocalMap<String, ImmutableBytes> dataMap = aSharedData.getLocalMap(IMAGE_BUFFER_KEY);
        final String tileRequestKey = aJson.getString(TILE_REQUEST_KEY);
        final int totalTileCount = aJson.getInteger(IMAGE_TILE_COUNT);
        final FileSystem fileSystem = vertx.fileSystem();
        final byte[] image;

        boolean skipBuffer = false;

        /* Check if we're processing the first tile from the batch */
        if (totalTileCount == aTileCount) {
            LOGGER.debug(MessageCodes.DBG_014, aFilePath);
            image = fileSystem.readFileBlocking(aFilePath).getBytes();
            dataMap.put(tileRequestKey, new ImmutableBytes(image));
        } else {
            /* Stall for a bit if we need to, so we're not double reading TIFF files */
            for (int count = 0; !dataMap.keySet().contains(tileRequestKey); count++) {
                try {
                    Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));

                    /* This is just a wild unfounded amount of time based on nothing */
                    if (count > 60 * 5) {
                        LOGGER.warn(MessageCodes.WARN_006, aFilePath);
                        skipBuffer = true;
                        break;
                    }
                } catch (final InterruptedException details) {
                    final String verticleName = getClass().getSimpleName();

                    LOGGER.warn(MessageCodes.WARN_005, verticleName, details.getMessage());
                }
            }

            if (skipBuffer) {
                LOGGER.debug(MessageCodes.DBG_009, aFilePath);
                image = fileSystem.readFileBlocking(aFilePath).getBytes();
            } else {
                LOGGER.debug(MessageCodes.DBG_013, aFilePath);

                if (aTileCount == 1) {
                    final boolean cleanupSource = aJson.getBoolean(IMAGE_CLEANUP_KEY);

                    LOGGER.debug(MessageCodes.DBG_012, aID, aFilePath);

                    image = dataMap.remove(tileRequestKey).getBytes();

                    if (cleanupSource) {
                        vertx.fileSystem().delete(aFilePath, delete -> {
                            if (!delete.succeeded()) {
                                LOGGER.error(MessageCodes.EXC_083, aFilePath);
                            }
                        });
                    }
                } else {
                    image = dataMap.get(tileRequestKey).getBytes();
                }
            }
        }

        return image;
    }

    private void processImage(final ImageRequest aRequest, final ImageObject aImage, final Message<JsonObject> aMessage)
            throws IOException {
        final PairtreeObject ptObj = getConfig().getDataDir(aRequest.getID()).getObject(aRequest.getID());
        final Buffer imageBuffer;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_011, aRequest.getID());
        }

        if (!aRequest.getRegion().isFullImage()) {
            aImage.extractRegion(aRequest.getRegion());
        }

        if (!aRequest.getSize().isFullSize()) {
            aImage.resize(aRequest.getSize());
        }

        if (aRequest.getRotation().isRotated()) {
            aImage.rotate(aRequest.getRotation());
        }

        if (!aRequest.getQuality().getValue().equals(ImageQuality.DEFAULT)) {
            aImage.adjustQuality(aRequest.getQuality());
        }

        imageBuffer = aImage.toBuffer(aRequest.getFormat().getExtension());
        aImage.free();

        ptObj.put(aRequest.getPath(), imageBuffer, handler -> {
            if (handler.succeeded()) {
                aMessage.reply(SUCCESS_RESPONSE);
            } else {
                final Throwable cause = handler.cause();

                LOGGER.error(cause, MessageCodes.EXC_000, cause.getMessage());
                aMessage.reply(FAILURE_RESPONSE);
            }
        });
    }
}
