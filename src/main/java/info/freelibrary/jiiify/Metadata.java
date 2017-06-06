
package info.freelibrary.jiiify;

/**
 * Defines basic metadata used in Jiiify.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface Metadata {

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String CACHE_CONTROL = "Cache-Control";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String JSON_MIME_TYPE = "application/json";

    public static final String TEXT_MIME_TYPE = "text/plain";

    public static final String HTML_MIME_TYPE = "text/html";

    public static final String ZIP_MIME_TYPE = "application/zip";

    public static final String MANIFEST_FILE = "manifest.json";

    public static final String PROPERTIES_FILE = "image.properties";

    public static final String LOCATION_HEADER = "Location";

    public static final String DEFAULT_CACHE_CONTROL = "max-age=86400";

}
