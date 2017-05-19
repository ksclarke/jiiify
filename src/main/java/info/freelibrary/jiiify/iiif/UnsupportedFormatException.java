
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

/**
 * An exception thrown when there is an invalid IIIF format.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class UnsupportedFormatException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the <code>UnsupportedFormatException</code>.
     */
    private static final long serialVersionUID = -6342025888322965791L;

    /**
     * Constructs a new <code>UnsupportedFormatException</code>.
     */
    public UnsupportedFormatException() {
        super();
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedFormatException(final String aMessageKey) {
        super(aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedFormatException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedFormatException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedFormatException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aCause The root cause of the exception
     */
    public UnsupportedFormatException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedFormatException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedFormatException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedFormatException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>UnsupportedFormatException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedFormatException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
