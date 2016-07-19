
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
import info.freelibrary.pairtree.PairtreeObject;
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
            final File configFile = new File(file.getParentFile(), FileUtils.stripExt(file.getName()) + ".cfg");
            final String configPath = configFile.getAbsolutePath();

            fileSystem.exists(configPath, fsHandler -> {
                if (fsHandler.succeeded()) {
                    // Sidecar configurations override other options (at this time anyway)
                    if (fsHandler.result()) {
                        // Found a special sidecar configuration, so let's use it for our ingest
                        useObjectConfig(fileSystem, file, configPath, message);
                    } else {
                        final String id;

                        if (json.containsKey(ID_KEY)) {
                            id = json.getString(ID_KEY);
                        } else {
                            id = FileUtils.stripExt(file.getName());
                        }

                        ingest(file, id, new Properties(), message);
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
            final Message<JsonObject> aMessage) {
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
                        ingest(aImageFile, id, props, aMessage);
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

    private void ingest(final File aImageFile, final String aID, final Properties aProperties,
            final Message<JsonObject> aMessage) {
        final PairtreeObject ptObj = getConfig().getDataDir(aID).getObject(aID);
        final JsonObject json = aMessage.body();

        if (json.getBoolean(OVERWRITE_KEY, false)) {
            messageIngestListeners(aProperties, aImageFile, aID, json);
            aMessage.reply(SUCCESS_RESPONSE);
        } else {
            ptObj.exists(handler -> {
                if (handler.succeeded()) {
                    if (!handler.result()) {
                        messageIngestListeners(aProperties, aImageFile, aID, json);
                    }

                    aMessage.reply(SUCCESS_RESPONSE);
                } else {
                    LOGGER.error(handler.cause(), "{}", handler.cause().getMessage());
                    aMessage.reply(FAILURE_RESPONSE);
                }
            });
        }
    }

    private void messageIngestListeners(final Properties aProperties, final File aImageFile, final String aID,
            final JsonObject aJsonObject) {
        final Object ts = aProperties.getOrDefault(TILE_SIZE_PROP, getConfig().getTileSize());
        final JsonObject jsonMessage = new JsonObject();

        // Pass along some metadata about the image being ingested
        jsonMessage.put(ID_KEY, aID);
        jsonMessage.put(TILE_SIZE_PROP, ts instanceof String ? Integer.parseInt((String) ts) : ts);
        jsonMessage.put(FILE_PATH_KEY, aImageFile.getAbsolutePath());

        if (!aJsonObject.getBoolean("skiptiles", false)) {
            sendMessage(jsonMessage, TileMasterVerticle.class.getName(), 0);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Skipping tile generation for: {}", aID);
        }

        if (!aJsonObject.getBoolean("skipindexing", false)) {
            sendMessage(jsonMessage, ImageIndexVerticle.class.getName(), 0);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Skipping indexing  for: {}", aID);
        }

        if (!aJsonObject.getBoolean("skipthumbs", false)) {
            sendMessage(jsonMessage, ThumbnailVerticle.class.getName(), 0);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Skipping thumbnail generation for: {}", aID);
        }

        if (!aJsonObject.getBoolean("skipproperties", false)) {
            sendMessage(jsonMessage, ImagePropertiesVerticle.class.getName(), 0);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Skipping property file generation for: {}", aID);
        }
    }

}
