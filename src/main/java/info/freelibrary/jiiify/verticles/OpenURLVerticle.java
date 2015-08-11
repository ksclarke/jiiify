
package info.freelibrary.jiiify.verticles;

import javax.naming.ConfigurationException;

import info.freelibrary.jiiify.MessageCodes;

import io.vertx.core.Future;

public class OpenURLVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException {
        // TODO: confirm this can be handed off to by the main JiiifyMainVerticle

        aFuture.complete();
    }

    @Override
    public void stop(final Future<Void> aFuture) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_000, getClass().getName(), deploymentID());
        }

        aFuture.complete();
    }

}
