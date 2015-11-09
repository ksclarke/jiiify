
package info.freelibrary.jiiify;

import java.nio.charset.Charset;

public interface Constants {

    public static final String UTF_8_ENCODING = Charset.forName("UTF-8").name();

    public static final String MESSAGES = "jiiify_messages";

    /* The following are properties that can be set by the user */

    public static final String HTTP_PORT_PROP = "jiiify.port";

    public static final String HTTP_PORT_REDIRECT_PROP = "jiiify.redirect.port";

    public static final String HTTP_HOST_PROP = "jiiify.host";

    public static final String URL_SCHEME_PROP = "jiiify.url.scheme";

    public static final String DATA_DIR_PROP = "jiiify.data.dir";

    public static final String TEMP_DIR_PROP = "jiiify.temp.dir";

    public static final String WATCH_FOLDER_PROP = "jiiify.watch.folder";

    public static final String SOLR_SERVER_PROP = "jiiify.solr.server";

    public static final String LOG_LEVEL_PROP = "jiiify.log.level";

    public static final String SERVICE_PREFIX_PROP = "jiiify.service.prefix";

    public static final String ID_PREFIXES_PROP = "jiiify.id.prefixes";

    public static final String TILE_SIZE_PROP = "jiiify.tile.size";

    public static final String THUMBNAIL_SIZE_PROP = "jiiify.thumbnail.size";

    public static final String KEY_PASS_PROP = "jiiify.key.pass";

    public static final String JCEKS_PROP = "jiiify.jceks";

    public static final String JKS_PROP = "jiiify.jks";

    public static final String METRICS_REG_PROP = "jiiify.metrics";

    /* These config values are only used internally. */

    public static final String FILE_PATH_KEY = "jiiify.file.path";

    public static final String IIIF_PATH_KEY = "jiiify.iiif.path";

    public static final String IMAGE_SOURCE_KEY = "jiiify.image.source";

    public static final String SHARED_DATA_KEY = "jiiify.shared.data";

    public static final String SOLR_SERVICE_KEY = "jiiify.solr";

    public static final String CONFIG_KEY = "jiiify.config";

    public static final String HBS_DATA_KEY = "hbs.data";

    public static final String HBS_PATH_SKIP_KEY = "hbs.path.skip";

    public static final String ID_KEY = "id";

    public static final String OVERWRITE_KEY = "jiiify.file.overwrite";

    public static final String THUMBNAIL_KEY = "jiiify_thumbnail_s";

    public static final String JIIIFY_ARRAY = "jiiify.json.array";

    /* Message values */

    public static final String SUCCESS_RESPONSE = "success";

    public static final String FAILURE_RESPONSE = "failure";

}
