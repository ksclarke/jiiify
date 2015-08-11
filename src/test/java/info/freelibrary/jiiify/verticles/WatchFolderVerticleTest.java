
package info.freelibrary.jiiify.verticles;

import java.io.File;
import java.util.Iterator;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.verticles.WatchFolderVerticle;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

//@RunWith(VertxUnitRunner.class)
public class WatchFolderVerticleTest {

    private final Logger LOGGER = LoggerFactory.getLogger(WatchFolderVerticleTest.class, Constants.MESSAGES);

    private Vertx myVertx;

    @Before
    public void before(final TestContext aContext) {
        final DeploymentOptions options = new DeploymentOptions().setWorker(true).setInstances(1);
        final File watchFolder = new File("/tmp/watch-" + UUID.randomUUID());
        final JsonObject config = new JsonObject();

        config.put(Constants.WATCH_FOLDER_PROP, watchFolder.getAbsolutePath());
        watchFolder.deleteOnExit();

        myVertx = Vertx.vertx();
        myVertx.deployVerticle(WatchFolderVerticle.class.getName(), options.setConfig(config), aContext
                .asyncAssertSuccess(response -> {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Successfully started {} [{}]", WatchFolderVerticle.class.getName(), response);
                    }
                }));
    }

    @After
    public void after(final TestContext aContext) {
        final Iterator<String> iterator = myVertx.deploymentIDs().iterator();

        while (iterator.hasNext()) {
            final String id = iterator.next();

            myVertx.undeploy(id, aContext.asyncAssertSuccess(response -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Successfully stopped {} [{}]", WatchFolderVerticle.class.getName(), id);
                }
            }));
        }
    }

    // @Test
    public void testSomethingSomething(final TestContext aContext) {
        // myVertx.eventBus()
        System.out.println("TEST!");
    }
}