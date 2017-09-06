
package info.freelibrary.jiiify.iiif;

import java.util.Locale;

import info.freelibrary.util.I18nException;

/**
 * A generic exception related to the IIIF Image API service.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class IIIFException extends I18nException {

    /**
     * The <code>serialVersionUID</code> for a <code>IIIFException</code>.
     */
    private static final long serialVersionUID = 8725269140335533371L;

    /**
     * The resource bundle name from which we pull our exception messages.
     */
    private static final String BUNDLE_NAME = "jiiify_messages";

    /**
     * Constructs a new <code>IIIFException</code>.
     */
    public IIIFException() {
        super();
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aMessageKey A message key to look up the exception message
     */
    public IIIFException(final String aMessageKey) {
        super(BUNDLE_NAME, aMessageKey);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public IIIFException(final String aMessageKey, final Object... aVarargs) {
        super(BUNDLE_NAME, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public IIIFException(final Locale aLocale, final String aMessageKey) {
        super(aLocale, BUNDLE_NAME, aMessageKey);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public IIIFException(final Locale aLocale, final String aMessageKey, final Object... aVarargs) {
        super(aLocale, BUNDLE_NAME, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aCause The root cause of the exception
     */
    public IIIFException(final Throwable aCause) {
        super(aCause);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     */
    public IIIFException(final Throwable aCause, final String aMessageKey) {
        super(aCause, BUNDLE_NAME, aMessageKey);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     */
    public IIIFException(final Throwable aCause, final Locale aLocale, final String aMessageKey) {
        super(aCause, aLocale, BUNDLE_NAME, aMessageKey);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public IIIFException(final Throwable aCause, final String aMessageKey, final Object... aVarargs) {
        super(aCause, BUNDLE_NAME, aMessageKey, aVarargs);
    }

    /**
     * Constructs a <code>IIIFException</code>.
     *
     * @param aCause The root cause of the exception
     * @param aLocale A locale to use when constructing the exception message
     * @param aMessageKey A message key to look up the exception message
     * @param aVarargs Additional details to add to the exception message
     */
    public IIIFException(final Throwable aCause, final Locale aLocale, final String aMessageKey,
            final Object... aVarargs) {
        super(aCause, aLocale, BUNDLE_NAME, aMessageKey, aVarargs);
    }

}
