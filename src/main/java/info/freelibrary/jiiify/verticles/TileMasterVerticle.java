
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.util.ImageUtils;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class TileMasterVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final String id = json.getString(ID_KEY);
            final int tileSize = json.getInteger(TILE_SIZE_PROP);
            final File file = new File(json.getString(FILE_PATH_KEY));
            final String filePath = file.getAbsolutePath();
            final EventBus eventBus = vertx.eventBus();

            try {
                final Dimension dimension = ImageUtils.getImageDimension(file);
                final String prefix = getConfig().getServicePrefix();
                final JsonObject jsonMessage = new JsonObject();

                /* Metadata needed for tile generation */
                jsonMessage.put(FILE_PATH_KEY, filePath);

                /* Send the IIIF paths for the tiles to be created to the image worker verticle */
                ImageUtils.getTilePaths(prefix, id, tileSize, dimension.width, dimension.height).forEach(path -> {
                    sendMessage(jsonMessage.copy().put(IIIF_PATH_KEY, path), ImageWorkerVerticle.class.getName(), 0);
                });

                /* Metadata needed for image info file generation */
                jsonMessage.put(Region.WIDTH.name(), dimension.width);
                jsonMessage.put(Region.HEIGHT.name(), dimension.height);
                jsonMessage.put(TILE_SIZE_PROP, tileSize);
                jsonMessage.put(ID_KEY, id);

                eventBus.send(ImageInfoVerticle.class.getName(), jsonMessage);
                message.reply(SUCCESS_RESPONSE);
            } catch (final IOException details) {
                LOGGER.error(details, MessageCodes.EXC_000, details.getMessage());
                message.reply(FAILURE_RESPONSE);
            }
        });

        aFuture.complete();
    }

}