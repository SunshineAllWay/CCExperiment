package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.DTD;
public final class DTDImpl extends XMLEventImpl implements DTD {
    private final String fDTD;
    public DTDImpl(final String dtd, final Location location) {
        super(DTD, location);
        fDTD = (dtd != null) ? dtd : null;
    }
    public String getDocumentTypeDeclaration() {
        return fDTD;
    }
    public Object getProcessedDTD() {
        return null;
    }
    public List getNotations() {
        return Collections.EMPTY_LIST;
    }
    public List getEntities() {
        return Collections.EMPTY_LIST;
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write(fDTD);
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
