
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

/**
 * An exception thrown when a supplied value is not a valid IIIF rotation.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class InvalidRotationException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the <code>InvalidRoationException</code>.
     */
    private static final long serialVersionUID = -6342025888322965791L;

    /**
     * Constructs a new <code>InvalidRoationException</code>.
     */
    public InvalidRotationException() {
        super();
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aMessageKey A message key to look up the exception message
     */
    public InvalidRotationException(final String aMessageKey) {
        super(aMessageKey);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public InvalidRotationException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public InvalidRotationException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public InvalidRotationException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aCause The root cause of the exception
     */
    public InvalidRotationException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     */
    public InvalidRotationException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public InvalidRotationException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public InvalidRotationException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    /**
     * Constructs a InvalidRoationException.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public InvalidRotationException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
