
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_SOURCE_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Metadata.PROPERTIES_FILE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

public class ImagePropertiesVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final String id = json.getString(ID_KEY);
            final String imageFilePath = json.getString(FILE_PATH_KEY);
            final String objPath = PathUtils.getObjectPath(vertx, id);
            final String propFilePath = new File(objPath, PROPERTIES_FILE).getAbsolutePath();
            final FileSystem fileSystem = vertx.fileSystem();

            /* Make sure our image object pairtree structure exists */
            fileSystem.mkdirs(objPath, mkdirsHandler -> {
                if (mkdirsHandler.succeeded()) {
                    writeProperties(id, imageFilePath, propFilePath, message);
                } else {
                    /* Check to see if it failed because it already exists */
                    fileSystem.exists(objPath, existHandler -> {
                        /* If it did, it's okay to go ahead and write our properties */
                        if (existHandler.succeeded() && existHandler.result()) {
                            writeProperties(id, imageFilePath, propFilePath, message);
                        } else {
                            LOGGER.error("Unable to find or create object directory: {}", objPath);
                            message.reply(FAILURE_RESPONSE);
                        }
                    });
                }
            });
        });
    }

    private void writeProperties(final String aID, final String aImageFilePath, final String aPropFilePath,
            final Message<JsonObject> aMessage) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final Properties properties = new Properties();

        try {
            properties.put(IMAGE_SOURCE_KEY, aImageFilePath);
            properties.storeToXML(stream, null);

            vertx.fileSystem().writeFile(aPropFilePath, Buffer.buffer(stream.toByteArray()), writeHandler -> {
                if (writeHandler.succeeded()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Wrote image properties file: {}", aPropFilePath);
                    }

                    aMessage.reply(SUCCESS_RESPONSE);
                } else {
                    LOGGER.error(writeHandler.cause(), "Failed to write image properties file: {}", aPropFilePath);
                    aMessage.reply(FAILURE_RESPONSE);
                }
            });
        } catch (final IOException details) {
            LOGGER.error(details, "Failed to write image properties files: {}", aPropFilePath);
            aMessage.reply(FAILURE_RESPONSE);
        }
    }

}
