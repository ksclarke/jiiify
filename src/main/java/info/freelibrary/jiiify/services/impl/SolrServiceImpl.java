
package info.freelibrary.jiiify.services.impl;

import static info.freelibrary.jiiify.Constants.JIIIFY_ARRAY;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

/**
 * Solr service implementation.
 */
public class SolrServiceImpl implements SolrService {

    private final Logger LOGGER = LoggerFactory.getLogger(SolrServiceImpl.class, MESSAGES);

    private final Configuration myConfig;

    private final Vertx myVertx;

    public SolrServiceImpl(final Configuration aConfig, final Vertx aVertx) {
        myConfig = aConfig;
        myVertx = aVertx;
    }

    @Override
    public void search(final JsonObject aJsonObject, final Handler<AsyncResult<JsonObject>> aHandler) {
        final String solr = myConfig.getSolrServer().toExternalForm() + "/query";
        final HttpClient client = myVertx.createHttpClient();
        final HttpClientRequest request;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending Solr query to: {}", solr);
        }

        request = client.getAbs(solr, response -> {
            if (response.statusCode() == 200) {
                response.bodyHandler(body -> {
                    aHandler.handle(Future.succeededFuture(new JsonObject(body.toString())));
                });
            } else {
                aHandler.handle(Future.failedFuture(response.statusMessage()));
            }
        }).exceptionHandler(exceptionHandler -> {
            aHandler.handle(Future.failedFuture(exceptionHandler));
        });

        request.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);
        request.end(aJsonObject.toString());
        client.close();
    }

    @Override
    public void index(final JsonObject aJsonObject, final Handler<AsyncResult<String>> aHandler) {
        final String solr = myConfig.getSolrServer().toExternalForm() + "/update/json?commit=true";
        final HttpClient client = myVertx.createHttpClient();
        final HttpClientRequest request;

        request = client.postAbs(solr, response -> {
            if (response.statusCode() == 200) {
                aHandler.handle(Future.succeededFuture());
            } else {
                aHandler.handle(Future.failedFuture(response.statusMessage()));
            }
        }).exceptionHandler(exceptionHandler -> {
            aHandler.handle(Future.failedFuture(exceptionHandler));
        });

        request.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);

        if (aJsonObject.size() == 1 && aJsonObject.containsKey(JIIIFY_ARRAY)) {
            request.end(aJsonObject.getJsonArray(JIIIFY_ARRAY).toString());
        } else {
            request.end(aJsonObject.toString());
        }

        client.close();
    }

}
