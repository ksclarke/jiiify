
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

import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.jiiify.util.SolrUtils;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ImageIndexVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        final SolrService service = SolrService.createProxy(vertx, SOLR_SERVICE_KEY);
        final List<KeyValue<String, ?>> fields = new ArrayList<KeyValue<String, ?>>();

        // We listen for new images that we should add to our browse list
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();

            fields.add(KeyValue.with(ID_KEY, json.getString(ID_KEY)));
            fields.add(KeyValue.with("jiiify_type_s", "image"));
            fields.add(KeyValue.with("jiiify_file_name_s", new File(json.getString(FILE_PATH_KEY)).getName()));

            service.index(SolrUtils.getSimpleIndexDoc(fields), handler -> {
                if (handler.failed()) {
                    LOGGER.error("Failed submitting '{}' to Solr: {}", message.body(), handler.result());
                    message.reply(FAILURE_RESPONSE);
                } else if (handler.succeeded()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Succeeded submitting '{}' to Solr", message.body());
                    }

                    message.reply(SUCCESS_RESPONSE);
                }
            });
        }).exceptionHandler(exceptionHandler -> {
            LOGGER.error(exceptionHandler.getMessage(), exceptionHandler);
        });

        // Debugging notification that we've successfully started is handled in the JiiifyMainVerticle
        aFuture.complete();
    }

}
