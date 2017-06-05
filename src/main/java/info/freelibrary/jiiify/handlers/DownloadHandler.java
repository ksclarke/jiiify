
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Metadata.CONTENT_DISPOSITION;
import static info.freelibrary.jiiify.Metadata.CONTENT_LENGTH;
import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.HTML_MIME_TYPE;
import static info.freelibrary.jiiify.Metadata.LOCATION_HEADER;
import static info.freelibrary.jiiify.Metadata.ZIP_MIME_TYPE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.IIIFException;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.InvalidInfoException;
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

/**
 * Handler that handles downloads.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class DownloadHandler extends JiiifyHandler {

    private static final String DOWNLOAD_SERVER_PARAM = "server";

    private static final Map<String, String> ENV = new HashMap<>();
    static {
        ENV.put("create", "true");
        ENV.put("encoding", "UTF-8");
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

    @SuppressWarnings("rawtypes")
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
                    final List<String> paths = new ArrayList<>(tilePaths.size());
                    final List<Future> futures = new ArrayList<>();

                    for (final String path : tilePaths) {
                        final Future<Void> future = Future.future();

                        futures.add(future);

                        try {
                            final String resourcePath = new ImageRequest(path).getPath();

                            paths.add(resourcePath);

                            ptObj.find(resourcePath, findHandler -> {
                                if (findHandler.succeeded()) {
                                    LOGGER.debug(MessageCodes.DBG_022, resourcePath);
                                    future.complete();
                                } else {
                                    LOGGER.debug(MessageCodes.DBG_023, resourcePath);
                                    future.fail(findHandler.cause());
                                }
                            });
                        } catch (final IIIFException details) { // this shouldn't happen
                            LOGGER.error(details, details.getMessage());
                            future.fail(details);
                        }
                    }

                    final CompositeFuture future = CompositeFuture.all(futures).setHandler(futuresHandler -> {
                        if (futuresHandler.succeeded()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(MessageCodes.DBG_024, ptObj.getID());
                            }

                            try {
                                startDownload(ptObj, paths, new ImageInfo(jsonObject), aContext);
                            } catch (final InvalidInfoException details) {
                                final String message = LOGGER.getMessage(MessageCodes.INFO_005);

                                LOGGER.info(message);
                                aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end(message);
                            }
                        } else {
                            final String message = LOGGER.getMessage(MessageCodes.INFO_006);

                            LOGGER.info(message);
                            aContext.response().putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end(message);
                        }
                    });

                    LOGGER.debug(MessageCodes.DBG_025, future.size());
                } else {
                    final String message = LOGGER.getMessage(MessageCodes.EXC_046);

                    fail(aContext, new IIIFException(message));

                    aContext.put(ERROR_HEADER, MessageCodes.EXC_047);
                    aContext.put(ERROR_MESSAGE, message);
                }
            } else {
                fail(aContext, handler.cause());
                aContext.put(ERROR_HEADER, MessageCodes.EXC_047);
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
            final String id = aPtObj.getID();
            final String fileName = PathUtils.encodeIdentifier(id) + ".zip";
            final Path zipPath = Paths.get(myConfig.getUploadsDir(), fileName);
            final String server = aContext.request().getParam(DOWNLOAD_SERVER_PARAM);
            final FileSystem zipFS = FileSystems.newFileSystem(createZip(zipPath, aImageInfo, server, id), null);
            final HttpServerResponse response = aContext.response();
            final List<Future> futures = new ArrayList<>();

            // Cycle through all our tile paths and add them to the zip file
            for (final String path : aPathsList) {
                final Future<Void> future = Future.future();

                futures.add(future);

                aPtObj.get(path, handler -> {
                    if (handler.succeeded()) {
                        try {
                            Files.createDirectories(zipFS.getPath(Paths.get(path).getParent().toString()));
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

                    try {
                        zipFS.close();

                        // Set download headers so zip file will be named with the object ID
                        headers.add(CONTENT_TYPE, ZIP_MIME_TYPE);
                        headers.add(CONTENT_DISPOSITION, "attachment; filename=" + fileName);
                        headers.add(CONTENT_LENGTH, Long.toString(zipFS.supportedFileAttributeViews().size()));

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(MessageCodes.DBG_026, zipPath);
                        }

                        // And, lastly, send the zip file to our downloader
                        response.sendFile(zipPath.toString(), responseHandler -> {
                            if (responseHandler.failed()) {
                                LOGGER.error(responseHandler.cause(), responseHandler.cause().getMessage());
                            }
                        });
                    } catch (final IOException details) {
                        response.putHeader(CONTENT_TYPE, HTML_MIME_TYPE).end(details.getMessage());
                    }
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

    /**
     * @param aZipPath
     */
    private Path createZip(final Path aZipPath, final ImageInfo aImageInfo, final String aServer, final String aID)
            throws IOException {
        // Set the server in the info JSON file with information supplied via download form
        aImageInfo.setID(aServer + (aServer.endsWith("/") ? "" : File.separator) + aID);

        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(aZipPath.toFile()));
        final ZipEntry zipEntry = new ZipEntry(ImageInfo.FILE_NAME);
        final byte[] bytes = aImageInfo.toString().getBytes();

        // Have to do this the old fashioned way thanks to JDK bug (https://bugs.openjdk.java.net/browse/JDK-7156873)
        out.putNextEntry(zipEntry);
        out.write(bytes, 0, bytes.length);
        out.closeEntry();
        out.close();

        return aZipPath;
    }
}
