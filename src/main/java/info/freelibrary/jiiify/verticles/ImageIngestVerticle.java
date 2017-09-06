
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

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.SolrMetadata;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;

/**
 * A verticle that ingests images.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageIngestVerticle extends AbstractJiiifyVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageIngestVerticle.class, Constants.MESSAGES);

    private static final String SKIP_PROPERTIES = "skipproperties";

    private static final String SKIP_IMAGES = "skipimages";

    @Override
    public void start() throws ConfigurationException, IOException {
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

                        // Use file name (without extension) as the ID if one is not supplied
                        if (json.containsKey(ID_KEY)) {
                            id = json.getString(ID_KEY);
                        } else {
                            id = FileUtils.stripExt(file.getName());
                        }

                        ingest(file, id, new Properties(), message);
                    }
                } else {
                    LOGGER.error(fsHandler.cause(), MessageCodes.EXC_037, file);
                    message.reply(FAILURE_RESPONSE);
                }
            });
        });
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
                        LOGGER.error(details, MessageCodes.EXC_037, aImageFile);
                        aMessage.reply(FAILURE_RESPONSE);
                    }
                });
            } else {
                LOGGER.error(fileHandler.cause(), MessageCodes.EXC_037, aImageFile);
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
                    LOGGER.error(handler.cause(), handler.cause().getMessage());
                    aMessage.reply(FAILURE_RESPONSE);
                }
            });
        }
    }

    /**
     * Dispatches messages for activities requested by the user at point of ingest.
     *
     * @param aProperties A properties file
     * @param aImageFile An image file for ingest
     * @param aID An ID for the image to ingest
     * @param aJsonObject A configuration file for the ingest activity
     */
    private void messageIngestListeners(final Properties aProperties, final File aImageFile, final String aID,
            final JsonObject aJsonObject) {
        final JsonObject jsonMessage = new JsonObject();
        FileUtils.getExt(aImageFile.getAbsolutePath());

        // Pass along some metadata about the image being ingested
        jsonMessage.put(ID_KEY, aID);
        jsonMessage.put(TILE_SIZE_PROP, getConfig().getTileSize());
        jsonMessage.put(FILE_PATH_KEY, aImageFile.getAbsolutePath());

        // These are the tasks we trigger, according to user's ingest request
        if (!aJsonObject.getBoolean(SKIP_PROPERTIES, false)) {
            sendMessage(jsonMessage, ImagePropertiesVerticle.class.getName(), INGEST_TIMEOUT);
        } else {
            LOGGER.debug(MessageCodes.DBG_105, aID);
        }

        if (!aJsonObject.getBoolean(SKIP_IMAGES, false)) {
            if (!aJsonObject.getBoolean(SolrMetadata.SKIP_INDEXING, false)) {
                jsonMessage.put(SolrMetadata.SKIP_INDEXING, false);
            } else {
                LOGGER.debug(MessageCodes.DBG_104, aID);
            }

            sendMessage(jsonMessage, TileMasterVerticle.class.getName(), INGEST_TIMEOUT);
        } else {
            if (!aJsonObject.getBoolean(SolrMetadata.SKIP_INDEXING, false)) {
                jsonMessage.put(SolrMetadata.ACTION_TYPE, SolrMetadata.UPDATE_ACTION);
                sendMessage(jsonMessage, ImageIndexVerticle.class.getName(), INGEST_TIMEOUT);
            } else {
                LOGGER.debug(MessageCodes.DBG_104, aID);
            }

            LOGGER.debug(MessageCodes.DBG_106, aID);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
