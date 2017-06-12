
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.OVERWRITE_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Metadata.MANIFEST_FILE;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.jiiify.verticles.ImageIngestVerticle;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler that handles requests for image ingests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class IngestHandler extends JiiifyHandler {

    /**
     * Creates an ingest handler.
     *
     * @param aConfig The application's configuration
     */
    public IngestHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final String fileType = request.getParam("file-type");
        final HttpMethod method = request.method();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();

        if (method.equals(HttpMethod.POST)) {
            for (final FileUpload upload : aContext.fileUploads()) {
                final String fileName = upload.fileName();
                final String filePath = upload.uploadedFileName();

                if (fileType.equals("csv-file")) {
                    jsonNode.put("csv-file", fileName);
                    processCSVUpload(fileName, filePath, aContext, jsonNode);
                } else if (fileType.equals("manifest-file")) {
                    final String id = aContext.request().getParam("manifest-id");

                    try {
                        jsonNode.put(fmt(SERVICE_PREFIX_PROP), myConfig.getServicePrefix());
                        jsonNode.put("manifest-file", fileName);
                        jsonNode.put("manifest-id", PathUtils.encodeIdentifier(id));

                        processManifestUpload(id, fileName, filePath, aContext, jsonNode);
                    } catch (final URISyntaxException details) {
                        fail(aContext, details);
                        toTemplate(aContext, jsonNode);
                    }
                } else {
                    fail(aContext, new FileNotFoundException("No ingest file found"));
                    toTemplate(aContext, jsonNode);
                }
            }
        } else {
            if (fileType == null) {
                jsonNode.put("default-view", "csv-file");
            } else if (fileType.equals("csv-file")) {
                jsonNode.put("csv-view", true);
            } else if (fileType.equals("manifest-file")) {
                jsonNode.put("manifest-view", true);
            }

            toTemplate(aContext, jsonNode);
        }
    }

    private void processManifestUpload(final String aID, final String aFilename, final String aFilePath,
            final RoutingContext aContext, final ObjectNode aJsonNode) {
        final PairtreeObject ptObj = myConfig.getDataDir(aID).getObject(aID);

        ptObj.create(existsHandler -> {
            if (existsHandler.succeeded()) {
                final String overwrite = aContext.request().getParam("overwrite");
                final boolean shouldOverwrite = overwrite != null && overwrite.equals("overwrite");

                // We want to see if a manifest already exists before writing a new one
                ptObj.find(MANIFEST_FILE, findHandler -> {
                    if (findHandler.succeeded()) {
                        if (findHandler.result()) {
                            if (shouldOverwrite) {
                                LOGGER.debug(MessageCodes.DBG_037, aID);
                                writeManifestFile(aFilePath, ptObj, aContext, aJsonNode);
                            } else {
                                aJsonNode.put("upload-message",
                                        "Manifest already exists and overwrite was not specified");
                                LOGGER.warn(MessageCodes.WARN_021, ptObj.getPath(MANIFEST_FILE));
                                toTemplate(aContext, aJsonNode);
                            }
                        } else {
                            LOGGER.debug(MessageCodes.DBG_038, aID);
                            writeManifestFile(aFilePath, ptObj, aContext, aJsonNode);
                        }
                    } else {
                        // FIXME: Is this handling this correctly?
                        fail(aContext, existsHandler.cause());
                        toTemplate(aContext, aJsonNode);
                    }
                });
            } else {
                fail(aContext, existsHandler.cause());
                toTemplate(aContext, aJsonNode);
            }
        });
    }

    private void writeManifestFile(final String aFilePath, final PairtreeObject aPtObj, final RoutingContext aContext,
            final ObjectNode aJsonNode) {
        aContext.vertx().fileSystem().readFile(aFilePath, readHandler -> {
            if (readHandler.succeeded()) {
                // This overwrites by default if the file exists
                aPtObj.put(MANIFEST_FILE, readHandler.result(), putHandler -> {
                    if (putHandler.succeeded()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(MessageCodes.DBG_039, aPtObj.getPath(MANIFEST_FILE));
                        }
                    } else {
                        fail(aContext, putHandler.cause());
                    }

                    toTemplate(aContext, aJsonNode);
                });
            } else {
                fail(aContext, readHandler.cause());
                toTemplate(aContext, aJsonNode);
            }
        });
    }

    private void processCSVUpload(final String aFileName, final String aFilePath, final RoutingContext aContext,
            final ObjectNode aJsonNode) {
        final HttpServerRequest request = aContext.request();
        final String overwrite = request.getParam("overwrite");
        final String skipTiles = request.getParam("skiptiles");
        final String skipThumbs = request.getParam("skipthumbs");
        final String skipIndexing = request.getParam("skipindexing");
        final String skipProperties = request.getParam("skipproperties");
        final boolean overwriteValue = overwrite != null && overwrite.equals("overwrite");

        CSVReader reader = null;
        String[] line;

        try {
            reader = new CSVReader(new FileReader(aFilePath));

            // File is ID,PATH_TO_IMAGE
            while ((line = reader.readNext()) != null) {
                if (line.length >= 2) {
                    final JsonObject json = new JsonObject();

                    json.put(ID_KEY, line[0]);
                    json.put(FILE_PATH_KEY, line[1]);
                    json.put(OVERWRITE_KEY, overwriteValue);

                    if (skipTiles != null) {
                        json.put(skipTiles, true);
                    }

                    if (skipThumbs != null) {
                        json.put(skipThumbs, true);
                    }

                    if (skipIndexing != null) {
                        json.put(skipIndexing, true);
                    }

                    if (skipProperties != null) {
                        json.put(skipProperties, true);
                    }

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageCodes.DBG_040, line[1], aFileName);
                    }

                    sendMessage(aContext, json, ImageIngestVerticle.class.getName(), 0);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageCodes.DBG_041, line[1]);
                    }
                } else {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(MessageCodes.DBG_042, Arrays.toString(line));
                    }
                }
            }
        } catch (final IOException details) {
            fail(aContext, details);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException details) {
                    LOGGER.error(details.getMessage(), details);
                }
            }

            toTemplate(aContext, aJsonNode);
        }
    }

    private void toTemplate(final RoutingContext aContext, final ObjectNode aJsonNode) {
        aContext.data().put(HBS_DATA_KEY, toHbsContext(aJsonNode, aContext));
        aContext.next();
    }

    // A copy and paste from the AbstractJiiifyVerticle... something to put in a utility class?
    protected void sendMessage(final RoutingContext aContext, final JsonObject aJsonObject, final String aVerticleName,
            final int aCount) {
        final long sendTimeout = DeliveryOptions.DEFAULT_TIMEOUT * aCount;
        final int retryCount = 10;
        final DeliveryOptions options = new DeliveryOptions();
        final EventBus eventBus = aContext.vertx().eventBus();

        // Slow down timeout expectations if we're taking a long time processing images
        if (aCount > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.DBG_043, getClass().getSimpleName(), sendTimeout);
            }

            options.setSendTimeout(sendTimeout);
        }

        LOGGER.debug(MessageCodes.DBG_044, sendTimeout, aJsonObject);

        eventBus.send(aVerticleName, aJsonObject, options, response -> {
            if (response.failed()) {
                if (aCount < retryCount) {
                    LOGGER.warn(MessageCodes.WARN_007, aVerticleName, aJsonObject);
                    sendMessage(aContext, aJsonObject, aVerticleName, aCount + 1);
                } else {
                    if (response.cause() != null) {
                        LOGGER.error(response.cause(), MessageCodes.EXC_061, aVerticleName, aJsonObject);
                    } else {
                        LOGGER.error(MessageCodes.EXC_039, aVerticleName, aJsonObject);
                    }
                }
            }
        });
    }

}
