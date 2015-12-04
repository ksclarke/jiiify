
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.util.List;

import com.jayway.jsonpath.JsonPath;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ThumbnailsHandler extends JiiifyHandler {

    public ThumbnailsHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();

        // Path: /service-prefix/[ID]/manifest/thumbnails
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);

        final String manifest = PathUtils.getFilePath(aContext.vertx(), id, Metadata.MANIFEST_FILE);
        final FileSystem fileSystem = aContext.vertx().fileSystem();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking IIIF manifest file for thumbnails: {}", manifest);
        }

        response.headers().set("Access-Control-Allow-Origin", "*");

        // TODO: check cache for a previously stored version before going to the file system

        fileSystem.exists(manifest, fsHandler -> {
            if (fsHandler.succeeded()) {
                if (fsHandler.result()) {
                    fileSystem.readFile(manifest, fileHandler -> {
                        if (fileHandler.succeeded()) {
                            response.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);
                            response.end(transformJSON(fileHandler.result()));
                            response.close();

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Served manifest file: {}", request.uri());
                            }
                        } else {
                            fail(aContext, fileHandler.cause());
                            aContext.put(ERROR_MESSAGE, msg("Failed to serve image manifest: {}", request.uri()));
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image manifest file not found: " + request.uri()));
                }
            } else {
                fail(aContext, fsHandler.cause());
                aContext.put(ERROR_MESSAGE, msg("Failed to serve image manifest: {}", request.uri()));
            }
        });
    }

    private String transformJSON(final Buffer aBuffer) {
        final String json = aBuffer.toString();
        final Object jdoc = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(json);
        final String idPath = "$.sequences[0].canvases[*].images[0].resource.service.@id";
        final String labelPath = "$.sequences[0].canvases[*].label";
        final List<String> images = JsonPath.read(jdoc, idPath);
        final List<String> labels = JsonPath.read(jdoc, labelPath);
        final JsonArray jsonArray = new JsonArray();

        for (int index = 0; index < images.size(); index++) {
            final JsonObject jsonObject = new JsonObject();

            jsonObject.put("id", extractID(images.get(index)));

            if (labels.size() >= images.size()) {
                jsonObject.put("label", labels.get(index));
            }

            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }

    private String extractID(final String aURL) {
        final String servicePrefix = myConfig.getServicePrefix();
        final int index = aURL.indexOf(servicePrefix) + 1;

        return aURL.substring(index + servicePrefix.length()).replace("/info.json", "");
    }
}
