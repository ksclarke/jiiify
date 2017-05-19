
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;

/**
 * Handler that handles exceptions.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class FailureHandler extends JiiifyHandler {

    public static final String ERROR_HEADER = "error-header";

    public static final String ERROR_MESSAGE = "error-message";

    private final Logger LOGGER = LoggerFactory.getLogger(FailureHandler.class, MESSAGES);

    private final TemplateEngine myTemplateEngine;

    /**
     * Creates a handler for system failures.
     *
     * @param aConfig The application's configuration
     * @param aTemplateEngine The template engine used to render the failure page
     */
    public FailureHandler(final Configuration aConfig, final TemplateEngine aTemplateEngine) {
        super(aConfig);
        myTemplateEngine = aTemplateEngine;
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final ObjectNode jsonObject = new ObjectMapper().createObjectNode();

        if (aContext.failed()) {
            final String errorHeader = (String) aContext.get(ERROR_HEADER);
            final String errorMessage = aContext.get(ERROR_MESSAGE);
            final Throwable throwable = aContext.failure();

            switch (aContext.statusCode()) {
                case 400:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Bad Request" : errorHeader);
                    break;
                case 401:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Unauthorized" : errorHeader);
                    jsonObject.put(ERROR_MESSAGE, "Please login to access this page");
                    break;
                case 403:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Forbidden" : errorHeader);
                    break;
                case 404:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Not Found" : errorHeader);
                    break;
                case 405:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Method Not Allowed" : errorHeader);
                    break;
                case 406:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Not Acceptable" : errorHeader);
                    break;
                case 410:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Gone" : errorHeader);
                    break;
                case 414:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Request URI Too Long" : errorHeader);
                    break;
                case 415:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Unsupported Media Type" : errorHeader);
                    break;
                case 500:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Internal Server Error" : errorHeader);
                    break;
                case 501:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Not Implemented" : errorHeader);
                    break;
                case 502:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Bad Gateway" : errorHeader);
                    break;
                case 503:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "Service Unavailable" : errorHeader);
                    break;
                case 505:
                    jsonObject.put(ERROR_HEADER, errorHeader == null ? "HTTP Version Not Supported" : errorHeader);
                default:

            }

            if (throwable != null) {
                String message = throwable.getMessage();

                if (errorMessage == null) {
                    if (message == null) {
                        message = "";
                    }
                } else {
                    message = errorMessage;
                }

                LOGGER.error(throwable, MessageCodes.EXC_000, message);
                jsonObject.put(ERROR_MESSAGE, message);
            } else if (errorMessage != null) {
                LOGGER.warn(MessageCodes.EXC_000, errorMessage);
                jsonObject.put(ERROR_MESSAGE, errorMessage);
            }
        }

        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonObject, aContext));

        myTemplateEngine.render(aContext, "templates/error", handler -> {
            if (handler.succeeded()) {
                final HttpServerResponse response = aContext.response();

                // Pass through the output of templating process
                response.end(handler.result());
                response.close();
            }
        });
    }

}
