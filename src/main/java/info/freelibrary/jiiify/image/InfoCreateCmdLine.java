
package info.freelibrary.jiiify.image;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;

import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.util.FileUtils;

public class InfoCreateCmdLine {

    private static final Map<String, String> MM_MAP = new HashMap<>();

    static {
        MM_MAP.put("ark:/21198/z1pr7wv9", "0.0296037296");
        MM_MAP.put("ark:/21198/z1pk0h05", "0.0296037296");
        MM_MAP.put("ark:/21198/z1mg7qd1", "0.0296037296");
        MM_MAP.put("ark:/21198/z1k07571", "0.0296037296");
        MM_MAP.put("ark:/21198/z1h70gth", "0.0296037296");
        MM_MAP.put("ark:/21198/z1ff3tcd", "0.0296037296");
        MM_MAP.put("ark:/21198/z1dv1kxr", "0.0296037296");
        MM_MAP.put("ark:/21198/z179454j", "0.02760869565");
        MM_MAP.put("ark:/21198/z17081w5", "0.02760869565");
        MM_MAP.put("ark:/21198/z16q1xqz", "0.02760869565");
        MM_MAP.put("ark:/21198/z16d5tfj", "0.02760869565");
        MM_MAP.put("ark:/21198/z15d8sc2", "0.02626680455");
        MM_MAP.put("ark:/21198/z1542p4n", "0.02626680455");
        MM_MAP.put("ark:/21198/z1fq9wzw", "0.02626680455");
        MM_MAP.put("ark:/21198/z1ff3sp1", "0.02626680455");
        MM_MAP.put("ark:/21198/z1319wgm", "0.03155279503");
        MM_MAP.put("ark:/21198/z12r3s8b", "0.03155279503");
        MM_MAP.put("ark:/21198/z12f7p2t", "0.03155279503");
        MM_MAP.put("ark:/21198/z1251jsj", "0.03155279503");
        MM_MAP.put("ark:/21198/z1d79brf", "0.03791044776");
        MM_MAP.put("ark:/21198/z1ws8txz", "0.03135802469");
        MM_MAP.put("ark:/21198/z19887f6", "0.02626680455");
        MM_MAP.put("ark:/21198/z1902477", "0.02626680455");
        MM_MAP.put("ark:/21198/z17s7p72", "0.02626680455");
    }

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
        for (final File file : FileUtils.listFiles(new File(args[0]), new ARKFilter(), true)) {
            final File jp2 = new File(file, file.getName() + ".jp2");
            final String id = file.getName().replace('~', ':').replace('+', ':').replace("=", "%2F");

            if (jp2.exists()) {
                final Dimension dims = ImageUtils.getImageDimension(jp2);
                final File info = new File(file, "info.json");
                final FileWriter writer = new FileWriter(info);
                final ImageInfo imageInfo = new ImageInfo(id);
                final String mmValue = MM_MAP.get(id.replace("%2F", "/"));

                imageInfo.setHeight(dims.height);
                imageInfo.setWidth(dims.width);
                imageInfo.setTileSize(1024);

                if (mmValue != null) {
                    imageInfo.setPhysicalScale(Double.parseDouble(mmValue), "mm");
                    System.out.println(info.getAbsolutePath());
                    writer.write(imageInfo.toString());
                } else {
                    System.out.println(id.replace("%2F", "/") + " didn't have mm value");
                }

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
