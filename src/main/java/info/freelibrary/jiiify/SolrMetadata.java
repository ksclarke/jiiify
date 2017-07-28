
package info.freelibrary.jiiify;

/**
 * Defines basic metadata used in Solr.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface SolrMetadata {

    public static final String THUMBNAIL_KEY = "jiiify_thumbnail_s";

    public static final String ITEM_TYPE_KEY = "jiiify_type_s";

    public static final String FILE_NAME_KEY = "jiiify_file_name_s";

    public static final String ACTION_TYPE = "jiiify.solr.action";

    public static final String UPDATE_ACTION = "update";

    public static final String INDEX_ACTION = "index";

    public static final String SKIP_INDEXING = "skipindexing";

}
