
package info.freelibrary.jiiify.iiif;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ImageInfo {

    public final static String FILE_NAME = "info.json";

    public final static String WIDTH = "width";

    public final static String HEIGHT = "height";

    public final static String SCALE_FACTORS = "scaleFactors";

    public final static String ID = "@id";

    public final static String FORMATS = "formats";

    public final static String QUALITIES = "qualities";

    private final static String CONTEXT = "http://iiif.io/api/image/2/context.json";

    private static final String PROTOCOL = "http://iiif.io/api/image";

    private static final String PROFILE = "http://iiif.io/api/image/2/level0.json";

    private final Logger LOGGER = LoggerFactory.getLogger(ImageInfo.class, MESSAGES);

    private String myID;

    private int myWidth;

    private int myHeight;

    private int myTileSize;

    private int[] myScales;

    private String[] myFormats;

    private String[] myQualities;

    /**
     * Creates an image info object from the supplied image ID.
     * 
     * @param aID The image ID of the image info object
     */
    public ImageInfo(final String aID) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Constructing image info from ID: {}", aID);
        }

        setID(aID);
    }

    /**
     * Creates an image info object from a supplied JSON representation.
     * 
     * @param aJsonObject A JSON representation of the image info object
     * @throws InvalidInfoException If the JSON object isn't a valid image info object
     */
    public ImageInfo(final JsonObject aJsonObject) throws InvalidInfoException {
        final JsonArray profileArray = aJsonObject.getJsonArray("profile");
        final JsonArray tileArray = aJsonObject.getJsonArray("tiles");

        myID = aJsonObject.getString(ImageInfo.ID);

        if (aJsonObject.containsKey(ImageInfo.WIDTH)) {
            myWidth = aJsonObject.getInteger(ImageInfo.WIDTH);
        }

        if (aJsonObject.containsKey(ImageInfo.HEIGHT)) {
            myHeight = aJsonObject.getInteger(ImageInfo.HEIGHT);
        }

        if (tileArray != null) {
            final JsonObject tileObject = tileArray.getJsonObject(0);
            final JsonArray scaleArray = tileObject.getJsonArray(ImageInfo.SCALE_FACTORS);

            if (tileObject.containsKey(ImageInfo.WIDTH)) {
                myTileSize = tileObject.getInteger(ImageInfo.WIDTH);
            }

            if (scaleArray != null) {
                myScales = new int[scaleArray.size()];

                for (int index = 0; index < myScales.length; index++) {
                    myScales[index] = scaleArray.getInteger(index);
                }
            }
        }

        if (profileArray != null) {
            for (int index = 1; index < profileArray.size(); index++) {
                final JsonObject profile = profileArray.getJsonObject(index);
                final JsonArray formats = profile.getJsonArray(FORMATS);
                final JsonArray qualities = profile.getJsonArray(QUALITIES);

                if (formats != null) {
                    myFormats = new String[formats.size()];

                    for (int formatIndex = 0; formatIndex < myFormats.length; formatIndex++) {
                        myFormats[formatIndex] = formats.getString(formatIndex);
                    }
                }

                if (qualities != null) {
                    myQualities = new String[qualities.size()];

                    for (int qualityIndex = 0; qualityIndex < myQualities.length; qualityIndex++) {
                        myQualities[qualityIndex] = qualities.getString(qualityIndex);
                    }
                }
            }
        }
    }

    /**
     * Sets the ID of the image info object.
     * 
     * @param aID The ID of the image info object
     * @return The image info object with the set ID
     */
    public ImageInfo setID(final String aID) {
        myID = aID;
        return this;
    }

    /**
     * Gets the ID of the image info object.
     * 
     * @return The ID of the image info object
     */
    public String getID() {
        return myID;
    }

    /**
     * Sets the width of the image info object.
     * 
     * @param aWidth The width of the image info object
     * @return The image info object with width set
     */
    public ImageInfo setWidth(final int aWidth) {
        myWidth = aWidth;
        return this;
    }

    /**
     * Gets the width of the image info object.
     * 
     * @return The image info object's width
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Sets the height of the image info object.
     * 
     * @param aHeight The height of the image info object
     * @return The image info object with height set
     */
    public ImageInfo setHeight(final int aHeight) {
        myHeight = aHeight;
        return this;
    }

    /**
     * Gets the height of the image info object.
     * 
     * @return The image info object's height
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Sets the tile size of the image info object.
     * 
     * @param aTileSize The tile size of the image info object
     * @return The image info object with the tile size set
     */
    public ImageInfo setTileSize(final int aTileSize) {
        myTileSize = aTileSize;
        return this;
    }

    /**
     * Gets the tile size of the image info object
     * 
     * @return The image info object's tile size
     */
    public int getTileSize() {
        return myTileSize;
    }

    @Override
    public String toString() {
        final JsonObject json = new JsonObject();
        final JsonArray tiles = new JsonArray();
        final JsonObject tilesObj = new JsonObject();
        final JsonArray profile = new JsonArray();
        final JsonObject profileObj = new JsonObject();
        final JsonArray formats = new JsonArray();
        final JsonArray qualities = new JsonArray();
        final JsonArray scaleFactors = new JsonArray();
        final int longDimension = Math.max(myWidth, myHeight);

        // Profiles array
        profile.add(PROFILE);

        if (myFormats == null) {
            formats.add("jpg");
        } else {
            for (int index = 0; index < myFormats.length; index++) {
                formats.add(myFormats[index]);
            }
        }

        if (myQualities == null) {
            qualities.add("default");
        } else {
            for (int index = 0; index < myQualities.length; index++) {
                qualities.add(myQualities[index]);
            }
        }

        profileObj.put("formats", formats);
        profileObj.put("qualities", qualities);
        profile.add(profileObj);

        // Tiles array
        tilesObj.put("width", myTileSize);

        for (int multiplier = 1; multiplier * myTileSize < longDimension; multiplier *= 2) {
            scaleFactors.add(multiplier);
        }

        tilesObj.put("scaleFactors", scaleFactors);
        tiles.add(tilesObj);

        // The standard stuff
        json.put("@context", CONTEXT);
        json.put("@id", myID);
        json.put("protocol", PROTOCOL);
        json.put("width", myWidth);
        json.put("height", myHeight);

        json.put("tiles", tiles);
        json.put("profile", profile);

        return json.encodePrettily();
    }
}
