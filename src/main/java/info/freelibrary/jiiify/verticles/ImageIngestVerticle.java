
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.FileUtils;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;

public class ImageIngestVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        final FileSystem fileSystem = vertx.fileSystem();

        getStringConsumer().handler(messageHandler -> {
            final File file = new File(messageHandler.body());
            final Properties properties = new Properties();
            final String configPath = new File(file.getParentFile(), file.getName() + ".cfg").getAbsolutePath();

            fileSystem.exists(configPath, fsHandler -> {
                if (fsHandler.succeeded()) {
                    if (fsHandler.result()) {
                        fileSystem.open(configPath, new OpenOptions().setWrite(false), fileHandler -> {
                            if (fileHandler.succeeded()) {
                                final AsyncFile asyncFile = fileHandler.result();

                                asyncFile.read(Buffer.buffer(), 0, 0, (int) file.length(), readHandler -> {
                                    final Buffer buffer = readHandler.result();

                                    try {
                                        final InputStream bytes = new ByteArrayInputStream(buffer.getBytes());

                                        // Check to see if our properties file is XML
                                        if (buffer.getString(0, 4).equals("<?xml")) {
                                            properties.loadFromXML(bytes);
                                        } else {
                                            properties.load(bytes);
                                        }

                                        messageIngestListeners(properties, file);
                                        messageHandler.reply(SUCCESS_RESPONSE);
                                    } catch (final IOException details) {
                                        LOGGER.error(details, MessageCodes.EXC_032, file);
                                        messageHandler.reply(FAILURE_RESPONSE);
                                    }
                                });
                            } else {
                                LOGGER.error(fileHandler.cause(), MessageCodes.EXC_032, file);
                                messageHandler.reply(FAILURE_RESPONSE);
                            }
                        });
                    } else {
                        messageIngestListeners(properties, file);
                        messageHandler.reply(SUCCESS_RESPONSE);
                    }
                } else {
                    LOGGER.error(fsHandler.cause(), MessageCodes.EXC_032, file);
                    messageHandler.reply(FAILURE_RESPONSE);
                }
            });
        });

        aFuture.complete();
    }

    private void messageIngestListeners(final Properties aProperties, final File aImageFile) {
        final Object ts = aProperties.getOrDefault(TILE_SIZE_PROP, getConfiguration().getTileSize());
        final JsonObject jsonMessage = new JsonObject();

        // Pass along some metadata about the image being ingested
        jsonMessage.put(ID_KEY, aProperties.getProperty(ID_KEY, FileUtils.stripExt(aImageFile.getName())));
        jsonMessage.put(TILE_SIZE_PROP, ts instanceof String ? Integer.parseInt((String) ts) : ts);
        jsonMessage.put(FILE_PATH_KEY, aImageFile.getAbsolutePath());

        /* TODO: send to different processes depending on ingest profile */
        sendMessage(jsonMessage, TileMasterVerticle.class.getName(), 0);
        sendMessage(jsonMessage, ImageIndexVerticle.class.getName(), 0);
        sendMessage(jsonMessage, ThumbnailVerticle.class.getName(), 0);
        sendMessage(jsonMessage, ImagePropertiesVerticle.class.getName(), 0);
    }

}
