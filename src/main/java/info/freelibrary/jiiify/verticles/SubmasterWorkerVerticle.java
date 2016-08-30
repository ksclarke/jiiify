
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FILE_PATH_KEY;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class SubmasterWorkerVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(message -> {
            final JsonObject json = message.body();
            new File(json.getString(FILE_PATH_KEY));
        });

        aFuture.complete();
    }

}
