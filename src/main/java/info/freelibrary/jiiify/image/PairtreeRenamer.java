
package info.freelibrary.jiiify.image;

import java.io.File;
import java.io.FilenameFilter;

import info.freelibrary.util.FileUtils;

/**
 * @author kevin
 */
public class PairtreeRenamer {

    private PairtreeRenamer() {
    }

    /**
     * Quick and stupid script
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        /* three times for the three occurrences in the path */
        for (final File file : FileUtils.listFiles(new File(args[0]), new PlusFilter(), true)) {
            if (file.renameTo(new File(file.getParentFile(), file.getName().replace('+', '~')))) {
                System.out.println(file);
            }
        }
        for (final File file : FileUtils.listFiles(new File(args[0]), new PlusFilter(), true)) {
            if (file.renameTo(new File(file.getParentFile(), file.getName().replace('+', '~')))) {
                System.out.println(file);
            }
        }
        for (final File file : FileUtils.listFiles(new File(args[0]), new PlusFilter(), true)) {
            if (file.renameTo(new File(file.getParentFile(), file.getName().replace('+', '~')))) {
                System.out.println(file);
            }
        }
    }

    static class PlusFilter implements FilenameFilter {

        @Override
        public boolean accept(final File aDir, final String aFileName) {
            if (aFileName.contains("+")) {
                return true;
            }

            return false;
        }

    }
}
