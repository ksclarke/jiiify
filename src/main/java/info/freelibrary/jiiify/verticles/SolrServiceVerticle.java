
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.util.SolrUtils.SOLR_OK_STATUS;
import static info.freelibrary.jiiify.util.SolrUtils.SOLR_STATUS;

import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.jiiify.services.impl.SolrServiceImpl;

import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * A very simple verticle that publishes the Solr service.
 */
public class SolrServiceVerticle extends AbstractJiiifyVerticle {

    private SolrService myService;

    @Override
    public void start(final Future<Void> aFuture) throws Exception {
        final String solr = getConfiguration().getSolrServer().toExternalForm() + "/admin/ping?wt=json";
        final HttpClient client = vertx.createHttpClient();
        final HttpClientRequest request;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to connect to Solr server: {}", solr);
        }

        // Create a connection to see if Solr responds at the expected location
        request = client.getAbs(solr, response -> {
            handlePingResponse(response, aFuture);
        }).exceptionHandler(exceptionHandler -> {
            LOGGER.error("Couldn't connect to Solr server: [" + exceptionHandler.getMessage() + "]");
            aFuture.fail(exceptionHandler.getMessage());
        });

        // Wrap things up...
        request.end();
        client.close();
    }

    private void handlePingResponse(final HttpClientResponse aResponse, final Future<Void> aFuture) {
        if (aResponse.statusCode() == 200) {
            aResponse.bodyHandler(body -> {
                final String status = new JsonObject(body.toString()).getString(SOLR_STATUS);

                if (status != null && status.equals(SOLR_OK_STATUS)) {
                    myService = new SolrServiceImpl(getConfiguration(), vertx);
                    ProxyHelper.registerService(SolrService.class, vertx, myService, SOLR_SERVICE_KEY);

                    aFuture.complete();
                } else {
                    aFuture.fail("Unexpected Solr server status response: " + status);
                }
            });
        } else {
            LOGGER.error("Couldn't connect to Solr server: [" + aResponse.statusCode() + ": " +
                    aResponse.statusMessage() + "]");
            aFuture.fail(aResponse.statusMessage() + " [" + aResponse.statusCode() + "]");
        }
    }
}
