
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
import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.IIIFException;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class DownloadHandler extends JiiifyHandler {

    private static final String DOWNLOAD_SERVER_PARAM = "server";

    private static final Map<String, String> ENV;
    static {
        ENV = new HashMap<String, String>();
        ENV.put("create", "true");
    }

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
                final String imageInfoString = handler.result().toString();
                final JsonObject jsonObject = new JsonObject(imageInfoString);

                if (jsonObject.containsKey(ImageInfo.WIDTH) && jsonObject.containsKey(ImageInfo.HEIGHT)) {
                    final String servicePrefix = myConfig.getServicePrefix();
                    final int tileSize = myConfig.getTileSize();
                    final int w = jsonObject.getInteger(ImageInfo.WIDTH);
                    final int h = jsonObject.getInteger(ImageInfo.HEIGHT);
                    final List<String> tilePaths = ImageUtils.getTilePaths(servicePrefix, aID, tileSize, w, h);
                    final List<Future> futures = new ArrayList<Future>();

                    for (final String path : tilePaths) {
                        final Future<Void> future = Future.future();

                        futures.add(future);

                        try {
                            ptObj.find(new ImageRequest(path).getPath(), findHandler -> {
                                if (findHandler.succeeded()) {
                                    future.complete();
                                } else {
                                    future.fail(findHandler.cause());
                                }
                            });
                        } catch (final IIIFException details) { // this shouldn't happen
                            LOGGER.error(details, details.getMessage());
                            future.fail(details);
                        }
                    }

                    CompositeFuture.all(futures).setHandler(futuresHandler -> {
                        if (futuresHandler.succeeded()) {
                            final ImageInfo imageInfo = new ImageInfo(imageInfoString);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Zipping up an object for download: {}", ptObj.getID());
                            }

                            startDownload(ptObj, tilePaths, imageInfo, aContext);
                        } else {
                            aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end("Not ready for download");
                        }
                    });
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
     * Currently all blocking code. This won't be used much, but still... FIXME.
     *
     * @param aContext A routing context
     * @param aPtObj A Pairtree object
     * @param aPathsList A list of tile paths for the supplied Pairtree object
     */
    @SuppressWarnings("rawtypes")
    private void startDownload(final PairtreeObject aPtObj, final List<String> aPathsList, final ImageInfo aImageInfo,
            final RoutingContext aContext) {
        try {
            final String encodedID = PathUtils.encodeIdentifier(aPtObj.getID());
            final Path zipFile = Paths.get(myConfig.getUploadsDir(), encodedID + ".zip");
            final FileSystem zipFS = FileSystems.newFileSystem(zipFile.toUri(), ENV);
            final String server = aContext.request().getParam(DOWNLOAD_SERVER_PARAM);
            final HttpServerResponse response = aContext.response();
            final List<Future> futures = new ArrayList<Future>();

            // Set the server in the info JSON file with information supplied via download form
            aImageInfo.setID(server + (server.endsWith("/") ? "" : File.separator) + aPtObj.getID());

            // Write ImageInfo to Zip file
            Files.write(zipFS.getPath("/", ImageInfo.FILE_NAME), aImageInfo.toString().getBytes(), CREATE_NEW);

            // Cycle through all our tile paths and add them to the zip file
            for (final String path : aPathsList) {
                final Future<Void> future = Future.future();

                futures.add(future);

                aPtObj.get(path, handler -> {
                    if (handler.succeeded()) {
                        try {
                            Files.write(zipFS.getPath("/", path), handler.result().getBytes(), CREATE_NEW);
                            future.complete();
                        } catch (final IOException details) {
                            future.fail(details);
                        }
                    } else {
                        future.fail(handler.cause());
                    }
                });
            }

            CompositeFuture.all(futures).setHandler(handler -> {
                if (handler.succeeded()) {
                    final MultiMap headers = response.headers();

                    // Set download headers so zip file will be named with the object ID
                    headers.add(CONTENT_TYPE, ZIP_MIME_TYPE);
                    headers.add("Content-Disposition", "attachment; filename=" + encodedID + ".zip");
                    headers.add("Content-Length", Long.toString(zipFS.supportedFileAttributeViews().size()));

                    // And, lastly, send the zip file to our downloader
                    response.sendFile(zipFile.toString(), responseHandler -> {
                        if (responseHandler.failed()) {
                            LOGGER.error(responseHandler.cause(), responseHandler.cause().getMessage());
                        }
                    });
                } else {
                    LOGGER.error(handler.cause(), handler.cause().getMessage());
                    response.putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end(handler.cause().getMessage());
                }
            });
        } catch (final URISyntaxException | IOException details) {
            LOGGER.error(details, details.getMessage());
            aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end(details.getMessage());
        }
    }

}
