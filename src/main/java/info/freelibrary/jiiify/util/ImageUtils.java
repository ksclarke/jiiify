
package info.freelibrary.jiiify.util;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.naming.ConfigurationException;

import org.opencv.core.Core;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.UnsupportedFormatException;
import info.freelibrary.jiiify.image.ImageObject;
import info.freelibrary.jiiify.image.JavaImageObject;
import info.freelibrary.jiiify.image.NativeImageObject;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.NativeLibraryLoader;
import info.freelibrary.util.StringUtils;

public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class, Constants.MESSAGES);

    private static boolean useNativeLibs;

    static {
        try {
            NativeLibraryLoader.load(Core.NATIVE_LIBRARY_NAME);

            if (LOGGER.isDebugEnabled() && useNativeLibs) {
                LOGGER.debug("Using native image processing libraries");
            }
        } catch (final IOException details) {
            LOGGER.warn("Error loading native libraries so using Java libraries instead: {}", details.getMessage());
        }
    }

    /* Template for the region part of the IIIF request */
    private static final String REGION = "{},{},{},{}";

    /* All out-of-the-box tiles are not rotated */
    private static final String LABEL = "0/default.jpg";

    private ImageUtils() {
    }

    /**
     * Return a list of derivative images to be pre-generated so that the OpenSeadragon viewer can use them.
     *
     * @return A list of derivative images to be pre-generated
     */
    public static List<String> getTilePaths(final String aService, final String aID, final int aTileSize,
            final int aWidth, final int aHeight) {
        final ArrayList<String> list = new ArrayList<String>();
        final int longDim = Math.max(aWidth, aHeight);
        final String id;

        // Object ID may need to be URL encoded for use on the Web
        try {
            id = URLEncoder.encode(aID, Constants.UTF_8_ENCODING);
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException(details); // All JVMs required to support UTF-8
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generating tile paths [ID: {}; Tile Size: {}; Width: {}; Height: {}]", aID, aTileSize,
                    aWidth, aHeight);
        }

        for (int multiplier = 1; multiplier * aTileSize < longDim; multiplier *= 2) {
            final int tileSize = multiplier * aTileSize;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating tiles using multiplier of {}", multiplier);
            }

            int x = 0;
            int y = 0;
            int xTileSize;
            int yTileSize;

            String region;
            String path;
            String size;

            for (x = 0; x < aWidth + tileSize; x += tileSize) {
                xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                yTileSize = tileSize < aHeight ? tileSize : aHeight;

                if (xTileSize > 0 && yTileSize > 0) {
                    region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                    size = getSize(multiplier, xTileSize, yTileSize);

                    // Support the canonical 2.0 Image API URI syntax
                    if (ratio(xTileSize, yTileSize).equals(ratio(size))) {
                        size = size.substring(0, size.indexOf(',') + 1);
                    }

                    path = StringUtils.toString('/', aService, id, region, size, LABEL);

                    if (!list.add(path)) {
                        LOGGER.warn("Tile path '{}' could not be added to queue", path);
                    }
                }

                for (y = tileSize; y < aHeight + tileSize; y += tileSize) {
                    xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                    yTileSize = y + tileSize < aHeight ? tileSize : aHeight - y;

                    if (xTileSize > 0 && yTileSize > 0) {
                        region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                        size = getSize(multiplier, xTileSize, yTileSize);

                        // Support the canonical 2.0 Image API URI syntax
                        if (ratio(xTileSize, yTileSize).equals(ratio(size))) {
                            size = size.substring(0, size.indexOf(',') + 1);
                        }

                        path = StringUtils.toString('/', aService, id, region, size, LABEL);

                        if (!list.add(path)) {
                            LOGGER.warn("Tile path '{}' could not be added to queue", path);
                        }
                    }
                }

                y = 0;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            final StringBuilder builder = new StringBuilder();

            LOGGER.debug("{} tiles needed for {}", list.size(), aID);

            for (final Object path : list.toArray()) {
                LOGGER.debug("Tile path: {}", path);
            }
        }

        // FIXME: tiles that are requested first should be at the top of the list
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns whether native image libraries or pure Java ones are being used.
     *
     * @return True if native image libraries are being used; else, false
     */
    public static boolean useNativeLibs() {
        return useNativeLibs;
    }

    /**
     * Sets whether to use native image libraries or to use the standard Java ones.
     *
     * @param aFlagToUseNativeLibs
     */
    public static void useNativeLibs(final boolean aFlagToUseNativeLibs) {
        useNativeLibs = aFlagToUseNativeLibs;
    }

    /**
     * Converts a source image into a destination image of a different format.
     *
     * @param aSrcImageFile The input image
     * @param aDestImageFile The output image
     * @throws IOException If there is trouble converting the image
     * @throws UnsupportedFormatException If one of the images' formats is not supported
     */
    public static void convert(final File aSrcImageFile, final File aDestImageFile) throws IOException,
            UnsupportedFormatException {
        final ImageObject image;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Converting '{}' into '{}'", aSrcImageFile, aDestImageFile);
        }

        if (useNativeLibs) {
            image = new NativeImageObject(aSrcImageFile);
        } else {
            image = new JavaImageObject(aSrcImageFile);
        }

        image.write(aDestImageFile);
    }

    /**
     * Transforms a source image into a cached image file using the supplied
     * {@see info.freelibrary.jiiify.iiif.ImageRequest}
     *
     * @param aImageFile A source image file
     * @param aImageRequest A IIIF image request
     * @param aCacheFile An output cached image file
     * @throws IOException If there is a problem reading or writing the image files
     */
    public static void transform(final File aImageFile, final ImageRequest aImageRequest, final File aCacheFile)
            throws IOException {
        final ImageObject image;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Transforming '{}' into '{}'", aImageFile, aCacheFile);
        }

        if (useNativeLibs) {
            image = new NativeImageObject(aImageFile);
        } else {
            image = new JavaImageObject(aImageFile);
        }

        if (!aImageRequest.getRegion().isFullImage()) {
            image.extractRegion(aImageRequest.getRegion());
        }

        if (!aImageRequest.getSize().isFullSize()) {
            image.resize(aImageRequest.getSize());
        }

        if (aImageRequest.getRotation().isRotated()) {
            image.rotate(aImageRequest.getRotation());
        }

        if (!aImageRequest.getQuality().equals(ImageQuality.DEFAULT)) {
            image.adjustQuality(aImageRequest.getQuality());
        }

        image.write(aCacheFile);
    }

    /**
     * This gets the image dimension without reading the whole file into memory.
     *
     * @param aImageFile A file from which to pull dimension
     * @return An image dimension
     */
    public static Dimension getImageDimension(final File aImageFile) throws IOException {
        final String mimeType = ImageFormat.getMIMEType(FileUtils.getExt(aImageFile.getName()));
        final Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimeType);

        if (readers.hasNext()) {
            final ImageReader reader = readers.next();
            final ImageInputStream inStream = ImageIO.createImageInputStream(aImageFile);

            try {
                reader.setInput(inStream);
                return new Dimension(reader.getWidth(0), reader.getHeight(0));
            } finally {
                inStream.close();
                reader.dispose();
            }
        }

        throw new RuntimeException(LOGGER.getMessage(MessageCodes.EXC_026, mimeType));
    }

    /**
     * Gets the center of an image.
     *
     * @param aImageFile A source image file
     * @return An image region representing the center of the image
     * @throws ConfigurationException If there is a configuration error
     * @throws IOException If there is trouble reading the source image file
     */
    public static ImageRegion getCenter(final File aImageFile) throws ConfigurationException, IOException {
        return getCenter(getImageDimension(aImageFile));
    }

    /**
     * Gets the center of an image, represented by a {@see java.awt.Dimension} object.
     *
     * @param aDimension Dimensions to use as the source for the calculation of center
     * @return An image region representing the center of the dimensions
     * @throws ConfigurationException If there is a configuration error
     */
    public static ImageRegion getCenter(final Dimension aDimension) throws ConfigurationException {
        final int smallSide = Math.min(aDimension.height, aDimension.width);
        final ImageRegion region;

        if (smallSide == aDimension.height) {
            region = new ImageRegion((aDimension.width - smallSide) / 2, 0, smallSide, smallSide);
        } else {
            region = new ImageRegion(0, (aDimension.height - smallSide) / 2, smallSide, smallSide);
        }

        return region;
    }

    /**
     * Gets the ratio of the supplied width and height.
     *
     * @param aWidth Width to use in getting the ratio
     * @param aHeight Height to use in getting the ratio
     * @return A string representation of the ratio
     */
    public static String ratio(final int aWidth, final int aHeight) {
        final int gcd = gcd(aWidth, aHeight);
        return aHeight / gcd + ":" + aHeight / gcd;
    }

    /**
     * Gets the ratio from the supplied IIIF size string.
     *
     * @param aSize A IIIF image size string
     * @return A string representation of the ratio
     */
    public static String ratio(final String aSize) {
        final String[] widthHeight = aSize.split("\\,");

        if (widthHeight.length != 2) {
            throw new IllegalArgumentException("Argument is not a comma delimited size: " + aSize);
        }

        return ratio(Integer.parseInt(widthHeight[0]), Integer.parseInt(widthHeight[1]));
    }

    private static String getSize(final double aMultiplier, final int aXTileSize, final int aYTileSize) {
        return (int) Math.ceil(aXTileSize / aMultiplier) + "," + (int) Math.ceil(aYTileSize / aMultiplier);
    }

    private static int gcd(final int aWidth, final int aHeight) {
        if (aHeight == 0) {
            return aWidth;
        } else {
            return gcd(aHeight, aWidth % aHeight);
        }
    }
}
