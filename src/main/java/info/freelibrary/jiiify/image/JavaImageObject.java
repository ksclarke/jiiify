
package info.freelibrary.jiiify.image;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.imgscalr.Scalr;

import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class JavaImageObject implements ImageObject {

    private final Logger LOGGER = LoggerFactory.getLogger(JavaImageObject.class, MESSAGES);

    private BufferedImage myImage;

    private boolean needsFormatChange;

    public JavaImageObject(final File aSourceImage) throws IOException {
        myImage = ImageIO.read(aSourceImage);
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
    public void changeFormat(final ImageFormat aFormat) throws IOException {

    }

    @Override
    public void write(final File aImageFile) throws IOException {
        final String mimeType = ImageFormat.getMIMEType(FileUtils.getExt(aImageFile.getAbsolutePath()));
        final Iterator<ImageWriter> iterator = ImageIO.getImageWritersByMIMEType(mimeType);

        if (iterator.hasNext()) {
            final ImageWriter writer = iterator.next();

            try {
                final File parent = aImageFile.getParentFile();

                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Unable to create directory structure: " + parent);
                }

                writer.setOutput(new FileImageOutputStream(aImageFile));
                writer.write(myImage);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Wrote [{}] to {}", myImage, aImageFile);
                }
            } finally {
                writer.dispose();
            }
        } else {
            throw new IOException(LOGGER.getMessage("Could not find a writer for {}", mimeType));
        }
    }

}
