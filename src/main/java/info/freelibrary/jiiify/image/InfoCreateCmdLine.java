
package info.freelibrary.jiiify.image;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import info.freelibrary.jiiify.util.ImageUtils;
import info.freelibrary.util.FileUtils;

public class InfoCreateCmdLine {

    private static final String MANIFEST_CPA_NF_FRG_5 = "/tmp/cpa_nf_frg_5-manifest.json";

    private static final String CPA_NF_FRG_5_MM = "0.037910448";

    private static final String OTHER_MM = "0.026266805";

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
    @SuppressWarnings("unchecked")
    public static void main(final String[] args) throws FileNotFoundException, IOException, ParseException {
        if (args.length != 2) {
            System.out.println("Please supply physicalScale (for instance: 0.026266805)");
            System.exit(1);
        }

        for (final File file : FileUtils.listFiles(new File(args[0]), new ARKFilter(), true)) {
            final File jp2 = new File(file, file.getName() + ".jp2");
            final String id = file.getName().replace('~', ':').replace('+', ':').replace("=", "%2F");

            if (jp2.exists()) {
                final Dimension dims = ImageUtils.getImageDimension(jp2);
                final File info = new File(file, "info.json");
                final FileWriter writer = new FileWriter(info);
                final JSONObject json = new JSONObject();

                json.put("width", (int) dims.getWidth());
                json.put("height", (int) dims.getHeight());
                json.put("@id", "https://sinai-images.library.ucla.edu/iiif/" + id);
                json.put("@context", "http://iiif.io/api/image/2/context.json");

                final JSONArray tiles = new JSONArray();
                final JSONObject tilesObject = new JSONObject();
                final JSONArray scaleFactors = new JSONArray();

                scaleFactors.addAll(Arrays.asList(new Integer[] { 1, 2, 4 }));
                tilesObject.put("scaleFactors", scaleFactors);
                tilesObject.put("width", 1024);
                tiles.add(tilesObject);
                json.put("tiles", tiles);

                final JSONArray profile = new JSONArray();
                final JSONObject profileObject = new JSONObject();
                final JSONArray formats = new JSONArray();
                final JSONArray qualities = new JSONArray();

                profile.add("http://iiif.io/api/image/2/level0.json");
                formats.add("jpg");
                profileObject.put("formats", formats);
                qualities.add("default");
                profileObject.put("qualities", qualities);
                profile.add(profileObject);
                json.put("profile", profile);

                final JSONObject service = new JSONObject();

                service.put("@context", "http://iiif.io/api/annex/services/physdim/1/context.json");
                service.put("profile", "http://iiif.io/api/annex/services/physdim");
                service.put("physicalScale", args[1]);
                service.put("physicalUnits", "mm");
                json.put("service", service);

                json.put("protocol", "http://iiif.io/api/image");

                // Give me some output so I can spot check
                System.out.println(info.getAbsolutePath());
                // final int start = "/media/kevin/LinuxDrive/pairtree_root_1/".length();
                // System.out.println("cp " + info.getAbsolutePath().substring(start) + " " + info.getAbsolutePath());

                json.writeJSONString(writer);
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
