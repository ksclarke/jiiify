
package info.freelibrary.jiiify.auth;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

public class GoogleOAuth2 extends DefaultApi20 {

    private static final String BASE_URL = "https://accounts.google.com/o/oauth2";

    private static final String AUTH_URL = BASE_URL + "/auth?response_type=code&client_id={}&redirect_uri={}";

    private static final String SCOPED_AUTH_URL = AUTH_URL + "&scope={}";

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuth2.class, MESSAGES);

    @Override
    public String getAccessTokenEndpoint() {
        return BASE_URL + "/token";
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new AccessTokenExtractor() {

            @Override
            public Token extract(final String aResponse) {
                final Matcher matcher;

                if (StringUtils.trimToNull(aResponse) == null) {
                    throw new IllegalArgumentException("Can't extract a token from an empty response string");
                }

                matcher = Pattern.compile("\"access_token\" : \"([^&\"]+)\"").matcher(aResponse);

                if (matcher.find()) {
                    return new Token(OAuthEncoder.decode(matcher.group(1)), "", aResponse);
                } else {
                    throw new OAuthException(LOGGER.getMessage("Can't extract a token from this response string: {}",
                            aResponse), null);
                }
            }
        };
    }

    @Override
    public String getAuthorizationUrl(final OAuthConfig aConfig) {
        if (aConfig.hasScope()) {
            return StringUtils.format(SCOPED_AUTH_URL, aConfig.getApiKey(), OAuthEncoder.encode(aConfig
                    .getCallback()), OAuthEncoder.encode(aConfig.getScope()));
        } else {
            return String.format(AUTH_URL, aConfig.getApiKey(), OAuthEncoder.encode(aConfig.getCallback()));
        }
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuthService createService(final OAuthConfig aConfig) {
        return new GoogleOAuth2Service(this, aConfig);
    }

    private class GoogleOAuth2Service extends OAuth20ServiceImpl {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

        private static final String GRANT_TYPE = "grant_type";

        private final DefaultApi20 myAPI;

        private final OAuthConfig myConfig;

        GoogleOAuth2Service(final DefaultApi20 aAPI, final OAuthConfig aConfig) {
            super(aAPI, aConfig);

            myAPI = aAPI;
            myConfig = aConfig;
        }

        @Override
        public Token getAccessToken(final Token aRequestToken, final Verifier aVerifier) {
            final OAuthRequest request = new OAuthRequest(myAPI.getAccessTokenVerb(), myAPI.getAccessTokenEndpoint());

            switch (myAPI.getAccessTokenVerb()) {
                case POST:
                    request.addBodyParameter(OAuthConstants.CLIENT_ID, myConfig.getApiKey());
                    request.addBodyParameter(OAuthConstants.CLIENT_SECRET, myConfig.getApiSecret());
                    request.addBodyParameter(OAuthConstants.CODE, aVerifier.getValue());
                    request.addBodyParameter(OAuthConstants.REDIRECT_URI, myConfig.getCallback());
                    request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);

                    break;
                case GET:
                default:
                    request.addQuerystringParameter(OAuthConstants.CLIENT_ID, myConfig.getApiKey());
                    request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, myConfig.getApiSecret());
                    request.addQuerystringParameter(OAuthConstants.CODE, aVerifier.getValue());
                    request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, myConfig.getCallback());

                    if (myConfig.hasScope()) {
                        request.addQuerystringParameter(OAuthConstants.SCOPE, myConfig.getScope());
                    }
            }

            return myAPI.getAccessTokenExtractor().extract(request.send().getBody());
        }
    }

}
