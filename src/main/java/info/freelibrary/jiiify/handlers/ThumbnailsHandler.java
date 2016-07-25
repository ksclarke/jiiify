
package info.freelibrary.jiiify.handlers;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static info.freelibrary.jiiify.Metadata.MANIFEST_FILE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.util.List;

import com.jayway.jsonpath.JsonPath;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Deprecated
public class ThumbnailsHandler extends JiiifyHandler {

    /**
     * Creates a thumbnail handler.
     *
     * @param aConfig The application's configuration
     */
    public ThumbnailsHandler(final Configuration aConfig) {
        super(aConfig);
    }

    /**
     * Handles thumbnail requests (mapped to: /service-prefix/[ID]/manifest/thumbnails).
     */
    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);
        final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking IIIF manifest file for thumbnails: {}", ptObj.getPath(MANIFEST_FILE));
        }

        // FIXME: put this centrally for all IIIF routes(?)
        response.headers().set("Access-Control-Allow-Origin", "*");

        ptObj.find(MANIFEST_FILE, findHandler -> {
            if (findHandler.succeeded()) {
                if (findHandler.result()) {
                    ptObj.get(MANIFEST_FILE, getHandler -> {
                        if (getHandler.succeeded()) {
                            response.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);
                            response.end(transformJSON(getHandler.result()));
                            response.close();
                        } else {
                            fail(aContext, getHandler.cause());
                            aContext.put(ERROR_MESSAGE, msg("Failed to serve image manifest: {}", request.uri()));
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image manifest file not found: " + request.uri()));
                }
            } else {
                fail(aContext, findHandler.cause());
                aContext.put(ERROR_MESSAGE, msg("Failed to serve image manifest: {}", request.uri()));
            }
        });
    }

    private String transformJSON(final Buffer aBuffer) {
        final String json = aBuffer.toString();
        final Object jdoc = defaultConfiguration().jsonProvider().parse(json);
        final String idPath = "$.sequences[0].canvases[*].images[0].resource.service.@id";
        final String thumbnailPath = "$.sequences[0].canvases[*].thumbnail";
        final String labelPath = "$.sequences[0].canvases[*].label";
        final List<String> images = JsonPath.read(jdoc, idPath);
        final List<String> labels = JsonPath.read(jdoc, labelPath);
        final List<String> thumbnails = JsonPath.read(jdoc, thumbnailPath);
        final JsonArray jsonArray = new JsonArray();

        for (int index = 0; index < images.size(); index++) {
            final JsonObject jsonObject = new JsonObject();

            jsonObject.put("id", extractID(images.get(index)));

            if (labels.size() >= images.size()) {
                jsonObject.put("label", labels.get(index));
            }

            if (thumbnails.size() >= images.size()) {
                jsonObject.put("thumbnail", thumbnails.get(index));
            }

            jsonArray.add(jsonObject);
        }

        return new JsonObject().put("items", jsonArray).toString();
    }

    private String extractID(final String aURL) {
        final String servicePrefix = myConfig.getServicePrefix();
        final int index = aURL.indexOf(servicePrefix) + 1;

        return aURL.substring(index + servicePrefix.length()).replace("/info.json", "");
    }
}
