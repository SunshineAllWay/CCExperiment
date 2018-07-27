package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.NotationDeclaration;
public final class NotationDeclarationImpl extends XMLEventImpl implements
        NotationDeclaration {
    private final String fSystemId;
    private final String fPublicId;
    private final String fName;
    public NotationDeclarationImpl(final String name, final String publicId, final String systemId, final Location location) {
        super(NOTATION_DECLARATION, location);
        fName = name;
        fPublicId = publicId;
        fSystemId = systemId;
    }
    public String getName() {
        return fName;
    }
    public String getPublicId() {
        return fPublicId;
    }
    public String getSystemId() {
        return fSystemId;
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write("<!NOTATION ");
            if (fPublicId != null) {
                writer.write("PUBLIC \"");
                writer.write(fPublicId);
                writer.write('\"');
                if (fSystemId != null) {
                    writer.write(" \"");
                    writer.write(fSystemId);
                    writer.write('\"');
                }
            }
            else {
                writer.write("SYSTEM \"");
                writer.write(fSystemId);
                writer.write('\"');
            }
            writer.write(fName);
            writer.write('>');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
