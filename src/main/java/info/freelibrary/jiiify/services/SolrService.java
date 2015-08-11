
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
 */
@ProxyGen
@VertxGen
public interface SolrService {

    static SolrService create(final Vertx aVertx) {
        return new SolrServiceImpl((Configuration) aVertx.sharedData().getLocalMap(SHARED_DATA_KEY).get(CONFIG_KEY), aVertx);
    }

    static SolrService createProxy(final Vertx aVertx, final String aAddress) {
        return ProxyHelper.createProxy(SolrService.class, aVertx, aAddress);
    }

    void search(JsonObject aJsonObject, Handler<AsyncResult<JsonObject>> aHandler);

    void index(JsonObject aJsonObject, Handler<AsyncResult<String>> aHandler);

}