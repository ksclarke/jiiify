
package info.freelibrary.jiiify;

/**
 * Defines basic metadata used in Solr.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface SolrMetadata {

    String THUMBNAIL_KEY = "jiiify_thumbnail_s";

    String ITEM_TYPE_KEY = "jiiify_type_s";

    String FILE_NAME_KEY = "jiiify_file_name_s";

    String ACTION_TYPE = "jiiify.solr.action";

    String UPDATE_ACTION = "update";

    String INDEX_ACTION = "index";

    String SKIP_INDEXING = "skipindexing";

}
