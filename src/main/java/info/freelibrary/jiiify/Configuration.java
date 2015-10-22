
package info.freelibrary.jiiify;

import static info.freelibrary.jiiify.Constants.DATA_DIR_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_PROP;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Constants.SOLR_SERVER_PROP;
import static info.freelibrary.jiiify.Constants.TEMP_DIR_PROP;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;
import static info.freelibrary.jiiify.Constants.URL_SCHEME_PROP;
import static info.freelibrary.jiiify.Constants.WATCH_FOLDER_PROP;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.handlers.LoginHandler;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.PairtreeRoot;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * A developer-friendly wrapper around the default Vertx configuration JsonObject.
 *
 * @author Kevin S. Clarke <a href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>
 */
public class Configuration implements Shareable {

    public static final int DEFAULT_PORT = 8443;

    public static final String DEFAULT_HOST = "localhost";

    public static final String DEFAULT_SERVICE_PREFIX = "/iiif";

    public static final int DEFAULT_TILE_SIZE = 1024;

    public static final File DEFAULT_TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "jiiify-temp-dir");

    public static final File DEFAULT_DATA_DIR = new File("jiiify-data");

    public static final int RETRY_COUNT = 10;

    private static final String DEFAULT_DATA_DIR_NAME = "default";

    private static final String DEFAULT_SOLR_SERVER = "http://localhost:8983/solr/jiiify";

    private final Logger LOGGER = LoggerFactory.getLogger(Configuration.class, Constants.MESSAGES);

    private final int myPort;

    private final int myTileSize;

    private final String myHost;

    private final String myServicePrefix;

    private final File myTempDir;

    private final File myWatchFolder;

    private final URL mySolrServer;

    private final String myURLScheme;

    private final Map<String, PairtreeRoot> myDataDirs;

    /**
     * Creates a new Jiiify configuration object, which simplifies accessing configuration information.
     *
     * @param aConfig A JSON configuration
     * @throws ConfigurationException If there is trouble reading or setting a configuration option
     */
    public Configuration(final JsonObject aConfig) throws ConfigurationException, IOException {
        myServicePrefix = setServicePrefix(aConfig);
        myTempDir = setTempDir(aConfig);
        myPort = setPort(aConfig);
        myHost = setHost(aConfig);
        myWatchFolder = setWatchFolder(aConfig);
        myTileSize = setTileSize(aConfig);
        myDataDirs = setDataDir(aConfig);
        mySolrServer = setSolrServer(aConfig);
        myURLScheme = setURLScheme(aConfig);

        // We can add additional data directories if needed
        addAdditionalDataDirs(aConfig);
    }

    public String getOAuthClientID(final String aService) {
        final String service = aService.toLowerCase();

        if (service.equals(LoginHandler.GOOGLE)) {
            return "587760109846-8ctkp2qbuag2n7kh0lnd0vv8ur5u1os9.apps.googleusercontent.com";
        } else if (service.equals(LoginHandler.TWITTER)) {
            return "LVNtnfPxmnTvBsKm9UumVmwiS";
        } else if (service.equals(LoginHandler.FACEBOOK)) {
            return "403322423208635";
        }

        // FIXME: something better than a RuntimeException
        throw new RuntimeException("Unsupported OAuth service");
    }

    public String getOAuthClientSecretKey(final String aService) {
        final String service = aService.toLowerCase();

        if (service.equals(LoginHandler.GOOGLE)) {
            return "";
        } else if (service.equals(LoginHandler.TWITTER)) {
            return "jE8sqlZ9skUg1GtEZtu6bw1e0tmMteXHXNKbbQHAOb39yvMMAK";
        } else if (service.equals(LoginHandler.FACEBOOK)) {
            return "";
        }

        // FIXME: something better than a RuntimeException
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
     * The host name of the server.
     *
     * @return The host name of the server
     */
    public String getHost() {
        return myHost;
    }

    /**
     * The scheme the server is using (e.g., http or https).
     *
     * @return The scheme the server is using
     */
    public String getScheme() {
        return myURLScheme;
    }

    /**
     * Returns true if the server is using https; else, false.
     *
     * @return True if the server is using https; else, false
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
    public File getTempDir() {
        if (!new File(BodyHandler.DEFAULT_UPLOADS_DIRECTORY).equals(myTempDir)) {
            // Sadly, Vertx BodyHandler wants to create this directory on its construction
            if (!new File(BodyHandler.DEFAULT_UPLOADS_DIRECTORY).delete()) {
                LOGGER.error("Couldn't delete the BodyHandler default uploads directory");
            }
        }

        return myTempDir;
    }

    /**
     * Gets the default non-prefixed data directory.
     *
     * @return The default data directory
     */
    public PairtreeRoot getDataDir() {
        return myDataDirs.get(DEFAULT_DATA_DIR_NAME);
    }

    public PairtreeRoot getDataDir(final String aIDPrefix) {
        return myDataDirs.get(aIDPrefix);
    }

    private void addDataDir(final String aIDPrefix, final File aDataDir) throws IOException, ConfigurationException {
        if (!myDataDirs.containsKey(aIDPrefix)) {
            myDataDirs.put(aIDPrefix, new PairtreeRoot(aDataDir, aIDPrefix));
        } else {
            throw new ConfigurationException("Data directory with this Pairtree prefix already exists");
        }
    }

    private void addDataDir(final PairtreeRoot aPairtreeRoot) throws ConfigurationException {
        final String prefix = aPairtreeRoot.getPairtreePrefix();

        if (prefix == null) {
            throw new ConfigurationException("Data directory without a Pairtree prefix already exists");
        } else if (myDataDirs.containsKey(prefix)) {
            throw new ConfigurationException("Data directory with this Pairtree prefix already exists");
        } else {
            myDataDirs.put(prefix, aPairtreeRoot);
        }
    }

    public boolean hasPrefixedDataDirs() {
        return myDataDirs.size() > 1;
    }

    public boolean hasDataDir(final String aIDPrefix) {
        return myDataDirs.containsKey(aIDPrefix);
    }

    public String getIDPrefix(final String aID) {
        final Iterator<String> iterator = myDataDirs.keySet().iterator();

        while (iterator.hasNext()) {
            final String prefix = iterator.next();

            if (aID.startsWith(prefix)) {
                return prefix;
            }
        }

        return null;
    }

    public boolean hasIDPrefixMatch(final String aID) {
        final Iterator<String> iterator = myDataDirs.keySet().iterator();

        while (iterator.hasNext()) {
            if (aID.startsWith(iterator.next())) {
                return true;
            }
        }

        return false;
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
     * Returns true if the ingest watch folder is configured.
     *
     * @return True if the ingest watch folder is configured
     */
    public boolean hasWatchFolder() {
        return myWatchFolder != null;
    }

    private void addAdditionalDataDirs(final JsonObject aConfig) {

    }

    private String setURLScheme(final JsonObject aConfig) throws ConfigurationException {
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

    private URL setSolrServer(final JsonObject aConfig) throws ConfigurationException {
        final Properties properties = System.getProperties();
        final String solrServer;

        // We'll give command line properties first priority then fall back to our JSON configuration
        if (properties.containsKey(SOLR_SERVER_PROP)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", SOLR_SERVER_PROP);
            }

            solrServer = properties.getProperty(SOLR_SERVER_PROP);
        } else {
            solrServer = aConfig.getString(SOLR_SERVER_PROP, DEFAULT_SOLR_SERVER);
        }

        // Check that it's a proper URL; we'll let the verticle test whether it's up and functioning
        try {
            return new URL(solrServer);
        } catch (final MalformedURLException details) {
            throw new ConfigurationException("Solr server URL is not well-formed: " + solrServer);
        }
    }

    /**
     * Sets the host at which Jiiify listens.
     *
     * @param aConfig A JsonObject with configuration information
     * @throws ConfigurationException If there is trouble configuring Jiiify
     */
    private String setHost(final JsonObject aConfig) throws ConfigurationException {
        String host;

        try {
            final Properties properties = System.getProperties();

            // We'll give command line properties first priority then fall back to our JSON configuration
            if (properties.containsKey(HTTP_HOST_PROP)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found {} set in system properties", HTTP_HOST_PROP);
                }

                host = properties.getProperty(HTTP_HOST_PROP);
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
     * @throws ConfigurationException If there is trouble configuring Jiiify
     */
    private int setPort(final JsonObject aConfig) throws ConfigurationException {
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
     * Sets the default tile size.
     *
     * @param aConfig A JsonObject with configuration information
     * @throws ConfigurationException If there is trouble configuring Jiiify
     */
    private int setTileSize(final JsonObject aConfig) throws ConfigurationException {
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

    private String setServicePrefix(final JsonObject aConfig) throws ConfigurationException {
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

    private File setTempDir(final JsonObject aConfig) throws ConfigurationException {
        final Properties properties = System.getProperties();
        final String defaultUploadDirPath = DEFAULT_TEMP_DIR.getAbsolutePath();
        final File tempDir;

        // First, clean up the default uploads directory that is automatically created
        new File(BodyHandler.DEFAULT_UPLOADS_DIRECTORY).delete();

        // Then get the uploads directory we want to use
        if (properties.containsKey(TEMP_DIR_PROP)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", TEMP_DIR_PROP);
            }

            tempDir = checkTmpDir(properties.getProperty(TEMP_DIR_PROP, defaultUploadDirPath));
        } else {
            tempDir = checkTmpDir(aConfig.getString(TEMP_DIR_PROP, defaultUploadDirPath));
        }

        LOGGER.info("Setting Jiiify file uploads directory to: {}", tempDir);
        return tempDir;
    }

    private Map<String, PairtreeRoot> setDataDir(final JsonObject aConfig) throws ConfigurationException,
            IOException {
        final Properties props = System.getProperties();
        final String path = DEFAULT_DATA_DIR.getAbsolutePath();
        final Map<String, PairtreeRoot> dataDirs = new HashMap<String, PairtreeRoot>();

        if (props.containsKey(DATA_DIR_PROP)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", DATA_DIR_PROP);
            }

            dataDirs.put(DEFAULT_DATA_DIR_NAME, makePairtreeRoot(new File(props.getProperty(DATA_DIR_PROP, path))));
        } else {
            dataDirs.put(DEFAULT_DATA_DIR_NAME, makePairtreeRoot(new File(aConfig.getString(DATA_DIR_PROP, path))));
        }

        LOGGER.info("Setting Jiiify data directory to: {}", dataDirs.get(DEFAULT_DATA_DIR_NAME));
        return Collections.unmodifiableMap(dataDirs);
    }

    private PairtreeRoot makePairtreeRoot(final File aDataDir) throws ConfigurationException, IOException {
        if (aDataDir.exists()) {
            if (!aDataDir.canWrite()) {
                throw new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_035, myDataDirs.get(0)));
            }
        } else if (!aDataDir.mkdirs()) {
            throw new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_036, myDataDirs.get(0)));
        }

        return new PairtreeRoot(aDataDir);
    }

    /**
     * Sets the ingest watch folder.
     *
     * @param aConfig A JsonObject configuration
     * @throws ConfigurationException If there is trouble setting the ingest watch folder
     */
    private File setWatchFolder(final JsonObject aConfig) throws ConfigurationException {
        final Properties properties = System.getProperties();

        File watchFolder = null;

        if (properties.containsKey(Constants.WATCH_FOLDER_PROP)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found {} set in system properties", WATCH_FOLDER_PROP);
            }

            watchFolder = new File(properties.getProperty(WATCH_FOLDER_PROP));
        } else {
            final String watchFolderPath = aConfig.getString(WATCH_FOLDER_PROP);

            if (watchFolderPath != null) {
                watchFolder = new File(watchFolderPath);
            }
        }

        if (watchFolder != null) {
            if (watchFolder.exists()) {
                if (!watchFolder.canWrite()) {
                    throw new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_023, watchFolder));
                }
            } else if (!watchFolder.mkdirs()) {
                throw new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_024, watchFolder));
            }

            LOGGER.info("Setting Jiiify ingest watch folder to: {}", watchFolder);
        }

        return watchFolder;
    }

    private File checkTmpDir(final String aDirPath) throws ConfigurationException {
        File uploadsDir;

        if (aDirPath.equalsIgnoreCase("java.io.tmpdir") || aDirPath.trim().equals("")) {
            uploadsDir = DEFAULT_TEMP_DIR;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Using a temporary directory {} for file uploads", uploadsDir);
            }
        } else {
            uploadsDir = new File(aDirPath);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Using a user supplied file uploads directory: {}", uploadsDir);
            }
        }

        if (uploadsDir.exists()) {
            if (!uploadsDir.canWrite()) {
                throw new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_021, uploadsDir));
            }
        } else if (!uploadsDir.mkdirs()) {
            throw new ConfigurationException(LOGGER.getMessage(MessageCodes.EXC_022, uploadsDir));
        }

        return uploadsDir;
    }
}
