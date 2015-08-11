package info.freelibrary.jiiify.handlers;

import info.freelibrary.jiiify.Configuration;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;


public class LogoutHandler extends JiiifyHandler {

    public LogoutHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final Session session = aContext.session();
        final String user = aContext.user().principal().toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Logging out of session {}: {}", session, user);
        }

        session.destroy();
        response.setStatusCode(303).putHeader("Location", "/admin/login").end();
    }

}
