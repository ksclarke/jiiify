
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

    /**
     * Creates a IIIF image request object for a particular image region.
     * 
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aRegion An image region being requested
     */
    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion) {
        myRegion = aRegion;
        myID = aID;
        myServicePrefix = aServicePrefix.replace("/", "");
        mySize = new ImageSize();
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    /**
     * Creates a IIIF image request object for a particular image region and with a particular size.
     * 
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aRegion An image region being requested
     * @param aSize A requested image size
     */
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

    /**
     * Creates a IIIF image request object for image with a particular size.
     * 
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aSize A requested image size
     */
    public ImageRequest(final String aID, final String aServicePrefix, final ImageSize aSize) {
        mySize = aSize;
        myID = aID;
        myServicePrefix = aServicePrefix.replace("/", "");
        myRegion = new ImageRegion();
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    /**
     * Creates a IIIF image request object for a region from an image with a particular size, rotation, quality, and
     * format.
     * 
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aRegion A region from the requested image
     * @param aSize A requested image size
     * @param aRotation A rotation to perform on the requested image region
     * @param aQuality A quality of image to return
     * @param aFormat A format of image to return
     */
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

    /**
     * Gets the IIIF service prefix at which the image request was made.
     * 
     * @return The IIIF service prefix at which the image request was made
     */
    public String getPrefix() {
        return myServicePrefix;
    }

    /**
     * Gets the ID of the requested image.
     * 
     * @return The ID of the requested image
     */
    public String getID() {
        return myID;
    }

    /**
     * Gets the desired region of the requested image.
     * 
     * @return The desired region of the requested image
     */
    public ImageRegion getRegion() {
        return myRegion;
    }

    /**
     * Gets the size of the requested image.
     * 
     * @return The size of the requested image
     */
    public ImageSize getSize() {
        return mySize;
    }

    /**
     * Gets the rotation of the requested image.
     * 
     * @return The rotation of the requested image
     */
    public ImageRotation getRotation() {
        return myRotation;
    }

    /**
     * Gets the quality of the requested image.
     * 
     * @return The quality of the requested image
     */
    public ImageQuality getQuality() {
        return myQuality;
    }

    /**
     * Gets the format of the requested image.
     * 
     * @return The format of the requested image
     */
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

    /**
     * Whether the image request has a previous cached version of its output file.
     * 
     * @param aDataDir The Pairtree root at which a cached file would be found
     * @return True if the image request has a previously cached output; else, false
     * @throws IOException If there is trouble reading or checking a previously cached file
     */
    public boolean hasCachedFile(final PairtreeRoot aDataDir) throws IOException {
        return getCacheFile(aDataDir).exists() && myCacheFile.length() > 0;
    }

    /**
     * Returns a cached file for this image request.
     * 
     * @param aDataDir The Pairtree root at which a cached file would be found
     * @return A file handled for a cached output file.
     * @throws IOException If there is trouble reading from the file cache
     */
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
