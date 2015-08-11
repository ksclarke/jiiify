package info.freelibrary.jiiify.iiif;

import java.util.Locale;


public class InvalidInfoException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> for InvalidInfoException.
     */
    private static final long serialVersionUID = 3424578672802638488L;

    public InvalidInfoException() {
    }

    public InvalidInfoException(final String aMessageKey) {
        super(aMessageKey);
    }

    public InvalidInfoException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    public InvalidInfoException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    public InvalidInfoException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    public InvalidInfoException(final Throwable aCause) {
        super(aCause);
    }

    public InvalidInfoException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    public InvalidInfoException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    public InvalidInfoException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    public InvalidInfoException(final Throwable aCause, final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
