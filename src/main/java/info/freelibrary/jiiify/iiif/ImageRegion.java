
package info.freelibrary.jiiify.iiif;

import info.freelibrary.jiiify.MessageCodes;

public class ImageRegion {

    public static final String FULL = "full";

    // X-WIDTH (horizontal) Y-HEIGHT (vertical)
    public static enum Region {
        X, Y, WIDTH, HEIGHT
    }

    private final float[] myDimensions;

    private boolean usesPercentages;

    private boolean isFullImage;

    public ImageRegion() {
        isFullImage = true;
        myDimensions = new float[] { 100f, 100f, 100f, 100f };
        usesPercentages = true;
    }

    public ImageRegion(final int aX, final int aY, final int aWidth, final int aHeight) {
        myDimensions = new float[] { aX, aY, aWidth, aHeight };
    }

    public ImageRegion(final String aRegionString) throws InvalidRegionException {
        if (aRegionString == null) {
            throw new InvalidRegionException(new NullPointerException());
        } else if (aRegionString.equals(FULL)) {
            isFullImage = true;
            usesPercentages = true;
            myDimensions = new float[] { 100f, 100f, 100f, 100f };
        } else if (aRegionString.startsWith("pct:")) {
            isFullImage = true;
            usesPercentages = true;
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

    public int getInt(final Region aRegionCoordinate) {
        return (int) getFloat(aRegionCoordinate);
    }

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
                sb.append("pct:");
            }

            sb.append(prettyPrint(myDimensions[0])).append(',');
            sb.append(prettyPrint(myDimensions[1])).append(',');
            sb.append(prettyPrint(myDimensions[2])).append(',');
            sb.append(prettyPrint(myDimensions[3]));
        }

        return sb.toString();
    }

    public boolean usesPercentages() {
        return usesPercentages;
    }

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
