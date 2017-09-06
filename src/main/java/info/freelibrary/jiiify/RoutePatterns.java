
package info.freelibrary.jiiify;

/**
 * Defines HTTP routing patterns used by the {@link info.freelibrary.jiiify.verticles.JiiifyMainVerticle}.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface RoutePatterns {

    /**
     * The IIIF service path (all requests are extensions of this).
     */
    String IIIF_URI = "{}/*";

    /**
     * An image info document request.
     */
    String IMAGE_INFO_DOC_RE = "\\{}\\/.+\\/info.json";

    /**
     * An image request.
     */
    String IMAGE_REQUEST_RE = "\\{}\\/.+\\/(default|color|gray|bitonal)\\.(jpg|gif|tif|png|jp2|pdf|webp)";

    /**
     * A manifest request.
     */
    String IMAGE_MANIFEST_RE = "\\{}\\/.+\\/manifest";

    /**
     * A catch-all path for the administrative browse.
     */
    String BROWSE_RE = "\\/admin\\/browse\\/?";

    /**
     * A catch-all path for the administrative search.
     */
    String SEARCH_RE = "\\/admin\\/search\\/?";

    /**
     * A catch-all path for the administrative ingest page.
     */
    String INGEST_RE = "\\/admin\\/ingest\\/?";

    /**
     * A catch-all path for the administrative statistics page.
     */
    String METRICS_RE = "\\/admin\\/metrics\\/?";

    /**
     * A path for logins to the administrative interface.
     */
    String LOGIN = "/admin/login";

    /**
     * A path for administrative interface login responses.
     */
    String LOGIN_RESPONSE_RE = "\\/login-response";

    /**
     * A path for logouts from the administrative interface.
     */
    String LOGOUT = "/admin/logout";

    /**
     * An administrative image view.
     */
    String ITEM = "/admin/item/*"; // "\\/admin\\/item\\/.+";

    /**
     * A generic path for the root of the Web application.
     */
    String ROOT = "/";

    /**
     * A generic path for Web application metrics.
     */
    String STATUS = "/status/*";

    /**
     * An administrative object refresh path.
     */
    String REFRESH = "/admin/refresh/*";

    /**
     * An administrative image properties view.
     */
    String PROPERTIES = "/admin/item/properties/*";

    /**
     * An administrative image zip download.
     */
    String DOWNLOAD_RE = "\\/admin\\/download\\/(zip|bagit)\\/.+";

    /**
     * A catch-all path for the administrative UI page templates (not static files though).
     */
    String ADMIN_UI = "/admin/*";

    /**
     * A route pattern for serving static files.
     */
    String STATIC_FILES_RE =
            ".*(\\.js|\\.map|\\.css|\\.ico|\\.png|\\.gif|\\.ttf|\\.eot|\\.svg|\\.woff|\\.txt|translation\\.json)$";

    /**
     * A route pattern to receive events from Fedora.
     */
    String FEDORA_EVENT = "/fcrepo-event";

}
