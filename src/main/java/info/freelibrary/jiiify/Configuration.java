
package info.freelibrary.jiiify;

import static info.freelibrary.jiiify.Constants.CONFIG_KEY;
import static info.freelibrary.jiiify.Constants.DATA_DIR_PROP;
import static info.freelibrary.jiiify.Constants.FACEBOOK_OAUTH_CLIENT_ID;
import static info.freelibrary.jiiify.Constants.GOOGLE_OAUTH_CLIENT_ID;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_REDIRECT_PROP;
import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Constants.OAUTH_USERS;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Constants.SHARED_DATA_KEY;
import static info.freelibrary.jiiify.Constants.SOLR_SERVER_PROP;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;
import static info.freelibrary.jiiify.Constants.UPLOADS_DIR_PROP;
import static info.freelibrary.jiiify.Constants.URL_SCHEME_PROP;
import static info.freelibrary.jiiify.Constants.WATCH_FOLDER_PROP;
import static info.freelibrary.jiiify.MessageCodes.EXC_021;
import static info.freelibrary.jiiify.MessageCodes.EXC_022;
import static info.freelibrary.jiiify.MessageCodes.EXC_023;
import static info.freelibrary.jiiify.MessageCodes.EXC_024;

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
 * @author Kevin S. Clarke <a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>
 */
public class Configuration implements Shareable {

    public static final int DEFAULT_PORT = 8443;

    public static final int DEFAULT_REDIRECT_PORT = 8000;

    public static final String DEFAULT_HOST = "localhost";

    public static final String DEFAULT_SERVICE_PREFIX = "/iiif";

    public static final int DEFAULT_TILE_SIZE = 1024;

    public static final long DEFAULT_SESSION_TIMEOUT = 7200000L; // two hours

    public static final File DEFAULT_UPLOADS_DIR = new File(System.getProperty("java.io.tmpdir"),
            "jiiify-file-uploads");

    public static final File DEFAULT_WATCH_FOLDER = new File(System.getProperty("java.io.tmpdir"),
            "jiiify-watch-folder");

    public static final File DEFAULT_DATA_DIR = new File("jiiify_data");

    public static final int RETRY_COUNT = 10;

    private static final String DEFAULT_DATA_DIR_NAME = "default";

    private static final String DEFAULT_SOLR_SERVER = "http://localhost:8983/solr/jiiify";

    private final Logger LOGGER = LoggerFactory.getLogger(Configuration.class, MESSAGES);

    private final int myPort;

    private final int myRedirectPort;

    private final int myTileSize;

    private final String myHost;

    private final String myServicePrefix;

    private File myUploadsDir;

    private File myWatchFolder;

    private URL mySolrServer;

    private final String myURLScheme;

    private final String myGoogleClientID;

    private final String myFacebookClientID;

    private final String[] myUsers;

    private final Vertx myVertx;

    /* FIXME: hard-coded for now */
    private final String[] mySubmasterFormats = new String[] { ImageFormat.TIFF_EXT, ImageFormat.TIF_EXT };

    private final Map<String, PairtreeRoot> myDataDirs = new HashMap<String, PairtreeRoot>();

    /**
     * Creates a new Jiiify configuration object, which simplifies accessing configuration information.
     *
     * @param aConfig A JSON configuration
     * @throws ConfigurationException If there is trouble reading or setting a configuration option
     */
    public Configuration(final JsonObject aConfig, final Vertx aVertx,
            final Handler<AsyncResult<Configuration>> aHandler) {
        final Future<Configuration> result = Future.future();

        myVertx = aVertx;
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

    private String[] setUsers(final JsonObject aConfig) {
        final List<?> list = aConfig.getJsonArray(OAUTH_USERS, new JsonArray()).getList();
        final String[] users = new String[list.size()];

        for (int index = 0; index < list.size(); index++) {
            users[index] = list.get(index).toString();
        }

        return users;
    }

    /**
     * Gets the users who are allowed to access the administrative side of things.
     *
     * @return
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", GOOGLE_OAUTH_CLIENT_ID);
            }

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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", FACEBOOK_OAUTH_CLIENT_ID);
            }

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
        throw new RuntimeException("Unsupported OAuth service");
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
        throw new RuntimeException("Unsupported OAuth service");
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
        return myURLScheme.equals("https");
    }

    /**
     * Returns the base URL of the Jiiify image server, including: scheme, host, and port (if something other than
     * 80).
     *
     * @return The base URL of the Jiiify image server
     */
    public String getServer() {
        return getScheme() + "://" + getHost() + (getPort() != 80 ? ":" + getPort() : "");
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
    public File getUploadsDir() {
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
        final String https = "https";
        final String http = "http";

        // We'll give command line properties first priority then fall back to our JSON configuration
        if (properties.containsKey(URL_SCHEME_PROP)) {
            final String scheme = properties.getProperty(URL_SCHEME_PROP);

            if (LOGGER.isDebugEnabled()) {
                if (scheme.equals(http)) {
                    LOGGER.debug("Found {} set in system properties as: {}", URL_SCHEME_PROP, http);
                } else if (scheme.equals(https)) {
                    LOGGER.debug("Found {} set in system properties as: {}", URL_SCHEME_PROP, https);
                }
            }

            if (!scheme.equals(http) && !scheme.equals(https)) {
                LOGGER.warn("Found {} set in system properties but its value ({}) isn't value so using: {}",
                        URL_SCHEME_PROP, scheme, https);

                return https;
            } else {
                LOGGER.info("Setting Jiiify URL scheme to: {}", scheme);
                return scheme;
            }
        } else {
            return https;
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found {} set in system properties", HTTP_HOST_PROP);
                }

                host = properties.getProperty(HTTP_HOST_PROP);
            } else if (configHost.equals(testHost)) {
                host = DEFAULT_HOST;
            } else {
                host = aConfig.getString(HTTP_HOST_PROP, DEFAULT_HOST);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn("Supplied port isn't valid so trying to use {} instead", DEFAULT_PORT);
            host = DEFAULT_HOST;
        }

        LOGGER.info("Setting Jiiify HTTP host to: {}", host);
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found {} set in system properties", HTTP_PORT_PROP);
                }

                port = Integer.parseInt(properties.getProperty(HTTP_PORT_PROP));
            } else {
                port = aConfig.getInteger(HTTP_PORT_PROP, DEFAULT_PORT);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn("Supplied port isn't valid so trying to use {} instead", DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        LOGGER.info("Setting Jiiify HTTP port to: {}", port);
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found {} set in system properties", HTTP_PORT_REDIRECT_PROP);
                }

                port = Integer.parseInt(properties.getProperty(HTTP_PORT_REDIRECT_PROP));
            } else {
                port = aConfig.getInteger(HTTP_PORT_REDIRECT_PROP, DEFAULT_REDIRECT_PORT);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn("Supplied redirect port isn't valid so trying to use {} instead", DEFAULT_REDIRECT_PORT);
            port = DEFAULT_REDIRECT_PORT;
        }

        LOGGER.info("Setting Jiiify HTTP redirect port to: {}", port);
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found {} set in system properties", TILE_SIZE_PROP);
                }

                tileSize = Integer.parseInt(properties.getProperty(TILE_SIZE_PROP));
            } else {
                tileSize = aConfig.getInteger(TILE_SIZE_PROP, DEFAULT_TILE_SIZE);
            }
        } catch (final NumberFormatException details) {
            LOGGER.warn("Supplied tile size isn't valid so trying to use {} instead", DEFAULT_TILE_SIZE);
            tileSize = DEFAULT_TILE_SIZE;
        }

        LOGGER.info("Setting Jiiify tile size to: {}", tileSize);
        return tileSize;
    }

    private String setServicePrefix(final JsonObject aConfig) {
        final Properties properties = System.getProperties();

        String prefix;

        if (properties.containsKey(SERVICE_PREFIX_PROP)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", SERVICE_PREFIX_PROP);
            }

            prefix = properties.getProperty(SERVICE_PREFIX_PROP, DEFAULT_SERVICE_PREFIX);
        } else {
            prefix = aConfig.getString(SERVICE_PREFIX_PROP, DEFAULT_SERVICE_PREFIX);
        }

        try {
            prefix = PathUtils.encodeServicePrefix(prefix.startsWith("/") ? prefix : "/" + prefix);
            LOGGER.info("Setting Jiiify service prefix to: {}", prefix);
        } catch (final URISyntaxException details) {
            LOGGER.warn("Prefix '{}' isn't valid so using '{}' instead", prefix, DEFAULT_SERVICE_PREFIX);
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

            if (LOGGER.isDebugEnabled() && properties.containsKey(SOLR_SERVER_PROP)) {
                LOGGER.debug("Found {} set in system properties", SOLR_SERVER_PROP);
            }

            try {
                // TODO: Actually ping the server here too?
                mySolrServer = new URL(solrServer);
                result.complete(this);
            } catch (final MalformedURLException details) {
                result.fail(new ConfigurationException("Solr server URL is not well-formed: " + solrServer));
            }
        } else {

        }
    }

    private void setUploadsDir(final JsonObject aConfig, final Handler<AsyncResult<Configuration>> aHandler) {
        final Properties properties = System.getProperties();
        final String defaultUploadDirPath = DEFAULT_UPLOADS_DIR.getAbsolutePath();

        // Then get the uploads directory we want to use, giving preference to system properties
        if (properties.containsKey(UPLOADS_DIR_PROP)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", UPLOADS_DIR_PROP);
            }

            confirmUploadsDir(properties.getProperty(UPLOADS_DIR_PROP, defaultUploadDirPath), aHandler);
        } else {
            confirmUploadsDir(aConfig.getString(UPLOADS_DIR_PROP, defaultUploadDirPath), aHandler);
        }
    }

    private void confirmUploadsDir(final String aDirPath, final Handler<AsyncResult<Configuration>> aHandler) {
        final Future<Configuration> result = Future.future();
        final File uploadsDir;

        if (aHandler != null) {
            result.setHandler(aHandler);

            if (aDirPath.equalsIgnoreCase("java.io.tmpdir") || aDirPath.trim().equals("")) {
                uploadsDir = DEFAULT_UPLOADS_DIR;

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Using a temporary directory {} for file uploads", uploadsDir);
                }
            } else {
                uploadsDir = new File(aDirPath);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Using a user supplied file uploads directory: {}", uploadsDir);
                }
            }

            try {
                if (!Files.createDirectories(uploadsDir.toPath()).toFile().canWrite()) {
                    result.fail(new ConfigurationException(LOGGER.getMessage(EXC_021, uploadsDir)));
                } else {
                    LOGGER.info("Setting Jiiify file uploads directory to: {}", uploadsDir);

                    myUploadsDir = uploadsDir;
                    result.complete(this);
                }
            } catch (final IOException details) {
                result.fail(new ConfigurationException(LOGGER.getMessage(EXC_022, uploadsDir)));
            }
        }
    }

    private void setDataDirs(final JsonObject aConfig, final Handler<AsyncResult<Configuration>> aHandler) {
        final Properties props = System.getProperties();
        final Future<Configuration> result = Future.future();
        final PairtreeRoot pairtree;
        final String location;

        if (aHandler != null) {
            result.setHandler(aHandler);

            if (LOGGER.isDebugEnabled() && props.containsKey(DATA_DIR_PROP)) {
                LOGGER.debug("Found {} set in system properties", DATA_DIR_PROP);
            }

            location = props.getProperty(DATA_DIR_PROP, aConfig.getString(DATA_DIR_PROP, DEFAULT_DATA_DIR
                    .getAbsolutePath()));

            // TODO: Get AWS access key and secret key from aConfig if available
            pairtree = PairtreeFactory.getFactory(myVertx).getPairtree(location);

            pairtree.create(handler -> {
                if (handler.succeeded()) {
                    LOGGER.info("Setting default Jiiify data directory to: {}", pairtree.getPath());

                    myDataDirs.put(DEFAULT_DATA_DIR_NAME, pairtree);
                    result.complete(this);
                } else {
                    LOGGER.error("Failed to set default Jiiify data directory to: {}", pairtree.getPath());
                    result.fail(handler.cause());
                }
            });
        } else {

        }
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
            result.setHandler(aHandler);

            if (LOGGER.isDebugEnabled() && properties.containsKey(WATCH_FOLDER_PROP)) {
                LOGGER.debug("Found {} set in system properties", WATCH_FOLDER_PROP);
            }

            watchFolder = Paths.get(properties.getProperty(WATCH_FOLDER_PROP, aConfig.getString(WATCH_FOLDER_PROP,
                    DEFAULT_WATCH_FOLDER.getAbsolutePath())));

            try {
                if (!Files.createDirectories(watchFolder).toFile().canWrite()) {
                    result.fail(new ConfigurationException(LOGGER.getMessage(EXC_023, watchFolder)));
                } else {
                    LOGGER.info("Setting Jiiify ingest watch folder to: {}", watchFolder);
                    myWatchFolder = watchFolder.toFile();
                    result.complete();
                }
            } catch (final IOException details) {
                result.fail(new ConfigurationException(LOGGER.getMessage(EXC_024, watchFolder)));
            }
        }
    }

}
