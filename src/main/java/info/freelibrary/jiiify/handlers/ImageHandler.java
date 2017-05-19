
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.imgscalr.Scalr;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.InvalidRotationException;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.FileUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler that handles image requests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
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
            final String id = imageRequest.getID();
            final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);
            final String requestPath = ptObj.getPath(imageRequest.getPath());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking whether cached image file '{}' exists", requestPath);
            }

            ptObj.find(imageRequest.getPath(), findHandler -> {
                if (findHandler.succeeded()) {
                    if (findHandler.result()) {
                        serveCachedImageFile(ptObj, imageRequest.getPath(), aContext);
                    } else if (imageRequest.getRotation().isRotated()) {
                        checkUnrotatedSource(ptObj, imageRequest, aContext);
                    } else {
                        LOGGER.info("Requested image file not found: {}", requestPath);
                        aContext.fail(404);
                        aContext.put(ERROR_MESSAGE, msg("Image file not found: {}", requestPath));
                    }
                } else {
                    LOGGER.info("Requested image file not found: {}", requestPath);
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image file not found: {}", requestPath));
                }
            });
        } catch (final Exception details) {
            fail(aContext, details);
            aContext.put(ERROR_MESSAGE, details.getMessage());
        }
    }

    private void serveRotatedImage(final PairtreeObject aPtObj, final ImageRequest aImageRequest,
            final Scalr.Rotation aRotation, final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final HttpServerResponse response = aContext.response();

        // Options: FileCacheImageInputStream or MemoryCacheImageInputStream or ?

        aPtObj.get(aImageRequest.getPath(), getHandler -> {
            if (getHandler.succeeded()) {
                try {
                    final String mimeType = ImageFormat.getMIMEType(FileUtils.getExt(request.uri()));
                    final ByteArrayInputStream inStream = new ByteArrayInputStream(getHandler.result().getBytes());
                    final MemoryCacheImageInputStream cacheStream = new MemoryCacheImageInputStream(inStream);
                    final BufferedImage image = ImageIO.read(cacheStream);
                    final BufferedImage rotatedImage = Scalr.rotate(image, aRotation, (BufferedImageOp[]) null);
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    image.flush();
                    ImageIO.write(rotatedImage, aImageRequest.getFormat().getExtension(), baos);
                    baos.flush();

                    final byte[] bytes = baos.toByteArray();
                    rotatedImage.flush();

                    response.putHeader(Metadata.CONTENT_TYPE, mimeType);
                    response.putHeader(Metadata.CONTENT_LENGTH, Integer.toString(bytes.length));
                    response.end(Buffer.buffer(bytes));
                    response.close();

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Served image file: {}", request.uri());
                    }
                } catch (final Exception details) {
                    LOGGER.error(details.getMessage(), details);
                    aContext.fail(500);
                    aContext.put(ERROR_MESSAGE, details.getMessage());
                }
            } else {
                LOGGER.error(getHandler.cause().getMessage(), getHandler.cause());
                aContext.fail(500);
                aContext.put(ERROR_MESSAGE, getHandler.cause().getMessage());
            }
        });
    }

    private void checkUnrotatedSource(final PairtreeObject aPtObj, final ImageRequest aImageRequest,
            final RoutingContext aContext) {
        final ImageRequest imageRequest = aImageRequest.clone();

        try {
            imageRequest.setRotation(new ImageRotation(0f));
            LOGGER.debug("Checking for default rotation: {}", imageRequest.toString());
        } catch (final InvalidRotationException details) {
            throw new RuntimeException(details); // This should never happen
        }

        aPtObj.find(imageRequest.getPath(), findHandler -> {
            if (findHandler.succeeded()) {
                if (findHandler.result()) {
                    final float degrees = aImageRequest.getRotation().getValue();
                    final Scalr.Rotation rotation;

                    if (degrees == 90f) {
                        rotation = Scalr.Rotation.CW_90;
                        serveRotatedImage(aPtObj, imageRequest, rotation, aContext);
                    } else if (degrees == 180f) {
                        rotation = Scalr.Rotation.CW_180;
                        serveRotatedImage(aPtObj, imageRequest, rotation, aContext);
                    } else if (degrees == 270f) {
                        rotation = Scalr.Rotation.CW_270;
                        serveRotatedImage(aPtObj, imageRequest, rotation, aContext);
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Got an unexpected rotation value: {}", degrees);
                        }

                        aContext.fail(404);
                    }
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Didn't find unrotated cache file: {}", aPtObj.getPath(imageRequest.getPath()));
                    }

                    aContext.fail(404);
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Filesystem check for unrotated cache file failed: {}", aPtObj.getPath(imageRequest
                            .getPath()));
                }

                aContext.fail(404);
            }
        });
    }

    private void serveCachedImageFile(final PairtreeObject aPtObj, final String aResourcePath,
            final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final HttpServerResponse response = aContext.response();

        aPtObj.get(aResourcePath, getHandler -> {
            if (getHandler.succeeded()) {
                final String mimeType = ImageFormat.getMIMEType(FileUtils.getExt(request.uri()));

                response.putHeader(Metadata.CONTENT_TYPE, mimeType);
                response.end(getHandler.result());
                response.close();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Served image file: {}", request.uri());
                }
            } else {
                fail(aContext, getHandler.cause());
            }
        });
    }

}
