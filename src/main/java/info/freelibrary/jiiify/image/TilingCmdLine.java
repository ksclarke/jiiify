
package info.freelibrary.jiiify.image;

import static info.freelibrary.pairtree.PairtreeUtils.encodeID;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.opencsv.CSVReader;

import info.freelibrary.jiiify.iiif.IIIFException;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.jiiify.iiif.ImageSize;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.ProcessListener;
import info.freelibrary.util.ProcessWatcher;
import info.freelibrary.util.Stopwatch;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * Tiling command line tool.
 */
public class TilingCmdLine {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private static final Vertx VERTX = Vertx.vertx();

    private static final String PREFIX = "/iiif";

    private static final int TILE_SIZE = 1024;

    private final Iterator<String[]> myIterator;

    private final PairtreeRoot myPtRoot;

    private static int JPG_COUNT = 0;

    private TilingCmdLine(final PairtreeRoot aPtRoot, final List<String[]> aIDsList) {
        myIterator = aIDsList.iterator();
        myPtRoot = aPtRoot;
    }

    @SuppressWarnings("rawtypes")
    private void run(final Future aFuture) throws IOException {
        final File idLogFile = new File("ids-not-found.log");
        final FileWriter writer = new FileWriter(idLogFile);
        final List<Future> futures = new ArrayList<>();
        final FileSystem fileSystem = VERTX.fileSystem();

        while (myIterator.hasNext()) {
            final String id = myIterator.next()[0];
            final PairtreeObject obj = myPtRoot.getObject(id);
            final Future<String> future = Future.<String>future();
            final String jp2Resource = encodeID(id) + ".jp2";

            futures.add(future);

            obj.find(jp2Resource, findResult -> {
                if (findResult.succeeded() && findResult.result()) {
                    obj.get(jp2Resource, getResult -> {
                        if (getResult.succeeded()) {
                            final String tmpFilePath = TMP_DIR + "/" + jp2Resource;

                            fileSystem.writeFile(tmpFilePath, getResult.result(), writeResult -> {
                                if (writeResult.succeeded()) {
                                    tile(id, new File(tmpFilePath), future);
                                } else {
                                    System.err.println(writeResult.cause());
                                    future.complete();
                                }
                            });
                        } else {
                            System.err.println(getResult.cause());
                            future.complete();
                        }
                    });
                } else {
                    try {
                        writer.write(id); // Log that we weren't able to process this ID
                        writer.write(System.getProperty("line.separator"));
                    } catch (final IOException details) {
                        System.err.println(details);
                    } finally {
                        future.complete();
                    }
                }
            });
        }

        CompositeFuture.all(futures).setHandler(result -> {
            try {
                writer.close();
                aFuture.complete();
            } catch (final IOException details) {
                aFuture.fail(details);
            } finally {
                if (idLogFile.length() == 0) {
                    idLogFile.delete();
                }
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private void tile(final String aID, final File aJp2File, final Future<String> aFuture) {
        final List<Future> futures = new ArrayList<>();
        final List<File> toCleans = new ArrayList<>();

        // Can't use lambda below because Checkstyle complains :-(
        final Handler<AsyncResult<String>> handler = aResult -> {
            if (aResult.succeeded()) {
                System.out.println("The result of processing " + aID + " is: " + aResult.result());
            } else {
                System.err.println(aResult.cause());
                aFuture.fail(aResult.cause());
            }
        };

        VERTX.executeBlocking(result -> {
            try {
                final Dimension dims = ImageUtils.getImageDimension(aJp2File);
                final Future<Void> future = Future.future();

                final int thumbnailSize = 150;
                final ImageRegion region = ImageUtils.getCenter(dims);
                final ImageSize size = new ImageSize(thumbnailSize);
                final ImageRequest request = new ImageRequest(aID, PREFIX, region, size);

                // Image size
                final double width = dims.getWidth();
                final double height = dims.getHeight();

                futures.add(future);
                toCleans.add(aJp2File);

                kduExpand(request, aJp2File, width, height, future);
                JPG_COUNT += 1;

                ImageUtils.getTilePaths(PREFIX, aID, TILE_SIZE, width, height).forEach(tile -> {
                    final Future<Void> tileFuture = Future.future();

                    futures.add(tileFuture);

                    try {
                        kduExpand(new ImageRequest(tile), aJp2File, width, height, tileFuture);
                        JPG_COUNT += 1;
                    } catch (final IIIFException | IOException details) {
                        tileFuture.fail(details);
                    }
                });;
            } catch (final IOException details) {
                System.err.println(details);
                result.fail("Block failed");
            }

            CompositeFuture.all(futures).setHandler(compositeResult -> {
                for (final File file : toCleans) {
                    file.delete();
                }

                if (result.succeeded()) {
                    aFuture.complete();
                } else {
                    aFuture.fail(result.cause());
                }
            });
        }, false, handler);
    }

    private void kduExpand(final ImageRequest aRequest, final File aJp2File, final double aWidth, final double aHeight,
            final Future<Void> aFuture) throws IOException {
        final ImageRegion region = aRequest.getRegion();
        final List<String> command = new ArrayList<>();
        final List<File> toClean = new ArrayList<>();

        // Region coordinates
        final float regionX = region.getFloat(Region.X);
        final float regionY = region.getFloat(Region.Y);
        final float regionW = region.getFloat(Region.WIDTH);
        final float regionH = region.getFloat(Region.HEIGHT);

        final File tmpFile = new File(aJp2File.getParentFile(), UUID.randomUUID() + ".bmp");

        toClean.add(tmpFile);

        command.add(TMP_DIR + "/kdu_expand");
        command.add("-i");
        command.add(aJp2File.getAbsolutePath());
        command.add("-o");
        command.add(tmpFile.getAbsolutePath());
        // command.add("-num_threads");
        // command.add("1");
        command.add("-region");
        command.add(getRegion(regionX, regionY, regionW, regionH, aWidth, aHeight));

        // execute(command.toArray(new String[command.size()]), new KakaduListener(toClean, aRequest, aFuture));
        singleThreadExecute(command.toArray(new String[command.size()]), toClean, aRequest, aFuture);

        System.out.println(StringUtils.toString('|', command));
    }

    private void singleThreadExecute(final String[] aCommand, final List<File> aCleanList, final ImageRequest aRequest,
            final Future<Void> aFuture) {
        try {
            final Process process = Runtime.getRuntime().exec(aCommand);
            final int exitCode = process.waitFor();

            if (exitCode == 0) {
                final FileSystem fileSystem = VERTX.fileSystem();

                // Clean up any temporary files that were created
                for (final File file : aCleanList) {
                    if ("bmp".equals(FileUtils.getExt(file.getName()))) {
                        try {
                            final Buffer buffer = fileSystem.readFileBlocking(file.getAbsolutePath());
                            final JavaImageObject image = new JavaImageObject(buffer);
                            final ImageSize imageSize = aRequest.getSize();
                            final String resourcePath = aRequest.getPath();
                            final String id = aRequest.getID();

                            image.resize(imageSize);

                            myPtRoot.getObject(id).put(resourcePath, image.toBuffer("jpg"), upload -> {
                                if (upload.succeeded()) {
                                    System.out.println("Stored in Pairtree: " + resourcePath);
                                    file.delete();
                                    aFuture.complete();
                                } else {
                                    System.out.println("Failed to store in Pairtree " + resourcePath);
                                    file.delete();
                                    aFuture.fail(upload.cause());
                                }
                            });
                        } catch (final IOException details) {
                            aFuture.fail(details);
                        }
                    }
                }
            } else {
                aFuture.fail("Kakadu exit code: " + exitCode);
            }
        } catch (final IOException | InterruptedException details) {
            aFuture.fail(details);
        }
    }

    private String getRegion(final float aRegionX, final float aRegionY, final float aRegionW, final float aRegionH,
            final double aWidth, final double aHeight) {
        final StringBuilder buffer = new StringBuilder();

        buffer.append('{').append(aRegionY / aHeight).append(',').append(aRegionX / aWidth).append("},{");
        buffer.append(aRegionH / aHeight).append(',').append(aRegionW / aWidth).append('}');

        return buffer.toString();
    }

    /**
     * Main method for the tiling command line tool.
     *
     * @param args
     */
    public static void main(final String[] args) throws FileNotFoundException, IOException {
        final String inputFilePath = args[args.length - 1];
        final PairtreeRoot root = PairtreeFactory.getFactory(VERTX, PairtreeImpl.S3Bucket).getPairtree(args);
        final Future<Void> future = Future.future();
        final CSVReader reader = new CSVReader(new FileReader(inputFilePath));
        final List<String[]> myIDs = reader.readAll();
        final Stopwatch timer = new Stopwatch();

        reader.close();
        timer.start();

        // Confirm that the Pairtree we've supplied exists
        root.exists(existsResult -> {
            if (existsResult.succeeded()) {
                if (existsResult.result()) {
                    try {
                        new TilingCmdLine(root, myIDs).run(future);
                    } catch (final IOException details) {
                        future.fail(details);
                    }
                } else {
                    future.fail("Connected to S3 but no Pairtree found");
                }
            } else {
                future.fail(existsResult.cause());
            }
        });

        // Close up our Vertx instance
        future.setHandler(result -> {
            if (result.failed()) {
                System.err.println(result.result());
            }

            timer.stop();
            System.out.println("Runtime: " + timer.getMilliseconds() + " - " + JPG_COUNT + " tiles");
            VERTX.close();
        });
    }

    private final void execute(final String[] aCommand, final ProcessListener aListener) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(aCommand).inheritIO();
        final ProcessWatcher processWatcher = new ProcessWatcher(processBuilder);

        processWatcher.addListener(aListener);
        processWatcher.start();
    }

    private class KakaduListener implements ProcessListener {

        private final List<File> myTempFiles;

        private final ImageSize myImageSize;

        private final String myResourcePath;

        private final String myID;

        private final Future<Void> myFuture;

        private KakaduListener(final List<File> aCleanList, final ImageRequest aRequest, final Future<Void> aFuture) {
            myTempFiles = aCleanList;
            myImageSize = aRequest.getSize();
            myID = aRequest.getID();
            myResourcePath = aRequest.getPath();
            myFuture = aFuture;
        }

        @Override
        public void processFinished(final Process aProcess) {
            final FileSystem fileSystem = VERTX.fileSystem();
            final int exitCode = aProcess.exitValue();

            switch (exitCode) {
                case 0:
                    break;
                default:
                    System.err.println("Exit value: " + exitCode);
            }

            // Clean up any temporary files that were created
            for (final File file : myTempFiles) {
                if ("bmp".equals(FileUtils.getExt(file.getName()))) {
                    try {
                        final Buffer buffer = fileSystem.readFileBlocking(file.getAbsolutePath());
                        final JavaImageObject image = new JavaImageObject(buffer);

                        image.resize(myImageSize);

                        myPtRoot.getObject(myID).put(myResourcePath, image.toBuffer("jpg"), upload -> {
                            if (upload.succeeded()) {
                                System.out.println("Stored in Pairtree: " + myResourcePath);
                                file.delete();
                                myFuture.complete();
                            } else {
                                System.out.println("Failed to store in Pairtree " + myResourcePath);
                                file.delete();
                                myFuture.fail(upload.cause());
                            }
                        });
                    } catch (final IOException details) {
                        System.err.println(details);
                    }
                }
            }

        }

    }
}
