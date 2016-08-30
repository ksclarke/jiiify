
package info.freelibrary.jiiify.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;

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
        myS3Pairtree = PairtreeFactory.getFactory(VERTX, PairtreeImpl.S3Bucket).getPairtree(args);
        myFSPairtree = args[args.length - 1];
    }

    /**
     * Runs the Pairtree copy script.
     *
     * @param args An arguments list
     */
    public static void main(final String[] args) {
        final Future<Void> future = Future.future();

        future.setHandler(result -> {
            if (!result.succeeded()) {
                System.err.println(result.cause());
            }

            VERTX.close();
        });

        new PairtreeCopyCmdLine(args).checkForJp2s(args[args.length - 1], future);
    }

    @SuppressWarnings("rawtypes")
    private void checkForJp2s(final String aPath, final Future<Void> aFuture) {
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

                        if (fileProps.isDirectory()) {
                            checkForJp2s(entry, future);
                        } else if (FileUtils.getExt(entry).equals("jp2")) {
                            final String path = entry.replaceAll(myFSPairtree + "/", "");

                            try {
                                final String id = getID(path);
                                final String name = PairtreeUtils.encodeID(id) + ".jp2";
                                final byte[] bytes = IOUtils.readBytes(new FileInputStream(entry));

                                System.out.println("Processing " + id + " [Adding " + name + "]");

                                myS3Pairtree.getObject(id).put(name, Buffer.buffer(bytes),
                                    putResult -> {
                                        if (putResult.succeeded()) {
                                            System.out.println("Success! " + name + " added.");
                                            future.complete();
                                        } else {
                                            System.err.println("Error! " + name + " couldn't be added.");
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
        return PairtreeUtils.decodeID(FileUtils.stripExt(new File(aPath).getName()));
    }
}
