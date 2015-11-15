
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.JSON_MIME_TYPE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * A redirect handler that handles all the redirects for the Jiiify application.
 *
 * @author Kevin S. Clarke <a href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>
 */
public class ImageInfoHandler extends JiiifyHandler {

    /**
     * Creates a handler that directs redirect requests for the Jiiify application.
     *
     * @param aConfig A configuration object
     */
    public ImageInfoHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();

        // Path: /service-prefix/[ID]/info.json
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);
        final String imageInfo = PathUtils.getFilePath(aContext.vertx(), id, ImageInfo.FILE_NAME);
        final FileSystem fileSystem = aContext.vertx().fileSystem();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for image info file: '{}'", imageInfo);
        }

        // FIXME: put this centrally for all IIIF routes(?)
        response.headers().set("Access-Control-Allow-Origin", "*");

        fileSystem.exists(imageInfo, fsHandler -> {
            if (fsHandler.succeeded()) {
                if (fsHandler.result()) {
                    fileSystem.readFile(imageInfo, fileHandler -> {
                        if (fileHandler.succeeded()) {
                            response.putHeader(CONTENT_TYPE, JSON_MIME_TYPE);
                            response.end(fileHandler.result());
                            response.close();

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Served image info: {}", request.uri());
                            }
                        } else {
                            fail(aContext, fileHandler.cause());
                            error(aContext, request.uri());
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_HEADER, "Image Info File Not Found");
                    aContext.put(ERROR_MESSAGE, msg("Image info file not found: " + request.uri()));
                }
            } else {
                fail(aContext, fsHandler.cause());
                error(aContext, request.uri());
            }
        });
    }

    private void error(final RoutingContext aContext, final String aURI) {
        aContext.put(ERROR_HEADER, "Image Info Request Error");
        aContext.put(ERROR_MESSAGE, msg("Failed to serve image info: {}", aURI));
    }

}