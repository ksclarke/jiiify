
package info.freelibrary.jiiify.image;

public interface PhotometricInterpretation {

    public static final String XPATH = "//TIFFField[@name='PhotometricInterpretation']";

    /* These values are the defaults */

    public static final int WHITE_IS_ZERO = 0;

    public static final int BLACK_IS_ZERO = 1;

    public static final int RGB = 2;

    public static final int PALETTE_COLOR = 3;

    public static final int TRANSPARENCY_MASK = 4;

    /* These values are extensions */

    public static final int SEPARATED = 5; // usually CMYK

    public static final int YCBCR = 6;

    public static final int CIE_LAB = 8;

    public static final int ICC_LAB = 9;

    /* These values are from the TIFF-F specification (RFC 2301) */

    public static final int ITU_LAB = 10;

    /* These values are from the DNG specification */

    public static final int LOGL = 32844;

    public static final int LOGLUV = 32845;

}
