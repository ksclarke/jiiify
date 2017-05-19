
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

/**
 * An exception thrown when there is an unsupported IIIF quality.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class UnsupportedQualityException extends IIIFException {

    /**
     * The <code>serialVersionUID</code> of the <code>UnsupportedQualityException</code>.
     */
    private static final long serialVersionUID = -6342025888322965791L;

    /**
     * Constructs a new <code>UnsupportedQualityException</code>.
     */
    public UnsupportedQualityException() {
        super();
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedQualityException(final String aMessageKey) {
        super(aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedQualityException(final String aMessageKey, final Object... aVarargs) {
        super(aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedQualityException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedQualityException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aCause The root cause of the exception
     */
    public UnsupportedQualityException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedQualityException(final Throwable aCause, final String aMessageKey) {
        super(aCause, aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public UnsupportedQualityException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, aMessageKey);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedQualityException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>UnsupportedQualityException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public UnsupportedQualityException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, aMessageKey, aVarargs);
    }

}
