
package info.freelibrary.jiiify.services;

import static info.freelibrary.jiiify.Constants.CONFIG_KEY;
import static info.freelibrary.jiiify.Constants.SHARED_DATA_KEY;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.services.impl.SolrServiceImpl;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Solr service interface that is used to generate the handler, proxy code, etc.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@ProxyGen
@VertxGen
public interface SolrService {

    /**
     * Creates a service object from the {@see info.freelibrary.jiiify.services.impl.SolrServiceImpl} implementation.
     *
     * @param aVertx A reference to the Vertx object
     * @return A new Solr service object
     */
    static SolrService create(final Vertx aVertx) {
        return new SolrServiceImpl((Configuration) aVertx.sharedData().getLocalMap(SHARED_DATA_KEY).get(CONFIG_KEY),
                aVertx);
    }

    /**
     * Creates a proxy object for the Solr service.
     *
     * @param aVertx A reference to the Vertx object
     * @param aAddress A string address at which the proxy will respond
     * @return A Solr service proxy
     */
    static SolrService createProxy(final Vertx aVertx, final String aAddress) {
        return ProxyHelper.createProxy(SolrService.class, aVertx, aAddress);
    }

    /**
     * Searches Solr using the supplied JSON object for search and the handler for results.
     *
     * @param aJsonObject A Solr search configured in a JSON object
     * @param aHandler A handler to handle the results of the search
     */
    void search(JsonObject aJsonObject, Handler<AsyncResult<JsonObject>> aHandler);

    /**
     * Indexes content from the supplied JSON object in Solr.
     *
     * @param aJsonObject The information to be indexed in Solr
     * @param aHandler A handler to handle the result of the indexing
     */
    void index(JsonObject aJsonObject, Handler<AsyncResult<String>> aHandler);

}