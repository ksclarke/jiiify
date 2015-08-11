
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.Constants.THUMBNAIL_KEY;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;
import static info.freelibrary.jiiify.util.SolrUtils.DOCS;
import static info.freelibrary.jiiify.util.SolrUtils.RESPONSE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.services.SolrService;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class IngestHandler extends JiiifyHandler {

    public IngestHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final SolrService service = SolrService.createProxy(aContext.vertx(), SOLR_SERVICE_KEY);
        final JsonObject query = new JsonObject().put("query", "*:*");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Constructing new Solr query: {}", query);
        }

        service.search(query, handler -> {
            if (handler.succeeded()) {
                final JsonObject solrJson = handler.result();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Solr response: {}", solrJson.toString());
                }

                aContext.data().put(HBS_DATA_KEY, toHbsContext(toJsonNode(solrJson), aContext));
                aContext.next();
            } else {
                fail(aContext, handler.cause());
                aContext.put(ERROR_HEADER, "Search Error");
                aContext.put(ERROR_MESSAGE, msg("Solr search failed: {}", handler.cause().getMessage()));
            }
        });
    }

    private ObjectNode toJsonNode(final JsonObject aJsonObject) {
        final JsonObject response = aJsonObject.getJsonObject(RESPONSE, new JsonObject());
        final JsonArray docs = response.getJsonArray(DOCS, new JsonArray());
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();
        final ArrayNode arrayNode = jsonNode.putArray("images");

        for (int index = 0; index < docs.size(); index++) {
            final JsonObject jsonObject = docs.getJsonObject(index);
            final ObjectNode objNode = mapper.createObjectNode();

            objNode.put(ID_KEY, jsonObject.getString(ID_KEY));
            objNode.put(THUMBNAIL_KEY, jsonObject.getString(THUMBNAIL_KEY));
            arrayNode.add(objNode);
        }

        // Put our service prefix in so we can construct URLs with it
        jsonNode.put(fmt(SERVICE_PREFIX_PROP), myConfig.getServicePrefix());

        return jsonNode;
    }

}
