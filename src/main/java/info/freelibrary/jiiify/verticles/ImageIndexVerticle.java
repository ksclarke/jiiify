
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.javatuples.KeyValue;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.SolrMetadata;
import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.jiiify.util.SolrUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * A verticle that indexes basic image metadata.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageIndexVerticle extends AbstractJiiifyVerticle implements SolrMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageIndexVerticle.class, Constants.MESSAGES);

    @Override
    public void start() throws ConfigurationException, IOException {
        final SolrService service = SolrService.createProxy(vertx, SOLR_SERVICE_KEY);
        final List<KeyValue<String, ?>> fields = new ArrayList<>();

        // We listen for new images that we should add to our browse list
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            final String id = json.getString(ID_KEY);
            final String filePath = json.getString(FILE_PATH_KEY);
            final String thumbnail = json.getString(IIIF_PATH_KEY);
            final String action = json.getString(ACTION_TYPE);

            fields.add(KeyValue.with(ID_KEY, id));
            fields.add(KeyValue.with(ITEM_TYPE_KEY, "image"));

            if (filePath != null) {
                fields.add(KeyValue.with(FILE_NAME_KEY, new File(filePath).getName()));
            }

            if (thumbnail != null) {
                fields.add(KeyValue.with(THUMBNAIL_KEY, thumbnail));
            }

            if (action.equals(SolrMetadata.INDEX_ACTION)) {
                LOGGER.debug(MessageCodes.DBG_117, id);

                service.index(SolrUtils.getSimpleIndexDoc(fields), handler -> {
                    handleResponse(handler, message);
                });
            } else if (action.equals(SolrMetadata.UPDATE_ACTION)) {
                LOGGER.debug(MessageCodes.DBG_118, id);

                service.index(SolrUtils.getSimpleUpdateDoc(fields), handler -> {
                    handleResponse(handler, message);
                });
            } else {
                LOGGER.error(MessageCodes.EXC_085, id);
            }
        }).exceptionHandler(exceptionHandler -> {
            LOGGER.error(exceptionHandler.getMessage(), exceptionHandler);
        });
    }

    private void handleResponse(final AsyncResult<String> aHandler, final Message<JsonObject> aMessage) {
        if (aHandler.succeeded()) {
            LOGGER.debug(MessageCodes.DBG_101, aMessage.body());
            aMessage.reply(SUCCESS_RESPONSE);
        } else {
            if (aHandler.cause() != null) {
                LOGGER.error(MessageCodes.EXC_053, aMessage.body(), aHandler.cause().getMessage());
            } else {
                LOGGER.error(MessageCodes.EXC_053, aMessage.body(), LOGGER.getMessage(MessageCodes.EXC_086));
            }

            aMessage.reply(FAILURE_RESPONSE);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
