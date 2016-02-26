
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

public class InvalidRegionException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the InvalidSizeException class.
     */
    private static final long serialVersionUID = -5965556268952910238L;

    /**
     * Creates an exception related to a IIIF region.
     */
    public InvalidRegionException() {
        super();
    }

    /**
     * Creates an exception and message related to a IIIF region.
     * 
     * @param aMessageKey A key that corresponds to the exception's message
     */
    public InvalidRegionException(final String aMessageKey) {
        super(aMessageKey);
    }

    /**
     * Creates an exception and message with additional details related to a IIIF region.
     * 
     * @param aMessageKey A key that corresponds to the exception's message
     * @param aVarargs Additional details to be added to the exception's message
     */
    public InvalidRegionException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    /**
     * Creates an exception and message related to a IIIF region.
     * 
     * @param aLocale A locale for the exception's message
     * @param aMessageKey A key that corresponds to the exception's message
     */
    public InvalidRegionException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    /**
     * Creates an exception and localized message with additional details related to a IIIF region.
     * 
     * @param aLocale A locale for the exception's message
     * @param aMessageKey A key that corresponds to the exception's message
     * @param aVarargs Additional details to be added to the exception's message
     */
    public InvalidRegionException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    /**
     * Creates a region related exception from the supplied underlying cause.
     * 
     * @param aCause An underlying cause of the <code>InvalidRegionException</code>
     */
    public InvalidRegionException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Creates a region related exception from the supplied underlying cause with the message for the supplied message
     * key.
     * 
     * @param aCause An underlying cause of the <code>InvalidRegionException</code>
     * @param aMessageKey A key that corresponds to the exception's message
     */
    public InvalidRegionException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    /**
     * Creates a region related exception from the supplied underlying cause with the localized message for the
     * supplied message key.
     * 
     * @param aCause An underlying cause of the <code>InvalidRegionException</code>
     * @param aLocale A locale for the exception's message
     * @param aMessageKey A key that corresponds to the exception's message
     */
    public InvalidRegionException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    /**
     * Creates a region related exception from the supplied underlying cause with the message (and additional details)
     * for the supplied message key.
     * 
     * @param aCause An underlying cause of the <code>InvalidRegionException</code>
     * @param aMessageKey A key that corresponds to the exception's message
     * @param aVarargs Additional details to be added to the exception's message
     */
    public InvalidRegionException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    /**
     * Creates a region related exception from the supplied underlying cause with the localized message (and
     * additional details) for the supplied message key.
     * 
     * @param aCause An underlying cause of the <code>InvalidRegionException</code>
     * @param aLocale A locale for the exception's message
     * @param aMessageKey A key that corresponds to the exception's message
     * @param aVarargs Additional details to be added to the exception's message
     */
    public InvalidRegionException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
