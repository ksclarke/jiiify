
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.TEXT_MIME_TYPE;

import javax.security.auth.login.FailedLoginException;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles administrative interface login attempts.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class LoginHandler extends JiiifyHandler {

    public static final String GOOGLE = "google";

    public static final String FACEBOOK = "facebook";

    private static final String GOOGLE_HOST = "www.googleapis.com";

    private static final String GOOGLE_PATH = "/oauth2/v3/tokeninfo?access_token={}";

    private static final String FACEBOOK_HOST = "graph.facebook.com";

    private static final String FACEBOOK_PATH = "/debug_token?input_token={}&access_token={}";

    // TODO: check JSON service aud to make sure it matches the client ID
    private final JWTAuth myJwtAuth;

    /**
     * Creates a login handler.
     *
     * @param aConfig The application's configuration
     * @param aJwtAuth The JWT authorization object
     */
    public LoginHandler(final Configuration aConfig, final JWTAuth aJwtAuth) {
        super(aConfig);
        myJwtAuth = aJwtAuth;
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpMethod method = aContext.request().method();

        // GET requests get served a template page for next actions
        if (method == HttpMethod.GET) {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode jsonNode = mapper.createObjectNode();

            jsonNode.put(HTTP_HOST_PROP.replace('.', '-'), myConfig.getServer());
            jsonNode.put(GOOGLE, myConfig.getOAuthClientID(GOOGLE));
            jsonNode.put(FACEBOOK, myConfig.getOAuthClientID(FACEBOOK));

            // Put our JSON data into our context so it can be used by handlebars
            aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
            aContext.next();
        } else if (method == HttpMethod.POST) {
            final String token = aContext.request().getParam("token");
            final String site = aContext.request().getParam("site");

            if (StringUtils.trimToNull(token) != null && StringUtils.trimToNull(site) != null) {
                final HttpClientOptions options = new HttpClientOptions().setSsl(true);
                final HttpClient client = aContext.vertx().createHttpClient(options);

                LOGGER.debug(MessageCodes.DBG_048, site, token);
                checkOAuthToken(client, aContext, site, token);
            } else {
                LOGGER.warn(MessageCodes.WARN_008);
                aContext.fail(500);
            }
        } else {
            LOGGER.warn(MessageCodes.WARN_009, method.name());
            aContext.response().headers().add("Allow", "GET, POST");
            aContext.fail(405);
        }
    }

    private void checkOAuthToken(final HttpClient aClient, final RoutingContext aContext, final String aSite,
            final String aToken) {
        final Pair<String, String> hostPath = getHostPath(aSite, aToken);
        final String site = StringUtils.upcase(aSite);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_049, site, aToken);
        }

        final HttpClientRequest request = aClient.get(443, hostPath.getValue0(), hostPath.getValue1(), handler -> {
            if (handler.statusCode() == 200) {
                handler.bodyHandler(new JWTBodyHandler(aClient, aContext));
            } else {
                final HttpServerResponse response = aContext.response();
                final String statusMessage = handler.statusMessage();
                final int statusCode = handler.statusCode();

                LOGGER.error(MessageCodes.EXC_051, site, statusMessage, statusCode);
                response.setStatusCode(statusCode).setStatusMessage(statusMessage).close();
                aClient.close();
            }
        }).exceptionHandler(exception -> {
            fail(aContext, exception);
            aClient.close();
        });

        request.end();
    }

    private Pair<String, String> getHostPath(final String aService, final String aToken) {
        final String service = aService.toLowerCase(); // Should already by lower-case, but...

        if (service.equals(GOOGLE)) {
            return Pair.with(GOOGLE_HOST, StringUtils.format(GOOGLE_PATH, aToken));
        } else if (service.equals(FACEBOOK)) {
            return Pair.with(FACEBOOK_HOST, StringUtils.format(FACEBOOK_PATH, aToken, aToken));
        } else {
            throw new RuntimeException(StringUtils.format("Unexpected OAuth service: {}", aService));
        }
    }

    private class JWTBodyHandler implements Handler<Buffer> {

        private final RoutingContext myContext;

        private final HttpClient myClient;

        private JWTBodyHandler(final HttpClient aClient, final RoutingContext aContext) {
            myContext = aContext;
            myClient = aClient;
        }

        @Override
        public void handle(final Buffer aBody) {
            LOGGER.debug(MessageCodes.DBG_050, getClass().getSimpleName(), aBody.toString());

            try {
                final JsonObject jwt = extractJWT(new JsonObject(aBody.toString()));
                final JWTOptions jwtOptions = new JWTOptions();
                final String token = myJwtAuth.generateToken(jwt, jwtOptions);

                LOGGER.debug(MessageCodes.DBG_051, aBody.toString());

                // Authenticating will give us a user which we can put into the session
                myJwtAuth.authenticate(new JsonObject().put("jwt", token), authHandler -> {
                    final HttpServerResponse response = myContext.response();

                    if (authHandler.succeeded()) {
                        LOGGER.debug(MessageCodes.DBG_052);
                        myContext.setUser(authHandler.result());
                        response.putHeader(CONTENT_TYPE, TEXT_MIME_TYPE);
                        response.end(SUCCESS_RESPONSE);
                    } else {
                        LOGGER.error(authHandler.cause(), MessageCodes.EXC_062);
                        response.putHeader(CONTENT_TYPE, TEXT_MIME_TYPE);
                        response.end(FAILURE_RESPONSE);
                    }

                    myClient.close();
                });
            } catch (final FailedLoginException details) {
                final HttpServerResponse response = myContext.response();

                LOGGER.error(details.getMessage());

                response.putHeader(CONTENT_TYPE, TEXT_MIME_TYPE);
                response.end(FAILURE_RESPONSE);

                myClient.close();
            }
        }

        private JsonObject extractJWT(final JsonObject aJsonObject) throws FailedLoginException {
            final JsonObject jsonObject = new JsonObject();
            final String email = aJsonObject.getString("email", "");
            final String[] users = myConfig.getUsers();

            // If we don't have any configured users, we allow all
            if (users.length != 0) {
                boolean found = false;

                for (final String user : users) {
                    if (user.equals(email)) {
                        found = true;
                    }
                }

                if (!found) {
                    if (email.equals("")) {
                        throw new FailedLoginException(msg(MessageCodes.EXC_063));
                    } else {
                        throw new FailedLoginException(msg(MessageCodes.EXC_064, email));
                    }
                }
            }

            jsonObject.put("email", email);

            if (aJsonObject.containsKey("name")) {
                jsonObject.put("name", aJsonObject.getString("name"));
            } else {
                LOGGER.warn(MessageCodes.WARN_010);
            }

            return jsonObject;
        }
    }
}
