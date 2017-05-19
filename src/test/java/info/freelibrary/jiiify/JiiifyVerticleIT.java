
package info.freelibrary.jiiify;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A test class for the Jiiify main verticle.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class JiiifyVerticleIT extends AbstractVerticleIT {

    /** Don't actually have debugging messages in the jiiify_messages bundle, but we need to use something */
    private static Logger LOGGER = LoggerFactory.getLogger(JiiifyVerticleIT.class, Constants.MESSAGES);

    // The path to a component that would only be there after a successful load of the OSD library
    // private static By OSD_XPATH = By.xpath("//*[@id='contentDiv']/div/div[1]");

    /**
     * Tests the 303 redirect on a base URL. <br/>
     * Temporarily removed @Test so Eclipse doesn't run it when doing coverage analysis
     */
    public void testBaseURL303() {
        final String url = myBaseURL + "/asdf";

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking base URL 303: {}", url);
        }

        // assertEquals("303|/iiif/asdf/info.json", getResponse(url));
    }

    // myWebDriver.navigate().to(myBaseURL);
    //
    // try {
    // final ExpectedCondition<WebElement> condition =
    // ExpectedConditions.presenceOfElementLocated(ERR_MSG_XPATH);
    // final String message = new WebDriverWait(driver, 10).until(condition).getText();
    //
    // // Look for an error message on the page and report a test failure if it's found
    // if (message.equals(IMG_LOAD_FAILURE)) {
    // fail("Failed to load image tiles");
    // } else if (LOGGER.isDebugEnabled() && !(message.trim()).equals("")) {
    // LOGGER.debug("Openseadragon console message: " + message);
    // }
    // } catch (final WebDriverException details) {
    // if (LOGGER.isDebugEnabled()) {
    // LOGGER.debug("Did not find an Openseadragon error message on page load");
    // }
    // }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}