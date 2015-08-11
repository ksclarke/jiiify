
package info.freelibrary.jiiify.util;

import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.JIIIFY_ARRAY;

import java.util.List;

import org.javatuples.KeyValue;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SolrUtils {

    public static final String ADD = "add";

    public static final String SET = "set";

    public static final String DOC = "doc";

    public static final String COMMIT = "commit";

    public static final String DOCS = "docs";

    public static final String RESPONSE = "response";

    public static final String SOLR_OK_STATUS = "OK";

    public static final String SOLR_STATUS = "status";

    private SolrUtils() {
    }

    public static JsonObject getSimpleIndexDoc(final List<KeyValue<String, ?>> aKeyValues) {
        final JsonObject update = new JsonObject();
        final JsonObject add = new JsonObject();
        final JsonObject doc = new JsonObject();

        update.put(ADD, add);
        add.put(DOC, doc);

        for (final KeyValue<String, ?> keyValue : aKeyValues) {
            doc.put(keyValue.getKey(), keyValue.getValue());
        }

        return update;
    }

    public static JsonObject addSimpleIndexDoc(final JsonObject aUpdate, final List<KeyValue<String, ?>> aKeyValues) {
        final JsonObject add = new JsonObject();
        final JsonObject doc = new JsonObject();

        aUpdate.put(ADD, add);
        add.put(DOC, doc);

        for (final KeyValue<String, ?> keyValue : aKeyValues) {
            doc.put(keyValue.getKey(), keyValue.getValue());
        }

        return aUpdate;
    }

    public static JsonObject getSimpleUpdateDoc(final List<KeyValue<String, ?>> aKeyValues) {
        final JsonObject doc = new JsonObject();

        for (final KeyValue<String, ?> keyValue : aKeyValues) {
            if (keyValue.getKey().equals(ID_KEY)) {
                doc.put(ID_KEY, keyValue.getValue());
            } else {
                doc.put(keyValue.getKey(), new JsonObject().put(SET, keyValue.getValue()));
            }
        }

        return new JsonObject().put(JIIIFY_ARRAY, new JsonArray().add(doc));
    }

    public static JsonObject addCommit(final JsonObject aUpdate) {
        aUpdate.put(COMMIT, new JsonObject());
        return aUpdate;
    }

}