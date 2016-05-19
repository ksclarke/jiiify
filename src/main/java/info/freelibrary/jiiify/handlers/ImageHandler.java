
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.InvalidRotationException;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.jiiify.verticles.ImageWorkerVerticle;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeRoot;

import io.vertx.core.buffer.Buffer;
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
            final ImageRequest imageRequest = new ImageRequest(request.path());
            final String imageID = imageRequest.getID();
            final PairtreeRoot pairtree = PathUtils.getPairtreeRoot(aContext.vertx(), imageID);
            final String cacheFilePath = imageRequest.getCacheFile(pairtree).getAbsolutePath();
            final FileSystem fileSystem = aContext.vertx().fileSystem();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking whether cached image file '{}' exists", cacheFilePath);
            }

            fileSystem.exists(cacheFilePath, fsHandler -> {
                if (fsHandler.succeeded()) {
                    /* fsHandler's result is the whether file exists or not */
                    if (fsHandler.result()) {
                        serveCachedImageFile(fileSystem, cacheFilePath, aContext);
                    } else if (imageRequest.getRotation().isRotated()) {
                        checkUnrotatedSource(fileSystem, imageRequest, pairtree, aContext);
                    }
                } else {
                    LOGGER.error("File not found (2): {}", cacheFilePath);
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image file not found: {}", cacheFilePath));
                }
            });
        } catch (final Exception details) {
            fail(aContext, details);
            aContext.put(ERROR_MESSAGE, details.getMessage());
        }
    }

    private void serveRotatedImage(final ImageRequest aImageRequest, final Scalr.Rotation aRotation,
            final PairtreeRoot aPairtree, final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final HttpServerResponse response = aContext.response();
        final BufferedImageOp[] ops = null;

        try {
            final String mimeType = ImageFormat.getMIMEType(FileUtils.getExt(request.uri()));
            final BufferedImage image = ImageIO.read(aImageRequest.getCacheFile(aPairtree));
            final BufferedImage rotatedImage = Scalr.rotate(image, aRotation, ops);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] bytes;

            // Clean up the old image buffer
            image.flush();

            // Write buffered image to byte array
            ImageIO.write(rotatedImage, aImageRequest.getFormat().getExtension(), baos);
            baos.flush();
            bytes = baos.toByteArray();
            rotatedImage.flush();

            response.putHeader(Metadata.CONTENT_TYPE, mimeType);
            response.putHeader(Metadata.CONTENT_LENGTH, Integer.toString(bytes.length));
            response.end(Buffer.buffer(bytes));
            response.close();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Served image file: {}", request.uri());
            }
        } catch (final IOException details) {
            LOGGER.error(details.getMessage(), details);
            aContext.fail(500);
        }
    }

    private void checkUnrotatedSource(final FileSystem aFileSystem, final ImageRequest aImageRequest,
            final PairtreeRoot aPairtree, final RoutingContext aContext) {
        final ImageRequest imageRequest = aImageRequest.clone();

        try {
            imageRequest.setRotation(new ImageRotation(0f));
            LOGGER.debug("Checking for default rotation: {}", imageRequest.toString());
        } catch (final InvalidRotationException details) {
            // 90 degrees should not throw an exception
            throw new RuntimeException(details);
        }

        try {
            final String cacheFilePath = imageRequest.getCacheFile(aPairtree).getAbsolutePath();

            aFileSystem.exists(cacheFilePath, fsHandler -> {
                if (fsHandler.succeeded()) {
                    if (fsHandler.result()) {
                        final float degrees = aImageRequest.getRotation().getValue();
                        final Scalr.Rotation rotation;

                        if (degrees == 90f) {
                            rotation = Scalr.Rotation.CW_90;
                            serveRotatedImage(imageRequest, rotation, aPairtree, aContext);
                        } else if (degrees == 180f) {
                            rotation = Scalr.Rotation.CW_180;
                            serveRotatedImage(imageRequest, rotation, aPairtree, aContext);
                        } else if (degrees == 270f) {
                            rotation = Scalr.Rotation.CW_270;
                            serveRotatedImage(imageRequest, rotation, aPairtree, aContext);
                        } else {
                            LOGGER.debug("Got an unexpected rotation value: {}", degrees);
                            aContext.fail(404);
                        }
                    } else {
                        LOGGER.debug("Didn't find unrotated cache file: {}", cacheFilePath);
                        aContext.fail(404);
                    }
                } else {
                    LOGGER.debug("Filesystem check for unrotated cache file failed: {}", cacheFilePath);
                    aContext.fail(404);
                }
            });
        } catch (final IOException details) {
            aContext.fail(500);
            aContext.put(ERROR_MESSAGE, msg("IO error while processing request: {}", imageRequest));
        }
    }

    private void serveCachedImageFile(final FileSystem aFileSystem, final String aFilePath,
            final RoutingContext aContext) {
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

    private void serveNewImageFile(final FileSystem aFileSystem, final File aSource, final ImageRequest aImageRequest,
            final RoutingContext aContext) {
        final JsonObject message = new JsonObject();

        message.put(IIIF_PATH_KEY, aImageRequest.toString());
        message.put(FILE_PATH_KEY, aSource.getAbsolutePath());

        aContext.vertx().eventBus().send(ImageWorkerVerticle.class.getName(), message, handler -> {
            if (handler.succeeded()) {
                // FIXME: does this block? is file created after handler returns
                serveCachedImageFile(aFileSystem, aSource.getAbsolutePath(), aContext);
            } else {
                aContext.fail(404);
                aContext.put(ERROR_MESSAGE, msg("{} not found", aImageRequest.toString()));
            }
        });
    }

}
