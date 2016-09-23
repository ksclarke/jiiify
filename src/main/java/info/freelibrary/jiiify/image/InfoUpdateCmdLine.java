
package info.freelibrary.jiiify.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.RegexFileFilter;

public class InfoUpdateCmdLine {

    // private static final String SERVER = "https://stage-images.library.ucla.edu/iiif/";

    private static final String MANIFEST_SYRIAC_2A = "/tmp/syriac-2a-manifest.json";

    private static final String MANIFEST_ARABIC_NF_8 = "/tmp/arabic-nf-8-manifest.json";

    private static final String SYRIAC_MM = "0.048";

    private static final String ARABIC_MM = "0.0296";

    private InfoUpdateCmdLine() {
    }

    /**
     * A cleanup script.
     * <p>
     * <pre><code>java -cp target/jiiify-0.0.1-SNAPSHOT-exec.jar info.freelibrary.jiiify.image.InfoUpdateCmdLine \
     * /media/kevin/LinuxDrive/pairtree_root</code></pre>
     * </p>
     *
     * @param args
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    @SuppressWarnings("unchecked")
    public static void main(final String[] args) throws FileNotFoundException, IOException, ParseException {
        final String syriac = new String(IOUtils.readBytes(new FileInputStream(MANIFEST_SYRIAC_2A)));
        final String arabic = new String(IOUtils.readBytes(new FileInputStream(MANIFEST_ARABIC_NF_8)));
        final RegexFileFilter infoFilter = new RegexFileFilter("^info\\.json$");
        final JSONParser parser = new JSONParser();

        for (final File file : FileUtils.listFiles(new File(args[0]), infoFilter, true)) {
            final FileReader reader = new FileReader(file);
            final JSONObject json = (JSONObject) parser.parse(reader);
            final JSONObject service = new JSONObject();
            final String id = (String) json.get("@id"); // underlying HashMap so only one value possible
            final String ark = id.substring(id.lastIndexOf("/") + 1);

            reader.close();

            if (json.containsKey("service")) {
                json.remove("service");
            }

            json.putIfAbsent("service", service);

            service.put("@context", "http://iiif.io/api/annex/services/physdim/1/context.json");
            service.put("profile", "http://iiif.io/api/annex/services/physdim");
            service.put("physicalUnits", "mm");

            if (syriac.contains(ark)) {
                addScale(json, SYRIAC_MM, file);
            } else if (arabic.contains(ark)) {
                addScale(json, ARABIC_MM, file);
            } else {
                System.err.println("===============> No scale found for: " + file.getAbsolutePath());
            }
        }

    }

    @SuppressWarnings("unchecked")
    private static void addScale(final JSONObject aJSONObject, final String aScale, final File aFile)
            throws IOException {
        final FileWriter writer = new FileWriter(aFile);

        ((JSONObject) aJSONObject.get("service")).put("physicalScale", aScale);

        // Give me some output so I can spot check
        System.out.println(aFile.getAbsolutePath());

        aJSONObject.writeJSONString(writer);
        writer.close();
    }

    /*
     * final RegexDirFilter thumbnailFilter = new RegexDirFilter("^150\\,150$"); final CSVWriter thumbnailWriter = new
     * CSVWriter(new FileWriter("/tmp/first-five-thumbnails.csv")); for (final File file : FileUtils.listFiles(new
     * File(PAIRTREE_ROOT), thumbnailFilter, true)) { final String path = file.getAbsolutePath(); final int startIndex
     * = path.indexOf("ark~="); final String subpath = path.substring(startIndex); final String id =
     * subpath.substring(0, subpath.indexOf('/')).replace('=', '/').replace('~', ':'); final String thumbnail = SERVER
     * + subpath.replace("~", ":").replace("=", "%2F") + "/0/default.jpg"; thumbnailWriter.writeNext(new String[] {
     * id, thumbnail }); } thumbnailWriter.close();
     */
}
