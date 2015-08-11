
package info.freelibrary.jiiify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.junit.Test;

import info.freelibrary.jiiify.util.LoggingUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import ch.qos.logback.classic.Level;
import io.vertx.core.json.JsonObject;

public class ConfigurationTest {

    private final Logger LOGGER = LoggerFactory.getLogger(ConfigurationTest.class, Constants.MESSAGES);

    @Test
    public void testConfiguration() throws ConfigurationException, IOException {
        final JsonObject config = new JsonObject();
        new Configuration(config);
    }

    @Test
    public void testGetPort() throws ConfigurationException, IOException {
        final JsonObject JsonCfgObj = new JsonObject().put(Constants.HTTP_PORT_PROP, 9999);
        final Configuration config = new Configuration(JsonCfgObj);

        assertEquals(9999, config.getPort());
    }

    @Test
    public void testGetServicePrefix() throws ConfigurationException, IOException {
        final JsonObject JsonCfgObj = new JsonObject().put(Constants.SERVICE_PREFIX_PROP, "/prefix");
        final Configuration config = new Configuration(JsonCfgObj);

        assertEquals("/prefix", config.getServicePrefix());
    }

    @Test
    public void testGetUploadsDir() throws ConfigurationException, IOException {
        final File dir = new File("/tmp/uploads-dir-" + UUID.randomUUID());
        final JsonObject JsonCfgObj = new JsonObject().put(Constants.TEMP_DIR_PROP, dir.getAbsolutePath());
        final Configuration config = new Configuration(JsonCfgObj);

        assertEquals(dir.getAbsolutePath(), config.getTempDir().getAbsolutePath());
    }

    @Test
    public void testSetPortInJsonConfig() throws ConfigurationException, IOException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

        method.setAccessible(true);
        assertEquals(8181, method.invoke(config, jsonConfig.put(Constants.HTTP_PORT_PROP, 8181)));
    }

    @Test
    public void testSetPortInSystemProperty() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

        method.setAccessible(true);
        System.setProperty(Constants.HTTP_PORT_PROP, Integer.toString(9191));
        assertEquals(9191, method.invoke(config, jsonConfig.put(Constants.HTTP_PORT_PROP, 8181)));
        System.clearProperty(Constants.HTTP_PORT_PROP);
    }

    @Test
    public void testSetPortInSystemPropertyWithoutLogging() throws ConfigurationException, IOException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
        final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

        method.setAccessible(true);
        LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
        System.setProperty(Constants.HTTP_PORT_PROP, Integer.toString(9191));
        assertEquals(9191, method.invoke(config, jsonConfig.put(Constants.HTTP_PORT_PROP, 8181)));
        System.clearProperty(Constants.HTTP_PORT_PROP);
        LoggingUtils.setLogLevel(Configuration.class, logLevel);
    }

    @Test
    public void testSetBadPortInSystemProperty() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

        method.setAccessible(true);
        System.setProperty(Constants.HTTP_PORT_PROP, "bad_port");
        assertEquals(Configuration.DEFAULT_PORT, method.invoke(config, jsonConfig.put(Constants.HTTP_PORT_PROP,
                8181)));
    }

    @Test
    public void testSetBadPortInSystemPropertyWithoutLogging() throws ConfigurationException, IOException,
            NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
        final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

        method.setAccessible(true);

        LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
        System.setProperty(Constants.HTTP_PORT_PROP, "bad_port");
        assertEquals(Configuration.DEFAULT_PORT, method.invoke(config, jsonConfig.put(Constants.HTTP_PORT_PROP,
                8181)));
        LoggingUtils.setLogLevel(Configuration.class, logLevel);
    }

    @Test
    public void testSetServicePrefix() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);

        method.setAccessible(true);
        assertEquals("/service", method.invoke(config, jsonConfig.put(Constants.SERVICE_PREFIX_PROP, "/service")));
    }

    @Test
    public void testSetBadServicePrefix() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);

        method.setAccessible(true);
        assertEquals("/iiif", method.invoke(config, jsonConfig.put(Constants.SERVICE_PREFIX_PROP, "^service")));
    }

    @Test
    public void testSetBadServicePrefixWithoutLogging() throws ConfigurationException, IOException,
            NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
        final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);

        method.setAccessible(true);
        LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
        assertEquals("/iiif", method.invoke(config, jsonConfig.put(Constants.SERVICE_PREFIX_PROP, "^service")));
        LoggingUtils.setLogLevel(Configuration.class, logLevel);
    }

    @Test
    public void testSetServicePrefixFromSystemProperty() throws ConfigurationException, IOException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);

        method.setAccessible(true);
        System.setProperty(Constants.SERVICE_PREFIX_PROP, "/iiif-service");
        assertEquals("/iiif-service", method.invoke(config, jsonConfig.put(Constants.SERVICE_PREFIX_PROP,
                "/service")));
        System.clearProperty(Constants.SERVICE_PREFIX_PROP);
    }

    @Test
    public void testSetServicePrefixFromSystemPropertyWithNoLogging() throws ConfigurationException, IOException,
            NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
        final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);

        method.setAccessible(true);
        LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
        System.setProperty(Constants.SERVICE_PREFIX_PROP, "/iiif-service");
        assertEquals("/iiif-service", method.invoke(config, jsonConfig.put(Constants.SERVICE_PREFIX_PROP,
                "/service")));
        System.clearProperty(Constants.SERVICE_PREFIX_PROP);
        LoggingUtils.setLogLevel(Configuration.class, logLevel);
    }

    @Test
    public void testSetTempDir() throws ConfigurationException, IOException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final File dir = new File("/tmp/uploads-dir-" + UUID.randomUUID());
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        assertEquals(new File(dir.getAbsolutePath()), method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP,
                dir.getAbsolutePath())));
        dir.delete();
    }

    @Test
    public void testsetTempDirUsingTmpLoc() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        assertEquals(Configuration.DEFAULT_TEMP_DIR, method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP,
                "java.io.tmpdir")));
    }

    @Test
    public void testsetTempDirUsingEmptyLoc() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        assertEquals(Configuration.DEFAULT_TEMP_DIR, method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP,
                "")));
    }

    @Test(expected = InvocationTargetException.class)
    public void testsetTempDirCannotWrite() throws ConfigurationException, IOException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final File file = new File("/tmp/test-file-" + UUID.randomUUID());
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        file.createNewFile();
        file.setReadOnly();
        method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP, file.getAbsolutePath()));
        file.setWritable(true);
        file.delete();
    }

    @Test(expected = InvocationTargetException.class)
    public void testsetTempDirDoesnotExist() throws ConfigurationException, IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final File file = new File("/this/path/does/not/exist/and/you/cannot/create/it");
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP, file.getAbsolutePath()));

        // It doesn't but just in case
        if (file.exists()) {
            file.delete();
            fail(LOGGER.getMessage("Path that shouldn't exists ({}) exists, WTF?", file));
        }
    }

    @Test
    public void testsetTempDirUsingSystemProperty() throws ConfigurationException, IOException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        System.setProperty(Constants.TEMP_DIR_PROP, "java.io.tmpdir");
        assertEquals(Configuration.DEFAULT_TEMP_DIR, method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP,
                "/tmp/test")));
        System.clearProperty(Constants.TEMP_DIR_PROP);
    }

    @Test
    public void testsetTempDirUsingSystemPropertyWithoutLogging() throws ConfigurationException, IOException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
        System.setProperty(Constants.TEMP_DIR_PROP, "java.io.tmpdir");
        assertEquals(Configuration.DEFAULT_TEMP_DIR, method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP,
                "/tmp/test")));
        System.clearProperty(Constants.TEMP_DIR_PROP);
        LoggingUtils.setLogLevel(Configuration.class, logLevel);
    }

    @Test
    public void testsetTempDirUsingSystemPropertyWithoutLoggingButPathSupplied() throws ConfigurationException,
            IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        final JsonObject jsonConfig = new JsonObject();
        final Configuration config = new Configuration(jsonConfig);
        final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
        final Method method = Configuration.class.getDeclaredMethod("setTempDir", JsonObject.class);

        method.setAccessible(true);
        LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
        System.setProperty(Constants.TEMP_DIR_PROP, Configuration.DEFAULT_TEMP_DIR.getAbsolutePath());
        assertEquals(Configuration.DEFAULT_TEMP_DIR, method.invoke(config, jsonConfig.put(Constants.TEMP_DIR_PROP,
                "/tmp/test")));
        System.clearProperty(Constants.TEMP_DIR_PROP);
        LoggingUtils.setLogLevel(Configuration.class, logLevel);
    }

}
