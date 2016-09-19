
package info.freelibrary.jiiify.image;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Stopwatch;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;

/*
 * java -cp src/test/resources:target/jiiify-0.0.1-SNAPSHOT-exec.jar info.freelibrary.jiiify.image.PairtreeCopyCmdLine \
 *   "ksclarke-jiiify" "AWS_ACCESS_KEY" "AWS_SECRET_KEY" "S3_ENDPOINT" /path/to/pairtree
 */
public class PairtreeCopyCmdLine {

    // How many upload processes we have running at one time
    private static final int BATCH_SIZE = 2;

    private static final Vertx VERTX = Vertx.vertx();

    private static final FileSystem FS = VERTX.fileSystem();

    private final PairtreeRoot myS3Pairtree;

    private final String myFSPairtree;

    @SuppressWarnings("rawtypes")
    private PairtreeCopyCmdLine(final String[] args) {
        final Queue<String> queue = new ConcurrentLinkedQueue<>();
        final Future<Void> future = Future.future();
        final Stopwatch timer = new Stopwatch();

        myS3Pairtree = PairtreeFactory.getFactory(VERTX, PairtreeImpl.S3Bucket).getPairtree(args);
        myFSPairtree = args[args.length - 1];

        // Gather the list of files to upload
        future.setHandler(result -> {
            if (!result.succeeded()) {
                System.err.println(result.cause());
            } else {
                final List<Future> compositeFutures = new ArrayList<>();

                while (queue.size() > 0) {
                    final List<Future> futures = new ArrayList<>();
                    final Future<Void> compositeFuture = Future.future();

                    compositeFutures.add(compositeFuture);


                    for (int index = 0; index < BATCH_SIZE; index++) {
                        if (queue.size() > 0) {
                            final String entry = queue.remove();
                            final Future<Void> uploadFuture = Future.future();
                            final String fullPath = entry.replace(myFSPairtree + "/pairtree_root/", "");
                            final String encodedID = getID(fullPath);
                            final String id = PairtreeUtils.decodeID(encodedID);
                            final int idIndex = fullPath.indexOf(encodedID) + encodedID.length() + 1;
                            final String resourcePath = fullPath.substring(idIndex);
                            final int size = (int) new File(entry).length();

                            System.out.println("Processing " + resourcePath + " [" + id + "] [" + entry + "]");
                            futures.add(uploadFuture);

                            VERTX.fileSystem().open(entry, new OpenOptions(), openResult -> {
                                if (openResult.succeeded()) {
                                    final AsyncFile file = openResult.result();

                                    System.out.println("==>");
                                    myS3Pairtree.getObject(id).put(resourcePath, file, size, putResult -> {
                                        if (putResult.succeeded()) {
                                            System.out.println("Success! " + entry + " successfully migrated");
                                            uploadFuture.complete();
                                        } else {
                                            System.err.println("Error! " + entry + " couldn't be migrated");
                                            uploadFuture.fail(putResult.cause());
                                        }
                                    });
                                } else {
                                    System.err.println(openResult.cause());
                                    uploadFuture.fail(openResult.cause());
                                }
                            });
                        }
                    }

                    CompositeFuture.all(futures).setHandler(uploadResult -> {
                        if (uploadResult.succeeded()) {
                            compositeFuture.complete();
                        } else {
                            compositeFuture.fail(uploadResult.cause());
                        }
                    });
                }

                CompositeFuture.all(compositeFutures).setHandler(compositeResult -> {
                    // timer.stop();
                    // System.out.println("Runtime: " + timer.getMilliseconds());
                    // VERTX.close();
                });
            }
        });

        // Create an S3 Pairtree and start copying
        myS3Pairtree.create(result -> {
            timer.start();

            if (result.succeeded()) {
                recursiveCopy(args[args.length - 1], future, queue);
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
    private void recursiveCopy(final String aPath, final Future<Void> aFuture, final Queue<String> aQueue) {
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
                            recursiveCopy(entry, future, aQueue);
                        } else {
                            if ("jp2".equals(fileExt) || "properties".equals(fileExt) || "jpg".equals(fileExt) || "json"
                                    .equals(fileExt)) {
                                aQueue.add(entry);
                            }

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
