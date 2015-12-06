
package info.freelibrary.jiiify;

public interface RoutePatterns {

    /**
     * The IIIF service path (all requests are extensions of this).
     */
    public static final String IIIF_URI = "{}/*";

    /**
     * The IIIF service path (all requests are extensions of this).
     */
    // public static final String IIIF_URI_404_RE = "\\{}\\/?";

    /**
     * The base URI for an image request; it redirects to an image info document for the image.
     */
    // public static final String BASE_URI = "{}/*";

    /**
     * An image info document request.
     */
    public static final String IMAGE_INFO_DOC_RE = "\\{}\\/.+\\/info.json";

    /**
     * An image request.
     */
    public static final String IMAGE_REQUEST_RE =
            "\\{}\\/.+\\/(default|color|gray|bitonal)\\.(jpg|gif|tif|png|jp2|pdf|webp)";

    /**
     * A manifest request.
     */
    public static final String IMAGE_MANIFEST_RE = "\\{}\\/.+\\/manifest";

    /**
     * A thumbnails request.
     */
    public static final String IMAGE_THUMBNAILS_RE = "\\{}\\/.+\\/manifest\\/thumbnails";

    /**
     * A catch-all path for the administrative browse.
     */
    public static final String BROWSE_RE = "\\/admin\\/browse\\/?";

    /**
     * A catch-all path for the administrative search.
     */
    public static final String SEARCH_RE = "\\/admin\\/search\\/?";

    /**
     * A catch-all path for the administrative ingest page.
     */
    public static final String INGEST_RE = "\\/admin\\/ingest\\/?";

    /**
     * A catch-all path for the administrative statistics page.
     */
    public static final String METRICS_RE = "\\/admin\\/metrics\\/?";

    /**
     * A path for logins to the administrative interface.
     */
    public static final String LOGIN = "/admin/login";

    /**
     * A path for administrative interface login responses.
     */
    public static final String LOGIN_RESPONSE_RE = "\\/login-response";

    /**
     * A path for logouts from the administrative interface.
     */
    public static final String LOGOUT = "/admin/logout";

    /**
     * An administrative image view.
     */
    public static final String ITEM = "/admin/item/*"; // "\\/admin\\/item\\/.+";

    /**
     * A generic path for the root of the Web application.
     */
    public static final String ROOT = "/";

    /**
     * A generic path for Web application metrics.
     */
    public static final String STATUS = "/status/*";

    /**
     * An administrative object refresh path.
     */
    public static final String REFRESH = "/admin/refresh/*";

    /**
     * An administrative image properties view.
     */
    public static final String PROPERTIES = "/admin/item/properties/*";

    /**
     * An administrative image zip download.
     */
    public static final String DOWNLOAD_RE = "\\/admin\\/download\\/(zip|bagit)\\/.+";

    /**
     * A catch-all path for the administrative UI page templates (not static files though).
     */
    public static final String ADMIN_UI = "/admin/*";

    /**
     * A route pattern for serving static files.
     */
    public static final String STATIC_FILES_RE =
            ".*(\\.js|\\.css|\\.ico|\\.png|\\.gif|\\.ttf|\\.eot|\\.svg|\\.woff|\\.txt)$";
}
