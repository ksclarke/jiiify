
package info.freelibrary.jiiify.util;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class PathUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathUtils.class, MESSAGES);

    private PathUtils() {
    }

    /**
     * Encodes a IIIF identifier so that it can be used in the RESTful URL.
     *
     * @param aIdentifier An identifier to encode
     * @return An encoded version of the identifier
     */
    public static final String encodeIdentifier(final String aIdentifier) throws URISyntaxException {
        return encode(aIdentifier, false);
    }

    /**
     * Encodes an optional IIIF service path so that it can be used in the RESTful URL.
     *
     * @param aPrefix An optional IIIF service path
     * @return The encoded form of the IIIF service path
     */
    public static final String encodeServicePrefix(final String aPrefix) throws URISyntaxException {
        return encode(aPrefix, true);
    }

    /**
     * Decodes an encoded IIIF identifier or service path. If it has been doubly encoded, it is doubly decoded.
     *
     * @param aString An encoded IIIF identifier or service path
     * @return A decoded IIIF identifier or service path
     */
    public static final String decode(final String aString) {
        return decode(aString, Constants.UTF_8_ENCODING);
    }

    /**
     * Decodes an encoded IIIF identifier or service path. If it has been doubly encoded, it is doubly decoded. This
     * method is really just here so our tests can test the checked UnsupportedEncodingException. Every Java Virtual
     * Machine is required to support UTF-8.
     *
     * @param aString An encoded IIIF identifier or service path
     * @param aEncoding A character encoding to use for the string decoding
     * @return A decoded IIIF identifier or service path
     */
    private static final String decode(final String aURIString, final String aEncoding) {
        String uriString = aURIString;
        String decodedString;

        try {
            do {
                decodedString = uriString;

                // Java's URLDecoder needs a little help with occurrences of '%' that aren't percent escaped values
                uriString = URLDecoder.decode(decodedString.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), aEncoding);
            } while (!uriString.equals(decodedString));
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException("An unsupported charset was supplied: " + aEncoding, details);
        }

        if (LOGGER.isDebugEnabled() && !aURIString.equals(decodedString)) {
            LOGGER.debug("Decoded {} to {}", aURIString, decodedString);
        }

        return decodedString;
    }

    /**
     * Percent-encodes supplied string but only after decoding it completely first.
     *
     * @param aString The string to encode
     * @param aIgnoreSlashFlag Whether slashes should be encoded or not
     * @return The percent-encoded string
     */
    private static final String encode(final String aString, final boolean aIgnoreSlashFlag)
            throws URISyntaxException {
        final CharacterIterator iterator = new StringCharacterIterator(decode(aString));
        final StringBuilder sb = new StringBuilder();

        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            switch (c) {
                case '%':
                    sb.append("%25");
                    break;
                case '/':
                    if (aIgnoreSlashFlag) {
                        sb.append(c);
                    } else {
                        sb.append("%2F");
                    }
                    break;
                case '?':
                    sb.append("%3F");
                    break;
                case '#':
                    sb.append("%23");
                    break;
                case '[':
                    sb.append("%5B");
                    break;
                case ']':
                    sb.append("%5D");
                    break;
                case '@':
                    sb.append("%40");
                    break;
                default:
                    sb.append(c);
            }
        }

        // Must percent-encode any characters outside the US-ASCII set
        return new URI(sb.toString()).toASCIIString();
    }

    /**
     * Strips parts of a slash delimited string.
     *
     * @param aPath A path from which parts should be stripped
     * @param aParts The index positions of the parts to be stripped
     * @return The path without the stripped parts
     */
    public static String stripPathParts(final String aPath, final int... aParts) {
        final String path = aPath.startsWith("/") ? aPath.replaceFirst("\\/", "") : aPath;
        final String[] parts = path.split("\\/");
        final StringBuilder builder = new StringBuilder();

        for (int index = 0; index < parts.length; index++) {
            boolean found = false;

            for (int partIndex = 0; partIndex < aParts.length; partIndex++) {
                if (index == partIndex) {
                    found = true;
                }
            }

            if (!found) {
                builder.append(parts[index]).append('/');
            }
        }

        if (builder.charAt(builder.length() - 1) == '/') {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    // final Configuration config = (Configuration) aVertx.sharedData().getLocalMap(SHARED_DATA_KEY).get(CONFIG_KEY);
}
