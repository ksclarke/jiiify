
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.CONFIG_KEY;
import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Constants.SHARED_DATA_KEY;
import static info.freelibrary.jiiify.MessageCodes.DBG_000;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public abstract class AbstractJiiifyVerticle extends AbstractVerticle {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName(), MESSAGES);

    @Override
    public void stop(final Future<Void> aFuture) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(DBG_000, getClass().getName(), deploymentID());
        }

        aFuture.complete();
    }

    protected Configuration getConfig() {
        return (Configuration) vertx.sharedData().getLocalMap(SHARED_DATA_KEY).get(CONFIG_KEY);
    }

    protected MessageConsumer<JsonObject> getJsonConsumer() {
        return vertx.eventBus().consumer(getClass().getName());
    }

    protected MessageConsumer<String> getStringConsumer() {
        return vertx.eventBus().consumer(getClass().getName());
    }

    /**
     * FIXME: Better handling of timeouts and responses (e.g. log a list of failures).
     *
     * @param aJsonObject A JSON message
     * @param aVerticleName A verticle name that will respond to the message
     * @param aTimeout A timeout measured in milliseconds
     */
    protected void sendMessage(final JsonObject aJsonObject, final String aVerticleName, final long aTimeout) {
        final DeliveryOptions options = new DeliveryOptions().setSendTimeout(aTimeout);
        final EventBus eventBus = vertx.eventBus();

        eventBus.send(aVerticleName, aJsonObject, options, response -> {
            if (response.failed()) {
                if (response.cause() != null) {
                    LOGGER.error(response.cause(), "Unable to send message to {}: {}", aVerticleName, aJsonObject);
                } else {
                    LOGGER.error("Unable to send message to {}: {}", aVerticleName, aJsonObject);
                }
            }
        });
    }

    /**
     * FIXME: Better handling of timeouts and responses (e.g. log a list of failures).
     *
     * @param aJsonObject A JSON message
     * @param aVerticleName A verticle name that will respond to the message
     */
    protected void sendMessage(final JsonObject aJsonObject, final String aVerticleName) {
        sendMessage(aJsonObject, aVerticleName, DeliveryOptions.DEFAULT_TIMEOUT);
    }
}
