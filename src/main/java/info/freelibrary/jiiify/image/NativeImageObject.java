
package info.freelibrary.jiiify.image;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;

import io.vertx.core.buffer.Buffer;

public class NativeImageObject implements ImageObject {

    private Mat myImage;

    /**
     * Creates new image using underlying native processing libraries.
     *
     * @param aImgBuffer A source image wrapped in a Vertx {@link io.vertx.core.buffer.Buffer}
     */
    public NativeImageObject(final Buffer aImgBuffer) {
        myImage = Imgcodecs.imdecode(new MatOfByte(aImgBuffer.getBytes()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
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
    public Buffer toBuffer(final String aFileExt) throws IOException {
        final int cols = myImage.cols();
        final int rows = myImage.rows();
        final int elemSize = (int) myImage.elemSize();
        final byte[] bytes = new byte[cols * rows * elemSize];

        try {
            myImage.get(0, 0, bytes);
            return Buffer.buffer(bytes);
        } finally {
            myImage.free();
        }
    }

}
