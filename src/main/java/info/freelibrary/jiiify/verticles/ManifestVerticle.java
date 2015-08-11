
package info.freelibrary.jiiify.verticles;

import static info.freelibrary.jiiify.Constants.FAILURE_RESPONSE;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.IIIF_PATH_KEY;
import static info.freelibrary.jiiify.Constants.SOLR_SERVICE_KEY;
import static info.freelibrary.jiiify.Constants.SUCCESS_RESPONSE;
import static info.freelibrary.jiiify.Constants.THUMBNAIL_KEY;
import static info.freelibrary.jiiify.Constants.UTF_8_ENCODING;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.ConfigurationException;

import org.javatuples.KeyValue;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.presentation.json.AbstractIiifResourceMixIn;
import info.freelibrary.jiiify.iiif.presentation.json.ManifestMixIn;
import info.freelibrary.jiiify.iiif.presentation.json.MetadataLocalizedValueMixIn;
import info.freelibrary.jiiify.iiif.presentation.json.ServiceMixIn;
import info.freelibrary.jiiify.iiif.presentation.model.AbstractIiifResource;
import info.freelibrary.jiiify.iiif.presentation.model.Canvas;
import info.freelibrary.jiiify.iiif.presentation.model.Manifest;
import info.freelibrary.jiiify.iiif.presentation.model.Sequence;
import info.freelibrary.jiiify.iiif.presentation.model.other.Image;
import info.freelibrary.jiiify.iiif.presentation.model.other.ImageResource;
import info.freelibrary.jiiify.iiif.presentation.model.other.MetadataLocalizedValue;
import info.freelibrary.jiiify.iiif.presentation.model.other.Resource;
import info.freelibrary.jiiify.iiif.presentation.model.other.Service;
import info.freelibrary.jiiify.services.SolrService;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.jiiify.util.SolrUtils;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

public class ManifestVerticle extends AbstractJiiifyVerticle {

    @Override
    public void start(final Future<Void> aFuture) throws ConfigurationException, IOException {
        getJsonConsumer().handler(messageHandler -> {
            final JsonObject jsonMessage = messageHandler.body();
            final String id = jsonMessage.getString(ID_KEY);
            final String thumbnail = jsonMessage.getString(IIIF_PATH_KEY);
            final String fsPath = PathUtils.getObjectPath(vertx, id);
            final FileSystem fileSystem = vertx.fileSystem();

            fileSystem.mkdirs(fsPath, mkdirsHandler -> {
                if (mkdirsHandler.succeeded()) {
                    final String manifestPath = fsPath + File.separator + Metadata.MANIFEST_FILE;

                    try {
                        final Manifest manifest = createManifest(jsonMessage);
                        final Buffer buffer = Buffer.buffer(toJson(manifest), UTF_8_ENCODING);

                        writeManifest(fileSystem, manifestPath, buffer, id, thumbnail, messageHandler);
                    } catch (final JsonProcessingException | URISyntaxException details) {
                        LOGGER.error(details, "Unexpected exception thrown");
                        messageHandler.reply(FAILURE_RESPONSE);
                    } catch (final IOException details) {
                        LOGGER.error(details, "Exception thrown while reading height and width");
                        messageHandler.reply(FAILURE_RESPONSE);
                    }
                } else {
                    LOGGER.error("Unable to create object directory: {}", fsPath);
                    messageHandler.reply(FAILURE_RESPONSE);
                }
            });
        });

        aFuture.complete();
    }

    private Manifest createManifest(final JsonObject aJsonObject) throws IOException, URISyntaxException {
        final Configuration config = getConfiguration();
        final String server = config.getServer();
        final String iiif = server + config.getServicePrefix();
        final String thumbnail = server + aJsonObject.getString(IIIF_PATH_KEY);
        final String filePath = aJsonObject.getString(Constants.FILE_PATH_KEY);
        final Dimension dims = ImageUtils.getImageDimension(new File(filePath));
        final String id = aJsonObject.getString(ID_KEY);
        final String encodedID = PathUtils.encodeIdentifier(id);
        final String manifestID = iiif + "/" + encodedID + "/manifest";
        final Manifest manifest = new Manifest(manifestID, id);
        final Sequence sequence = new Sequence();
        final Canvas canvas = new Canvas(id(iiif, encodedID, "canvas"), id, dims.height, dims.width);
        final Image image = new Image(id(iiif, encodedID, "imageanno"));
        final ImageResource resource = new ImageResource();
        final Service service = new Service();

        service.setId(iiif + "/" + encodedID);
        service.setProfile("http://iiif.io/api/image/2/level0.json");
        service.setContext("http://iiif.io/api/image/2/context.json");

        resource.setId(iiif + "/" + encodedID);
        resource.setType("dctypes:Image");
        // FIXME: should this really be using the default format?
        resource.setFormat(ImageFormat.getMIMEType(ImageFormat.DEFAULT_FORMAT));
        resource.setService(service);
        resource.setHeight(dims.height);
        resource.setWidth(dims.width);

        image.setResource(resource);
        image.setOn(canvas.getId());

        canvas.setImages(Arrays.asList(image));
        canvas.setThumbnail(thumbnail); /* this is where edsu has it */

        sequence.setId(id(iiif, encodedID, "sequence"));
        sequence.setThumbnail(thumbnail);
        sequence.setLabel(id);
        sequence.setCanvases(Arrays.asList(canvas));

        manifest.setThumbnail(thumbnail);
        manifest.setSequences(Arrays.asList(sequence));
        manifest.setLogo(server + "/images/logos/iiif_logo.png");

        return manifest;
    }

    private String id(final String aHost, final String aID, final String aType) {
        final StringBuilder builder = new StringBuilder(aHost);
        builder.append('/').append(aID).append('/').append(aType).append('/').append(aType).append("-1");
        return builder.toString();
    }

    private void writeManifest(final FileSystem aFileSystem, final String aFilePath, final Buffer aManifest,
            final String aID, final String aThumbnail, final Message<JsonObject> aMessageHandler) {
        aFileSystem.writeFile(aFilePath, aManifest, writeHandler -> {
            if (writeHandler.succeeded()) {
                final SolrService service = SolrService.createProxy(vertx, SOLR_SERVICE_KEY);
                final List<KeyValue<String, ?>> fields = new ArrayList<KeyValue<String, ?>>();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Wrote manifest file: {}", aFilePath);
                }

                fields.add(KeyValue.with(ID_KEY, aID));
                fields.add(KeyValue.with(THUMBNAIL_KEY, aThumbnail));

                service.index(SolrUtils.getSimpleUpdateDoc(fields), handler -> {
                    if (handler.failed()) {
                        LOGGER.error("Failed submitting thumbnail to Solr: {}", handler.result());
                        aMessageHandler.reply(FAILURE_RESPONSE);
                    } else if (handler.succeeded()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Succeeded submitting thumbnail to Solr");
                        }

                        aMessageHandler.reply(SUCCESS_RESPONSE);
                    }
                });
            } else {
                final Throwable throwable = writeHandler.cause();

                if (throwable != null) {
                    LOGGER.error(throwable, "Failed to write manifest file: {}", aFilePath);
                } else {
                    LOGGER.error("Failed to write manifest file: {}", aFilePath);
                }
            }
        });
    }

    private String toJson(final Manifest manifest) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.addMixIn(AbstractIiifResource.class, AbstractIiifResourceMixIn.class);
        mapper.addMixIn(Image.class, AbstractIiifResourceMixIn.class);
        mapper.addMixIn(Manifest.class, ManifestMixIn.class);
        mapper.addMixIn(MetadataLocalizedValue.class, MetadataLocalizedValueMixIn.class);
        mapper.addMixIn(Resource.class, AbstractIiifResourceMixIn.class);
        mapper.addMixIn(Service.class, ServiceMixIn.class);
        mapper.setSerializationInclusion(Include.NON_NULL);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest);
    }
}
