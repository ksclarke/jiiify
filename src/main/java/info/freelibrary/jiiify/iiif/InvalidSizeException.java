
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

public class InvalidSizeException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the InvalidSizeException class.
     */
    private static final long serialVersionUID = -5965556268952910238L;

    public InvalidSizeException() {
        super();
    }

    public InvalidSizeException(final String aMessageKey) {
        super(aMessageKey);
    }

    public InvalidSizeException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    public InvalidSizeException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    public InvalidSizeException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    public InvalidSizeException(final Throwable aCause) {
        super(aCause);
    }

    public InvalidSizeException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    public InvalidSizeException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    public InvalidSizeException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    public InvalidSizeException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
