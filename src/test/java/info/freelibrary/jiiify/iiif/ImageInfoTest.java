
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.fail;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

/**
 * A test of {@link info.freelibrary.jiiify.iiif.ImageInfo}
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageInfoTest {

    @Test
    public void testImageInfoJsonObject() {
        try {
            final ImageInfo info = new ImageInfo(new JsonObject());
        } catch (final InvalidInfoException details) {
            fail(details.getMessage());
        }
    }

}
