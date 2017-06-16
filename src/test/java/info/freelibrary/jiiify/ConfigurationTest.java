
package info.freelibrary.jiiify;

import static info.freelibrary.jiiify.Configuration.DEFAULT_PORT;
import static info.freelibrary.jiiify.Configuration.DEFAULT_UPLOADS_DIR;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_PROP;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Constants.UPLOADS_DIR_PROP;
import static java.util.UUID.randomUUID;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.jiiify.util.LoggingUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import ch.qos.logback.classic.Level;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * A test for the Jiiify configuration.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationTest {

    private final Logger LOGGER = LoggerFactory.getLogger(ConfigurationTest.class, Constants.MESSAGES);

    private static Vertx myVertx;

    @BeforeClass
    public static void before(final TestContext aContext) {
        myVertx = Vertx.vertx();
    }

    @AfterClass
    public static void after(final TestContext aContext) {
        myVertx.close(aContext.asyncAssertSuccess());
    }

    @Test
    public void testConfiguration(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
            }

            async.complete();
        });
    }

    @Test
    public void testGetPort(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject().put(HTTP_PORT_PROP, 9999), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
            } else {
                aContext.assertEquals(9999, handler.result().getPort());
            }

            async.complete();
        });
    }

    @Test
    public void testGetServicePrefix(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject().put(SERVICE_PREFIX_PROP, "/prefix"), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
            } else {
                aContext.assertEquals("/prefix", handler.result().getServicePrefix());
            }

            async.complete();
        });
    }

    @Test
    public void testGetUploadsDir(final TestContext aContext) {
        final File dir = new File("/tmp/uploads-dir-" + randomUUID());
        final Async async = aContext.async();

        new Configuration(new JsonObject().put(UPLOADS_DIR_PROP, dir.getAbsolutePath()), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
            } else {
                aContext.assertEquals(dir.getAbsolutePath(), handler.result().getUploadsDir());
            }

            async.complete();
        });
    }

    @Test
    public void testSetPortInJsonConfig(final TestContext aContext) {
        final JsonObject config = new JsonObject();
        final Async async = aContext.async();

        new Configuration(config, myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

                    method.setAccessible(true);
                    System.getProperties().remove(HTTP_PORT_PROP);
                    aContext.assertEquals(8181, method.invoke(handler.result(), config.put(HTTP_PORT_PROP, 8181)));
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetPortInSystemProperty(final TestContext aContext) {
        final JsonObject config = new JsonObject();
        final Async async = aContext.async();

        new Configuration(config, myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

                    method.setAccessible(true);
                    System.setProperty(HTTP_PORT_PROP, Integer.toString(9191));
                    aContext.assertEquals(9191, method.invoke(handler.result(), config.put(HTTP_PORT_PROP, 8181)));
                    System.clearProperty(HTTP_PORT_PROP);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetPortInSystemPropertyWithoutLogging(final TestContext aContext) {
        final JsonObject config = new JsonObject();
        final Async async = aContext.async();

        new Configuration(config, myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
                    final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);

                    method.setAccessible(true);
                    LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
                    System.setProperty(HTTP_PORT_PROP, Integer.toString(9191));
                    aContext.assertEquals(9191, method.invoke(handler.result(), config.put(HTTP_PORT_PROP, 8181)));
                    System.clearProperty(HTTP_PORT_PROP);
                    LoggingUtils.setLogLevel(Configuration.class, logLevel);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetBadPortInSystemProperty(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);
                    final JsonObject config = new JsonObject().put(HTTP_PORT_PROP, 8181);

                    method.setAccessible(true);
                    System.setProperty(HTTP_PORT_PROP, "bad_port");
                    aContext.assertEquals(DEFAULT_PORT, method.invoke(handler.result(), config));
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetBadPortInSystemPropertyWithoutLogging(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
                    final Method method = Configuration.class.getDeclaredMethod("setPort", JsonObject.class);
                    final JsonObject config = new JsonObject().put(HTTP_PORT_PROP, 8181);

                    method.setAccessible(true);

                    LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
                    System.setProperty(HTTP_PORT_PROP, "bad_port");
                    aContext.assertEquals(DEFAULT_PORT, method.invoke(handler.result(), config));
                    LoggingUtils.setLogLevel(Configuration.class, logLevel);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetServicePrefix(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);
                    final JsonObject config = new JsonObject().put(SERVICE_PREFIX_PROP, "/service");

                    method.setAccessible(true);
                    aContext.assertEquals("/service", method.invoke(handler.result(), config));
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetBadServicePrefix(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);
                    final JsonObject config = new JsonObject().put(SERVICE_PREFIX_PROP, "^service");

                    method.setAccessible(true);
                    aContext.assertEquals("/iiif", method.invoke(handler.result(), config));
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetBadServicePrefixWithoutLogging(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
                    final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);
                    final JsonObject config = new JsonObject().put(SERVICE_PREFIX_PROP, "^service");

                    method.setAccessible(true);
                    LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
                    aContext.assertEquals("/iiif", method.invoke(handler.result(), config));
                    LoggingUtils.setLogLevel(Configuration.class, logLevel);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetServicePrefixFromSystemProperty(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);
                    final JsonObject config = new JsonObject().put(SERVICE_PREFIX_PROP, "/service");

                    method.setAccessible(true);
                    System.setProperty(SERVICE_PREFIX_PROP, "/iiif-service");
                    aContext.assertEquals("/iiif-service", method.invoke(handler.result(), config));
                    System.clearProperty(SERVICE_PREFIX_PROP);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetServicePrefixFromSystemPropertyWithNoLogging(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
                    final Method method = Configuration.class.getDeclaredMethod("setServicePrefix", JsonObject.class);
                    final JsonObject config = new JsonObject().put(SERVICE_PREFIX_PROP, "/service");

                    method.setAccessible(true);
                    LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
                    System.setProperty(SERVICE_PREFIX_PROP, "/iiif-service");
                    aContext.assertEquals("/iiif-service", method.invoke(handler.result(), config));
                    System.clearProperty(SERVICE_PREFIX_PROP);
                    LoggingUtils.setLogLevel(Configuration.class, logLevel);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                } finally {
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDir(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final File dir = new File("/tmp/uploads-dir-" + randomUUID());
                    final JsonObject config = new JsonObject().put(UPLOADS_DIR_PROP, dir.getAbsolutePath());
                    final Method method = getSetUploadsDirMethod();

                    method.setAccessible(true);
                    method.invoke(handler.result(), config, (Handler<AsyncResult<Configuration>>) aResult -> {
                        if (aResult.failed()) {
                            aContext.fail(aResult.cause());
                        } else {
                            aContext.assertEquals(dir.getAbsolutePath(), aResult.result().getUploadsDir());
                        }

                        dir.delete();
                        async.complete();
                    });
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDirUsingTmpLoc(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = getSetUploadsDirMethod();
                    final JsonObject config = new JsonObject().put(UPLOADS_DIR_PROP, "java.io.tmpdir");

                    method.setAccessible(true);
                    method.invoke(handler.result(), config, (Handler<AsyncResult<Configuration>>) aResult -> {
                        if (aResult.failed()) {
                            aContext.fail(aResult.cause());
                        } else {
                            aContext.assertEquals(DEFAULT_UPLOADS_DIR, aResult.result().getUploadsDir());
                        }

                        async.complete();
                    });
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDirUsingEmptyLoc(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = getSetUploadsDirMethod();
                    final JsonObject config = new JsonObject().put(UPLOADS_DIR_PROP, "");

                    method.setAccessible(true);

                    method.invoke(handler.result(), config, (Handler<AsyncResult<Configuration>>) aResult -> {
                        if (aResult.failed()) {
                            aContext.fail(aResult.cause());
                        } else {
                            aContext.assertEquals(DEFAULT_UPLOADS_DIR, aResult.result().getUploadsDir());
                        }

                        async.complete();
                    });
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDirCannotWrite(final TestContext aContext) {
        final JsonObject config = new JsonObject();
        final Async async = aContext.async();

        new Configuration(config, myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final File file = new File("/tmp/test-file-" + randomUUID());
                    final Method method = getSetUploadsDirMethod();

                    method.setAccessible(true);
                    file.createNewFile();
                    file.setReadOnly();

                    method.invoke(handler.result(), config.put(UPLOADS_DIR_PROP, file.getAbsolutePath()),
                            (Handler<AsyncResult<Configuration>>) aResult -> {
                                if (!aResult.failed()) {
                                    aContext.fail("Failed to catch expected failure");
                                }

                                file.setWritable(true);
                                file.delete();
                                async.complete();
                            });
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException | IOException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDirDoesnotExist(final TestContext aContext) {
        final JsonObject config = new JsonObject();
        final Async async = aContext.async();

        new Configuration(config, myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final File file = new File("/this/path/does/not/exist/and/you/cannot/create/it");
                    final Method method = getSetUploadsDirMethod();

                    method.setAccessible(true);
                    method.invoke(handler.result(), config.put(UPLOADS_DIR_PROP, file.getAbsolutePath()),
                            (Handler<AsyncResult<Configuration>>) aResult -> {
                                if (!aResult.failed()) {
                                    aContext.fail("Failed to catch expected failure");
                                }

                                // It doesn't but just in case
                                if (file.exists()) {
                                    file.delete();
                                    aContext.fail(LOGGER.getMessage("Path that shouldn't exists ({}) exists, WTF?",
                                            file));
                                }

                                async.complete();
                            });
                } catch (final InvocationTargetException details) {
                    async.complete();
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDirUsingSystemProperty(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final Method method = getSetUploadsDirMethod();
                    final JsonObject config = new JsonObject().put(UPLOADS_DIR_PROP, "/tmp/test");

                    method.setAccessible(true);
                    System.setProperty(UPLOADS_DIR_PROP, "java.io.tmpdir");
                    method.invoke(handler.result(), config, (Handler<AsyncResult<Configuration>>) aResult -> {
                        if (aResult.failed()) {
                            aContext.fail(aResult.cause());
                        } else {
                            aContext.assertEquals(DEFAULT_UPLOADS_DIR, aResult.result().getUploadsDir());
                        }

                        System.clearProperty(UPLOADS_DIR_PROP);
                        async.complete();
                    });
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });

    }

    @Test
    public void testSetUploadsDirUsingSystemPropertyWithoutLogging(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
                    final Method method = getSetUploadsDirMethod();
                    final JsonObject config = new JsonObject().put(UPLOADS_DIR_PROP, "/tmp/test");

                    method.setAccessible(true);

                    LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
                    System.setProperty(UPLOADS_DIR_PROP, "java.io.tmpdir");

                    method.invoke(handler.result(), config, (Handler<AsyncResult<Configuration>>) aResult -> {
                        if (aResult.failed()) {
                            aContext.fail(aResult.cause());
                        } else {
                            aContext.assertEquals(DEFAULT_UPLOADS_DIR, aResult.result().getUploadsDir());
                        }

                        System.clearProperty(UPLOADS_DIR_PROP);
                        LoggingUtils.setLogLevel(Configuration.class, logLevel);

                        async.complete();
                    });
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    @Test
    public void testSetUploadsDirUsingSystemPropertyWithoutLoggingButPathSupplied(final TestContext aContext) {
        final Async async = aContext.async();

        new Configuration(new JsonObject(), myVertx, handler -> {
            if (handler.failed()) {
                aContext.fail(handler.cause());
                async.complete();
            } else {
                try {
                    final String logLevel = LoggingUtils.getLogLevel(Configuration.class);
                    final Method method = getSetUploadsDirMethod();
                    final JsonObject config = new JsonObject().put(UPLOADS_DIR_PROP, "/tmp/test");

                    method.setAccessible(true);

                    LoggingUtils.setLogLevel(Configuration.class, Level.OFF.levelStr);
                    System.setProperty(UPLOADS_DIR_PROP, DEFAULT_UPLOADS_DIR.toString());

                    method.invoke(handler.result(), config, (Handler<AsyncResult<Configuration>>) aResult -> {
                        if (aResult.failed()) {
                            aContext.fail(aResult.cause());
                        } else {
                            aContext.assertEquals(DEFAULT_UPLOADS_DIR, aResult.result().getUploadsDir());
                        }

                        System.clearProperty(UPLOADS_DIR_PROP);
                        LoggingUtils.setLogLevel(Configuration.class, logLevel);

                        async.complete();
                    });
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException details) {
                    aContext.fail(details);
                    async.complete();
                }
            }
        });
    }

    private Method getSetUploadsDirMethod() throws NoSuchMethodException, SecurityException {
        return Configuration.class.getDeclaredMethod("setUploadsDir", JsonObject.class, Handler.class);
    }
}
