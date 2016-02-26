
package info.freelibrary.jiiify.iiif;

import info.freelibrary.util.StringUtils;

/**
 * An image quality for a IIIF image request.
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class ImageQuality {

    public static final String DEFAULT = "default";

    public static final String COLOR = "color";

    public static final String GRAY = "gray";

    public static final String BITONAL = "bitonal";

    private String myQuality;

    /**
     * Creates an image quality object.
     */
    public ImageQuality() {
        myQuality = DEFAULT;
    }

    /**
     * Constructs a new <code>ImageQuality</code> from the supplied string.
     *
     * @param aQualityString The value of the image quality
     * @throws UnsupportedQualityException If the supplied quality isn't valid
     */
    public ImageQuality(final String aQualityString) throws UnsupportedQualityException {
        if (aQualityString.equals(DEFAULT) || aQualityString.equals(COLOR) || aQualityString.equals(GRAY) ||
                aQualityString.equals(BITONAL)) {
            myQuality = aQualityString;
        } else {
            throw new UnsupportedQualityException("EXC-012", aQualityString, StringUtils.toString(' ', DEFAULT,
                    COLOR, GRAY, BITONAL));
        }
    }

    /**
     * Returns the <code>ImageQuality</code> value.
     *
     * @return The image quality value
     */
    public String getValue() {
        return myQuality;
    }

    @Override
    public String toString() {
        return myQuality;
    }

}
