
package info.freelibrary.jiiify.image;

import static info.freelibrary.jiiify.iiif.ImageFormat.JP2_EXT;
import static info.freelibrary.jiiify.image.PhotometricInterpretation.XPATH;
import static info.freelibrary.util.FileUtils.convertToPermissionsSet;
import static info.freelibrary.util.PairtreeUtils.encodeID;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.util.UUID.randomUUID;
import static javax.xml.xpath.XPathConstants.NODESET;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;

import info.freelibrary.jiiify.iiif.UnsupportedFormatException;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.JarUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.ProcessListener;
import info.freelibrary.util.ProcessWatcher;

/**
 * A workaround until the JNI layer is finished.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class KakaduCmdLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(KakaduCmdLine.class);

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private static final String KDU_COMPRESS = "kdu_compress";

    private static final String KDU_EXPAND = "kdu_expand";

    private static final Pattern JAR_PATTERN = Pattern.compile(
            ".*\\/freelib-kakadu-[0-9\\.]*(-SNAPSHOT)?-[a-z0-9]+\\.jar\\!\\/");

    private final PairtreeRoot myPtRoot;

    // -jp2_space = sRGB | file extension: .jp2
    //
    // jp2 = "sLUM", "sRGB", "sYCC", "iccLUM" or "iccRGB"
    //
    // jpx = "bilevel1", "bilevel2", "YCbCr1", "YCbCr2", "YCbCr3", "PhotoYCC", "CMY", "CMYK", "YCCK", "CIELab",
    // "CIEJab", "sLUM", "sRGB", "sYCC", "esRGB", "esYCC", "ROMMRGB", "YPbPr60", "YPbPr50"
    //
    // For now, we're going to assume sRGB; we'll convert CIELab to that
    private static final String[] COMPRESS_ARGS = new String[] { null, "-rate",
        "2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171", "Creversible=yes", "Clevels=7",
        "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes", "Corder=RLCP", "ORGgen_plt=yes", "ORGtparts=R",
        "Stiles={1024,1024}", "-double_buffering", "10", "-no_weights", "-i", null, "-o", null };

    private static final String[] EXPAND_ARGS = new String[] {};

    static {
        final File kduCompress = new File(TMP_DIR, KDU_COMPRESS);
        final File kduExpand = new File(TMP_DIR, KDU_EXPAND);

        try {
            for (final URL url : JarUtils.getJarURLs()) {
                final String jarPath = url.toExternalForm();

                if (JAR_PATTERN.matcher(jarPath).matches()) {
                    JarUtils.extract(jarPath, KDU_COMPRESS, new File(TMP_DIR));
                    JarUtils.extract(jarPath, KDU_EXPAND, new File(TMP_DIR));

                    // TODO: support Windows?
                    setPosixFilePermissions(kduCompress.toPath(), convertToPermissionsSet(0700));
                    setPosixFilePermissions(kduExpand.toPath(), convertToPermissionsSet(0700));
                }
            }
        } catch (final IOException details) {
            throw new ExceptionInInitializerError(details);
        }

        if (!kduCompress.exists()) {
            throw new ExceptionInInitializerError("Failed to find kdu_compress command line program");
        }

        if (!kduExpand.exists()) {
            throw new ExceptionInInitializerError("Failed to find kdu_expand command line program");
        }
    }

    /**
     * Creates a new Kakadu command line object that will write to the supplied Pairtree.
     *
     * @param aPtRoot A Pairtree into which and from which images will be written and read
     * @throws IOException Is there is trouble writing or reading the images
     * @throws InterruptedException If the command line execution is interrupted
     */
    public KakaduCmdLine(final PairtreeRoot aPtRoot) throws IOException, InterruptedException {
        myPtRoot = aPtRoot;
    }

    /**
     * Creates a JP2 image.
     *
     * @param aID The ID of the image, which will be used as the JP2's name
     * @param aImageFile The source image file
     * @throws IOException If there is trouble creating the JP2
     */
    public final void createJP2(final String aID, final File aImageFile) throws IOException {
        final PairtreeObject ptObj = myPtRoot.getObject(aID);

        if (!ptObj.exists() && !ptObj.mkdirs()) {
            System.err.println("Can't create Pairtree structure: " + ptObj);
        } else {
            run(KDU_COMPRESS, aImageFile, new File(ptObj, encodeID(aID) + "." + JP2_EXT));
        }
    }

    private void run(final String aCommand, final File aInputFile, final File aOutputFile) throws IOException {
        final List<File> tempFiles = new ArrayList<File>();
        final String[] command;

        if (aCommand.equals(KDU_COMPRESS)) {
            command = Arrays.copyOf(COMPRESS_ARGS, COMPRESS_ARGS.length);

            // If TIFF, convert any color spaces that don't fit in JP2 (not JPX)
            if (FileUtils.getExt(aInputFile.getName()).matches("(?i)^tiff?$")) {
                // Can't rely on ImageIO.read(aInputFile).getColorModel().getColorSpace() for TIFF color space because
                // TwelveMonkeys image library converts CIELab to sRGB on read -- create ticket to preserve CIELab(?)
                final ImageInputStream iis = ImageIO.createImageInputStream(aInputFile);
                final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

                readers.forEachRemaining(reader -> {
                    reader.setInput(iis, true);

                    try {
                        final String filePath = getDestFile(reader.getImageMetadata(0), aInputFile, tempFiles);

                        if (filePath != null) {
                            command[16] = filePath;
                        }
                    } catch (final IOException details) {
                        System.err.println(details);
                    } catch (final UnsupportedFormatException details) {
                        System.err.println(details);
                    } catch (final XPathExpressionException details) {
                        System.err.println(details);
                    } finally {
                        reader.dispose();
                    }
                });

            }

            command[0] = new File(TMP_DIR, KDU_COMPRESS).getAbsolutePath();
            command[18] = aOutputFile.getAbsolutePath();
        } else if (aCommand.equals(KDU_EXPAND)) {
            command = Arrays.copyOf(EXPAND_ARGS, EXPAND_ARGS.length);
            command[0] = new File(TMP_DIR, KDU_EXPAND).getAbsolutePath();
        } else {
            throw new RuntimeException("Unexpected Kakadu command");
        }

        execute(command, new KakaduListener(tempFiles));
    }

    private final String getDestFile(final IIOMetadata aMetadata, final File aSrcFile, final List<File> aTempFileList)
            throws XPathExpressionException, UnsupportedFormatException, IOException {
        final String[] names = aMetadata.getMetadataFormatNames();
        final int length = names.length;

        for (int index = 0; index < length; index++) {
            final XPath xPath = XPathFactory.newInstance().newXPath();
            final Node metadataNode = aMetadata.getAsTree(names[index]);
            final NodeList nodes = (NodeList) xPath.evaluate(XPATH, metadataNode, NODESET);

            if (nodes.getLength() > 0) {
                final Node node = nodes.item(0);
                final NamedNodeMap atts = node.getFirstChild().getFirstChild().getAttributes();
                final String value = atts.getNamedItem("value").getNodeValue();

                switch (Integer.parseInt(value)) {
                    case 8: // CIELab
                        final String path = System.getProperty("java.io.tmpdir");
                        final String name = FileUtils.stripExt(aSrcFile);
                        final String ext = FileUtils.getExt(aSrcFile.getName());
                        final String uuid = randomUUID().toString();
                        final String tempName = name + "-" + uuid + "." + ext;
                        final File file = new File(new File(path), tempName);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Converting {} to {} to normalize color space", aSrcFile, file);
                        }

                        // This will, at this point, convert from CIELab to sRGB
                        ImageUtils.convert(aSrcFile, file);

                        // Keep a record of this temporary file so we can clean it up
                        aTempFileList.add(file);

                        // Tell Kakadu to read our temporary TIFF file
                        return file.getAbsolutePath();
                    default:
                        return aSrcFile.getAbsolutePath();
                }
            }
        }

        return null;
    }

    private final void execute(final String[] aCommand, final ProcessListener aListener) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(aCommand).inheritIO();
        final ProcessWatcher processWatcher = new ProcessWatcher(processBuilder);

        processWatcher.addListener(aListener);
        processWatcher.start();
    }

    /**
     * Runs the KakaduCmdLine program from a command line.
     *
     * @param args Arguments which include the Pairtree location and the path to the CSV file
     * @throws IOException If there is trouble reading or writing content
     * @throws InterruptedException If the Kakadu command line is interrupted
     */
    public static void main(final String... args) throws IOException, InterruptedException {
        final Options options = new Options();

        options.addOption("p", true, "Location of Pairtree directory");
        options.addOption("f", true, "Path to CSV file with IDs and file paths");

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmdLine = parser.parse(options, args);

            if (!cmdLine.hasOption("p") || !cmdLine.hasOption("f")) {
                printHelp("Please supply Pairtree directory and a path to the CSV file", options);
            } else {
                final File ptRootDir = new File(cmdLine.getOptionValue("p"));

                if (!ptRootDir.exists() && !ptRootDir.mkdirs()) {
                    System.err.println("Can't find or create Pairtree root: " + ptRootDir);
                } else {
                    final PairtreeRoot ptRoot = new PairtreeRoot(ptRootDir.getAbsolutePath());
                    final File csvFile = new File(cmdLine.getOptionValue("f"));

                    if (!csvFile.exists()) {
                        System.err.println("Can't find supplied CSV file: " + csvFile);
                    } else {
                        final KakaduCmdLine kakadu = new KakaduCmdLine(ptRoot);
                        final CSVReader reader = new CSVReader(new FileReader(csvFile));

                        try {
                            final List<String[]> valuesList = reader.readAll();

                            int totalImages = valuesList.size();

                            for (int index = 0; index < valuesList.size(); index++) {
                                final String[] values = valuesList.get(index);
                                final String id = values[0].trim();

                                // If it's a comment or blank line, we just skip it
                                if (!id.startsWith("#") && !id.isEmpty()) {
                                    final File imageFile = new File(values[1]);

                                    if (!imageFile.exists()) {
                                        System.err.println("Image file doesn't exist: " + imageFile);
                                    } else {
                                        // We supply image ID and file to Kakadu
                                        try {
                                            kakadu.createJP2(values[0], imageFile);

                                            System.out.println("Converted: " + imageFile + " (" + (index + 1 -
                                                    (valuesList.size() - totalImages)) + " out of " + totalImages +
                                                    ")");
                                        } catch (final IOException details) {
                                            System.err.println(details.getMessage());
                                        }
                                    }
                                } else {
                                    --totalImages;
                                }
                            }
                        } finally {
                            reader.close();
                        }
                    }
                }
            }
        } catch (final ParseException details) {
            printHelp("Error parsing program's startup arguments", options);
        }
    }

    private static void printHelp(final String aMessage, final Options aOptions) {
        final String help = "java -cp target/jiiify-*-exec.jar info.freelibrary.jiiify.image.KakaduCmdLine";
        new HelpFormatter().printHelp(help, aOptions);
    }

    private class KakaduListener implements ProcessListener {

        private final Logger LOGGER = LoggerFactory.getLogger(KakaduListener.class);

        private final List<File> myTempFiles;

        private KakaduListener(final List<File> aCleanupList) {
            myTempFiles = aCleanupList;
        }

        @Override
        public void processFinished(final Process aProcess) {
            final int exitCode = aProcess.exitValue();

            switch (exitCode) {
                case 0:
                    break;
                default:
                    LOGGER.error("Exit value: {}", aProcess.exitValue());
            }

            // Clean up any temporary files that were created
            for (final File file : myTempFiles) {
                if (!file.delete()) {
                    LOGGER.error("Can't delete file: {}", file);
                } else {
                    LOGGER.debug("Deleted temp file: {}", file);
                }
            }
        }

    }
}
