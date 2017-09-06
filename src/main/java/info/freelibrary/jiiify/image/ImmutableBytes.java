
package info.freelibrary.jiiify.image;

import io.vertx.core.shareddata.Shareable;

/**
 * Creates a class that wraps a byte array in a way to make it shareable in VertX. The assumption / contract here is
 * that anything else that uses the bytes wrapped by this class <i>will not</i> modify them.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public final class ImmutableBytes implements Shareable {

    private final byte[] myBytes;

    /**
     * Creates an immutable wrapper for a byte array.
     *
     * @param aByteArray An byte array to wrap
     */
    public ImmutableBytes(final byte[] aByteArray) {
        myBytes = aByteArray;
    }

    /**
     * Gets the byte array wrapped by the class.
     *
     * @return A byte array
     */
    public byte[] getBytes() {
        return myBytes;
    }
}
