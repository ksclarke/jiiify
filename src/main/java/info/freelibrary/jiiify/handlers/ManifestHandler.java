
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles requests for IIIF manifests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ManifestHandler extends JiiifyHandler {

    /**
     * A IIIF manifest handler.
     *
     * @param aConfig The application's configuration
     */
    public ManifestHandler(final Configuration aConfig) {
        super(aConfig);
    }

    /**
     * Handles image info requests (mapped to: /service-prefix/[ID]/manifest).
     */
    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);
        final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);

        LOGGER.debug(MessageCodes.DBG_054, ptObj.getPath(Metadata.MANIFEST_FILE));

        // FIXME: put this centrally for all IIIF routes(?)
        response.headers().set("Access-Control-Allow-Origin", "*");

        ptObj.find(Metadata.MANIFEST_FILE, findResult -> {
            if (findResult.succeeded()) {
                if (findResult.result()) {
                    ptObj.get(Metadata.MANIFEST_FILE, getResult -> {
                        if (getResult.succeeded()) {
                            final JsonObject json = getResult.result().toJsonObject();
                            final String server = myConfig.getServer();
                            final String service = myConfig.getServicePrefix();
                            final Buffer buffer;

                            updateJsonObject(json, server, service);
                            buffer = Buffer.buffer(json.toString());

                            response.putHeader(Metadata.CONTENT_LENGTH, Integer.toString(buffer.length()));
                            response.putHeader(Metadata.CACHE_CONTROL, Metadata.DEFAULT_CACHE_CONTROL);
                            response.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);
                            response.end(buffer);
                            response.close();

                            LOGGER.debug(MessageCodes.DBG_055, request.uri());
                        } else {
                            fail(aContext, getResult.cause());
                            aContext.put(ERROR_MESSAGE, msg(MessageCodes.EXC_065, request.uri()));
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg(MessageCodes.EXC_066, request.uri()));
                }
            } else {
                fail(aContext, findResult.cause());
                aContext.put(ERROR_MESSAGE, msg(MessageCodes.EXC_065, request.uri()));
            }
        });
    }

    /* Not the way I really want to do this, but as a temporary workaround... */
    private void updateJsonObject(final JsonObject aJsonObject, final String aServer, final String aService) {
        final JsonObject defaultItem = aJsonObject.getJsonObject("default");
        final JsonObject resource = aJsonObject.getJsonObject("resource");
        final JsonObject service = aJsonObject.getJsonObject("service");
        final String thumbnail = aJsonObject.getString("thumbnail");
        final String logo = aJsonObject.getString("logo");
        final String id = aJsonObject.getString("@id");
        final String on = aJsonObject.getString("on");
        final String iiifService = aService + "/";
        final JsonArray jsonArray = new JsonArray();
        final Iterator<Object> iterator;

        if (thumbnail != null && thumbnail.contains(iiifService)) {
            aJsonObject.put("thumbnail", aServer + thumbnail.substring(thumbnail.indexOf(iiifService)));
        }

        if (logo != null) {
            try {
                aJsonObject.put("logo", new URL(aServer + new URL(logo).getPath()).toExternalForm());
            } catch (final MalformedURLException details) {
                LOGGER.error(MessageCodes.EXC_052, logo, details);
            }
        }

        if (id != null && id.contains(iiifService)) {
            aJsonObject.put("@id", aServer + id.substring(id.indexOf(iiifService)));
        }

        if (on != null && on.contains(iiifService)) {
            aJsonObject.put("on", aServer + on.substring(on.indexOf(iiifService)));
        }

        if (resource != null) {
            updateJsonObject(resource, aServer, aService);
        }

        if (service != null) {
            updateJsonObject(service, aServer, aService);
        }

        if (defaultItem != null) {
            updateJsonObject(defaultItem, aServer, aService);
        }

        jsonArray.addAll(aJsonObject.getJsonArray("sequences", new JsonArray()));
        jsonArray.addAll(aJsonObject.getJsonArray("canvases", new JsonArray()));
        jsonArray.addAll(aJsonObject.getJsonArray("images", new JsonArray()));
        jsonArray.addAll(aJsonObject.getJsonArray("item", new JsonArray()));

        iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            final Object obj = iterator.next();

            if (obj instanceof JsonObject) {
                updateJsonObject((JsonObject) obj, aServer, aService);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.DBG_056, obj.getClass().getName());
            }
        }
    }
}
