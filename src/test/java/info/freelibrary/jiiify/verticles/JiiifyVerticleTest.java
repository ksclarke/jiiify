
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Configuration.DEFAULT_HOST;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.HTTP_PORT_PROP;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class JiiifyVerticleTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiiifyVerticleTest.class, MESSAGES);

    private static Vertx myVertx;

    private static int myPort;

    private String myDeploymentID;

    @BeforeClass
    public static void before(final TestContext aContext) {
        final DeploymentOptions options = new DeploymentOptions();
        final JsonObject config = new JsonObject();

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

        // Deploy our base verticle, which should deploy its related verticles
        // myVertx.deployVerticle(JiiifyMainVerticle.class.getName(), options.setConfig(config), aContext
        // .asyncAssertSuccess(response -> {
        // LOGGER.debug("Successfull deployed JiiifyMainVerticle");
        // }));

    }

    @AfterClass
    public static void after(final TestContext aContext) {
        final Iterator<String> idIterator = myVertx.deploymentIDs().iterator();

        if (idIterator.hasNext()) {
            myVertx.undeploy(idIterator.next(), aContext.asyncAssertSuccess());
        } else {
            aContext.async().complete();
        }

        myVertx.close();
    }

    @Test
    public void testBaseURLRedirect(final TestContext aContext) {
        // final HttpClientOptions options = new
        // HttpClientOptions().setSsl(true).setTrustAll(true).setVerifyHost(false);
        // final HttpClient client = myVertx.createHttpClient(options);
        aContext.async().complete();

        // client.getNow(myPort, "localhost", Configuration.DEFAULT_SERVICE_PREFIX + "/asdf", response -> {
        // response.bodyHandler(body -> {
        // aContext.assertEquals(response.statusCode(), 303);
        // client.close();
        // async.complete();
        // });
        // });
    }

    // @Test
    public void testImageRequest(final TestContext aContext) {
        // final String path = Configuration.DEFAULT_SERVICE_PREFIX + "/abcd1234/full/full/0/default.jpg";
        // final HttpClient client = myVertx.createHttpClient();
        // final Async async = aContext.async();
        //
        // client.getNow(myPort, "localhost", path, response -> {
        // response.handler(body -> {
        // aContext.assertEquals(response.statusCode(), 200);
        // LOGGER.debug(response.getHeader("asdf"));
        // client.close();
        // async.complete();
        // });
        // });
        final Async async = aContext.async();
        async.complete();
    }

}
