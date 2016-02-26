
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

public class InvalidSizeException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the InvalidSizeException class.
     */
    private static final long serialVersionUID = -5965556268952910238L;

    /**
     * Creates an invalid size exception.
     */
    public InvalidSizeException() {
        super();
    }

    /**
     * Creates an invalid size exception using the message for the supplied message key.
     */
    public InvalidSizeException(final String aMessageKey) {
        super(aMessageKey);
    }

    /**
     * Creates an invalid size exception using the message and additional details for the supplied message key.
     */
    public InvalidSizeException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    /**
     * Creates an invalid size exception using the localized message for the supplied message key.
     */
    public InvalidSizeException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    /**
     * Creates an invalid size exception using the localized message and additional details for the supplied message
     * key.
     */
    public InvalidSizeException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    /**
     * Creates an invalid size exception from the supplied underlying cause.
     * 
     * @param aCause An underlying cause of the exception
     */
    public InvalidSizeException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Creates an invalid size exception from the supplied underlying exception with the message for the supplied
     * message key.
     * 
     * @param aCause An underlying cause of the exception
     * @param aMessageKey A message key for the desired message
     */
    public InvalidSizeException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    /**
     * Creates an invalid size exception from the supplied underlying exception with the localized message for the
     * supplied message key.
     * 
     * @param aCause An underlying cause of the exception
     * @param aLocale The locale for the exception's message
     * @param aMessageKey A message key for the desired message
     */
    public InvalidSizeException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    /**
     * Creates an invalid size exception from the supplied underlying exception with the message and additional
     * details for the supplied message key.
     * 
     * @param aCause An underlying cause of the exception
     * @param aMessageKey A message key for the desired message
     * @param aVarargs Additional details to include in the exception message
     */
    public InvalidSizeException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    /**
     * Creates an invalid size exception from the supplied underlying exception with the message and additional
     * details for the supplied message key.
     * 
     * @param aCause An underlying cause of the exception
     * @param aLocale A locale to use with the supplied message key
     * @param aMessageKey A message key for the desired message
     * @param aVarargs Additional details to include in the exception message
     */
    public InvalidSizeException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
