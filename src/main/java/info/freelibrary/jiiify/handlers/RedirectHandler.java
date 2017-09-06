
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Constants.SLASH;
import static info.freelibrary.jiiify.Metadata.LOCATION_HEADER;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * A redirect handler that handles all the redirects for the Jiiify application.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class RedirectHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectHandler.class, MESSAGES);

    private String myBaseURI;

    /**
     * Creates a handler that directs redirect requests for the Jiiify application.
     *
     * @param aConfig A configuration object
     */
    public RedirectHandler(final Configuration aConfig) {
        // myBaseURI = StringUtils.format(BASE_URI, aConfig.getServicePrefix());
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response().setStatusCode(303);
        final String uri = aContext.request().uri();
        final String redirectURI;

        if (uri.matches(myBaseURI)) {
            if (uri.endsWith(SLASH)) {
                redirectURI = uri + ImageInfo.FILE_NAME;
            } else {
                redirectURI = uri + SLASH + ImageInfo.FILE_NAME;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.DBG_061, redirectURI);
            }
        } else {
            LOGGER.warn(MessageCodes.WARN_011);
            redirectURI = SLASH;
        }

        response.putHeader(LOCATION_HEADER, redirectURI).end();
    }

}
