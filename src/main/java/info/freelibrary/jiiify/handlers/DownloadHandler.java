
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.HTML_MIME_TYPE;
import static info.freelibrary.jiiify.Metadata.LOCATION_HEADER;
import static info.freelibrary.jiiify.Metadata.ZIP_MIME_TYPE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.IIIFException;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.InvalidInfoException;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class DownloadHandler extends JiiifyHandler {

    private static final String DOWNLOAD_SERVER_PARAM = "server";

    /**
     * Creates a download handler.
     *
     * @param aConfig The application's configuration
     */
    public DownloadHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final String requestPath = aContext.request().uri();
        final String id = PathUtils.decode(requestPath.split("\\/")[4]);

        if (aContext.request().method().equals(HttpMethod.GET)) {
            final ObjectMapper objMapper = new ObjectMapper();
            final ObjectNode jsonNode = objMapper.createObjectNode();
            final String servicePrefix = myConfig.getServicePrefix();

            try {
                jsonNode.put(ID_KEY, PathUtils.encodeIdentifier(id));
                jsonNode.put(fmt(SERVICE_PREFIX_PROP), servicePrefix);
                jsonNode.put(fmt(HTTP_HOST_PROP), myConfig.getServer());

                /* To drop the ID from the path for template processing */
                aContext.data().put(HBS_PATH_SKIP_KEY, 1 + slashCount(id));
                aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
                aContext.next();
            } catch (final URISyntaxException details) {
                fail(aContext, details);
            }
        } else {
            final HttpServerRequest request = aContext.request();
            final String param = request.getParam(DOWNLOAD_SERVER_PARAM);

            if (param == null || param.trim().equals("") || !param.startsWith("http")) {
                final HttpServerResponse response = aContext.response();

                // FIXME: Quick and dirty redirect back to form for now -- add message
                response.setStatusCode(301);
                response.headers().add(LOCATION_HEADER, request.absoluteURI());
                response.end();
            } else {
                downloadObject(aContext, id);
            }
        }
    }

    private void downloadObject(final RoutingContext aContext, final String aID) {
        final PairtreeObject ptObj = myConfig.getDataDir(aID).getObject(aID);

        ptObj.get(ImageInfo.FILE_NAME, handler -> {
            if (handler.succeeded()) {
                final JsonObject jsonObject = new JsonObject(handler.result().toString());

                if (jsonObject.containsKey(ImageInfo.WIDTH) && jsonObject.containsKey(ImageInfo.HEIGHT)) {
                    final String servicePrefix = myConfig.getServicePrefix();
                    final int tileSize = myConfig.getTileSize();
                    final int w = jsonObject.getInteger(ImageInfo.WIDTH);
                    final int h = jsonObject.getInteger(ImageInfo.HEIGHT);
                    final List<String> tilePaths = ImageUtils.getTilePaths(servicePrefix, aID, tileSize, w, h);

                    // FIXME: Works fine for local file system but doubtful for s3 backed pairtree
                    if (tilePaths.stream().allMatch(tilePath -> isReady(tilePath, aID))) {
                        startDownload(aContext, ptObj, tilePaths);
                    } else {
                        respondNotReady(aContext);
                    }
                } else {
                    final String message = "Height and/or width not found in image info file";

                    fail(aContext, new IIIFException(message));
                    aContext.put(ERROR_HEADER, "Failed to read image info file");
                    aContext.put(ERROR_MESSAGE, message);
                }
            } else {
                fail(aContext, handler.cause());
                aContext.put(ERROR_HEADER, "Failed to read image info file");
            }
        });
    }

    /**
     * FIXME to do something else when working with S3.
     *
     * @param aPath A path of a tile in the Pairtree object
     * @param aID A Pairtree object ID
     * @return True if the file at the supplied path exists
     */
    private boolean isReady(final String aPath, final String aID) {
        try {
            final PairtreeObject ptObj = myConfig.getDataDir(aID).getObject(aID);
            final ImageRequest request = new ImageRequest(aPath);
            final boolean isReady = ptObj.findBlocking(request.getPath());

            if (LOGGER.isDebugEnabled() && !isReady) {
                LOGGER.debug("Derivative tile is not yet ready for download: {}", aPath);
            }

            return isReady;
        } catch (final IIIFException details) {
            throw new RuntimeException(details);
        }
    }

    private void respondNotReady(final RoutingContext aContext) {
        aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end("Not ready for download");
    }

    /**
     * Currently all blocking code. This won't be used much, but still... FIXME.
     *
     * @param aContext A routing context
     * @param aPtObj A Pairtree object
     * @param aPathsList A list of tile paths for the supplied Pairtree object
     */
    private void startDownload(final RoutingContext aContext, final PairtreeObject aPtObj,
            final List<String> aPathsList) {
        try {
            final File uploadsDir = myConfig.getUploadsDir();
            final String encodedID = PathUtils.encodeIdentifier(aPtObj.getID());
            final File zipFile = new File(uploadsDir, encodedID + ".zip");
            final ZipOutputStream zipFileStream = new ZipOutputStream(new FileOutputStream(zipFile));
            final Buffer infoBuffer = aPtObj.getBlocking(ImageInfo.FILE_NAME);
            final ImageInfo imageInfo = new ImageInfo(new JsonObject(infoBuffer.toString()));
            final String server = aContext.request().getParam(DOWNLOAD_SERVER_PARAM);
            final HttpServerResponse response = aContext.response();

            // Set the server in the info JSON file with information supplied via download form
            imageInfo.setID(server + (server.endsWith("/") ? "" : File.separator) + aPtObj.getID());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Zipping up an object for download: {}", aPtObj.getID());
            }

            // Write our new image info JSON file into our nascent zip file
            final ZipEntry infoEntry = new ZipEntry(ImageInfo.FILE_NAME);
            final byte[] infoBytes = imageInfo.toString().getBytes();

            infoEntry.setSize(infoBytes.length);
            zipFileStream.putNextEntry(infoEntry);
            zipFileStream.write(infoBytes);
            zipFileStream.closeEntry();

            // Cycle through all our tile paths and add them to the zip file
            for (final String path : aPathsList) {
                final byte[] imageBytes = aPtObj.getBlocking(path).getBytes();
                final ZipEntry imageEntry = new ZipEntry(path);

                imageEntry.setSize(imageBytes.length);
                zipFileStream.putNextEntry(imageEntry);
                zipFileStream.write(imageBytes);
                zipFileStream.closeEntry();
            }

            // Wrap up our new zip file so it can be delivered to the downloader
            zipFileStream.close();

            // Set download headers so zip file will be named with the object ID
            final MultiMap headers = response.headers();

            headers.add(CONTENT_TYPE, ZIP_MIME_TYPE);
            headers.add("Content-Disposition", "attachment; filename=" + encodedID + ".zip");
            headers.add("Content-Length", Long.toString(zipFile.length()));

            // And, lastly, send the zip file to our downloader
            response.sendFile(zipFile.getAbsolutePath(), handler -> {
                if (handler.failed()) {
                    LOGGER.error(handler.cause(), handler.cause().getMessage());
                }
            });
        } catch (final URISyntaxException | IOException | InvalidInfoException details) {
            aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end(details.getMessage());
        }
    }

}
