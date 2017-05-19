
package info.freelibrary.jiiify.iiif;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.StringUtils;

/**
 * An image quality for a IIIF image request.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
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
     * @param aQuality The value of the image quality
     * @throws UnsupportedQualityException If the supplied quality isn't valid
     */
    public ImageQuality(final String aQuality) throws UnsupportedQualityException {
        if (aQuality.equals(DEFAULT) || aQuality.equals(COLOR) || aQuality.equals(GRAY) || aQuality.equals(BITONAL)) {
            myQuality = aQuality;
        } else {
            final String availQualities = StringUtils.toString(' ', DEFAULT, COLOR, GRAY, BITONAL);
            throw new UnsupportedQualityException(MessageCodes.EXC_012, aQuality, availQualities);
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
