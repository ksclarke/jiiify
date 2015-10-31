
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ManifestHandler extends JiiifyHandler {

    public ManifestHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();

        // Path: /service-prefix/[ID]/manifest
        final String id = request.uri().split("\\/")[2];
        final String manifest = PathUtils.getFilePath(aContext.vertx(), id, Metadata.MANIFEST_FILE);
        final FileSystem fileSystem = aContext.vertx().fileSystem();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for IIIF manifest file: {}", manifest);
        }

        response.headers().set("Access-Control-Allow-Origin", "*");

        fileSystem.exists(manifest, fsHandler -> {
            if (fsHandler.succeeded()) {
                if (fsHandler.result()) {
                    fileSystem.readFile(manifest, fileHandler -> {
                        if (fileHandler.succeeded()) {
                            response.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);
                            response.end(fileHandler.result());
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
}
