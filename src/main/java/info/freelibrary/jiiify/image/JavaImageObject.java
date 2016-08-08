
package info.freelibrary.jiiify.image;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageInputStream;

import org.imgscalr.Scalr;

import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;

public class JavaImageObject implements ImageObject {

    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    private final Logger LOGGER = LoggerFactory.getLogger(JavaImageObject.class, MESSAGES);

    private BufferedImage myImage;

    private boolean needsFormatChange;

    /**
     * Creates new image using the pure Java image processing.
     *
     * @param aImgBuffer A source image in a Vertx {@link io.vertx.core.buffer.Buffer}
     * @throws IOException If there is trouble reading the image file
     */
    public JavaImageObject(final Buffer aImgBuffer) throws IOException {
        final ByteArrayInputStream inStream = new ByteArrayInputStream(aImgBuffer.getBytes());
        final FileCacheImageInputStream cacheStream = new FileCacheImageInputStream(inStream, TMP_DIR);
        // final MemoryCacheImageInputStream cacheStream = new MemoryCacheImageInputStream(inStream);

        myImage = ImageIO.read(cacheStream);
    }

    @Override
    public void extractRegion(final ImageRegion aRegion) throws IOException {
        final int x = aRegion.getInt(Region.X);
        final int y = aRegion.getInt(Region.Y);
        final int width = aRegion.getInt(Region.WIDTH);
        final int height = aRegion.getInt(Region.HEIGHT);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cropping {} to {},{},{},{}", myImage, x, y, width, height);
        }

        myImage = Scalr.crop(myImage, x, y, width, height, Scalr.OP_ANTIALIAS);
    }

    @Override
    public void resize(final ImageSize aSize) throws IOException {
        if (!aSize.isFullSize()) {
            final int height = aSize.getHeight(myImage.getHeight(), myImage.getWidth());
            final int width = aSize.getWidth(myImage.getWidth(), myImage.getHeight());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resizing [{}] to {},{}", myImage, width, height);
            }

            myImage = Scalr.resize(myImage, width, height, Scalr.OP_ANTIALIAS);
        }
    }

    @Override
    public void rotate(final ImageRotation aRotation) throws IOException {

    }

    @Override
    public void adjustQuality(final ImageQuality aQuality) throws IOException {

    }

    @Override
    public Buffer toBuffer(final String aFileExt) throws IOException {
        final String mimeType = ImageFormat.getMIMEType(aFileExt);
        final Iterator<ImageWriter> iterator = ImageIO.getImageWritersByMIMEType(mimeType);

        if (iterator.hasNext()) {
            final ImageWriter writer = iterator.next();
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            try {
                writer.setOutput(outStream);
                writer.write(myImage);
                outStream.flush();

                return Buffer.buffer(outStream.toByteArray());
            } finally {
                outStream.close();
                writer.dispose();
            }
        } else {
            throw new IOException(LOGGER.getMessage("Could not find a writer for {}", mimeType));
        }
    }

}
