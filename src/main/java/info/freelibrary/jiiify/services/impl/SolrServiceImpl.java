
package info.freelibrary.jiiify.services.impl;

import static info.freelibrary.jiiify.Constants.JIIIFY_ARRAY;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

/**
 * Solr service implementation.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class SolrServiceImpl implements SolrService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrServiceImpl.class, MESSAGES);

    private static final int MAX_TRIES = 10;

    private final Configuration myConfig;

    private final Vertx myVertx;

    /**
     * Creates a implementation of the {@link info.freelibrary.jiiify.services.SolrService}.
     *
     * @param aConfig A Jiiify configuration object
     * @param aVertx A reference to the Vertx object
     */
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
            LOGGER.debug(MessageCodes.DBG_086, solr);
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
        // request.setTimeout(arg0)
        client.close();
    }

    @Override
    public void index(final JsonObject aJsonObject, final Handler<AsyncResult<String>> aHandler) {
        final String solr = myConfig.getSolrServer().toExternalForm() + "/update/json?commit=true";
        final HttpClientOptions options = new HttpClientOptions().setTcpKeepAlive(false).setMaxPoolSize(1);
        final HttpClient client = myVertx.createHttpClient(options);

        post(client, solr, aJsonObject, aHandler, 0);
    }

    private void post(final HttpClient aClient, final String aURL, final JsonObject aJsonObject,
            final Handler<AsyncResult<String>> aHandler, final int aCounter) {
        final HttpClientRequest request = aClient.postAbs(aURL, response -> {
            if (response.statusCode() == 200) {
                aClient.close();
                aHandler.handle(Future.succeededFuture());
            } else if (response.statusCode() == 503) {
                if (aCounter < MAX_TRIES) {
                    post(aClient, aURL, aJsonObject, aHandler, aCounter + 1);
                } else {
                    aClient.close();
                    aHandler.handle(Future.failedFuture(response.statusMessage()));
                }
            } else {
                aClient.close();
                aHandler.handle(Future.failedFuture(response.statusMessage()));
            }
        }).exceptionHandler(exception -> {
            LOGGER.error(exception, exception.getMessage());
            aClient.close();
            aHandler.handle(Future.failedFuture(exception));
        });

        request.putHeader(Metadata.CONTENT_TYPE, Metadata.JSON_MIME_TYPE);

        if ((aJsonObject.size() == 1) && aJsonObject.containsKey(JIIIFY_ARRAY)) {
            request.end(aJsonObject.getJsonArray(JIIIFY_ARRAY).toString());
        } else {
            request.end(aJsonObject.toString());
        }
    }
}
