
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.OVERWRITE_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.util.FileUtils;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;

public class ImageIngestVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        final FileSystem fileSystem = vertx.fileSystem();

        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final File file = new File(json.getString(FILE_PATH_KEY));
            final boolean overwriteIfExists = json.getBoolean(OVERWRITE_KEY, false);
            final String configPath = new File(file.getParentFile(), file.getName() + ".cfg").getAbsolutePath();

            fileSystem.exists(configPath, fsHandler -> {
                if (fsHandler.succeeded()) {
                    // Sidecar configurations override other options (at this time anyway)
                    if (fsHandler.result()) {
                        // Found a special sidecar configuration, so let's use it for our ingest
                        useObjectConfig(fileSystem, file, configPath, message, overwriteIfExists);
                    } else {
                        final String id;

                        if (json.containsKey(ID_KEY)) {
                            id = json.getString(ID_KEY);
                        } else {
                            id = FileUtils.stripExt(file.getName());
                        }
                        ingest(fileSystem, file, id, new Properties(), overwriteIfExists, message);
                    }
                } else {
                    // FIXME: EXC_032 is wrong exception -- fix ALL its occurrences everywhere!
                    LOGGER.error(fsHandler.cause(), MessageCodes.EXC_032, file);
                    message.reply(FAILURE_RESPONSE);
                }
            });
        });

        aFuture.complete();
    }

    private void useObjectConfig(final FileSystem aFileSystem, final File aImageFile, final String aConfigPath,
            final Message<JsonObject> aMessage, final boolean aOverwriteIfExists) {
        aFileSystem.open(aConfigPath, new OpenOptions().setWrite(false), fileHandler -> {
            if (fileHandler.succeeded()) {
                final AsyncFile asyncFile = fileHandler.result();
                final Properties props = new Properties();

                asyncFile.read(Buffer.buffer(), 0, 0, (int) aImageFile.length(), readHandler -> {
                    final Buffer buffer = readHandler.result();
                    final String id;

                    try {
                        final InputStream bytes = new ByteArrayInputStream(buffer.getBytes());

                        // Check to see if our properties file is XML
                        if (buffer.getString(0, 4).equals("<?xml")) {
                            props.loadFromXML(bytes);
                        } else {
                            props.load(bytes);
                        }

                        id = props.getProperty(ID_KEY, FileUtils.stripExt(aImageFile.getName()));
                        ingest(aFileSystem, aImageFile, id, props, aOverwriteIfExists, aMessage);
                    } catch (final IOException details) {
                        LOGGER.error(details, MessageCodes.EXC_032, aImageFile);
                        aMessage.reply(FAILURE_RESPONSE);
                    }
                });
            } else {
                LOGGER.error(fileHandler.cause(), MessageCodes.EXC_032, aImageFile);
                aMessage.reply(FAILURE_RESPONSE);
            }
        });
    }

    private void ingest(final FileSystem aFileSystem, final File aImageFile, final String aID,
            final Properties aProps, final boolean aOverwriteIfExists, final Message<JsonObject> aMessage) {
        if (aOverwriteIfExists) {
            messageIngestListeners(aProps, aImageFile, aID);
            aMessage.reply(SUCCESS_RESPONSE);
        } else {
            aFileSystem.exists(PathUtils.getObjectPath(vertx, aID), fsHandler -> {
                if (fsHandler.succeeded()) {
                    if (!fsHandler.result()) {
                        messageIngestListeners(aProps, aImageFile, aID);
                    } // else it exists and we don't want to overwrite it

                    aMessage.reply(SUCCESS_RESPONSE);
                } else {
                    LOGGER.error(fsHandler.cause(), "{}", fsHandler.cause().getMessage());
                    aMessage.reply(FAILURE_RESPONSE);
                }
            });
        }
    }

    private void messageIngestListeners(final Properties aProperties, final File aImageFile, final String aID) {
        final Object ts = aProperties.getOrDefault(TILE_SIZE_PROP, getConfiguration().getTileSize());
        final JsonObject jsonMessage = new JsonObject();

        // Pass along some metadata about the image being ingested
        jsonMessage.put(ID_KEY, aID);
        jsonMessage.put(TILE_SIZE_PROP, ts instanceof String ? Integer.parseInt((String) ts) : ts);
        jsonMessage.put(FILE_PATH_KEY, aImageFile.getAbsolutePath());

        /* TODO: send to different processes depending on ingest profile */
        sendMessage(jsonMessage, TileMasterVerticle.class.getName(), 0);
        sendMessage(jsonMessage, ImageIndexVerticle.class.getName(), 0);
        sendMessage(jsonMessage, ThumbnailVerticle.class.getName(), 0);
        sendMessage(jsonMessage, ImagePropertiesVerticle.class.getName(), 0);
    }

}
