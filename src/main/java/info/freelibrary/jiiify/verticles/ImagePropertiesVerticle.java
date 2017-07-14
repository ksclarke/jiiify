
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_SOURCE_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Metadata.PROPERTIES_FILE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.IOUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * A verticle that creates image properties files.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImagePropertiesVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start() throws IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final String id = json.getString(ID_KEY);
            final String imageFilePath = json.getString(FILE_PATH_KEY);
            final PairtreeObject ptObj = getConfig().getDataDir(id).getObject(id);

            ptObj.create(createHandler -> {
                if (createHandler.succeeded()) {
                    writePropertiesFile(ptObj, id, imageFilePath, message);
                } else {
                    LOGGER.error(MessageCodes.EXC_056, ptObj.getPath());
                    message.reply(FAILURE_RESPONSE);
                }
            });
        });
    }

    private void writePropertiesFile(final PairtreeObject aPtObj, final String aID, final String aImageFilePath,
            final Message<JsonObject> aMessage) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final Properties properties = new Properties();

        try {
            properties.put(IMAGE_SOURCE_KEY, aImageFilePath);
            properties.storeToXML(stream, null);

            aPtObj.put(PROPERTIES_FILE, Buffer.buffer(stream.toByteArray()), writeHandler -> {
                if (writeHandler.succeeded()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageCodes.DBG_107, aPtObj.getPath(PROPERTIES_FILE));
                    }

                    aMessage.reply(SUCCESS_RESPONSE);
                } else {
                    LOGGER.error(writeHandler.cause(), MessageCodes.EXC_074, aPtObj.getPath(PROPERTIES_FILE));
                    aMessage.reply(FAILURE_RESPONSE);
                }
            });
        } catch (final IOException details) {
            LOGGER.error(details, MessageCodes.EXC_074, aPtObj.getPath(PROPERTIES_FILE));
            aMessage.reply(FAILURE_RESPONSE);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
