
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.RoutePatterns.BASE_URI;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * A redirect handler that handles all the redirects for the Jiiify application.
 *
 * @author Kevin S. Clarke <a href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>
 */
public class RedirectHandler implements Handler<RoutingContext> {

    private final Logger LOGGER = LoggerFactory.getLogger(RedirectHandler.class, MESSAGES);

    private final String myBaseURI;

    /**
     * Creates a handler that directs redirect requests for the Jiiify application.
     *
     * @param aConfig A configuration object
     */
    public RedirectHandler(final Configuration aConfig) {
        myBaseURI = StringUtils.format(BASE_URI, aConfig.getServicePrefix());
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response().setStatusCode(303);
        final String uri = aContext.request().uri();
        final String redirectURI;

        if (uri.matches(myBaseURI)) {
            if (uri.endsWith("/")) {
                redirectURI = uri + ImageInfo.FILE_NAME;
            } else {
                redirectURI = uri + "/" + ImageInfo.FILE_NAME;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Redirecting to image info request path: {}", redirectURI);
            }
        } else {
            LOGGER.warn("Redirecting unexpectedly to webroot... why?!");
            redirectURI = "/";
        }

        response.putHeader("Location", redirectURI).end();
    }

}