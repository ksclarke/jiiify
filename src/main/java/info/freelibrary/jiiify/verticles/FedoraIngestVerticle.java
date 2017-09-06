
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IMAGE_CLEANUP_KEY;
import static info.freelibrary.jiiify.Constants.SLASH;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.TILE_SIZE_PROP;
import static info.freelibrary.jiiify.handlers.FedoraHandler.FILE_PARAM;
import static info.freelibrary.jiiify.handlers.FedoraHandler.IP_PARAM;
import static info.freelibrary.jiiify.handlers.FedoraHandler.URL_PARAM;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

/**
 * A verticle that downloads images from Fedora and queues them up for processing.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class FedoraIngestVerticle extends AbstractJiiifyVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(FedoraIngestVerticle.class, Constants.MESSAGES);

    @Override
    public void start() throws IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();

            if (json.containsKey(FILE_PARAM) && json.containsKey(URL_PARAM) && json.containsKey(IP_PARAM)) {
                final String file = json.getString(FILE_PARAM);
                final String url = json.getString(URL_PARAM);
                final String ip = json.getString(IP_PARAM);
                final Configuration config = getConfig();

                if (config.hasFedoraIP()) {
                    String ipPattern = config.getFedoraIP();

                    if (ipPattern.endsWith("*")) {
                        ipPattern = ipPattern.substring(0, ipPattern.length() - 1);
                    }

                    if (ip.startsWith(ipPattern)) {
                        final String tmpImageFilePath = config.getUploadsDir() + SLASH + file;

                        LOGGER.debug(MessageCodes.DBG_116, ip, url);

                        vertx.fileSystem().open(tmpImageFilePath, new OpenOptions(), open -> {
                            if (open.succeeded()) {
                                try {
                                    download(new URL(url), tmpImageFilePath, open);
                                } catch (final MalformedURLException details) {
                                    LOGGER.error(MessageCodes.EXC_084, url);
                                }
                            } else {
                                LOGGER.error(open.cause(), open.cause().getMessage());
                            }
                        });

                        // The message got here successfully... not whether the download, etc., succeeded
                        message.reply(SUCCESS_RESPONSE);
                    } else {
                        LOGGER.warn(MessageCodes.WARN_022, ip);
                        message.reply(FAILURE_RESPONSE);
                    }
                } else {
                    LOGGER.warn(MessageCodes.WARN_023, ip);
                    message.reply(FAILURE_RESPONSE);
                }
            } else if (json.containsKey(IP_PARAM)) {
                // FIXME: These alternative paths might be better as debug messages(?)
                LOGGER.warn(MessageCodes.WARN_024, json.getString(IP_PARAM));
                message.reply(FAILURE_RESPONSE);
            } else {
                final Logger logger = LOGGER;
                logger.warn(MessageCodes.WARN_024, logger.getMessage(MessageCodes.WARN_025));
                message.reply(FAILURE_RESPONSE);
            }
        });
    }

    private void download(final URL aSource, final String aPath, final AsyncResult<AsyncFile> aLocalFile) {
        final WebClient client = WebClient.create(vertx);
        final int port = aSource.getPort();
        final String host = aSource.getHost();
        final String path = aSource.getPath();
        final String protocol = aSource.getProtocol();
        final boolean ssl;

        switch (protocol) {
            case "https":
                ssl = true;
                break;
            default:
                ssl = false;
        }

        client.get(port, host, path).ssl(ssl).as(BodyCodec.pipe(aLocalFile.result())).send(send -> {
            if (send.succeeded()) {
                final HttpResponse<Void> response = send.result();

                if (response.statusCode() == 200) {
                    final JsonObject request = new JsonObject();
                    final String id = path.substring(path.lastIndexOf(SLASH) + 1);
                    final String tileMaster = TileMasterVerticle.class.getName();

                    if (id.length() > 0) {
                        request.put(ID_KEY, id);
                        request.put(TILE_SIZE_PROP, getConfig().getTileSize());
                        request.put(FILE_PATH_KEY, aPath);
                        request.put(IMAGE_CLEANUP_KEY, true);

                        sendMessage(request, tileMaster, INGEST_TIMEOUT);
                    } else {
                        LOGGER.error(MessageCodes.EXC_082, aSource);
                    }
                } else {
                    LOGGER.error(MessageCodes.EXC_081, response.statusMessage());
                }
            } else {
                LOGGER.error(send.cause(), send.cause().getMessage());
            }
        });
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
