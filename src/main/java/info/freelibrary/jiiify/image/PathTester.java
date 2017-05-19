
package info.freelibrary.jiiify.image;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * A temporary command line application for statistics (used for Sinai).
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PathTester {

    private static final String SERVER = "https://sinai-images.library.ucla.edu/iiif/";

    private static final int ARK_INDEX = 0;

    private static final int REGION_INDEX = 1;

    private static final int SIZE_INDEX = 2;

    private static final int ROTATION_INDEX = 3;

    private static final int FILE_INDEX = 3;

    private PathTester() {
    }

    /**
     * Utility class.
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final BufferedReader reader = new BufferedReader(new FileReader(args[0]));

        int thumbnailCount = 0;
        int jp2Count = 0;
        int arkCount = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains("ark~=")) {
                final String path = line.substring(line.indexOf("ark~=")).replace('~', ':').replace("=", "%2F");
                final String[] parts = path.split("/");
                final String ark = parts[ARK_INDEX].replace('=', '/').replace("%2F", "/");

                ++arkCount;

                if (line.endsWith(".jp2")) {
                    ++jp2Count;
                } else if (line.endsWith("manifest.json")) {

                } else if (line.endsWith("image.properties")) {

                } else if (line.endsWith("info.json")) {

                } else if (line.endsWith("README.txt")) {

                } else {
                    try {
                        final String size = parts[SIZE_INDEX];

                        if (size.equals("150,150")) {
                            ++thumbnailCount;
                            printThumbnailsCSV(ark, SERVER + path);
                        }
                    } catch (final ArrayIndexOutOfBoundsException details) {
                        System.err.println("ERROR: " + path);
                    }
                }
            }
        }

        System.err.println();
        System.err.println("ARK count: " + arkCount);
        System.err.println("JP2 count: " + jp2Count);
        System.err.println("Thumbnail count: " + thumbnailCount);

        reader.close();
    }

    private static void printThumbnailsCSV(final String aID, final String aURL) {
        System.out.println("\"" + aID + "\",\"" + aURL + "\"");
    }
}
