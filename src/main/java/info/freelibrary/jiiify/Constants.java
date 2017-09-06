
package info.freelibrary.jiiify;

import java.nio.charset.Charset;

/**
 * Defines constants used in Jiiify.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface Constants {

    String UTF_8_ENCODING = Charset.forName("UTF-8").name();

    String MESSAGES = "jiiify_messages";

    /* The following are properties that can be set by the user */

    String HTTP_PORT_PROP = "jiiify.port";

    String HTTP_PORT_REDIRECT_PROP = "jiiify.redirect.port";

    String HTTP_HOST_PROP = "jiiify.host";

    String JIIIFY_CORES_PROP = "jiiify.cores";

    String URL_SCHEME_PROP = "jiiify.url.scheme";

    String DATA_DIR_PROP = "jiiify.data.dir";

    String UPLOADS_DIR_PROP = "jiiify.uploads.dir";

    String WATCH_FOLDER_PROP = "jiiify.watch.folder";

    String SOLR_SERVER_PROP = "jiiify.solr.server";

    String LOG_LEVEL_PROP = "jiiify.log.level";

    String SERVICE_PREFIX_PROP = "jiiify.service.prefix";

    String ID_PREFIXES_PROP = "jiiify.id.prefixes";

    String TILE_SIZE_PROP = "jiiify.tile.size";

    String THUMBNAIL_SIZE_PROP = "jiiify.thumbnail.size";

    String KEY_PASS_PROP = "jiiify.key.pass";

    String JCEKS_PROP = "jiiify.jceks";

    String JKS_PROP = "jiiify.jks";

    String METRICS_REG_PROP = "jiiify.metrics";

    String FEDORA_IP_PROP = "fedora.ip";

    /* These config values are only used internally. */

    String JIIIFY_TESTING = "jiiify.ignore.failures";

    String FILE_PATH_KEY = "jiiify.file.path";

    String IIIF_PATH_KEY = "jiiify.iiif.path";

    String IMAGE_SOURCE_KEY = "jiiify.image.source";

    String IMAGE_CLEANUP_KEY = "jiiify.image.cleanup";

    String SHARED_DATA_KEY = "jiiify.shared.data";

    String SOLR_SERVICE_KEY = "jiiify.solr";

    String CONFIG_KEY = "jiiify.config";

    String HBS_DATA_KEY = "hbs.data";

    String HBS_PATH_SKIP_KEY = "hbs.path.skip";

    String ID_KEY = "id";

    String GOOGLE_OAUTH_CLIENT_ID = "jiiify.oauth.google.clientId";

    String FACEBOOK_OAUTH_CLIENT_ID = "jiiify.oauth.facebook.clientId";

    String OAUTH_USERS = "jiiify.oauth.users";

    String OVERWRITE_KEY = "jiiify.file.overwrite";

    String JIIIFY_ARRAY = "jiiify.json.array";

    String TILE_REQUEST_KEY = "jiiify.tile.request.id";

    String IMAGE_BUFFER_KEY = "jiiify.image.buffer";

    String IMAGE_COUNTER_KEY = "jiiify.image.counter";

    String IMAGE_TILE_COUNT = "jiiify.tile.count";

    /* Message values */

    String SUCCESS_RESPONSE = "success";

    String FAILURE_RESPONSE = "failure";

    /* Commonly used values */

    String SLASH = "/";

}
