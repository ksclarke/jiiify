
package info.freelibrary.jiiify.iiif;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import info.freelibrary.util.StringUtils;

public class ImageFormat {

    public static final String DEFAULT_FORMAT = "jpg";

    /* Image extensions */

    public static final String JPG_EXT = "jpg";

    public static final String TIF_EXT = "tif";

    public static final String TIFF_EXT = "tiff";

    public static final String PNG_EXT = "png";

    public static final String GIF_EXT = "gif";

    public static final String JP2_EXT = "jp2";

    public static final String PDF_EXT = "pdf";

    public static final String WEBP_EXT = "webp";

    /* Image MIME types */

    public static final String JPG_MIME_TYPE = "image/jpeg";

    public static final String TIF_MIME_TYPE = "image/tiff";

    public static final String PNG_MIME_TYPE = "image/png";

    public static final String GIF_MIME_TYPE = "image/gif";

    public static final String JP2_MIME_TYPE = "image/jp2";

    public static final String PDF_MIME_TYPE = "application/pdf";

    public static final String WEBP_MIME_TYPE = "image/webp";

    /* Extension to MIME type maps */

    private static final Map<String, String> EXT2MIME_MAP;

    private static final Map<String, String> MIME2EXT_MAP;

    /* Initialization of the extension to MIME type maps */
    static {
        final HashMap<String, String> formatExtMap = new HashMap<String, String>(7);
        final HashMap<String, String> formatMTMap = new HashMap<String, String>(7);

        formatExtMap.put(JPG_EXT, JPG_MIME_TYPE);
        formatExtMap.put(TIF_EXT, TIF_MIME_TYPE);
        formatExtMap.put(TIFF_EXT, TIF_MIME_TYPE);
        formatExtMap.put(PNG_EXT, PNG_MIME_TYPE);
        formatExtMap.put(GIF_EXT, GIF_MIME_TYPE);
        formatExtMap.put(JP2_EXT, JP2_MIME_TYPE);
        formatExtMap.put(PDF_EXT, PDF_MIME_TYPE);
        formatExtMap.put(WEBP_EXT, WEBP_MIME_TYPE);

        formatMTMap.put(JPG_MIME_TYPE, JPG_EXT);
        formatMTMap.put(TIF_MIME_TYPE, TIF_EXT);
        formatMTMap.put(PNG_MIME_TYPE, PNG_EXT);
        formatMTMap.put(GIF_MIME_TYPE, GIF_EXT);
        formatMTMap.put(JP2_MIME_TYPE, JP2_EXT);
        formatMTMap.put(PDF_MIME_TYPE, PDF_EXT);
        formatMTMap.put(WEBP_MIME_TYPE, WEBP_EXT);

        EXT2MIME_MAP = Collections.unmodifiableMap(formatExtMap);
        MIME2EXT_MAP = Collections.unmodifiableMap(formatMTMap);
    }

    private String myFormat;

    /**
     * Creates a new image format object.
     */
    public ImageFormat() {
        myFormat = MIME2EXT_MAP.get(EXT2MIME_MAP.get(DEFAULT_FORMAT));
    }

    /**
     * Creates a new <code>ImageFormat</code> from the supplied query string.
     *
     * @param aFormatString The format part of a IIIF query
     */
    public ImageFormat(final String aFormatString) throws UnsupportedFormatException {
        if (EXT2MIME_MAP.containsKey(aFormatString)) {
            myFormat = MIME2EXT_MAP.get(EXT2MIME_MAP.get(aFormatString));
        } else {
            throw new UnsupportedFormatException("EXC-011", aFormatString, StringUtils.joinKeys(EXT2MIME_MAP, ' '));
        }
    }

    /**
     * Determines if the supplied file extension matches one of the supported image formats.
     * 
     * @param aFileExtension A file extension from an image file
     * @return True if the image type represented by the extension is supported; else, false
     */
    public static boolean isSupportedFormat(final String aFileExtension) {
        return EXT2MIME_MAP.containsKey(aFileExtension.toLowerCase());
    }

    /**
     * Gets the supported image extensions.
     * 
     * @return The extensions of the supported image types
     */
    public static String[] getExtensions() {
        return EXT2MIME_MAP.keySet().toArray(new String[EXT2MIME_MAP.size()]);
    }

    /**
     * Gets the supported image MIME types.
     * 
     * @return The MIME types of the supported image types
     */
    public static String[] getMimeTypes() {
        return MIME2EXT_MAP.keySet().toArray(new String[MIME2EXT_MAP.size()]);
    }

    @Override
    public String toString() {
        return myFormat;
    }

    /**
     * Gets the file extension associated with this particular image format.
     * 
     * @return The file extension associated with this particular image format
     */
    public String getExtension() {
        return myFormat;
    }

    /**
     * Gets the MIME type associated with this particular image format.
     * 
     * @return The MIME type associated with this particular image format
     */
    public String getMIMEType() {
        return EXT2MIME_MAP.get(myFormat);
    }

    /**
     * Gets the preferred file extension for the supplied MIME type.
     * 
     * @param aMIMEType A MIME type for which to return a file extension
     * @return A file extension for the supplied MIME type
     */
    public final static String getExtension(final String aMIMEType) {
        return MIME2EXT_MAP.get(aMIMEType);
    }

    /**
     * Gets the MIME type for the supplied file extension.
     * 
     * @param aFileExt A file extension for which to return a MIME type
     * @return A MIME type for the supplied file extension
     */
    public final static String getMIMEType(final String aFileExt) {
        return EXT2MIME_MAP.get(aFileExt);
    }

    /**
     * Returns whether the supplied file extension is valid for the image format.
     * 
     * @param aFileExt A file extension to compare to the current image format
     * @return True if the supplied file extension is valid for the image format in hand; else, false
     */
    public boolean matches(final String aFileExt) {
        return EXT2MIME_MAP.containsKey(aFileExt) ? myFormat.equals(getExtension(getMIMEType(aFileExt))) : false;
    }

}
