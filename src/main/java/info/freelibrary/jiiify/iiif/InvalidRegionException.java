
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

public class InvalidRegionException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the InvalidSizeException class.
     */
    private static final long serialVersionUID = -5965556268952910238L;

    public InvalidRegionException() {
        super();
    }

    public InvalidRegionException(final String aMessageKey) {
        super(aMessageKey);
    }

    public InvalidRegionException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    public InvalidRegionException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    public InvalidRegionException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    public InvalidRegionException(final Throwable aCause) {
        super(aCause);
    }

    public InvalidRegionException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    public InvalidRegionException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    public InvalidRegionException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    public InvalidRegionException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
