
package info.freelibrary.jiiify.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.jiiify.iiif.IIIFException;
import info.freelibrary.jiiify.iiif.ImageRequest;
import info.freelibrary.util.PairtreeRoot;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ReportUtils {

    private static final File CSV_FILE = new File("input.csv");

    private static final File CSV_OUT = new File("output.csv");

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportUtils.class);

    private static int myFoundCount;

    private static int myNotFoundCount;

    private ReportUtils() {
    }

    /**
     * Given an input CSV file,
     *
     * @param args Arguments to the report script
     * @throws IOException If there is trouble reading or writing the CSV files
     */
    public static final void main(final String[] args) throws IOException {
        if (args.length > 1 && args[0].equals("ptcheck")) {
            checkPairtree(args[1]);
        } else {
            System.err.println("Usage: java -cp path/to/my.jar " + ReportUtils.class.getName() +
                    " ptcheck /my/pt/path");
        }
    }

    private static final void checkPairtree(final String aFilePath) throws IOException {
        final CSVReader csvReader = new CSVReader(new FileReader(CSV_FILE));
        final PairtreeRoot ptRoot = new PairtreeRoot(new File(aFilePath));
        final CSVWriter csvWriter = new CSVWriter(new FileWriter(CSV_OUT));

        csvReader.readAll().forEach(image -> {
            try {
                final File imageFile = new File(image[1]);
                final Dimension dim = ImageUtils.getImageDimension(imageFile);

                myFoundCount = 0;
                myNotFoundCount = 0;

                ImageUtils.getTilePaths("/iiif", image[0], 1024, dim.width, dim.height).forEach(path -> {
                    try {
                        final ImageRequest request = new ImageRequest(path);

                        if (request.hasCachedFile(ptRoot)) {
                            myFoundCount++;
                        } else {
                            myNotFoundCount++;
                        }
                    } catch (final IIIFException | IOException details) {
                        try {
                            csvWriter.close();
                            csvReader.close();
                        } catch (final IOException ignorable) {
                            LOGGER.error(ignorable.getMessage(), ignorable);
                        }

                        throw new RuntimeException(details);
                    }
                });

                csvWriter.writeNext(new String[] { imageFile.getAbsolutePath(), Integer.toString(myFoundCount),
                    Integer.toString(myNotFoundCount) });
            } catch (final IOException details) {
                try {
                    csvWriter.close();
                    csvReader.close();
                } catch (final IOException ignorable) {
                    LOGGER.error(ignorable.getMessage(), ignorable);
                }

                throw new RuntimeException(details);
            }
        });

        csvReader.close();
        csvWriter.close();
    }
}
