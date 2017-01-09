
package info.freelibrary.jiiify.image;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.util.FileUtils;

public class InfoCreateCmdLine {

    private InfoCreateCmdLine() {
    }

    /**
     * A cleanup script.
     *
     * @param args
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    public static void main(final String[] args) throws FileNotFoundException, IOException, ParseException {
        if (args.length != 2) {
            System.err.println("Usage: \"/path/to/jp2s/dir\" \"0.04576576577\"");
        }

        for (final File file : FileUtils.listFiles(new File(args[0]), new ARKFilter(), true)) {
            final File jp2 = new File(file, file.getName() + ".jp2");
            final String id = file.getName().replace('~', ':').replace('+', ':').replace("=", "%2F");

            if (jp2.exists()) {
                final Dimension dims = ImageUtils.getImageDimension(jp2);
                final File info = new File(file, "info.json");
                final FileWriter writer = new FileWriter(info);
                final ImageInfo imageInfo = new ImageInfo(id);

                imageInfo.setHeight(dims.height);
                imageInfo.setWidth(dims.width);
                imageInfo.setTileSize(1024);
                imageInfo.setPhysicalScale(Double.parseDouble(args[1]), "mm");

                System.out.println(info.getAbsolutePath());

                writer.write(imageInfo.toString());
                writer.close();
            }
        }
    }

    static class ARKFilter implements FilenameFilter {

        @Override
        public boolean accept(final File aDir, final String aFileName) {
            if (aFileName.startsWith("ark") && aFileName.contains("21198")) {
                return true;
            }

            return false;
        }

    }
}
