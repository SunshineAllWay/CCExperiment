package org.apache.xerces.stax.events;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndDocument;
public final class EndDocumentImpl extends XMLEventImpl implements EndDocument {
    public EndDocumentImpl(Location location) {
        super(END_DOCUMENT, location);
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {}
}
