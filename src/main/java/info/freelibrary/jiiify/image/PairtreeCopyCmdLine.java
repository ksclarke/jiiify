
package info.freelibrary.jiiify.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.Stopwatch;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;

/*
 * java -cp src/test/resources:target/jiiify-0.0.1-SNAPSHOT-exec.jar info.freelibrary.jiiify.image.PairtreeCopyCmdLine \
 *   "ksclarke-jiiify" "AWS_ACCESS_KEY" "AWS_SECRET_KEY" ~/test-jp2s.cs
 */
public class PairtreeCopyCmdLine {

    private static final Vertx VERTX = Vertx.vertx();

    private static final FileSystem FS = VERTX.fileSystem();

    private final PairtreeRoot myS3Pairtree;

    private final String myFSPairtree;

    private PairtreeCopyCmdLine(final String[] args) {
        final Future<Void> future = Future.future();
        final Stopwatch timer = new Stopwatch();

        myS3Pairtree = PairtreeFactory.getFactory(VERTX, PairtreeImpl.S3Bucket).getPairtree(args);
        myFSPairtree = args[args.length - 1];

        // Cleanup after we've finished the run
        future.setHandler(result -> {
            if (!result.succeeded()) {
                System.err.println(result.cause());
            }

            timer.stop();
            System.out.println("Runtime: " + timer.getMilliseconds());
            VERTX.close();

        });

        // Create an S3 Pairtree and start copying
        myS3Pairtree.create(result -> {
            timer.start();

            if (result.succeeded()) {
                recursiveCopy(args[args.length - 1], future);
            } else {
                future.fail(result.cause());
            }
        });
    }

    /**
     * Runs the Pairtree copy script.
     *
     * @param args An arguments list
     */
    public static void main(final String[] args) {
        new PairtreeCopyCmdLine(args);
    }

    @SuppressWarnings("rawtypes")
    private void recursiveCopy(final String aPath, final Future<Void> aFuture) {
        FS.readDir(aPath, readDirResult -> {
            if (readDirResult.succeeded()) {
                final Iterator<String> iterator = readDirResult.result().iterator();
                final List<Future> futures = new ArrayList<>();

                // Check directory listing for directories and JP2 files
                while (iterator.hasNext()) {
                    final String entry = iterator.next();
                    final Future<Void> future = Future.future();

                    futures.add(future);

                    FS.props(entry, propsResult -> {
                        final FileProps fileProps = propsResult.result();
                        final String fileExt = FileUtils.getExt(entry);

                        if (fileProps.isDirectory()) {
                            recursiveCopy(entry, future);
                        } else if ("jp2".equals(fileExt) || "properties".equals(fileExt) || "jpg".equals(fileExt) ||
                            "json".equals(fileExt)) {
                            try {
                                final String fullPath = entry.replace(myFSPairtree + "/pairtree_root/", "");
                                final String encodedID = getID(fullPath);
                                final String id = PairtreeUtils.decodeID(encodedID);
                                final int idIndex = fullPath.indexOf(encodedID) + encodedID.length() + 1;
                                final String resourcePath = fullPath.substring(idIndex);
                                final byte[] bytes = IOUtils.readBytes(new FileInputStream(entry));

                                System.out.println("Processing " + resourcePath + " [" + id + "]");

                                myS3Pairtree.getObject(id).put(resourcePath, Buffer.buffer(bytes), putResult -> {
                                    if (putResult.succeeded()) {
                                        System.out.println("Success! " + entry + " successfully migrated");
                                        future.complete();
                                    } else {
                                        System.err.println("Error! " + entry + " couldn't be migrated");
                                        future.fail(putResult.cause());
                                    }
                                });
                            } catch (final IOException details) {
                                future.fail(details);
                            }
                        } else {
                            future.complete();
                        }
                    });
                }

                CompositeFuture.all(futures).setHandler(compositeResult -> {
                    if (compositeResult.succeeded()) {
                        aFuture.complete();
                    } else {
                        aFuture.fail(compositeResult.cause());
                    }
                });
            } else {
                aFuture.fail(readDirResult.cause());
            }
        });
    }

    private String getID(final String aPath) {
        for (final String pathPart : aPath.split("\\/")) {
            if (pathPart.length() > 2) {
                return pathPart;
            }
        }

        throw new RuntimeException("Didn't find a Pairtree object: " + aPath);
    }
}
