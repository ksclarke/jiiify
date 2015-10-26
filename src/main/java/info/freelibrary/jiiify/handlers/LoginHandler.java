
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.TEXT_MIME_TYPE;

import java.net.MalformedURLException;
import java.net.URL;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
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

public class LoginHandler extends JiiifyHandler {

    public static final String GOOGLE = "google";

    public static final String FACEBOOK = "facebook";

    public static final String TWITTER = "twitter";

    private static final String GOOGLE_HOST = "www.googleapis.com";

    private static final String GOOGLE_PATH = "/oauth2/v3/tokeninfo?access_token={}";

    private static final String FACEBOOK_HOST = "graph.facebook.com";

    private static final String FACEBOOK_PATH = "/debug_token?input_token={}&access_token={}";

    // TODO: Implement this locally -- the heroku app is okay for testing purposes though
    private static final String TWITTER_HOST = "auth-server.herokuapp.com";

    private static final String TWITTER_PATH =
            "/proxy?path=https://api.twitter.com/1.1/account/verify_credentials.json&access_token={}";

    // TODO: check JSON service aud to make sure it matches the client ID
    private final JWTAuth myJwtAuth;

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
            jsonNode.put(TWITTER, myConfig.getOAuthClientID(TWITTER));
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

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Processing {} login token: {}", site, token);
                }

                checkOAuthToken(client, aContext, site, token);
            } else {
                LOGGER.warn("Received a login POST message without a token");
                aContext.fail(500);
            }
        } else {
            LOGGER.warn("Received a {} request but only POST and GET are supported", method.name());
            aContext.response().headers().add("Allow", "GET, POST");
            aContext.fail(405);
        }
    }

    private void checkOAuthToken(final HttpClient aClient, final RoutingContext aContext, final String aSite,
            final String aToken) {
        final Pair<String, String> hostPath = getHostPath(aSite, aToken);
        final String site = StringUtils.upcase(aSite);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Verifying user login token with {}: {}", site, aToken);
        }

        final HttpClientRequest request = aClient.get(443, hostPath.getValue0(), hostPath.getValue1(), handler -> {
            if (handler.statusCode() == 200) {
                handler.bodyHandler(new JWTBodyHandler(aClient, aContext));
            } else if (handler.statusCode() == 302) {
                redirectOAuth1Token(aClient, aContext, aSite, aToken, handler.getHeader("Location"));
            } else {
                final HttpServerResponse response = aContext.response();
                final String statusMessage = handler.statusMessage();
                final int statusCode = handler.statusCode();

                LOGGER.error("{} verfication responded with: {} [{}]", site, statusMessage, statusCode);
                response.setStatusCode(statusCode).setStatusMessage(statusMessage).close();
                aClient.close();
            }
        }).exceptionHandler(exception -> {
            fail(aContext, exception);
            aClient.close();
        });

        request.end();
    }

    private void redirectOAuth1Token(final HttpClient aClient, final RoutingContext aContext, final String aSite,
            final String aToken, final String aRedirectURL) {
        if (aRedirectURL != null) {
            try {
                final URL url = new URL(aRedirectURL);
                final String host = url.getHost();
                final String oauthCheck = url.getPath() + "?" + url.getQuery();

                LOGGER.debug("Redirecting login... [host: {}] [path: {}]", host, oauthCheck);

                final HttpClientRequest redirectRequest = aClient.get(443, host, oauthCheck, redirect -> {
                    if (redirect.statusCode() == 200) {
                        redirect.bodyHandler(new JWTBodyHandler(aClient, aContext));
                    } else {
                        fail(aContext, redirect.statusCode(), redirect.statusMessage());
                        aClient.close();
                    }
                });

                redirectRequest.end();
            } catch (final MalformedURLException details) {
                fail(aContext, details, "Malformed redirect URL location");
                aClient.close();
            }
        } else {
            fail(aContext, null, "Redirect didn't have 'Location' header");
            aClient.close();
        }
    }

    private Pair<String, String> getHostPath(final String aService, final String aToken) {
        final String service = aService.toLowerCase(); // Should already by lower-case, but...

        if (service.equals(GOOGLE)) {
            return Pair.with(GOOGLE_HOST, StringUtils.format(GOOGLE_PATH, aToken));
        } else if (service.equals(FACEBOOK)) {
            return Pair.with(FACEBOOK_HOST, StringUtils.format(FACEBOOK_PATH, aToken, aToken));
        } else if (service.equals(TWITTER)) {
            return Pair.with(TWITTER_HOST, StringUtils.format(TWITTER_PATH, aToken));
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
            LOGGER.debug("{} handling body: {}", getClass().getSimpleName(), aBody.toString());

            final JsonObject jwt = extractJWT(new JsonObject(aBody.toString()));
            final JWTOptions jwtOptions = new JWTOptions().setExpiresInMinutes(120);
            final String token = myJwtAuth.generateToken(jwt, jwtOptions);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Token's decoded JSON contents: {}", aBody.toString());
            }

            // Authenticating will give us a user which we can put into the session
            myJwtAuth.authenticate(new JsonObject().put("jwt", token), authHandler -> {
                final HttpServerResponse response = myContext.response();

                if (authHandler.succeeded()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("User successfully validated");
                    }

                    myContext.setUser(authHandler.result());
                    response.putHeader(CONTENT_TYPE, TEXT_MIME_TYPE);
                    response.end("success");
                } else {
                    LOGGER.error(authHandler.cause(), "Authentication did not succeed");
                    response.putHeader(CONTENT_TYPE, TEXT_MIME_TYPE);
                    response.end("failure");
                }

                myClient.close();
            });
        }

        private JsonObject extractJWT(final JsonObject aJsonObject) {
            final JsonObject jsonObject = new JsonObject();

            if (aJsonObject.containsKey("email")) {
                final String email = aJsonObject.getString("email");

                jsonObject.put("email", email);
                jsonObject.put("username", email);
            } else {
                jsonObject.put("username", "screen_name");
            }

            if (aJsonObject.containsKey("name")) {
                jsonObject.put("name", aJsonObject.getString("name"));
            } else {
                LOGGER.warn("User login JWT does not contain a name");
            }

            return jsonObject;
        }
    }
}
