
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.javatuples.KeyValue;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.jiiify.util.SolrUtils;

import io.vertx.core.json.JsonObject;

/**
 * A verticle that indexes basic image metadata.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageIndexVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start() throws ConfigurationException, IOException {
        final SolrService service = SolrService.createProxy(vertx, SOLR_SERVICE_KEY);
        final List<KeyValue<String, ?>> fields = new ArrayList<>();

        // We listen for new images that we should add to our browse list
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();

            fields.add(KeyValue.with(ID_KEY, json.getString(ID_KEY)));
            fields.add(KeyValue.with("jiiify_type_s", "image"));
            fields.add(KeyValue.with("jiiify_file_name_s", new File(json.getString(FILE_PATH_KEY)).getName()));

            service.index(SolrUtils.getSimpleIndexDoc(fields), handler -> {
                if (handler.failed()) {
                    final String details;

                    if (handler.cause() != null) {
                        details = handler.cause().getMessage();
                    } else {
                        details = "(No details)";
                    }

                    LOGGER.error(MessageCodes.EXC_053, message.body(), details);
                    message.reply(FAILURE_RESPONSE);
                } else if (handler.succeeded()) {
                    LOGGER.debug(MessageCodes.DBG_101, message.body());
                    message.reply(SUCCESS_RESPONSE);
                }
            });
        }).exceptionHandler(exceptionHandler -> {
            LOGGER.error(exceptionHandler.getMessage(), exceptionHandler);
        });
    }

}
