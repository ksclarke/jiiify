
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Metadata.LOCATION_HEADER;
import static info.freelibrary.jiiify.RoutePatterns.LOGIN;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

/**
 * A handler that handles administrative interface logout attempts.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class LogoutHandler extends JiiifyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class, MESSAGES);

    /**
     * Creates a logout handler.
     *
     * @param aConfig The application's configuration
     */
    public LogoutHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final Session session = aContext.session();

        if (LOGGER.isDebugEnabled()) {
            final JsonObject principal = aContext.user().principal();
            final String user = principal.getString("name");
            final String email = principal.getString("email");

            LOGGER.debug(MessageCodes.DBG_053, session.id(), user, email);
        }

        aContext.clearUser();
        response.setStatusCode(303).putHeader(LOCATION_HEADER, LOGIN).end();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
