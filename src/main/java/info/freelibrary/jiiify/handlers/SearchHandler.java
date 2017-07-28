
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;
import static info.freelibrary.jiiify.util.SolrUtils.DOCS;
import static info.freelibrary.jiiify.util.SolrUtils.RESPONSE;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.SolrMetadata;
import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.util.StringUtils;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles search requests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class SearchHandler extends JiiifyHandler {

    private final String TYPE_FIELD = "jiiify_type_s";

    /* Default Solr start */
    private final int START = 0;

    /* Default Solr rows */
    private final int COUNT = 10;

    /**
     * Creates a search handler for the administrative interface.
     *
     * @param aConfig The application's configuration
     */
    public SearchHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final SolrService service = SolrService.createProxy(aContext.vertx(), SOLR_SERVICE_KEY);
        final HttpServerRequest request = aContext.request();
        final String query = StringUtils.trimTo(request.getParam("query"), "*:*");
        final String type = StringUtils.trimTo(request.getParam("type"), "image");
        final int start = toInt(StringUtils.trimTo(request.getParam("start"), String.valueOf(START)), START);
        final int count = toInt(StringUtils.trimTo(request.getParam("count"), String.valueOf(COUNT)), COUNT);
        final JsonObject solrQuery = new JsonObject();

        solrQuery.put("query", query).put("limit", count).put("offset", start).put("filter", TYPE_FIELD + ":" + type);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_063, solrQuery);
        }

        service.search(solrQuery, handler -> {
            if (handler.succeeded()) {
                final JsonObject solrJson = handler.result();

                // Add a little additional contextual information
                solrJson.put("path", aContext.request().path());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.DBG_064, solrJson.toString());
                }

                try {
                    aContext.data().put(HBS_DATA_KEY, toHbsContext(toJsonNode(solrJson), aContext));
                    aContext.next();
                } catch (final URISyntaxException details) {
                    fail(aContext, details);
                }
            } else {
                fail(aContext, handler.cause());
                aContext.put(ERROR_HEADER, msg(MessageCodes.EXC_068));
                aContext.put(ERROR_MESSAGE, msg(MessageCodes.EXC_069, handler.cause().getMessage()));
            }
        });
    }

    /* FIXME: We need a better way to work with all this JSON -- a Solr object(?) */
    private ObjectNode toJsonNode(final JsonObject aJsonObject) throws URISyntaxException {
        final List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        final JsonObject emptyObject = new JsonObject();
        final JsonObject responseHeader = aJsonObject.getJsonObject("responseHeader", emptyObject);
        final JsonObject queryParams = responseHeader.getJsonObject("params", emptyObject);
        final JsonObject jsonParams = new JsonObject(queryParams.getString("json", "{}"));
        final JsonObject response = aJsonObject.getJsonObject(RESPONSE, emptyObject);
        final JsonArray docs = response.getJsonArray(DOCS, new JsonArray());
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();
        final ArrayNode imageArray = jsonNode.putArray("images");
        final ArrayNode pageArray = jsonNode.putArray("pages");
        final int count = jsonParams.getInteger("limit", COUNT);
        final String filter = jsonParams.getString("filter", "");
        final String query = jsonParams.getString("query", "");
        final int total = response.getInteger("numFound", 0);
        final int start = jsonParams.getInteger("offset", START);

        // Return the list of images to display on the page
        for (int index = 0; index < docs.size(); index++) {
            final JsonObject jsonObject = docs.getJsonObject(index);
            final ObjectNode objNode = mapper.createObjectNode();

            objNode.put(ID_KEY, PathUtils.encodeIdentifier(jsonObject.getString(ID_KEY)));
            objNode.put(SolrMetadata.THUMBNAIL_KEY, jsonObject.getString(SolrMetadata.THUMBNAIL_KEY));
            imageArray.add(objNode);
        }

        // Set which filter we're currently using
        if (getFilter(filter).equals("image")) {
            jsonNode.put("imageType", "yes");
            jsonNode.put("filter", "image");
        } else if (getFilter(filter).equals("manifest")) {
            jsonNode.put("manifestType", "yes");
            jsonNode.put("filter", "manifest");
        } else {
            LOGGER.debug(MessageCodes.DBG_065, filter);
        }

        jsonNode.put("total", total);
        jsonNode.put("start", start);

        if (aJsonObject.getString("path").endsWith("browse")) {
            LOGGER.debug(MessageCodes.DBG_066, query);
            jsonNode.put("browseQuery", query);
        } else {
            LOGGER.debug(MessageCodes.DBG_067, query);
            jsonNode.put("searchQuery", query.equals("*:*") ? "" : query);
        }

        // Set here so we can add selected to our count dropdown
        switch (count) {
            case 10:
                jsonNode.put("count10", count);
                break;
            case 15:
                jsonNode.put("count15", count);
                break;
            case 20:
                jsonNode.put("count20", count);
                break;
            case 50:
                jsonNode.put("count50", count);
                break;
            default:
                LOGGER.debug(MessageCodes.DBG_068, count);
        }

        // Note our count to make constructing URLs easier
        jsonNode.put("count", count);

        // We have to use two -- Handlebars' #if doesn't see prevPageNum == 0
        if (total > 0 && start - count >= 0) {
            jsonNode.put("prevPage", "yes");
            jsonNode.put("prevPageNum", start - count);
        }

        // And we'll do the same here for consistency's sake
        if (start + count < total) {
            jsonNode.put("nextPage", "yes");
            jsonNode.put("nextPageNum", start + count);
        }

        int currentPage = 0;

        // Generate our pages list for page to page navigation
        for (int index = 0, pageNumber = 0; index < total; index += count) {
            if (start >= index && start < index + count) {
                currentPage = currentPage + 1;
                LOGGER.debug(MessageCodes.DBG_069, currentPage);
            }

            pairs.add(Pair.with(Integer.valueOf(index), Integer.valueOf(++pageNumber)));
        }

        if (pairs.size() > 200) {
            final int listStart = currentPage - 100 <= 0 ? 0 : currentPage - 100;
            final int listEnd = currentPage + 100 >= pairs.size() ? pairs.size() : currentPage + 100;
            final List<Pair<Integer, Integer>> subpairs = pairs.subList(listStart, listEnd);

            for (int index = 0; index < subpairs.size(); index++) {
                final ObjectNode objNode = mapper.createObjectNode();
                final Pair<Integer, Integer> pair = pairs.get(index);

                objNode.put("page-start", pair.getValue0());
                objNode.put("page-number", pair.getValue1());
                pageArray.add(objNode);
            }
        } else {
            for (int index = 0; index < pairs.size(); index++) {
                final ObjectNode objNode = mapper.createObjectNode();
                final Pair<Integer, Integer> pair = pairs.get(index);

                objNode.put("page-start", pair.getValue0());
                objNode.put("page-number", pair.getValue1());
                pageArray.add(objNode);
            }
        }

        // Put our service prefix in so we can construct URLs with it
        jsonNode.put(fmt(SERVICE_PREFIX_PROP), myConfig.getServicePrefix());

        return jsonNode;
    }

    private String getFilter(final String aFilter) {
        final int colonIndex = aFilter.indexOf(":");
        final String filter;

        if (colonIndex != -1) {
            filter = aFilter.substring(colonIndex + 1);
        } else {
            filter = aFilter;
        }

        return filter;
    }

    private int toInt(final String aIntString, final int aDefaultInt) {
        try {
            return Integer.parseInt(aIntString);
        } catch (final NumberFormatException details) {
            LOGGER.warn(MessageCodes.WARN_013, aIntString);
            return aDefaultInt;
        }
    }

}
