package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
public final class EntityReferenceImpl extends XMLEventImpl implements
        EntityReference {
    private final String fName;
    private final EntityDeclaration fDecl;
    public EntityReferenceImpl(final EntityDeclaration decl, final Location location) {
        this(decl != null ? decl.getName() : "", decl, location);
    }
    public EntityReferenceImpl(final String name, final EntityDeclaration decl, final Location location) {
        super(ENTITY_REFERENCE, location);
        fName = (name != null) ? name : "";
        fDecl = decl;
    }
    public EntityDeclaration getDeclaration() {
        return fDecl;
    }
    public String getName() {
        return fName;
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write('&');
            writer.write(fName);
            writer.write(';');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
