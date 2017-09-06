
package info.freelibrary.jiiify.image;

/**
 * Some defaults used in image processing.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface PhotometricInterpretation {

    String XPATH = "//TIFFField[@name='PhotometricInterpretation']";

    /* These values are the defaults */

    int WHITE_IS_ZERO = 0;

    int BLACK_IS_ZERO = 1;

    int RGB = 2;

    int PALETTE_COLOR = 3;

    int TRANSPARENCY_MASK = 4;

    /* These values are extensions */

    int SEPARATED = 5; // usually CMYK

    int YCBCR = 6;

    int CIE_LAB = 8;

    int ICC_LAB = 9;

    /* These values are from the TIFF-F specification (RFC 2301) */

    int ITU_LAB = 10;

    /* These values are from the DNG specification */

    int LOGL = 32844;

    int LOGLUV = 32845;

}
