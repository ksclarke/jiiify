
package info.freelibrary.jiiify;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import info.freelibrary.util.Logger;

/**
 * An abstract verticle test class.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public abstract class AbstractVerticleIT {

    private static final File LOGFILE = new File("target/phantomjsdriver.log");

    protected static WebDriver myWebDriver;

    protected static String myBaseURL;

    public static final String GET = "GET";

    /**
     * Check that the PhantomJS binary was installed successfully.
     */
    @BeforeClass
    public static void beforeTest() {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        final String phantomjsBinary = System.getProperty("phantomjs.binary");
        final String port = System.getProperty("jiiify.port");
        final PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();

        // Make sure our PhantomJS binary property was set properly
        assertNotNull(phantomjsBinary);
        assertTrue(new File(phantomjsBinary).exists());

        // Configure our builder so it know where to run from and write to
        builder.withLogFile(LOGFILE);
        builder.usingPhantomJSExecutable(new File(phantomjsBinary));

        // Stop our test framework from being so darn chatty as it loads... ain't nobody got time for that!
        builder.usingCommandLineArguments(new String[] { "--webdriver-loglevel=NONE" });

        if (port == null) {
            fail("System testing property 'jiiify.port' is not set");
        }

        // Configure our WebDriver to support JavaScript and be able to find the PhantomJS binary
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability("takesScreenshot", false);

        myBaseURL = "http://localhost:" + port + System.getProperty("jiiify.service.prefix");
        myWebDriver = new PhantomJSDriver(builder.build(), capabilities);

        // We'll also use plain old HttpURLConnections to check response codes since WebDriver doesn't do that
        HttpURLConnection.setFollowRedirects(false);
    }

    @AfterClass
    public static void afterTest() {
        myWebDriver.quit();
    }

    protected String getResponse(final String aURL) {
        String response;

        try {
            final HttpURLConnection http = (HttpURLConnection) new URL(aURL).openConnection();
            int responseCode;

            http.setRequestMethod("GET");
            http.connect();
            responseCode = http.getResponseCode();

            switch (responseCode) {
                case 301:
                case 302:
                case 303:
                    response = responseCode + "|" + http.getHeaderField("Location");
                    break;
                default:
                    response = Integer.toString(responseCode);
            }

            http.disconnect();
        } catch (final IOException details) {
            getLogger().error(details.getMessage(), details);
            response = details.getMessage();
        }

        return response;
    }

    abstract protected Logger getLogger();
}
