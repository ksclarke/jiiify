
package info.freelibrary.jiiify;

/**
 * Defines basic metadata used in Jiiify.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface Metadata {

    String CONTENT_TYPE = "Content-Type";

    String CONTENT_LENGTH = "Content-Length";

    String CACHE_CONTROL = "Cache-Control";

    String CONTENT_DISPOSITION = "Content-Disposition";

    String JSON_MIME_TYPE = "application/json";

    String TEXT_MIME_TYPE = "text/plain";

    String HTML_MIME_TYPE = "text/html";

    String ZIP_MIME_TYPE = "application/zip";

    String MANIFEST_FILE = "manifest.json";

    String PROPERTIES_FILE = "image.properties";

    String LOCATION_HEADER = "Location";

    String DEFAULT_CACHE_CONTROL = "max-age=86400";

}
