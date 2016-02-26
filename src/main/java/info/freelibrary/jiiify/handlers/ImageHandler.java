
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.io.File;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.jiiify.verticles.ImageWorkerVerticle;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeRoot;

import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ImageHandler extends JiiifyHandler {

    /**
     * Creates a IIIF image handler.
     *
     * @param aConfig The application's configuration
     */
    public ImageHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final String uri = request.uri();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("IIIF Image request: {}", uri);
        }

        try {
            final ImageRequest image = new ImageRequest(request.path());
            final PairtreeRoot pairtree = PathUtils.getPairtreeRoot(aContext.vertx(), image.getID());
            final String cacheFilePath = image.getCacheFile(pairtree).getAbsolutePath();
            final FileSystem fileSystem = aContext.vertx().fileSystem();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking whether cached image file '{}' exists", cacheFilePath);
            }

            fileSystem.exists(cacheFilePath, fsHandler -> {
                if (fsHandler.succeeded()) {
                    /* fsHandler's result is the whether file exists or not */
                    if (fsHandler.result()) {
                        serveImageFile(fileSystem, cacheFilePath, aContext);
                    } else {
                        /* Turning this off until it's better tested */
                        // trySourceFile(pairtree, image, fileSystem, aContext);

                        /* Instead, return 404 */
                        aContext.fail(404);
                        aContext.put(ERROR_MESSAGE, msg("Image file not found: {}", cacheFilePath));
                    }
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image file not found: {}", cacheFilePath));
                }
            });
        } catch (final Exception details) {
            fail(aContext, details);
            aContext.put(ERROR_MESSAGE, details.getMessage());
        }
    }

    private void serveImageFile(final FileSystem aFileSystem, final String aFilePath, final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final HttpServerResponse response = aContext.response();

        aFileSystem.readFile(aFilePath, fileHandler -> {
            if (fileHandler.succeeded()) {
                final String mimeType = ImageFormat.getMIMEType(FileUtils.getExt(request.uri()));

                response.putHeader(Metadata.CONTENT_TYPE, mimeType);
                response.end(fileHandler.result());
                response.close();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Served image file: {}", request.uri());
                }
            } else {
                fail(aContext, fileHandler.cause());
            }
        });
    }

    private void serveSourceFile(final RoutingContext aContext, final FileSystem aFileSystem, final File aSource,
            final ImageRequest aImage) {
        final JsonObject message = new JsonObject();

        message.put(IIIF_PATH_KEY, aImage.toString());
        message.put(FILE_PATH_KEY, aSource.getAbsolutePath());

        aContext.vertx().eventBus().send(ImageWorkerVerticle.class.getName(), message, handler -> {
            if (handler.succeeded()) {
                // FIXME: does this block? is file created after handler returns
                serveImageFile(aFileSystem, aSource.getAbsolutePath(), aContext);
            } else {
                aContext.fail(404);
                aContext.put(ERROR_MESSAGE, msg("{} not found", aImage.toString()));
            }
        });
    }

}
