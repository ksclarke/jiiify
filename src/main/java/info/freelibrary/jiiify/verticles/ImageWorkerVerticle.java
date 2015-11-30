
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.util.ImageUtils;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ImageWorkerVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();

            try {
                final ImageRequest request = new ImageRequest(json.getString(IIIF_PATH_KEY));
                final File imageFile = new File(json.getString(FILE_PATH_KEY));
                final File cacheFile = request.getCacheFile(getConfiguration().getDataDir());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating derivative image for: {}", request.getID());
                }

                ImageUtils.transform(imageFile, request, cacheFile);
                message.reply(SUCCESS_RESPONSE);
            } catch (final Exception | OutOfMemoryError details) {
                LOGGER.error(details, MessageCodes.EXC_000, details.getMessage());
                message.reply(FAILURE_RESPONSE);
            }
        });

        aFuture.complete();
    }

}
