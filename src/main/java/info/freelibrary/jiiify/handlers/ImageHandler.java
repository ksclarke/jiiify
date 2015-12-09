
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_SOURCE_KEY;
import static info.freelibrary.jiiify.Metadata.PROPERTIES_FILE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.jiiify.verticles.ImageWorkerVerticle;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.PairtreeRoot;

import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ImageHandler extends JiiifyHandler {

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

    private void trySourceFile(final PairtreeRoot aPairtree, final ImageRequest aImage, final FileSystem aFileSystem,
            final RoutingContext aContext) {
        final File ptObjDir = aPairtree.getObject(aImage.getID());
        final String propFilePath = new File(ptObjDir, PROPERTIES_FILE).getAbsolutePath();

        aFileSystem.exists(propFilePath, propFileHandler -> {
            if (propFileHandler.succeeded()) {
                if (propFileHandler.result()) {
                    aFileSystem.readFile(propFilePath, fileHandler -> {
                        if (fileHandler.succeeded()) {
                            final InputStream bytes = new ByteArrayInputStream(fileHandler.result().getBytes());
                            final Properties properties = new Properties();

                            try {
                                properties.loadFromXML(bytes);
                                bytes.close(); /* Has no effect */

                                if (properties.containsKey(IMAGE_SOURCE_KEY)) {
                                    final File source = new File(properties.getProperty(IMAGE_SOURCE_KEY));

                                    aFileSystem.exists(source.getAbsolutePath(), sourceHandler -> {
                                        if (sourceHandler.succeeded()) {
                                            if (sourceHandler.result()) {
                                                serveSourceFile(aContext, aFileSystem, source, aImage);
                                            } else {
                                                aContext.fail(404);
                                                aContext.put(ERROR_MESSAGE, msg("{} not found", aImage.toString()));
                                            }
                                        } else {
                                            fail(aContext, sourceHandler.cause());
                                        }
                                    });
                                } else {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("The {} file was found but it didn't contain the '{}' key",
                                                PROPERTIES_FILE, IMAGE_SOURCE_KEY);
                                    }

                                    aContext.fail(404);
                                    aContext.put(ERROR_MESSAGE, msg("{} not found", aImage.toString()));
                                }
                            } catch (final IOException details) {
                                fail(aContext, details);
                            }

                            IOUtils.closeQuietly(bytes);
                        } else {
                            fail(aContext, propFileHandler.cause());
                        }
                    });
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Looked for {} file at {} but didn't find one", PROPERTIES_FILE, ptObjDir);
                    }

                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("{} not found", aImage.toString()));
                }
            } else {
                fail(aContext, propFileHandler.cause());
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
