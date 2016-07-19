
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;
import static info.freelibrary.jiiify.Constants.UTF_8_ENCODING;

import java.io.IOException;
import java.net.URISyntaxException;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class ImageInfoVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final String id = json.getString(ID_KEY);
            final int tileSize = json.getInteger(TILE_SIZE_PROP);
            final int width = json.getInteger(Region.WIDTH.name());
            final int height = json.getInteger(Region.HEIGHT.name());
            final PairtreeObject ptObj = getConfig().getDataDir(id).getObject(id);

            ptObj.create(createandler -> {
                if (createandler.succeeded()) {
                    final String imageInfo = getImageInfoJson(id, tileSize, width, height);
                    final Buffer buffer = Buffer.buffer(imageInfo, UTF_8_ENCODING);

                    ptObj.put(ImageInfo.FILE_NAME, buffer, writeHandler -> {
                        if (writeHandler.succeeded()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Wrote JSON image info file: {}", ptObj.getPath(ImageInfo.FILE_NAME));
                            }

                            message.reply(SUCCESS_RESPONSE);
                        } else {
                            LOGGER.error(writeHandler.cause(), "Failed to write image info file: {}", ptObj.getPath(
                                    ImageInfo.FILE_NAME));
                            message.reply(FAILURE_RESPONSE);
                        }
                    });
                } else {
                    LOGGER.error("Unable to create object directory: {}", ptObj.getPath());
                    message.reply(FAILURE_RESPONSE);
                }
            });
        });
    }

    // This is pretty simple now but perhaps it needs more complexity in the near future
    private String getImageInfoJson(final String aID, final int aTileSize, final int aWidth, final int aHeight) {
        try {
            final Configuration config = getConfig();
            final String id = PathUtils.encodeIdentifier(aID);
            final String server = config.getServer() + config.getServicePrefix() + "/" + id;

            return new ImageInfo(server).setTileSize(aTileSize).setWidth(aWidth).setHeight(aHeight).toString();
        } catch (final URISyntaxException details) {
            throw new RuntimeException(details);
        }
    }

}
