
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.CONFIG_KEY;
import static info.freelibrary.jiiify.Constants.KEY_PASS_PROP;
import static info.freelibrary.jiiify.Constants.SHARED_DATA_KEY;

import java.io.IOException;
import java.io.InputStream;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.RoutePatterns;
import info.freelibrary.jiiify.handlers.BrowseHandler;
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
import info.freelibrary.jiiify.handlers.PageHandler;
import info.freelibrary.jiiify.handlers.PropertiesHandler;
import info.freelibrary.jiiify.handlers.RedirectHandler;
import info.freelibrary.jiiify.handlers.RefreshHandler;
import info.freelibrary.jiiify.handlers.SearchHandler;
import info.freelibrary.jiiify.handlers.StatisticsHandler;
import info.freelibrary.jiiify.templates.HandlebarsTemplateEngine;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.StringUtils;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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

public class JiiifyMainVerticle extends AbstractJiiifyVerticle implements RoutePatterns {

    private Configuration myConfig;

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        final TemplateEngine templateEngine = HandlebarsTemplateEngine.create();
        final TemplateHandler templateHandler = TemplateHandler.create(templateEngine);
        final FailureHandler failureHandler = new FailureHandler(myConfig, templateEngine);
        final PageHandler genericPageHandler = new PageHandler(myConfig);
        final HttpServerOptions options = new HttpServerOptions();
        final String keyPass = System.getProperty(KEY_PASS_PROP);
        final Router router = Router.router(vertx);
        final JWTAuth jwtAuth;

        // Store our parsed configuration so we can access it when needed
        myConfig = new Configuration(config());
        vertx.sharedData().getLocalMap(SHARED_DATA_KEY).put(CONFIG_KEY, myConfig);

        // Start up Jiiify's other verticles
        deployJiiifyVerticles();

        // Set the port on which we want to listen for connections
        options.setPort(myConfig.getPort());

        // FIXME? This isn't the same, but an OK indicator of a development box?
        if (myConfig.getHost().equals("localhost")) {
            options.setHost("0.0.0.0");
        } else {
            options.setHost(myConfig.getHost());
        }

        // Give the option of using https or http -- switching between them requires re-ingesting though
        if (myConfig.usesHttps()) {
            final InputStream inStream = getClass().getResourceAsStream("/jiiify.jks");
            final JksOptions jksOptions = new JksOptions().setPassword(keyPass);

            jwtAuth = JWTAuth.create(vertx, new JsonObject().put("keyStore", new JsonObject().put("path",
                    "jiiify.jceks").put("type", "jceks").put("password", keyPass)));

            if (inStream != null) {
                jksOptions.setValue(Buffer.buffer(IOUtils.readBytes(inStream)));
            } else {
                // TODO: Make the store configurable (but keep this one around too for testing purposes)
                jksOptions.setPath("target/classes/jiiify.jks");
            }

            options.setSsl(true);
            options.setKeyStoreOptions(jksOptions);

            configureHttpRedirect(aFuture);
        } else {
            jwtAuth = null;
        }

        // Configure some basics
        router.route().handler(BodyHandler.create().setUploadsDirectory(myConfig.getTempDir().getAbsolutePath()));
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // Serve static files like images, scripts, css, etc.
        router.getWithRegex(STATIC_FILES).handler(StaticHandler.create());

        // Put everything in the administrative interface behind an authentication check
        if (jwtAuth != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Using the JWT authentication handler");
            }

            router.route().handler(UserSessionHandler.create(jwtAuth));
            router.routeWithRegex(ADMIN_UI).handler(JWTAuthHandler.create(jwtAuth, LOGIN));
        }

        // Configure our IIIF specific handlers
        router.getWithRegex(iiif(BASE_URI)).handler(new RedirectHandler(myConfig));
        router.getWithRegex(iiif(IMAGE_INFO_DOC)).handler(new ImageInfoHandler(myConfig));
        router.getWithRegex(iiif(IMAGE_REQUEST)).handler(new ImageHandler(myConfig));
        router.getWithRegex(iiif(IMAGE_MANIFEST)).handler(new ManifestHandler(myConfig));
        router.getWithRegex(iiif(IIIF_URI)).failureHandler(new IIIFErrorHandler(myConfig));

        // Then we have the plain old administrative UI patterns
        router.get(LOGIN).handler(new LoginHandler(myConfig, jwtAuth));
        router.post(LOGIN).handler(new LoginHandler(myConfig, jwtAuth));
        router.get(LOGIN_RESPONSE).handler(new LoginHandler(myConfig, jwtAuth));
        router.getWithRegex(BROWSE).handler(new BrowseHandler(myConfig));
        router.getWithRegex(SEARCH).handler(new SearchHandler(myConfig));
        router.getWithRegex(INGEST).handler(new IngestHandler(myConfig));
        router.getWithRegex(STATISTICS).handler(new StatisticsHandler(myConfig));
        router.getWithRegex(ITEM).handler(new ItemHandler(myConfig));
        router.getWithRegex(PROPERTIES).handler(new PropertiesHandler(myConfig));
        router.getWithRegex(REFRESH).handler(new RefreshHandler(myConfig));
        router.getWithRegex(DOWNLOAD).handler(new DownloadHandler(myConfig));
        router.postWithRegex(DOWNLOAD).handler(new DownloadHandler(myConfig));
        router.getWithRegex(LOGOUT).handler(new LogoutHandler(myConfig));

        router.getWithRegex(LOGIN_RESPONSE).handler(templateHandler).failureHandler(failureHandler);
        router.getWithRegex(ADMIN_UI).handler(templateHandler).failureHandler(failureHandler);

        // Create a index handler just to test for session; this could go in template handler
        router.get(ROOT).handler(genericPageHandler);
        router.get(ROOT).handler(templateHandler).failureHandler(failureHandler);

        // Start the server and start listening for connections
        vertx.createHttpServer(options).requestHandler(router::accept).listen(response -> {
            if (response.succeeded()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.DBG_006, JiiifyMainVerticle.class.getName(), deploymentID());
                }

                aFuture.complete();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.DBG_007, myConfig.getPort());
                }

                aFuture.fail(response.cause());
            }
        });
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
                LOGGER.debug("Redirecting HTTP request to: {}", httpsURL);
            }

            response.setStatusCode(303).putHeader("Location", httpsURL).end();
            response.close();
        }).listen(myConfig.getRedirectPort(), response -> {
            if (response.failed()) {
                if (response.cause() != null) {
                    LOGGER.error("{}", response.cause(), response.cause());
                }

                aFuture.fail(LOGGER.getMessage("Could not configure redirect port: {}", myConfig.getRedirectPort()));
            }
        });

        // FIXME: Accidentally connecting to http port with a https connection fails badly
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
     * Loads the set of verticles that comprise "Jiiify". Jiiify verticles are used to create content that's then
     * served by the Jiiify handlers.
     *
     * @param aConfig A Jiiify configuration
     */
    private void deployJiiifyVerticles() {
        final DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true).setMultiThreaded(true);
        final DeploymentOptions options = new DeploymentOptions();

        // Check to see whether we've configured the watch folder ingest service and start it up if so
        if (myConfig.hasWatchFolder()) {
            deployVerticle(WatchFolderVerticle.class.getName(), options);
        }

        // TODO: Perhaps some method more dynamic in the future?
        deployVerticle(ImageWorkerVerticle.class.getName(), workerOptions);
        deployVerticle(TileMasterVerticle.class.getName(), options);
        deployVerticle(SolrServiceVerticle.class.getName(), options);
        deployVerticle(ImageIndexVerticle.class.getName(), options);
        deployVerticle(ImageIngestVerticle.class.getName(), options);
        deployVerticle(ThumbnailVerticle.class.getName(), options);
        deployVerticle(ManifestVerticle.class.getName(), options);
        deployVerticle(ImageInfoVerticle.class.getName(), options);
        deployVerticle(ImagePropertiesVerticle.class.getName(), options);
    }

    /**
     * Deploys a particular verticle.
     *
     * @param aVerticleName The name of the verticle to deploy
     * @param aOptions Any deployment options that should be considered
     */
    private void deployVerticle(final String aVerticleName, final DeploymentOptions aOptions) {
        vertx.deployVerticle(aVerticleName, aOptions, response -> {
            if (response.succeeded()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Successfully deployed {} [{}]", aVerticleName, response.result());
                }
            } else {
                LOGGER.error("Failed to launch {}", aVerticleName);
            }
        });
    }

}
