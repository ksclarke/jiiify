
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_TILE_COUNT;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_REQUEST_KEY;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.util.ImageUtils;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;

/**
 * A verticle that calculates needed tiles and triggers their production.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class TileMasterVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(message -> {
            final SharedData sharedData = vertx.sharedData();
            final JsonObject json = message.body();
            final String id = json.getString(ID_KEY);
            final int tileSize = json.getInteger(TILE_SIZE_PROP);
            final File file = new File(json.getString(FILE_PATH_KEY));
            final JsonObject newMessage = new JsonObject();
            final String filePath = file.getAbsolutePath();

            try {
                final Dimension dim = ImageUtils.getImageDimension(file);
                final String prefix = getConfig().getServicePrefix();
                final String tileRequestKey = UUID.randomUUID().toString();
                final List<String> tiles = ImageUtils.getTilePaths(prefix, id, tileSize, dim.width, dim.height);

                /* Metadata needed for tile generation */
                newMessage.put(FILE_PATH_KEY, filePath);
                newMessage.put(TILE_REQUEST_KEY, tileRequestKey);

                sharedData.getCounter(tileRequestKey, getCounter -> {
                    if (getCounter.succeeded()) {
                        getCounter.result().compareAndSet(0, tiles.size(), compareAndSet -> {
                            if (compareAndSet.succeeded()) {
                                if (compareAndSet.result()) {
                                    LOGGER.debug(MessageCodes.DBG_021, id, tiles.size());

                                    queueImageInfo(newMessage, dim, id, tileSize);
                                    queueTileCreation(newMessage.copy(), tiles, message);

                                    // FIXME: This is not actually checking for the success of the message sending so
                                    // could be higher in the code (or could check the success of queuing tasks)
                                    message.reply(SUCCESS_RESPONSE);
                                } else {
                                    LOGGER.error(MessageCodes.EXC_045, id);
                                    message.reply(FAILURE_RESPONSE);
                                }
                            } else {
                                LOGGER.error(compareAndSet.cause(), compareAndSet.cause().getMessage());
                                message.reply(FAILURE_RESPONSE);
                            }
                        });
                    } else {
                        LOGGER.error(getCounter.cause(), getCounter.cause().getMessage());
                        message.reply(FAILURE_RESPONSE);
                    }
                });
            } catch (final IOException details) {
                LOGGER.error(details, MessageCodes.EXC_000, details.getMessage());
                message.reply(FAILURE_RESPONSE);
            }
        });

        aFuture.complete();
    }

    private void queueImageInfo(final JsonObject aMessage, final Dimension aDim, final String aID,
            final int aTileSize) {
        aMessage.put(Region.WIDTH.name(), aDim.width);
        aMessage.put(Region.HEIGHT.name(), aDim.height);
        aMessage.put(TILE_SIZE_PROP, aTileSize);
        aMessage.put(ID_KEY, aID);

        sendMessage(aMessage, ImageInfoVerticle.class.getName(), INGEST_TIMEOUT);
    }

    private void queueTileCreation(final JsonObject aMessage, final List<String> aTilesList,
            final Message<JsonObject> aResponse) {
        aTilesList.forEach(path -> {
            aMessage.put(IIIF_PATH_KEY, path);
            aMessage.put(IMAGE_TILE_COUNT, aTilesList.size());

            sendMessage(aMessage, ImageWorkerVerticle.class.getName(), INGEST_TIMEOUT);
        });
    }
}