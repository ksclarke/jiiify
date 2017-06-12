
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Configuration.DEFAULT_SESSION_TIMEOUT;
import static info.freelibrary.jiiify.Constants.JCEKS_PROP;
import static info.freelibrary.jiiify.Constants.JIIIFY_CORES_PROP;
import static info.freelibrary.jiiify.Constants.JKS_PROP;
import static info.freelibrary.jiiify.Constants.KEY_PASS_PROP;
import static info.freelibrary.jiiify.Metadata.CACHE_CONTROL;
import static info.freelibrary.jiiify.Metadata.LOCATION_HEADER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.RoutePatterns;
import info.freelibrary.jiiify.handlers.DownloadHandler;
import info.freelibrary.jiiify.handlers.FailureHandler;
import info.freelibrary.jiiify.handlers.IIIFErrorHandler;
import info.freelibrary.jiiify.handlers.ImageHandler;
import info.freelibrary.jiiify.handlers.ImageInfoHandler;
import info.freelibrary.jiiify.handlers.IngestHandler;
import info.freelibrary.jiiify.handlers.ItemHandler;
import info.freelibrary.jiiify.handlers.LoginHandler;
import info.freelibrary.jiiify.handlers.LogoutHandler;
import info.freelibrary.jiiify.handlers.ManifestHandler;
import info.freelibrary.jiiify.handlers.MetricsHandler;
import info.freelibrary.jiiify.handlers.PageHandler;
import info.freelibrary.jiiify.handlers.PropertiesHandler;
import info.freelibrary.jiiify.handlers.RefreshHandler;
import info.freelibrary.jiiify.handlers.SearchHandler;
import info.freelibrary.jiiify.handlers.StatusHandler;
import info.freelibrary.jiiify.templates.HandlebarsTemplateEngine;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.TemplateEngine;

/**
 * The main Jiiify verticle that route and creates other verticles.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class JiiifyMainVerticle extends AbstractJiiifyVerticle implements RoutePatterns {

    private Configuration myConfig;

    @Override
    public void start(final Future<Void> aFuture) {
        new Configuration(config(), vertx, configHandler -> {
            if (configHandler.succeeded()) {
                myConfig = configHandler.result();

                deployJiiifyVerticles(deployHandler -> {
                    if (deployHandler.succeeded()) {
                        initializeMainVerticle(aFuture);
                    } else {
                        aFuture.fail(deployHandler.cause());
                    }
                });
            } else {
                aFuture.fail(configHandler.cause());
            }
        });
    }

    private void initializeMainVerticle(final Future<Void> aFuture) {
        final SessionHandler sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx));
        final HttpServerOptions options = new HttpServerOptions();
        final Router router = Router.router(vertx);

        // Set the port on which we want to listen for connections
        options.setPort(myConfig.getPort());
        options.setHost("0.0.0.0");
        options.setCompressionSupported(true);
        // options.setUseAlpn(true);

        if (myConfig.usesHttps()) {
            final String jksProperty = System.getProperty(JKS_PROP, JKS_PROP);
            final String ksPassword = System.getProperty(KEY_PASS_PROP, "");
            final JksOptions jksOptions = new JksOptions().setPassword(ksPassword);
            final JsonObject jceksConfig = new JsonObject();
            final File jksFile = new File(jksProperty);

            jceksConfig.put("path", JCEKS_PROP).put("type", "jceks").put("password", ksPassword);

            try {
                /* This is where "Keystore was tampered with, or password was incorrect" is thrown */
                final JWTAuth jwtAuth = JWTAuth.create(vertx, new JsonObject().put("keyStore", jceksConfig));

                // Get JKS from an external configuration file
                if (jksFile.exists()) {
                    LOGGER.info(MessageCodes.INFO_004, jksFile);
                    jksOptions.setPath(jksFile.getAbsolutePath());
                } else {
                    final InputStream inStream = getClass().getResourceAsStream("/" + jksProperty);

                    /* Get JKS configuration from a configuration file in the jar file */
                    if (inStream != null) {
                        LOGGER.debug(MessageCodes.DBG_019);
                        jksOptions.setValue(Buffer.buffer(IOUtils.readBytes(inStream)));
                    } else {
                        /* Get JKS configuration from the Maven build's target directory */
                        LOGGER.debug(MessageCodes.DBG_018, jksProperty);
                        jksOptions.setPath("target/classes/" + jksProperty);
                    }
                }

                options.setSsl(true).setKeyStoreOptions(jksOptions);
                sessionHandler.setCookieHttpOnlyFlag(true).setCookieSecureFlag(true);
                sessionHandler.setSessionTimeout(DEFAULT_SESSION_TIMEOUT);

                configureHttpRedirect(aFuture);
                configureRouter(router, sessionHandler, jwtAuth);
                startServer(router, options, aFuture);
            } catch (final RuntimeException details) {
                final Throwable cause = details.getCause();
                final String message;

                /* Let's report the underlying cause if there is one */
                if (cause != null) {
                    message = cause.getMessage();

                    /* If issue is keystore password and we're running in debug mode, log password */
                    if (message != null && message.contains("password was incorrect")) {
                        LOGGER.debug(MessageCodes.DBG_017, ksPassword);
                    }
                } else {
                    message = details.getMessage();
                }

                LOGGER.error(MessageCodes.EXC_044, message);
                aFuture.fail(message);
            } catch (final IOException details) {
                final String message = details.getMessage();

                LOGGER.error(MessageCodes.EXC_043, message);
                aFuture.fail(message);
            }
        } else {
            configureRouter(router, sessionHandler);
            startServer(router, options, aFuture);
        }
    }

    private void startServer(final Router aRouter, final HttpServerOptions aOptions, final Future<Void> aFuture) {
        vertx.createHttpServer(aOptions).requestHandler(aRouter::accept).listen(response -> {
            if (response.succeeded()) {
                LOGGER.info(MessageCodes.INFO_003, JiiifyMainVerticle.class.getSimpleName(), deploymentID());
                aFuture.complete();
            } else {
                final String message = response.cause().getMessage();

                LOGGER.error(MessageCodes.EXC_042, aOptions.getHost(), aOptions.getPort(), message);
                aFuture.fail(response.cause());
            }
        });
    }

    private void configureRouter(final Router aRouter, final SessionHandler aSessionHandler) {
        configureRouter(aRouter, aSessionHandler, null);
    }

    private void configureRouter(final Router aRouter, final SessionHandler aSessionHandler, final JWTAuth aJWTAuth) {
        final TemplateEngine templateEngine = HandlebarsTemplateEngine.create();
        final TemplateHandler templateHandler = TemplateHandler.create(templateEngine);

        // Some reused handlers
        final FailureHandler failureHandler = new FailureHandler(myConfig, templateEngine);
        final DownloadHandler downloadHandler = new DownloadHandler(myConfig);
        final SearchHandler searchHandler = new SearchHandler(myConfig);
        final IngestHandler ingestHandler = new IngestHandler(myConfig);

        // Configure some basics
        aRouter.route().handler(BodyHandler.create().setUploadsDirectory(myConfig.getUploadsDir()));
        aRouter.route().handler(CookieHandler.create());
        aRouter.route().handler(aSessionHandler);

        // Serve static files like images, scripts, css, etc.
        aRouter.getWithRegex(STATIC_FILES_RE).handler(StaticHandler.create());

        // Put everything in the administrative interface behind an authentication check
        if (aJWTAuth != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.DBG_020);
            }

            aRouter.route(ADMIN_UI).handler(UserSessionHandler.create(aJWTAuth));
            aRouter.route(ADMIN_UI).handler(JWTAuthHandler.create(aJWTAuth, LOGIN));
            aRouter.route(ADMIN_UI).handler(handler -> {
                handler.response().headers().add(CACHE_CONTROL, "no-store, no-cache");
                handler.next();
            });

            aRouter.get(LOGOUT).handler(new LogoutHandler(myConfig));
            aRouter.get(LOGIN).handler(new LoginHandler(myConfig, aJWTAuth));
            aRouter.post(LOGIN).handler(new LoginHandler(myConfig, aJWTAuth));
            aRouter.getWithRegex(LOGIN_RESPONSE_RE).handler(new LoginHandler(myConfig, aJWTAuth));
            aRouter.getWithRegex(LOGIN_RESPONSE_RE).handler(templateHandler).failureHandler(failureHandler);
        }

        // Configure our IIIF specific handlers
        aRouter.getWithRegex(iiif(IMAGE_INFO_DOC_RE)).handler(new ImageInfoHandler(myConfig));
        aRouter.getWithRegex(iiif(IMAGE_REQUEST_RE)).handler(new ImageHandler(myConfig));
        aRouter.getWithRegex(iiif(IMAGE_MANIFEST_RE)).handler(new ManifestHandler(myConfig));
        // router.getWithRegex(iiif(BASE_URI)).handler(new RedirectHandler(myConfig));
        aRouter.get(iiif(IIIF_URI)).failureHandler(new IIIFErrorHandler(myConfig));

        // Then we have the plain old administrative UI patterns
        aRouter.getWithRegex(BROWSE_RE).handler(searchHandler);
        aRouter.getWithRegex(SEARCH_RE).handler(searchHandler);
        aRouter.getWithRegex(INGEST_RE).handler(ingestHandler);
        aRouter.postWithRegex(INGEST_RE).handler(ingestHandler);
        aRouter.postWithRegex(INGEST_RE).handler(templateHandler);
        aRouter.getWithRegex(METRICS_RE).handler(new MetricsHandler(myConfig));
        aRouter.get(ITEM).handler(new ItemHandler(myConfig));
        aRouter.get(PROPERTIES).handler(new PropertiesHandler(myConfig));
        aRouter.get(REFRESH).handler(new RefreshHandler(myConfig));
        aRouter.getWithRegex(DOWNLOAD_RE).handler(downloadHandler);
        aRouter.postWithRegex(DOWNLOAD_RE).handler(downloadHandler);
        aRouter.get(ADMIN_UI).handler(templateHandler).failureHandler(failureHandler);

        // Create a index handler just to test for session; this could go in template handler
        aRouter.get(ROOT).handler(new PageHandler(myConfig));
        aRouter.get(ROOT).handler(templateHandler).failureHandler(failureHandler);

        aRouter.get(STATUS).handler(new StatusHandler(myConfig));
    }

    /**
     * Redirect all requests to the non-secure port to the secure port when there is a secure port available.
     *
     * @param aFuture A verticle future that we can fail if we can't bind to the redirect port
     */
    private void configureHttpRedirect(final Future<Void> aFuture) {
        vertx.createHttpServer().requestHandler(redirect -> {
            final HttpServerResponse response = redirect.response();
            final String httpsURL = "https://" + myConfig.getHost() + ":" + myConfig.getPort() + redirect.uri();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.DBG_016, httpsURL);
            }

            response.setStatusCode(303).putHeader(LOCATION_HEADER, httpsURL).end();
            response.close();
        }).listen(myConfig.getRedirectPort(), response -> {
            if (response.failed()) {
                if (response.cause() != null) {
                    LOGGER.error(response.cause(), response.cause().getMessage());
                }

                aFuture.fail(LOGGER.getMessage(MessageCodes.EXC_041, myConfig.getRedirectPort()));
            }
        });

        // FIXME: Accidentally connecting to HTTP port with a HTTPS connection fails badly
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=479488
    }

    /**
     * A simple convenience method for building IIIF routes.
     *
     * @param aPattern A regular expression routing pattern
     * @return The constructed path that should be used to route
     */
    private String iiif(final String aPattern) {
        return StringUtils.format(aPattern, myConfig.getServicePrefix());
    }

    /**
     * Loads the set of verticles that comprise "Jiiify". Jiiify verticles are used to create content that's then served
     * by the Jiiify handlers.
     *
     * @param aConfig A Jiiify configuration
     */
    @SuppressWarnings("rawtypes")
    private void deployJiiifyVerticles(final Handler<AsyncResult<Void>> aHandler) {
        final DeploymentOptions imgWorkerOptions = new DeploymentOptions().setWorker(true);
        final DeploymentOptions options = new DeploymentOptions();
        final List<Future> futures = new ArrayList<>();
        final Future<Void> future = Future.future();
        final int maxCores;

        if (aHandler != null) {
            future.setHandler(aHandler);

            try {
                String coreCount = System.getProperty(JIIIFY_CORES_PROP);

                if (StringUtils.trimToNull(coreCount) == null) {
                    coreCount = getCoreCount();
                }

                maxCores = Integer.parseInt(coreCount);

                imgWorkerOptions.setInstances(maxCores);
                imgWorkerOptions.setWorkerPoolSize(maxCores);
                imgWorkerOptions.setWorkerPoolName(ImageWorkerVerticle.class.getSimpleName());
            } catch (final NumberFormatException details) {
                LOGGER.error(details.getMessage(), details);
            }

            futures.add(deployVerticle(WatchFolderVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(ImageWorkerVerticle.class.getName(), imgWorkerOptions, Future.future()));
            futures.add(deployVerticle(TileMasterVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(SolrServiceVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(ImageIndexVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(ImageIngestVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(ManifestVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(ImageInfoVerticle.class.getName(), options, Future.future()));
            futures.add(deployVerticle(ImagePropertiesVerticle.class.getName(), options, Future.future()));

            // Confirm all our verticles were successfully deployed
            CompositeFuture.all(futures).setHandler(handler -> {
                if (handler.succeeded()) {
                    future.complete();
                } else {
                    future.fail(handler.cause());
                }
            });
        }
    }

    /**
     * Returns a default core count for the machine on which Jiiify is running.
     *
     * @return A default core count
     */
    private String getCoreCount() {
        final double memory = Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0 * 1024.0);
        int coreCount = Runtime.getRuntime().availableProcessors() - 1;

        // We have to have at least one core
        if (coreCount < 1) {
            coreCount = 1;
        }

        // Make sure system has resources and adjust core count if necessary
        if (memory >= 1.9) { // Give the JVM a little fudge space for -Xmx2G
            if (memory < coreCount) {
                final DecimalFormat decimalFormat = new DecimalFormat("##.#");

                decimalFormat.setRoundingMode(RoundingMode.CEILING);

                // Below should perhaps be the more conservative floor(), but accounting for JVM underestimates
                coreCount = (int) Math.ceil(memory);
                LOGGER.warn(MessageCodes.WARN_004, coreCount, decimalFormat.format(memory));
            }
        } else {
            LOGGER.warn(MessageCodes.WARN_003, memory);
        }

        return Integer.toString(coreCount);
    }

    /**
     * Deploys a particular verticle.
     *
     * @param aVerticleName The name of the verticle to deploy
     * @param aOptions Any deployment options that should be considered
     */
    private Future<Void> deployVerticle(final String aVerticleName, final DeploymentOptions aOptions,
            final Future<Void> aFuture) {
        vertx.deployVerticle(aVerticleName, aOptions, response -> {
            try {
                final String verticleName = Class.forName(aVerticleName).getSimpleName();

                if (response.succeeded()) {
                    LOGGER.debug(MessageCodes.DBG_006, verticleName, response.result());
                    aFuture.complete();
                } else {
                    LOGGER.error(MessageCodes.EXC_032, verticleName, response.cause());
                    aFuture.fail(response.cause());
                }
            } catch (final ClassNotFoundException details) {
                aFuture.fail(details);
            }
        });

        return aFuture;
    }

}
