
package info.freelibrary.jiiify.iiif;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * IIIF image size.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageSize {

    private final Logger LOGGER = LoggerFactory.getLogger(ImageSize.class, Constants.MESSAGES);

    public static final String FULL = "full";

    private boolean isPercentage;

    private boolean canBeScaled;

    private int myPercentage;

    private int myHeight;

    private int myWidth;

    /**
     * Creates a new image size object.
     */
    public ImageSize() {
        isPercentage = true;
        myPercentage = 100;
    }

    /**
     * Creates a new image size object from the supplied width and height.
     *
     * @param aWidthHeight An integer value that is both height and width
     */
    public ImageSize(final int aWidthHeight) {
        myWidth = aWidthHeight;
        myHeight = aWidthHeight;
    }

    /**
     * Creates a new image size object from the supplied width and height.
     *
     * @param aWidth An image width
     * @param aHeight An image height
     */
    public ImageSize(final int aWidth, final int aHeight) {
        myWidth = aWidth;
        myHeight = aHeight;
    }

    /**
     * Creates a new image size object from the supplied IIIF URI width and height string.
     *
     * @param aSizeString A IIIF URI string with width and height
     * @throws InvalidSizeException If the supplied string isn't a valid height and width representation
     */
    public ImageSize(final String aSizeString) throws InvalidSizeException {
        if (aSizeString == null) {
            throw new InvalidSizeException(new NullPointerException(), MessageCodes.EXC_014, "null");
        } else if (aSizeString.equals(FULL)) {
            isPercentage = true;
            myPercentage = 100;
        } else if (aSizeString.startsWith("pct:")) {
            try {
                isPercentage = true;
                myPercentage = Integer.parseInt(aSizeString.substring(4));

                if (myPercentage < 1 || myPercentage > 100) {
                    throw new InvalidSizeException(MessageCodes.EXC_014, aSizeString);
                }
            } catch (final NumberFormatException details) {
                throw new InvalidSizeException(MessageCodes.EXC_014, aSizeString);
            }
        } else if (aSizeString.contains(",")) {
            final String[] parts = aSizeString.split(",");

            if (parts.length == 0) {
                throw new InvalidSizeException(MessageCodes.EXC_015);
            } else if (aSizeString.length() - aSizeString.replace(",", "").length() > 1) {
                throw new InvalidSizeException(MessageCodes.EXC_016, aSizeString);
            }

            if (aSizeString.startsWith(",")) {
                try {
                    myHeight = Integer.parseInt(parts[1]);
                } catch (final NumberFormatException details) {
                    throw new InvalidSizeException(MessageCodes.EXC_017, "height", parts[1]);
                }
            } else if (aSizeString.startsWith("!")) {
                canBeScaled = true;

                try {
                    // Chop off the exclamation point at the start of the string
                    myWidth = Integer.parseInt(parts[0].substring(1));
                } catch (final NumberFormatException details) {
                    throw new InvalidSizeException(MessageCodes.EXC_017, "width", parts[0]);
                }

                try {
                    myHeight = Integer.parseInt(parts[1]);
                } catch (final NumberFormatException details) {
                    throw new InvalidSizeException(MessageCodes.EXC_017, "height", parts[0]);
                }
            } else if (aSizeString.endsWith(",")) {
                try {
                    myWidth = Integer.parseInt(parts[0]);
                } catch (final NumberFormatException details) {
                    throw new InvalidSizeException(MessageCodes.EXC_017, "width", parts[0]);
                }
            } else {
                try {
                    myWidth = Integer.parseInt(parts[0]);
                } catch (final NumberFormatException details) {
                    throw new InvalidSizeException(MessageCodes.EXC_017, "width", parts[0]);
                }

                try {
                    myHeight = Integer.parseInt(parts[1]);
                } catch (final NumberFormatException details) {
                    throw new InvalidSizeException(MessageCodes.EXC_017, "height", parts[0]);
                }
            }
        } else {
            throw new InvalidSizeException(MessageCodes.EXC_014, aSizeString);
        }
    }

    /**
     * Returns true if the image size request is for a percentage of the actual image size.
     *
     * @return True if the image size request is for a percentage of the actual image size
     */
    public boolean isPercentage() {
        return isPercentage;
    }

    /**
     * Returns the percent of the image size request as an integer.
     *
     * @return The percent of the image size request as an integer
     */
    public int getPercentage() {
        return myPercentage;
    }

    /**
     * Returns true if this image size request is for the full size of the image.
     *
     * @return True if this image size request is for the full size of the image
     */
    public boolean isFullSize() {
        return isPercentage && myPercentage == 100;
    }

    /**
     * Returns true if the width is set.
     *
     * @return True if the width is set
     */
    public boolean hasWidth() {
        return myWidth != 0;
    }

    /**
     * Returns true if the height is set.
     *
     * @return True if the height is set
     */
    public boolean hasHeight() {
        return myHeight != 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        if (isFullSize()) {
            sb.append(FULL);
        } else if (isPercentage()) {
            sb.append("pct:").append(getPercentage());
        } else if (canBeScaled()) {
            sb.append('!').append(getWidth()).append(',').append(getHeight());
        } else if (!hasHeight()) {
            sb.append(getWidth()).append(',');
        } else if (!hasWidth()) {
            sb.append(',').append(getHeight());
        } else {
            sb.append(getWidth()).append(',').append(getHeight());
        }

        return sb.toString();
    }

    /**
     * Returns true if a scaled response is acceptable; else, false.
     *
     * @return True if a scaled response is acceptable; else, false
     */
    public boolean canBeScaled() {
        return canBeScaled;
    }

    /**
     * Returns the height of the image size request.
     *
     * @return The height of the image size request
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Returns the width of the image size request.
     *
     * @return The width of the image size request
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Returns height of the image request taking into consideration the supplied actual height of the image. If the
     * supplied height is less than the requested height, the supplied height is returned. If the image size request is
     * for a percentage of the original image, the percentage of the supplied number is returned.
     *
     * @param aImageHeight The image's actual height in pixels
     * @param aImageWidth The image's actual width in pixels
     * @return The computed height of the image request
     */
    public int getHeight(final int aImageHeight, final int aImageWidth) {
        final int height;

        if (myHeight == 0) {
            if (isPercentage) {
                LOGGER.debug(MessageCodes.DBG_073, myPercentage);
                height = myPercentage / 100 * aImageHeight;
            } else {
                height = Math.round(scale(myWidth, aImageWidth) * aImageHeight);
                LOGGER.debug(MessageCodes.DBG_074, height);
            }
        } else {
            height = aImageHeight < myHeight ? aImageHeight : myHeight;

            if (height == myHeight) {
                LOGGER.debug(MessageCodes.DBG_075, myHeight);
            } else {
                LOGGER.debug(MessageCodes.DBG_076, aImageHeight);
            }
        }

        LOGGER.debug(MessageCodes.DBG_077, height, aImageHeight);

        return height;
    }

    /**
     * Returns width of the image request taking into consideration the supplied actual width of the image. If the
     * supplied width is less than the requested width, the supplied width is returned. If the image size request is for
     * a percentage of the original image, the percentage of the supplied number is returned.
     *
     * @param aImageWidth The image's actual width in pixels
     * @param aImageHeight The image's actual height in pixels
     * @return The computed width of the image request
     */
    public int getWidth(final int aImageWidth, final int aImageHeight) {
        final int width;

        if (myWidth == 0) {
            if (isPercentage) {
                LOGGER.debug(MessageCodes.DBG_078, myPercentage);
                width = myPercentage / 100 * aImageWidth;
            } else {
                width = Math.round(scale(myHeight, aImageHeight) * aImageWidth);
                LOGGER.debug(MessageCodes.DBG_079, width);
            }
        } else {
            width = aImageWidth < myWidth ? aImageWidth : myWidth;

            if (width == myWidth) {
                LOGGER.debug(MessageCodes.DBG_080, myWidth);
            } else {
                LOGGER.debug(MessageCodes.DBG_081, aImageWidth);
            }
        }

        LOGGER.debug(MessageCodes.DBG_082, width, aImageWidth);

        return width;
    }

    private float scale(final int aSizeValue, final int aImageValue) {
        final float scale;

        if (aSizeValue >= aImageValue) {
            scale = 1f;
        } else {
            scale = (float) aSizeValue / (float) aImageValue;
        }

        LOGGER.debug(MessageCodes.DBG_083, scale);

        return scale;
    }
}
