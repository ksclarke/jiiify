
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ManifestHandler extends JiiifyHandler {

    /**
     * A IIIF manifest handler.
     *
     * @param aConfig The application's configuration
     */
    public ManifestHandler(final Configuration aConfig) {
        super(aConfig);
    }

    /**
     * Handles image info requests (mapped to: /service-prefix/[ID]/manifest).
     */
    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);
        final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for IIIF manifest file: {}", ptObj.getPath(Metadata.MANIFEST_FILE));
        }

        // FIXME: put this centrally for all IIIF routes(?)
        response.headers().set("Access-Control-Allow-Origin", "*");

        ptObj.find(Metadata.MANIFEST_FILE, findHandler -> {
            if (findHandler.succeeded()) {
                if (findHandler.result()) {
                    ptObj.get(Metadata.MANIFEST_FILE, getHandler -> {
                        if (getHandler.succeeded()) {
                            response.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);
                            response.end(getHandler.result());
                            response.close();

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Served IIIF manifest file: {}", request.uri());
                            }
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

}
