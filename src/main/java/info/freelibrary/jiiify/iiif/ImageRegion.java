
package info.freelibrary.jiiify.iiif;

import info.freelibrary.jiiify.MessageCodes;

/**
 * IIIF image region.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageRegion {

    public static final String FULL = "full";

    public static final String PERCENT = "pct:";

    // X-WIDTH (horizontal) Y-HEIGHT (vertical)
    public enum Region {
        X, Y, WIDTH, HEIGHT
    }

    private final float[] myDimensions;

    private boolean isPercentage;

    private boolean isFullImage;

    /**
     * Creates a new image region object representing the whole image.
     */
    public ImageRegion() {
        isFullImage = true;
        isPercentage = true;
        myDimensions = new float[] { 100f, 100f, 100f, 100f };
    }

    /**
     * Creates a new image region object from the supplied X, Y, width and height.
     *
     * @param aX A X dimension for an image region
     * @param aY A Y dimension for an image region
     * @param aWidth A width for an image region
     * @param aHeight A height for an image region
     */
    public ImageRegion(final int aX, final int aY, final int aWidth, final int aHeight) {
        myDimensions = new float[] { aX, aY, aWidth, aHeight };
    }

    /**
     * Creates a new image region object from the supplied IIIF URI image region string.
     *
     * @param aRegionString A region string from a IIIF URI
     * @throws InvalidRegionException If the supplied string isn't a valid representation of a IIIF region
     */
    public ImageRegion(final String aRegionString) throws InvalidRegionException {
        if (aRegionString == null) {
            throw new InvalidRegionException(new NullPointerException());
        } else if (aRegionString.equals(FULL)) {
            isFullImage = true;
            isPercentage = true;
            myDimensions = new float[] { 100f, 100f, 100f, 100f };
        } else if (aRegionString.startsWith(PERCENT)) {
            isFullImage = true;
            isPercentage = true;
            myDimensions = getDimensions(aRegionString.substring(4));

            // Mark it a full size image even if it's using percentages
            for (final float dim : myDimensions) {
                if (dim != 100f) {
                    isFullImage = false;
                }
            }
        } else {
            myDimensions = getDimensions(aRegionString);
        }
    }

    /**
     * Gets the integer value for the supplied region coordinate.
     *
     * @param aRegionCoordinate A region coordinate
     * @return An integer value for the supplied region coordinate
     */
    public int getInt(final Region aRegionCoordinate) {
        return (int) getFloat(aRegionCoordinate);
    }

    /**
     * Gets the float value for the supplied region coordinate.
     *
     * @param aRegionCoordinate A region coordinate
     * @return A float value for the supplied region coordinate
     */
    public float getFloat(final Region aRegionCoordinate) {
        switch (aRegionCoordinate) {
            case X:
                return myDimensions[0];
            case Y:
                return myDimensions[1];
            case WIDTH:
                return myDimensions[2];
            default:
                return myDimensions[3];
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        if (isFullImage()) {
            sb.append(FULL);
        } else {
            if (usesPercentages()) {
                sb.append(PERCENT);
            }

            sb.append(prettyPrint(myDimensions[0])).append(',');
            sb.append(prettyPrint(myDimensions[1])).append(',');
            sb.append(prettyPrint(myDimensions[2])).append(',');
            sb.append(prettyPrint(myDimensions[3]));
        }

        return sb.toString();
    }

    /**
     * Returns whether this image region is represented with percentages.
     *
     * @return True if this image region is represented with percentages
     */
    public boolean usesPercentages() {
        return isPercentage;
    }

    /**
     * Returns whether this image region represents the full image.
     *
     * @return True if this image region represents the full image
     */
    public boolean isFullImage() {
        return isFullImage;
    }

    private String prettyPrint(final float aValue) {
        final String floatValue = Float.toString(aValue);
        return floatValue.endsWith(".0") ? floatValue.substring(0, floatValue.length() - 2) : floatValue;
    }

    private float[] getDimensions(final String aRegionString) throws InvalidRegionException {
        final String[] parts = aRegionString.split(",");
        final float[] dimensions = new float[4];

        if (parts.length != 4) {
            throw new InvalidRegionException(MessageCodes.EXC_020, aRegionString);
        }

        for (int index = 0; index < parts.length; index++) {
            try {
                dimensions[index] = Float.parseFloat(parts[index]);
            } catch (final NumberFormatException details) {
                throw new InvalidRegionException(MessageCodes.EXC_018, parts[index]);
            }
        }

        return dimensions;
    }
}
