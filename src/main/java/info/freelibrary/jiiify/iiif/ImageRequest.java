
package info.freelibrary.jiiify.iiif;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.StringUtils;

public class ImageRequest {

    private final Logger LOGGER = LoggerFactory.getLogger(ImageRequest.class, MESSAGES);

    private final String myID;

    private final ImageRegion myRegion;

    private final ImageSize mySize;

    private final ImageRotation myRotation;

    private final ImageQuality myQuality;

    private final ImageFormat myFormat;

    private final String myServicePrefix;

    private File myCacheFile;

    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion) {
        myRegion = aRegion;
        myID = aID;
        myServicePrefix = aServicePrefix.replace("/", "");
        mySize = new ImageSize();
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion,
            final ImageSize aSize) {
        myRegion = aRegion;
        myID = aID;
        myServicePrefix = aServicePrefix.replace("/", "");
        mySize = aSize;
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    public ImageRequest(final String aID, final String aServicePrefix, final ImageSize aSize) {
        mySize = aSize;
        myID = aID;
        myServicePrefix = aServicePrefix.replace("/", "");
        myRegion = new ImageRegion();
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion,
            final ImageSize aSize, final ImageRotation aRotation, final ImageQuality aQuality,
            final ImageFormat aFormat) {
        mySize = aSize;
        myID = aID;
        myServicePrefix = aServicePrefix.replace("/", "");
        myRegion = aRegion;
        myRotation = aRotation;
        myQuality = aQuality;
        myFormat = aFormat;
    }

    /**
     * Takes a IIIF request in the form of /service-prefix/id/region/size/rotation/quality.format
     *
     * @param aIIIFImagePath
     * @throws IIIFException
     */
    public ImageRequest(final String aIIIFImagePath) throws IIIFException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Constructing image request from: {}", aIIIFImagePath);
        }

        final String[] pathComponents = aIIIFImagePath.substring(1).split("/");
        final int dotIndex = pathComponents[5].lastIndexOf(".");

        if (dotIndex == -1) {
            throw new IIIFException();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request [Prefix: {}], [ID: {}], [Region: {}], [Size: {}], [Rotation: {}], [File: {}]",
                    pathComponents[0], PairtreeUtils.decodeID(pathComponents[1]), pathComponents[2],
                    pathComponents[3], pathComponents[4], pathComponents[5]);
        }

        myServicePrefix = pathComponents[0];
        myID = PathUtils.decode(pathComponents[1]);
        myRegion = new ImageRegion(pathComponents[2]);
        mySize = new ImageSize(pathComponents[3]);
        myRotation = new ImageRotation(pathComponents[4]);
        myQuality = new ImageQuality(pathComponents[5].substring(0, dotIndex));
        myFormat = new ImageFormat(pathComponents[5].substring(dotIndex + 1));
    }

    public String getPrefix() {
        return myServicePrefix;
    }

    public String getID() {
        return myID;
    }

    public ImageRegion getRegion() {
        return myRegion;
    }

    public ImageSize getSize() {
        return mySize;
    }

    public ImageRotation getRotation() {
        return myRotation;
    }

    public ImageQuality getQuality() {
        return myQuality;
    }

    public ImageFormat getFormat() {
        return myFormat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("/").append(myServicePrefix).append('/');

        try {
            sb.append(PathUtils.encodeIdentifier(myID)).append('/').append(myRegion).append('/').append(mySize);
            sb.append('/').append(myRotation).append('/').append(myQuality).append('.').append(myFormat);
        } catch (final URISyntaxException details) {
            LOGGER.warn("Identifier contains characters invalid for a URI: {}", myID);
            sb.append(myID).append('/').append(myRegion).append('/').append(mySize);
            sb.append('/').append(myRotation).append('/').append(myQuality).append('.').append(myFormat);
        }

        return sb.toString();
    }

    public boolean hasCachedFile(final PairtreeRoot aDataDir) throws IOException {
        return getCacheFile(aDataDir).exists() && myCacheFile.length() > 0;
    }

    public File getCacheFile(final PairtreeRoot aDataDir) throws IOException {
        if (myCacheFile == null) {
            final String fileName = StringUtils.toString('.', myQuality, myFormat);
            final String objPath = PairtreeUtils.mapToPtPath(aDataDir.getAbsolutePath(), myID, myID);
            final String imagePath = StringUtils.toString(File.separatorChar, myRegion, mySize, myRotation);
            final File cacheFile = new File(objPath + File.separatorChar + imagePath + File.separatorChar + fileName);

            myCacheFile = cacheFile;
        }

        return myCacheFile;
    }
}
