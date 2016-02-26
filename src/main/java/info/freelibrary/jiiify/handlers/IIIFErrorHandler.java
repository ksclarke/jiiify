
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class IIIFErrorHandler extends JiiifyHandler {

    public static final String ERROR_HEADER = "error-header";

    public static final String ERROR_MESSAGE = "error-message";

    private final Logger LOGGER = LoggerFactory.getLogger(IIIFErrorHandler.class, MESSAGES);

    /**
     * An error handler for IIIF-related exceptions.
     *
     * @param aConfig The application's configuration
     */
    public IIIFErrorHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        if (aContext.failed()) {
            final Throwable throwable = aContext.failure();
            final HttpServerResponse response = aContext.response();

            String errorMessage = (String) aContext.get(ERROR_MESSAGE);

            switch (aContext.statusCode()) {
                case 400:
                    errorMessage = errorMessage == null ? "Bad Request" : errorMessage;
                case 401:
                    errorMessage = errorMessage == null ? "Unauthorized" : errorMessage;
                case 403:
                    errorMessage = errorMessage == null ? "Forbidden" : errorMessage;
                case 404:
                    errorMessage = errorMessage == null ? "Not Found" : errorMessage;
                case 405:
                    errorMessage = errorMessage == null ? "Method Not Allowed" : errorMessage;
                case 406:
                    errorMessage = errorMessage == null ? "Not Acceptable" : errorMessage;
                case 410:
                    errorMessage = errorMessage == null ? "Gone" : errorMessage;
                case 414:
                    errorMessage = errorMessage == null ? "Request URI Too Long" : errorMessage;
                case 415:
                    errorMessage = errorMessage == null ? "Unsupported Media Type" : errorMessage;
                case 500:
                    errorMessage = errorMessage == null ? "Internal Server Error" : errorMessage;
                case 501:
                    errorMessage = errorMessage == null ? "Not Implemented" : errorMessage;
                case 502:
                    errorMessage = errorMessage == null ? "Bad Gateway" : errorMessage;
                case 503:
                    errorMessage = errorMessage == null ? "Service Unavailable" : errorMessage;
                case 505:
                    errorMessage = errorMessage == null ? "HTTP Version Not Supported" : errorMessage;
                default:
                    errorMessage = errorMessage == null ? "Unspecified Error Occurred" : errorMessage;

                    if (throwable != null) {
                        LOGGER.error(throwable, errorMessage);
                    } else {
                        LOGGER.warn(errorMessage);
                    }

            }

            response.setStatusCode(aContext.statusCode());
            response.setStatusMessage(errorMessage);
            response.end(errorMessage);
            response.close();
        }
    }

}
