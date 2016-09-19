
package info.freelibrary.jiiify.util;

import static info.freelibrary.jiiify.MessageCodes.EXC_026;
import static info.freelibrary.jiiify.iiif.ImageFormat.JP2_MIME_TYPE;

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

import org.opencv.core.Core;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.image.ImageObject;
import info.freelibrary.jiiify.image.JavaImageObject;
import info.freelibrary.jiiify.image.NativeImageObject;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.NativeLibraryLoader;
import info.freelibrary.util.StringUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import kdu_jni.Jp2_family_src;
import kdu_jni.Jp2_locator;
import kdu_jni.Jp2_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_channel_mapping;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_compressed_source;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;

public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class, Constants.MESSAGES);

    private static boolean useNativeLibs = true;

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
            final double aWidth, final double aHeight) {
        return getTilePaths(aService, aID, aTileSize, (int) aWidth, (int) aHeight);
    }

    /**
     * Return a list of derivative images to be pre-generated so that the OpenSeadragon viewer can use them.
     *
     * @return A list of derivative images to be pre-generated
     */
    public static List<String> getTilePaths(final String aService, final String aID, final int aTileSize,
            final int aWidth, final int aHeight) {
        final ArrayList<String> list = new ArrayList<>();
        final int longDim = Math.max(aWidth, aHeight);
        final String id;

        // Object ID may need to be URL encoded for use on the Web
        try {
            id = URLEncoder.encode(aID, Constants.UTF_8_ENCODING);
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException(details); // All JVMs required to support UTF-8
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generating tile paths [ID: {}; Tile Size: {}; Width: {}; Height: {}]", aID, aTileSize, aWidth,
                    aHeight);
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
            new StringBuilder();

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
     * This gets the image dimension without reading the whole file into memory.
     *
     * @param aImageFile A file from which to pull dimension
     * @return An image dimension
     */
    public static Dimension getImageDimension(final File aImageFile) throws IOException {
        // FIXME: Workaround for JP2 until it's supported by our ImageIO libraries
        if (JP2_MIME_TYPE.equals(ImageFormat.getMIMEType(FileUtils.getExt(aImageFile.getAbsolutePath())))) {
            final int height;
            final int width;

            try {
                final Jp2_source inputSource = new Jp2_source();
                final Jp2_family_src jp2_family_in = new Jp2_family_src();
                final Jp2_locator loc = new Jp2_locator();

                jp2_family_in.Open(aImageFile.getAbsolutePath(), true);
                inputSource.Open(jp2_family_in, loc);
                inputSource.Read_header();

                final Kdu_compressed_source kduIn = inputSource;
                final Kdu_codestream codestream = new Kdu_codestream();

                codestream.Create(kduIn);

                final Kdu_channel_mapping channels = new Kdu_channel_mapping();

                if (inputSource.Exists()) {
                    channels.Configure(inputSource, false);
                } else {
                    channels.Configure(codestream);
                }

                final int ref_component = channels.Get_source_component(0);
                final Kdu_dims image_dims = new Kdu_dims();

                codestream.Get_dims(ref_component, image_dims);

                final Kdu_coords imageSize = image_dims.Access_size();

                width = imageSize.Get_x();
                height = imageSize.Get_y();

                channels.Native_destroy();

                if (codestream.Exists()) {
                    codestream.Destroy();
                }

                kduIn.Native_destroy();
                inputSource.Native_destroy();
                jp2_family_in.Native_destroy();
            } catch (final KduException details) {
                System.err.println(details);
                throw new IOException(details);
            }

            // // Using OpenCV
            // final Buffer buffer = Buffer.buffer(IOUtils.readBytes(new FileInputStream(aImageFile)));
            // final NativeImageObject image = new NativeImageObject(buffer);
            //
            // height = image.getHeight();
            // width = image.getWidth();
            //
            // image.flush();

            return new Dimension(width, height);
        } else {
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

            throw new RuntimeException(LOGGER.getMessage(EXC_026, mimeType));
        }
    }

    /**
     * Gets the center of an image.
     *
     * @param aImageFile A source image file
     * @return An image region representing the center of the image
     * @throws IOException If there is trouble reading the source image file
     */
    public static ImageRegion getCenter(final File aImageFile) throws IOException {
        return getCenter(getImageDimension(aImageFile));
    }

    /**
     * Gets the scale factors for an image.
     *
     * @param aWidth An image width
     * @param aHeight An image height
     * @param aTileSize A tile size
     * @return The scale factors for an image with the supplied characteristics
     */
    public static JsonArray getScaleFactors(final int aWidth, final int aHeight, final int aTileSize) {
        final int longDimension = Math.max(aWidth, aHeight);
        final JsonArray scaleFactors = new JsonArray();

        for (int multiplier = 1; multiplier * aTileSize < longDimension; multiplier *= 2) {
            scaleFactors.add(multiplier);
        }

        return scaleFactors;
    }

    /**
     * Gets the center of an image, represented by a {@see java.awt.Dimension} object.
     *
     * @param aDimension Dimensions to use as the source for the calculation of center
     * @return An image region representing the center of the dimensions
     */
    public static ImageRegion getCenter(final Dimension aDimension) {
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

    /**
     * Gets an <code>ImageObject</code> for the supplied image {@link io.vertx.core.buffer.Buffer}.
     *
     * @param aImageBuffer An <code>ImageBuffer</code> to convert into an <code>ImageObject</code>
     * @return An <code>ImageBuffer</code> for the supplied <code>Buffer</code>
     * @throws IOException If there is trouble reading the supplied <code>Buffer</code>
     */
    public static ImageObject getImage(final Buffer aImageBuffer) throws IOException {
        final ImageObject image;

        if (useNativeLibs) {
            image = new NativeImageObject(aImageBuffer);
        } else {
            image = new JavaImageObject(aImageBuffer);
        }

        return image;
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
