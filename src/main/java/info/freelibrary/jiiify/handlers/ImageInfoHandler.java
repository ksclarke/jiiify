
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.JSON_MIME_TYPE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles requests for image info JSON files.
 *
 * @author Kevin S. Clarke <a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>
 */
public class ImageInfoHandler extends JiiifyHandler {

    /**
     * Creates a handler that handles requests for image info JSON files.
     *
     * @param aConfig A configuration object
     */
    public ImageInfoHandler(final Configuration aConfig) {
        super(aConfig);
    }

    /**
     * Handles image info requests (mapped to: /service-prefix/[ID]/info.json).
     */
    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);
        final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for IIIF image info file: {}", ptObj.getPath(ImageInfo.FILE_NAME));
        }

        // FIXME: put this centrally for all IIIF routes(?)
        response.headers().set("Access-Control-Allow-Origin", "*");

        ptObj.find(ImageInfo.FILE_NAME, existsHandler -> {
            if (existsHandler.succeeded()) {
                if (existsHandler.result()) {
                    ptObj.get(ImageInfo.FILE_NAME, getHandler -> {
                        if (getHandler.succeeded()) {
                            response.putHeader(CONTENT_TYPE, JSON_MIME_TYPE);
                            response.end(getHandler.result());
                            response.close();

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Served image info: {}", request.uri());
                            }
                        } else {
                            fail(aContext, existsHandler.cause());
                            error(aContext, request);
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_HEADER, "Image Info File Not Found");
                    aContext.put(ERROR_MESSAGE, msg("Image info file not found: " + request.uri()));
                }
            } else {
                fail(aContext, existsHandler.cause());
                error(aContext, request);
            }
        });
    }

    private void error(final RoutingContext aContext, final HttpServerRequest aRequest) {
        aContext.put(ERROR_HEADER, "Image Info Request Error");
        aContext.put(ERROR_MESSAGE, msg("Failed to serve image info: {}", aRequest.uri()));
    }

}