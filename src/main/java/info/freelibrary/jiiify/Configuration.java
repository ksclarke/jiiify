
package info.freelibrary.jiiify;

import static info.freelibrary.jiiify.Constants.CONFIG_KEY;
import static info.freelibrary.jiiify.Constants.DATA_DIR_PROP;
import static info.freelibrary.jiiify.Constants.FACEBOOK_OAUTH_CLIENT_ID;
import static info.freelibrary.jiiify.Constants.FEDORA_IP_PROP;
import static info.freelibrary.jiiify.Constants.GOOGLE_OAUTH_CLIENT_ID;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_REDIRECT_PROP;
import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Constants.OAUTH_USERS;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Constants.SHARED_DATA_KEY;
import static info.freelibrary.jiiify.Constants.SLASH;
import static info.freelibrary.jiiify.Constants.SOLR_SERVER_PROP;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;
import static info.freelibrary.jiiify.Constants.UPLOADS_DIR_PROP;
import static info.freelibrary.jiiify.Constants.URL_SCHEME_PROP;
import static info.freelibrary.jiiify.Constants.WATCH_FOLDER_PROP;
import static info.freelibrary.jiiify.MessageCodes.EXC_021;
import static info.freelibrary.jiiify.MessageCodes.EXC_022;
import static info.freelibrary.jiiify.MessageCodes.EXC_023;
import static info.freelibrary.jiiify.MessageCodes.EXC_024;
import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.S3Bucket;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.handlers.LoginHandler;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

/**
 * A developer-friendly wrapper around the default Vertx configuration JsonObject.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class Configuration implements Shareable {

    public static final int DEFAULT_PORT = 8443;

    public static final int DEFAULT_REDIRECT_PORT = 8000;

    public static final String DEFAULT_HOST = "localhost";

    public static final String DEFAULT_SERVICE_PREFIX = "/iiif";

    public static final int DEFAULT_TILE_SIZE = 1024;

    public static final long DEFAULT_SESSION_TIMEOUT = 7200000L; // two hours

    public static final String TMP_DIR_PROPERTY = "java.io.tmpdir";

    public static final String TMP_DIR = System.getProperty(TMP_DIR_PROPERTY);

    public static final String DEFAULT_UPLOADS_DIR = Paths.get(TMP_DIR, "jiiify-file-uploads").toString();

    public static final String DEFAULT_WATCH_FOLDER = Paths.get(TMP_DIR, "jiiify-watch-folder").toString();

    public static final File DEFAULT_DATA_DIR = new File("jiiify_data");

    public static final int RETRY_COUNT = 10;

    private static final String DEFAULT_DATA_DIR_NAME = "default";

    private static final String DEFAULT_SOLR_SERVER = "http://localhost:8983/solr/jiiify";

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class, MESSAGES);

    private static final String HTTPS = "https";

    private static final String HTTP = "http";

    private final int myPort;

    private final int myRedirectPort;

    private final int myTileSize;

    private final String myHost;

    private final String myServicePrefix;

    private String myUploadsDir;

    private File myWatchFolder;

    private URL mySolrServer;

    private final String myURLScheme;

    private final String myGoogleClientID;

    private final String myFacebookClientID;

    private final String[] myUsers;

    private final String myFedoraIP;

    private final Vertx myVertx;

    /* FIXME: hard-coded for now */
    private final String[] mySubmasterFormats = new String[] { ImageFormat.TIFF_EXT, ImageFormat.TIF_EXT };

    private final Map<String, PairtreeRoot> myDataDirs = new HashMap<>();

    /**
     * Creates a new Jiiify configuration object, which simplifies accessing configuration information.
     *
     * @param aConfig A JSON configuration
     * @param aVertx A Vert.x object
     * @param aHandler A handler for an asynchronous result
     */
    public Configuration(final JsonObject aConfig, final Vertx aVertx,
            final Handler<AsyncResult<Configuration>> aHandler) {
        final Future<Configuration> result = Future.future();

        myVertx = aVertx;
        myFedoraIP = aConfig.getString(FEDORA_IP_PROP);
        myServicePrefix = setServicePrefix(aConfig);
        myPort = setPort(aConfig);
        myRedirectPort = setRedirectPort(aConfig);
        myHost = setHost(aConfig);
        myTileSize = setTileSize(aConfig);
        myURLScheme = setURLScheme(aConfig);
        // TODO: Handle OAuth configs better than this
        myGoogleClientID = setGoogleClientID(aConfig);
        myFacebookClientID = setFacebookClientID(aConfig);
        myUsers = setUsers(aConfig);

        if (aHandler != null) {
            result.setHandler(aHandler);

            setWatchFolder(aConfig, watchFolderHandler -> {
                if (watchFolderHandler.failed()) {
                    result.fail(watchFolderHandler.cause());
                } else {
                    setUploadsDir(aConfig, uploadsDirHandler -> {
                        if (uploadsDirHandler.failed()) {
                            result.fail(uploadsDirHandler.cause());
                        } else {
                            setDataDirs(aConfig, dataDirsHandler -> {
                                if (dataDirsHandler.failed()) {
                                    result.fail(dataDirsHandler.cause());
                                } else {
                                    setSolrServer(aConfig, solrServerHandler -> {
                                        if (solrServerHandler.failed()) {
                                            result.fail(solrServerHandler.cause());
                                        }

                                        aVertx.sharedData().getLocalMap(SHARED_DATA_KEY).put(CONFIG_KEY, this);
                                        result.complete(this);
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    /**
     * Gets the Fedora IP address that's allowed to send image ingest requests.
     *
     * @return The Fedora IP address that's allowed to send image ingest requests
     */
    public String getFedoraIP() {
        return myFedoraIP;
    }

    /**
     * Whether there is a Fedora instance that's allowed to send image ingest requests configured
     *
     * @return True if there is a Fedora instance that will communicate with the image server; else, false
     */
    public boolean hasFedoraIP() {
        return myFedoraIP != null;
    }

    /**
     * Gets the users who are allowed to access the administrative side of things.
     *
     * @return An string array of users
     */
    public String[] getUsers() {
        return myUsers;
    }

    /**
     * Returns whether a submaster is needed for the supplied image.
     *
     * @param aFileName The name of the image in question
     * @return True if a submaster is needed; else, false
     */
    public boolean getsSubmaster(final String aFileName) {
        final String fileExt;

        if (aFileName.contains(".")) {
            fileExt = FileUtils.stripExt(aFileName);
        } else {
            fileExt = aFileName;
        }

        for (final String format : mySubmasterFormats) {
            if (fileExt.equalsIgnoreCase(format)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the Google authentication client ID.
     *
     * @param aConfig A configuration object
     * @return The Google authentication client ID
     */
    public String setGoogleClientID(final JsonObject aConfig) {
        final Properties properties = System.getProperties();

        // We'll give command line properties first priority then fall back to our JSON configuration
        if (properties.containsKey(GOOGLE_OAUTH_CLIENT_ID)) {
            LOGGER.debug(MessageCodes.DBG_111, GOOGLE_OAUTH_CLIENT_ID);
            return properties.getProperty(GOOGLE_OAUTH_CLIENT_ID);
        } else {
            return aConfig.getString(GOOGLE_OAUTH_CLIENT_ID, "");
        }
    }

    /**
     * Sets the Facebook authentication client ID.
     *
     * @param aConfig A configuration object
     * @return The Facebook authentication client ID
     */
    public String setFacebookClientID(final JsonObject aConfig) {
        final Properties properties = System.getProperties();

        // We'll give command line properties first priority then fall back to our JSON configuration
        if (properties.containsKey(FACEBOOK_OAUTH_CLIENT_ID)) {
            LOGGER.debug(MessageCodes.DBG_111, FACEBOOK_OAUTH_CLIENT_ID);
            return properties.getProperty(FACEBOOK_OAUTH_CLIENT_ID);
        } else {
            return aConfig.getString(FACEBOOK_OAUTH_CLIENT_ID, "");
        }
    }

    /**
     * Gets the OAuth client ID for the supplied service name.
     *
     * @param aService The name of the OAuth service
     * @return The OAuth client ID for the supplied service
     */
    public String getOAuthClientID(final String aService) {
        final String service = aService.toLowerCase();

        if (service.equals(LoginHandler.GOOGLE)) {
            return myGoogleClientID;
        } else if (service.equals(LoginHandler.FACEBOOK)) {
            return myFacebookClientID;
        }

        // FIXME: With something better than a RuntimeException
        throw new RuntimeException(LOGGER.getMessage(MessageCodes.EXC_087));
    }

    /**
     * Gets the OAuth client secret key for the supplied service name
     *
     * @param aService The name of the OAuth service
     * @return The OAuth client secret key for the supplied service
     */
    public String getOAuthClientSecretKey(final String aService) {
        final String service = aService.toLowerCase();

        if (service.equals(LoginHandler.GOOGLE)) {
            return "";
        } else if (service.equals(LoginHandler.FACEBOOK)) {
            return "";
        }

        // FIXME: With something better than a RuntimeException
        throw new RuntimeException(LOGGER.getMessage(MessageCodes.EXC_087));
    }

    /**
     * The number of times a message should be retried if it times out.
     *
     * @return The number of times a message should be tried if it times out
     */
    public int getRetryCount() {
        // FIXME: Add optional configuration through system property
        return RETRY_COUNT;
    }

    /**
     * Gets the default tile size.
     *
     * @return The default tile size
     */
    public int getTileSize() {
        return myTileSize;
    }

    /**
     * Gets the port at which Jiiify has been configured to run.
     *
     * @return The port at which Jiiify has been configured to run
     */
    public int getPort() {
        return myPort;
    }

    /**
     * Gets the redirect port that redirects to the secure port.
     *
     * @return The redirect port that redirects to the secure port
     */
    public int getRedirectPort() {
        return myRedirectPort;
    }

    /**
     * The host name of the server.
     *
     * @return The host name of the server
     */
    public String getHost() {
        return myHost;
    }

    /**
     * The scheme the server is using (e.g., HTTP or HTTPS).
     *
     * @return The scheme the server is using
     */
    public String getScheme() {
        return myURLScheme;
    }

    /**
     * Returns true if the server is using HTTPS; else, false.
     *
     * @return True if the server is using HTTPS; else, false
     */
    public boolean usesHttps() {
        return HTTPS.equals(myURLScheme);
    }

    /**
     * Returns the base URL of the Jiiify image server, including: scheme, host, and port (if something other than
     * 80).
     *
     * @return The base URL of the Jiiify image server
     */
    public String getServer() {
        final int port = getPort();
        return getScheme() + "://" + getHost() + ((port != 80) && (port != 443) ? ":" + getPort() : "");
    }

    /**
     * Gets Solr server Jiiify is configured to use.
     *
     * @return The Solr server that Jiiify should be able to use
     */
    public URL getSolrServer() {
        return mySolrServer;
    }

    /**
     * Gets the service prefix at which Jiiify has been configured to run.
     *
     * @return The service prefix at which Jiiify has been configured to run
     */
    public String getServicePrefix() {
        return myServicePrefix;
    }

    /**
     * Gets the directory into which file uploads should be put. If "java.io.tmpdir" is configured as the file uploads
     * location, a <code>jiiify-file-uploads</code> directory will be created in the system's temp directory and file
     * uploads will be written there; otherwise, the supplied configured directory is used as the file uploads folder.
     *
     * @return The directory into which uploads should be put
     */
    public String getUploadsDir() {
        return myUploadsDir;
    }

    /**
     * Gets the default non-prefixed data directory.
     *
     * @return The default data directory
     */
    public PairtreeRoot getDataDir() {
        return myDataDirs.get(DEFAULT_DATA_DIR_NAME);
    }

    /**
     * Gets the Pairtree data directory for the supplied ID, which may or may not be using a Pairtree prefix.
     *
     * @param aID An ID for which to get a Pairtree data directory
     * @return The Pairtree root directory for the supplied ID
     */
    public PairtreeRoot getDataDir(final String aID) {
        if (hasPrefixedDataDirs() && hasIDPrefixMatch(aID)) {
            return myDataDirs.get(getIDPrefix(aID));
        } else {
            return myDataDirs.get(DEFAULT_DATA_DIR_NAME);
        }
    }

    /**
     * Returns the ingest watch folder.
     *
     * @return The ingest watch folder
     */
    public File getWatchFolder() {
        return myWatchFolder;
    }

    /**
     * Sets the users allowed to interact with Jiiify.
     *
     * @param aConfig A configuration that includes allowed users
     * @return An array of users
     */
    private String[] setUsers(final JsonObject aConfig) {
        final List<?> list = aConfig.getJsonArray(OAUTH_USERS, new JsonArray()).getList();
        final String[] users = new String[list.size()];

        for (int index = 0; index < list.size(); index++) {
            users[index] = list.get(index).toString();
        }

        return users;
    }

    /**
     * Checks whether we are using more than one Pairtree data directory.
     *
     * @return True if we are using multiple data directories; else, false
     */
    private boolean hasPrefixedDataDirs() {
        return myDataDirs.size() > 1;
    }

    /**
     * Gets the ID prefix for the supplied ID.
     *
     * @param aID An image ID from which to get prefix
     * @return The ID prefix for the supplied ID
     */
    private String getIDPrefix(final String aID) {
        final Iterator<String> iterator = myDataDirs.keySet().iterator();

        while (iterator.hasNext()) {
            final String prefix = iterator.next();

            if (aID.startsWith(prefix)) {
                return prefix;
            }
        }

        return null;
    }

    /**
     * Determines if the supplied ID has an ID prefix match.
     *
     * @param aID An ID to check against known ID prefixes
     * @return True if the supplied ID has a match; else, false
     */
    private boolean hasIDPrefixMatch(final String aID) {
        final Iterator<String> iterator = myDataDirs.keySet().iterator();

        while (iterator.hasNext()) {
            if (aID.startsWith(iterator.next())) {
                return true;
            }
        }

        return false;
    }

    private String setURLScheme(final JsonObject aConfig) {
        final Properties properties = System.getProperties();

        // We'll give command line properties first priority then fall back to our JSON configuration
        if (properties.containsKey(URL_SCHEME_PROP)) {
            final String scheme = properties.getProperty(URL_SCHEME_PROP);

            if (LOGGER.isDebugEnabled()) {
                if (HTTP.equals(scheme)) {
                    LOGGER.debug(MessageCodes.DBG_112, URL_SCHEME_PROP, HTTP);
                } else if (HTTPS.equals(scheme)) {
                    LOGGER.debug(MessageCodes.DBG_112, URL_SCHEME_PROP, HTTPS);
                }
            }

            if (!HTTP.equals(scheme) && !HTTPS.equals(scheme)) {
                LOGGER.warn(MessageCodes.WARN_016, URL_SCHEME_PROP, scheme, HTTPS);

                return HTTPS;
            } else {
                LOGGER.info(MessageCodes.INFO_008, scheme);
                return scheme;
            }
        } else {
            return HTTPS;
        }
    }

    /**
     * Sets the host at which Jiiify listens.
     *
     * @param aConfig A JsonObject with configuration information
     */
    private String setHost(final JsonObject aConfig) {
        final String configHost = aConfig.getString(HTTP_HOST_PROP, "");
        final String testHost = DEFAULT_HOST + "-test";

        String host;

        try {
            final Properties properties = System.getProperties();

            // We'll give command line properties first priority then fall back to our JSON configuration
            if (properties.containsKey(HTTP_HOST_PROP) && !configHost.equals(testHost)) {
                LOGGER.debug(MessageCodes.DBG_111, HTTP_HOST_PROP);
                host = properties.getProperty(HTTP_HOST_PROP);
            } else if (configHost.equals(testHost)) {
                host = DEFAULT_HOST;
            } else {
                host = aConfig.getString(HTTP_HOST_PROP, DEFAULT_HOST);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn(MessageCodes.WARN_017, DEFAULT_PORT);
            host = DEFAULT_HOST;
        }

        LOGGER.info(MessageCodes.INFO_009, host);
        return host;
    }

    /**
     * Sets the port at which Jiiify listens.
     *
     * @param aConfig A JsonObject with configuration information
     */
    private int setPort(final JsonObject aConfig) {
        int port;

        try {
            final Properties properties = System.getProperties();

            // We'll give command line properties first priority then fall back to our JSON configuration
            if (properties.containsKey(HTTP_PORT_PROP)) {
                LOGGER.debug(MessageCodes.DBG_111, HTTP_PORT_PROP);
                port = Integer.parseInt(properties.getProperty(HTTP_PORT_PROP));
            } else {
                port = aConfig.getInteger(HTTP_PORT_PROP, DEFAULT_PORT);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn(MessageCodes.WARN_017, DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        LOGGER.info(MessageCodes.INFO_010, port);
        return port;
    }

    /**
     * Sets the port that redirects to a secure port (only when https is configured).
     *
     * @param aConfig A JsonObject with configuration information
     */
    private int setRedirectPort(final JsonObject aConfig) {
        int port;

        try {
            final Properties properties = System.getProperties();

            // We'll give command line properties first priority then fall back to our JSON configuration
            if (properties.containsKey(HTTP_PORT_REDIRECT_PROP)) {
                LOGGER.debug(MessageCodes.DBG_111, HTTP_PORT_REDIRECT_PROP);
                port = Integer.parseInt(properties.getProperty(HTTP_PORT_REDIRECT_PROP));
            } else {
                port = aConfig.getInteger(HTTP_PORT_REDIRECT_PROP, DEFAULT_REDIRECT_PORT);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn(MessageCodes.WARN_018, DEFAULT_REDIRECT_PORT);
            port = DEFAULT_REDIRECT_PORT;
        }

        LOGGER.info(MessageCodes.INFO_011, port);
        return port;
    }

    /**
     * Sets the default tile size.
     *
     * @param aConfig A JsonObject with configuration information
     */
    private int setTileSize(final JsonObject aConfig) {
        int tileSize;

        try {
            final Properties properties = System.getProperties();

            // We'll give command line properties first priority then fall back to our JSON configuration
            if (properties.containsKey(TILE_SIZE_PROP)) {
                LOGGER.debug(MessageCodes.DBG_111, TILE_SIZE_PROP);
                tileSize = Integer.parseInt(properties.getProperty(TILE_SIZE_PROP));
            } else {
                tileSize = aConfig.getInteger(TILE_SIZE_PROP, DEFAULT_TILE_SIZE);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn(MessageCodes.WARN_019, DEFAULT_TILE_SIZE);
            tileSize = DEFAULT_TILE_SIZE;
        }

        LOGGER.info(MessageCodes.INFO_012, tileSize);
        return tileSize;
    }

    private String setServicePrefix(final JsonObject aConfig) {
        final Properties properties = System.getProperties();

        String prefix;

        if (properties.containsKey(SERVICE_PREFIX_PROP)) {
            LOGGER.debug(MessageCodes.DBG_111, SERVICE_PREFIX_PROP);
            prefix = properties.getProperty(SERVICE_PREFIX_PROP, DEFAULT_SERVICE_PREFIX);
        } else {
            prefix = aConfig.getString(SERVICE_PREFIX_PROP, DEFAULT_SERVICE_PREFIX);
        }

        try {
            prefix = PathUtils.encodeServicePrefix(prefix.startsWith(SLASH) ? prefix : SLASH + prefix);
            LOGGER.info(MessageCodes.INFO_013, prefix);
        } catch (final URISyntaxException details) {
            LOGGER.warn(MessageCodes.WARN_020, prefix, DEFAULT_SERVICE_PREFIX);
            prefix = DEFAULT_SERVICE_PREFIX;
        }

        return prefix;
    }

    private void setSolrServer(final JsonObject aConfig, final Handler<AsyncResult<Configuration>> aHandler) {
        final Properties properties = System.getProperties();
        final Future<Configuration> result = Future.future();

        if (aHandler != null) {
            result.setHandler(aHandler);

            final String solrServer = properties.getProperty(SOLR_SERVER_PROP, aConfig.getString(SOLR_SERVER_PROP,
                    DEFAULT_SOLR_SERVER));

            if (properties.containsKey(SOLR_SERVER_PROP)) {
                LOGGER.debug(MessageCodes.DBG_111, SOLR_SERVER_PROP);
            }

            try {
                // TODO: Actually ping the server here too?
                mySolrServer = new URL(solrServer);
                result.complete(this);
            } catch (final MalformedURLException details) {
                result.fail(new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_079, solrServer)));
            }
        } else {

        }
    }

    private void setUploadsDir(final JsonObject aConfig, final Handler<AsyncResult<Configuration>> aHandler) {
        String uploadsDir = StringUtils.trimToNull(System.getProperties().getProperty(UPLOADS_DIR_PROP));

        if (aHandler != null) {
            final Future<Configuration> result = Future.future();

            result.setHandler(aHandler);

            if (uploadsDir != null) {
                LOGGER.debug(MessageCodes.DBG_111, UPLOADS_DIR_PROP);
            } else {
                uploadsDir = StringUtils.trimTo(aConfig.getString(UPLOADS_DIR_PROP), DEFAULT_UPLOADS_DIR);
            }

            if (TMP_DIR_PROPERTY.equalsIgnoreCase(uploadsDir)) {
                uploadsDir = DEFAULT_UPLOADS_DIR;
                LOGGER.debug(MessageCodes.DBG_113, uploadsDir);
            }

            try {
                if (!Files.createDirectories(Paths.get(uploadsDir)).toFile().canWrite()) {
                    result.fail(new ConfigurationException(LOGGER.getMessage(EXC_021, uploadsDir)));
                } else {
                    LOGGER.info(MessageCodes.INFO_014, uploadsDir);

                    myUploadsDir = uploadsDir;
                    result.complete(this);
                }
            } catch (final IOException details) {
                result.fail(new ConfigurationException(LOGGER.getMessage(EXC_022, uploadsDir)));
            }
        }
    }

    private void setDataDirs(final JsonObject aConfig, final Handler<AsyncResult<Configuration>> aHandler) {
        Objects.requireNonNull(aHandler);

        final String awsAccessKey = StringUtils.trimToNull(aConfig.getString("aws.access.key"));
        final String awsSecretKey = StringUtils.trimToNull(aConfig.getString("aws.secret.key"));
        final String s3Endpoint = StringUtils.trimToNull(aConfig.getString("s3.endpoint"));
        final Future<Configuration> result = Future.future();
        final Properties props = System.getProperties();
        final PairtreeRoot pairtree;
        final String location;

        result.setHandler(aHandler);

        if (props.containsKey(DATA_DIR_PROP)) {
            LOGGER.debug(MessageCodes.DBG_111, DATA_DIR_PROP);
        }

        location = props.getProperty(DATA_DIR_PROP, aConfig.getString(DATA_DIR_PROP, DEFAULT_DATA_DIR
                .getAbsolutePath()));

        if ((awsAccessKey != null) && (awsSecretKey != null)) {
            LOGGER.info(MessageCodes.INFO_015, awsAccessKey);

            if (s3Endpoint == null) {
                pairtree = PairtreeFactory.getFactory(myVertx, S3Bucket).getPairtree(location, awsAccessKey,
                        awsSecretKey);
            } else {
                LOGGER.info(MessageCodes.INFO_016, s3Endpoint);
                pairtree = PairtreeFactory.getFactory(myVertx, S3Bucket).getPairtree(location, awsAccessKey,
                        awsSecretKey, s3Endpoint);
            }
        } else {
            pairtree = PairtreeFactory.getFactory(myVertx).getPairtree(location);
        }

        pairtree.create(handler -> {
            if (handler.succeeded()) {
                LOGGER.info(MessageCodes.INFO_017, pairtree.getPath());
                myDataDirs.put(DEFAULT_DATA_DIR_NAME, pairtree);
                result.complete(this);
            } else {
                final Throwable details = handler.cause();

                LOGGER.error(MessageCodes.EXC_060, pairtree.getPath(), details);
                result.fail(details);
            }
        });
    }

    /**
     * Sets the ingest watch folder.
     *
     * @param aConfig A JsonObject configuration
     * @throws ConfigurationException If there is trouble setting the ingest watch folder
     */
    private void setWatchFolder(final JsonObject aConfig, final Handler<AsyncResult<Configuration>> aHandler) {
        final Properties properties = System.getProperties();
        final Future<Configuration> result = Future.future();
        final Path watchFolder;

        if (aHandler != null) {
            final String watchFolderPath = StringUtils.trimToNull(properties.getProperty(WATCH_FOLDER_PROP));

            result.setHandler(aHandler);

            if (watchFolderPath != null) {
                LOGGER.debug(MessageCodes.DBG_111, WATCH_FOLDER_PROP);

                watchFolder = Paths.get(watchFolderPath);
            } else {
                watchFolder = Paths.get(aConfig.getString(WATCH_FOLDER_PROP, DEFAULT_WATCH_FOLDER));
            }

            try {
                if (!Files.createDirectories(watchFolder).toFile().canWrite()) {
                    result.fail(new ConfigurationException(LOGGER.getMessage(EXC_023, watchFolder)));
                } else {
                    LOGGER.info(MessageCodes.INFO_018, watchFolder);
                    myWatchFolder = watchFolder.toFile();
                    result.complete();
                }
            } catch (final IOException details) {
                result.fail(new ConfigurationException(LOGGER.getMessage(EXC_024, watchFolder)));
            }
        }
    }

}
