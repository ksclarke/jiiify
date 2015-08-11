package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.fail;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class ImageInfoTest {

    @Test
    public void testImageInfo() {
        System.out.println(new ImageInfo("asdf").toString());
    }

    @Test
    public void testImageInfoJsonObject() {
        try {
            final ImageInfo info = new ImageInfo(new JsonObject());
        } catch (final InvalidInfoException details) {
            fail(details.getMessage());
        }
    }

}
