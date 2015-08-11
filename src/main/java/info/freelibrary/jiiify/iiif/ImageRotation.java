
package info.freelibrary.jiiify.iiif;

import info.freelibrary.jiiify.MessageCodes;

public class ImageRotation {

    public boolean isMirrored;

    public float myRotation;

    public ImageRotation() {
        myRotation = 0;
    }

    public ImageRotation(final String aRotationString) throws InvalidRotationException {
        if (aRotationString.startsWith("!")) {
            isMirrored = true;
            parseRotation(aRotationString.substring(1));
        } else {
            parseRotation(aRotationString);
        }
    }

    private void parseRotation(final String aRotationString) throws InvalidRotationException {
        try {
            myRotation = Float.parseFloat(aRotationString);

            if (myRotation < 0 || myRotation > 360) {
                throw new InvalidRotationException(MessageCodes.EXC_013, myRotation);
            }
        } catch (final NumberFormatException details) {
            throw new InvalidRotationException(MessageCodes.EXC_013, aRotationString);
        }
    }

    /**
     * Returns the rotation value.
     *
     * @return The rotation value
     */
    public float getValue() {
        return myRotation;
    }

    /**
     * Returns the rotation value as a string.
     *
     * @return The rotation value as a string
     */
    public String getValueAsString() {
        return Float.toString(myRotation);
    }

    @Override
    public String toString() {
        final String value = isMirrored() ? "!" + getValueAsString() : getValueAsString();
        return value.endsWith(".0") ? value.substring(0, value.length() - 2) : value;
    }

    /**
     * Returns whether the image is mirrored or not.
     *
     * @return Whether the image is mirrored
     */
    public boolean isMirrored() {
        return isMirrored;
    }

    /**
     * Returns whether the image is rotated or not.
     *
     * @return Whether the image is rotated
     */
    public boolean isRotated() {
        return myRotation != 0;
    }
}
