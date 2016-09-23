package info.freelibrary.jiiify.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler for Kdu's jp2info output.
 *
 * @author Kevin S. Clarke (<a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>)
 */
public class KduWidthHeightHandler extends DefaultHandler {

    final StringBuilder myValue = new StringBuilder();

    int myHeight;

    int myWidth;

    String myLastElement;

    @Override
    public void characters(final char[] aCharArray, final int aStart, final int aLength) throws SAXException {
        if (myLastElement.equals("width") || myLastElement.equals("height")) {
            myValue.append(aCharArray, aStart, aLength);
        }
    }

    @Override
    public void startElement(final String aURI, final String aLocalName, final String aQName,
            final Attributes aAttributes) throws SAXException {
        if (aLocalName.equals("width") || aLocalName.equals("height")) {
            myValue.delete(0, myValue.length());
        }

        myLastElement = aLocalName;
    }

    @Override
    public void endElement(final String aURI, final String aLocalName, final String aQName) {
        if (aLocalName.equals("width")) {
            myWidth = Integer.parseInt(myValue.toString().trim());
        } else if (aLocalName.equals("height")) {
            myHeight = Integer.parseInt(myValue.toString().trim());
        }
    }

}