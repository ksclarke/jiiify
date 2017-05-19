
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

/**
 * An exception thrown when there is an invalid info response.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class InvalidInfoException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> for InvalidInfoException.
     */
    private static final long serialVersionUID = 3424578672802638488L;

    /**
     * Creates an exception related to an invalid IIIF info response.
     */
    public InvalidInfoException() {
    }

    /**
     * Creates an exception with message related to an invalid IIIF info response.
     *
     * @param aMessageKey A message key for the message
     */
    public InvalidInfoException(final String aMessageKey) {
        super(aMessageKey);
    }

    /**
     * Creates an exception with message and additional details related to an invalid IIIF info response.
     *
     * @param aMessageKey A message key for the message
     * @param aVarargs Additional details for the message
     */
    public InvalidInfoException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    /**
     * Creates an exception with localized message related to an invalid IIIF info response.
     *
     * @param aLocale A locale for the exception message
     * @param aMessageKey A message key for the message
     */
    public InvalidInfoException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    /**
     * Creates an exception with localized message and additional details related to an invalid IIIF info response.
     *
     * @param aLocale A locale for the exception message
     * @param aMessageKey A message key for the message
     * @param aVarargs Additional details for the message
     */
    public InvalidInfoException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    /**
     * Creates an exception from a root exception.
     *
     * @param aCause The underlying cause of this exception
     */
    public InvalidInfoException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Creates an exception with a message from a root exception.
     *
     * @param aCause The underlying cause of this exception
     * @param aMessageKey A message key for the message
     */
    public InvalidInfoException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    /**
     * Creates an exception with a localized message from a root exception.
     *
     * @param aCause The underlying cause of this exception
     * @param aLocale The locale to use for the message
     * @param aMessageKey A message key for the message
     */
    public InvalidInfoException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    /**
     * Creates an exception with a message and additional details from a root exception.
     *
     * @param aCause The underlying cause of this exception
     * @param aMessageKey A message key for the message
     * @param aVarargs Additional details about the exception
     */
    public InvalidInfoException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    /**
     * Creates an exception with a localized message and additional details from a root exception.
     *
     * @param aCause The underlying cause of this exception
     * @param aLocale The locale to use for the message
     * @param aMessageKey A message key for the message
     * @param aVarargs Additional details about the exception
     */
    public InvalidInfoException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
