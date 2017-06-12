
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.JIIIFY_TESTING;
import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.util.SolrUtils.SOLR_OK_STATUS;
import static info.freelibrary.jiiify.util.SolrUtils.SOLR_STATUS;

import info.freelibrary.jiiify.MessageCodes;
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
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class SolrServiceVerticle extends AbstractJiiifyVerticle {

    private SolrService myService;

    @Override
    public void start(final Future<Void> aFuture) throws Exception {
        final boolean inTestingMode = System.getProperty(JIIIFY_TESTING, "false").equals("true");
        final String solr = getConfig().getSolrServer().toExternalForm() + "/admin/ping?wt=json";

        if (!inTestingMode) {
            final HttpClient client = vertx.createHttpClient();
            final HttpClientRequest request;

            LOGGER.debug(MessageCodes.DBG_110, solr);

            // Create a connection to see if Solr responds at the expected location
            request = client.getAbs(solr, response -> {
                handlePingResponse(response, aFuture, client);
            }).exceptionHandler(handler -> {
                LOGGER.error(MessageCodes.EXC_058, handler.getMessage());
                client.close();
                aFuture.fail(handler.getMessage());
            });

            request.end();
        } else {
            aFuture.complete();
        }
    }

    private void handlePingResponse(final HttpClientResponse aResponse, final Future<Void> aFuture,
            final HttpClient aClient) {
        if (aResponse.statusCode() == 200) {
            aResponse.bodyHandler(body -> {
                final String status = new JsonObject(body.toString()).getString(SOLR_STATUS);

                if (status != null && status.equals(SOLR_OK_STATUS)) {
                    myService = new SolrServiceImpl(getConfig(), vertx);
                    ProxyHelper.registerService(SolrService.class, vertx, myService, SOLR_SERVICE_KEY);

                    aClient.close();
                    aFuture.complete();
                } else {
                    aClient.close();
                    aFuture.fail(LOGGER.getMessage(MessageCodes.EXC_078, status));
                }
            });
        } else {
            LOGGER.error(MessageCodes.EXC_059, aResponse.statusCode(), aResponse.statusMessage());
            aClient.close();
            aFuture.fail(aResponse.statusMessage() + " [" + aResponse.statusCode() + "]");
        }
    }
}
