
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.IIIFException;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.InvalidInfoException;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.ZipUtils;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class DownloadHandler extends JiiifyHandler {

    private static final String DOWNLOAD_SERVER_PARAM = "server";

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
                downloadObject(aContext, aContext.vertx().fileSystem(), id);
            }
        }
    }

    private void downloadObject(final RoutingContext aContext, final FileSystem aFileSystem, final String aID) {
        final String objPath = PathUtils.getObjectPath(aContext.vertx(), aID);
        final String filePath = new File(objPath, ImageInfo.FILE_NAME).getAbsolutePath();

        aFileSystem.readFile(filePath, fileHandler -> {
            if (fileHandler.succeeded()) {
                final JsonObject jsonObject = new JsonObject(fileHandler.result().toString());

                if (jsonObject.containsKey(ImageInfo.WIDTH) && jsonObject.containsKey(ImageInfo.HEIGHT)) {
                    final String servicePrefix = myConfig.getServicePrefix();
                    final int tileSize = myConfig.getTileSize();
                    final int width = jsonObject.getInteger(ImageInfo.WIDTH);
                    final int height = jsonObject.getInteger(ImageInfo.HEIGHT);

                    // Getting the list of images needed for OpenSeadragon so we can bundle them up
                    if (ImageUtils.getTilePaths(servicePrefix, aID, tileSize, width, height).stream().allMatch(
                            tilePath -> isReady(aFileSystem, tilePath, aContext.vertx(), aID))) {
                        startDownload(aContext, aFileSystem, aID);
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
                fail(aContext, fileHandler.cause());
                aContext.put(ERROR_HEADER, "Failed to read image info file");
            }
        });
    }

    private boolean isReady(final FileSystem aFileSystem, final String aPath, final Vertx aVertx, final String aID) {
        try {
            final PairtreeRoot pairtreeRoot = PathUtils.getPairtreeRoot(aVertx, aID);
            final ImageRequest request = new ImageRequest(aPath);
            final boolean isReady = aFileSystem.existsBlocking(request.getCacheFile(pairtreeRoot).getAbsolutePath());

            if (LOGGER.isDebugEnabled() && !isReady) {
                LOGGER.debug("Derivative tile is not yet ready for download: {}", aPath);
            }

            return isReady;
        } catch (final IIIFException details) {
            throw new RuntimeException(details);
        } catch (final IOException details) {
            LOGGER.error(details, details.getMessage());
            return false;
        }
    }

    private void respondNotReady(final RoutingContext aContext) {
        aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end("Not ready for download");
    }

    private void startDownload(final RoutingContext aContext, final FileSystem aFileSystem, final String aID) {
        try {
            final String objPath = PathUtils.getObjectPath(aContext.vertx(), aID);
            final File uploadsDir = myConfig.getTempDir();
            final String encodedID = PathUtils.encodeIdentifier(aID);
            final File zipFile = new File(uploadsDir, encodedID + ".zip");
            final FilenameFilter filter = new FileExtFileFilter("jpg", "tiff", "png", "webp", "pdf", "jp2", "gif");
            final String imageInfoPath = new File(objPath, ImageInfo.FILE_NAME).getAbsolutePath();
            final Buffer infoBuffer = aFileSystem.readFileBlocking(imageInfoPath);
            final ImageInfo imageInfo = new ImageInfo(new JsonObject(infoBuffer.toString()));
            final File tmpImageInfoFile = new File(new File(uploadsDir, encodedID), ImageInfo.FILE_NAME);
            final String tmpImageInfoPath = tmpImageInfoFile.getAbsolutePath();
            final String server = aContext.request().getParam(DOWNLOAD_SERVER_PARAM);
            final String tmpObjPath = tmpImageInfoFile.getParentFile().getAbsolutePath();
            final HttpServerResponse response = aContext.response();
            final MultiMap headers;

            if (!aFileSystem.existsBlocking(tmpObjPath)) {
                aFileSystem.mkdirBlocking(tmpObjPath);
            }

            imageInfo.setID(server + (server.endsWith("/") ? "" : File.separator) + aID);
            aFileSystem.writeFileBlocking(tmpImageInfoPath, Buffer.buffer(imageInfo.toString()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Zipping up an object for download: {}", objPath);
            }

            ZipUtils.zip(new File(objPath), zipFile, filter, tmpImageInfoFile);

            headers = response.headers();
            headers.add(CONTENT_TYPE, ZIP_MIME_TYPE);
            headers.add("Content-Disposition", "attachment; filename=" + encodedID + ".zip");
            headers.add("Content-Length", Long.toString(zipFile.length()));
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
