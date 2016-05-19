
package info.freelibrary.jiiify.image;

import java.io.File;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;

public class NativeImageObject implements ImageObject {

    private Mat myImage;

    /**
     * Creates new image using underlying native processing libraries.
     *
     * @param aSourceImage A source image file
     */
    public NativeImageObject(final File aSourceImage) {
        myImage = Imgcodecs.imread(aSourceImage.getAbsolutePath());
    }

    @Override
    public void extractRegion(final ImageRegion aRegion) throws IOException {
        final Rect roi = new Rect();
        final Mat newImage;

        if (aRegion.isFullImage()) {
            return;
        } else if (aRegion.usesPercentages()) {
            // TODO
        } else {
            roi.height = aRegion.getInt(Region.HEIGHT);
            roi.width = aRegion.getInt(Region.WIDTH);
            roi.x = aRegion.getInt(Region.X);
            roi.y = aRegion.getInt(Region.Y);
        }

        newImage = myImage.submat(roi);
        myImage.free();
        myImage = newImage;
    }

    @Override
    public void resize(final ImageSize aSize) throws IOException {
        final Mat resizedImage;

        if (!aSize.isFullSize()) {
            final int width = aSize.getWidth(myImage.width(), myImage.height());
            final int height = aSize.getHeight(myImage.height(), myImage.width());

            resizedImage = new Mat(myImage.rows(), myImage.cols(), myImage.type());
            Imgproc.resize(myImage, resizedImage, new Size(width, height));
            myImage.free();
            myImage = resizedImage;
        }
    }

    @Override
    public void rotate(final ImageRotation aRotation) throws IOException {

    }

    @Override
    public void adjustQuality(final ImageQuality aQuality) throws IOException {

    }

    @Override
    public void write(final File aImageFile) throws IOException {
        final File parent = aImageFile.getParentFile();
        final boolean written;

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create directory structure: " + parent);
        }

        written = Imgcodecs.imwrite(aImageFile.getAbsolutePath(), myImage);
        myImage.free();

        if (!written) {
            throw new IOException("Unable to write image with native libraries");
        }
    }

}
