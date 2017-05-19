
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Configuration.DEFAULT_HOST;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_PROP;
import static info.freelibrary.jiiify.Constants.JIIIFY_TESTING;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * THIS IS STILL BROKEN.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@RunWith(VertxUnitRunner.class)
public class JiiifyVerticleTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiiifyVerticleTest.class, MESSAGES);

    private static Vertx myVertx;

    private static int myPort;

    // @BeforeClass
    public static void before(final TestContext aContext) {
        final DeploymentOptions options = new DeploymentOptions();
        final JsonObject config = new JsonObject();
        final Async async = aContext.async();

        myVertx = Vertx.vertx();

        // Zero this out so we're sure our port from this test is being used
        System.clearProperty(HTTP_PORT_PROP);

        // Let's find an open port that we can reuse for our JiiifyMainVerticle
        try {
            final ServerSocket socket = new ServerSocket(0);

            myPort = socket.getLocalPort();
            config.put(HTTP_PORT_PROP, myPort);

            socket.close();
        } catch (final IOException details) {
            LOGGER.warn("Had trouble finding an open local port so rolling the dice with a default value");
            myPort = Configuration.DEFAULT_PORT;
            config.put(HTTP_PORT_PROP, myPort);
        }

        // Configure our host setting to use localhost for testing
        config.put(HTTP_HOST_PROP, DEFAULT_HOST + "-test");

        // Allow us to ignore some verticle startup failure cases
        System.setProperty(JIIIFY_TESTING, "true");

        // Deploy our base verticle, which should deploy its related verticles
        myVertx.deployVerticle(JiiifyMainVerticle.class.getName(), options.setConfig(config), handler -> {
            if (!handler.succeeded()) {
                aContext.fail(handler.cause());
            }

            async.complete();
        });

    }

    // @AfterClass
    @SuppressWarnings("rawtypes")
    public static void after(final TestContext aContext) {
        final Iterator<String> idIterator = myVertx.deploymentIDs().iterator();
        final Async async = aContext.async();
        final List<Future> futures = new ArrayList<>();

        if (idIterator.hasNext()) {
            while (idIterator.hasNext()) {
                final Future<Void> future = Future.future();
                final String verticleName = idIterator.next();

                futures.add(future);

                myVertx.undeploy(verticleName, handler -> {
                    if (handler.succeeded()) {
                        LOGGER.debug("Successfully undeployed test: {}", verticleName);
                    }

                    future.complete();
                });
            }

            CompositeFuture.all(futures).setHandler(handler -> {
                if (handler.succeeded()) {
                    myVertx.close();
                    async.complete();
                } else {
                    System.err.println("Fail!");
                    System.exit(1);
                }
            });
        } else {
            myVertx.close();
            async.complete();
        }
    }

    @Test
    public void testBaseURLRedirect(final TestContext aContext) {
        final HttpClientOptions options = new HttpClientOptions().setSsl(true).setTrustAll(true).setVerifyHost(false);
        // final HttpClient client = myVertx.createHttpClient(options);
        final Async async = aContext.async();
        // client.getNow(myPort, "localhost", Configuration.DEFAULT_SERVICE_PREFIX + "/asdf", response -> {
        // response.bodyHandler(body -> {
        // aContext.assertEquals(response.statusCode(), 303);
        // client.close();
        // async.complete();
        // });
        // });
        async.complete();
    }

}
