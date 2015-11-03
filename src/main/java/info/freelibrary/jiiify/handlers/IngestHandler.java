
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.OVERWRITE_KEY;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.verticles.ImageIngestVerticle;

import au.com.bytecode.opencsv.CSVReader;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public class IngestHandler extends JiiifyHandler {

    public IngestHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpMethod method = aContext.request().method();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();

        if (method.equals(HttpMethod.POST)) {
            for (final FileUpload upload : aContext.fileUploads()) {
                final String fileName = upload.fileName();
                final String filePath = upload.uploadedFileName();

                CSVReader reader = null;
                String[] line;

                jsonNode.put("csv-file", fileName);

                try {
                    reader = new CSVReader(new FileReader(filePath));

                    // File is ID,PATH_TO_IMAGE
                    while ((line = reader.readNext()) != null) {
                        if (line.length >= 2) {
                            final JsonObject json = new JsonObject();

                            json.put(ID_KEY, line[0]);
                            json.put(FILE_PATH_KEY, line[1]);
                            json.put(OVERWRITE_KEY, true);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("To be ingested: {} ({})", line[1], fileName);
                            }

                            sendMessage(aContext, json, ImageIngestVerticle.class.getName(), 0);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Added to ingest queue: {}", line[1]);
                            }
                        } else {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Invalid CSV values detected: {}", Arrays.toString(line));
                            }
                        }
                    }
                } catch (final IOException details) {
                    LOGGER.error(details.getMessage(), details);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException details) {
                            LOGGER.error(details.getMessage(), details);
                        }
                    }
                }
            }
        }

        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
        aContext.next();
    }

    // A copy and paste from the AbstractJiiifyVerticle... something to put in a utility class?
    protected void sendMessage(final RoutingContext aContext, final JsonObject aJsonObject,
            final String aVerticleName, final int aCount) {
        final long sendTimeout = DeliveryOptions.DEFAULT_TIMEOUT * aCount;
        final int retryCount = 10;
        final DeliveryOptions options = new DeliveryOptions();
        final EventBus eventBus = aContext.vertx().eventBus();

        // Slow down timeout expectations if we're taking a long time processing images
        if (aCount > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Slowing down the {}'s timeout to: {}", getClass().getSimpleName(), sendTimeout);
            }

            options.setSendTimeout(sendTimeout);
        }

        eventBus.send(aVerticleName, aJsonObject, options, response -> {
            if (response.failed()) {
                if (aCount < retryCount) {
                    LOGGER.warn("Unable to send message to {}; retrying: {}", aVerticleName, aJsonObject);
                    sendMessage(aContext, aJsonObject, aVerticleName, aCount + 1);
                } else {
                    if (response.cause() != null) {
                        LOGGER.error(response.cause(), MessageCodes.EXC_000, "Unable to send message to {}: {}",
                                aVerticleName, aJsonObject);
                    } else {
                        LOGGER.error(MessageCodes.EXC_000, "Unable to send message to {}: {}", aVerticleName,
                                aJsonObject);
                    }
                }
            }
        });
    }

}
