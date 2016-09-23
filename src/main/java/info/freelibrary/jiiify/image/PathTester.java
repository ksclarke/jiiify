
package info.freelibrary.jiiify.image;

import java.io.BufferedReader;
import java.io.FileReader;

public class PathTester {

    private static final String INPUT_FILE = "/home/kevin/sinai-data-list.txt";

    // private static final String INPUT_FILE = "/home/kevin/sinai-data-list.txt";

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
        final BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE));

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

                } else {
                    final String size = parts[SIZE_INDEX];

                    if (size.equals("150,150")) {
                        ++thumbnailCount;
                        printThumbnailsCSV(ark, SERVER + path);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("ARK count: " + arkCount);
        System.out.println("JP2 count: " + jp2Count);
        System.out.println("Thumbnail count: " + thumbnailCount);

        reader.close();
    }

    private static void printThumbnailsCSV(final String aID, final String aURL) {
        System.out.println("\"" + aID + "\",\"" + aURL + "\"");
    }
}
