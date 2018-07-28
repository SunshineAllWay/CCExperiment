package org.apache.xerces.stax.events;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.xerces.stax.EmptyLocation;
import org.apache.xerces.stax.ImmutableLocation;
abstract class XMLEventImpl implements XMLEvent {
    private int fEventType;
    private Location fLocation;
    XMLEventImpl(final int eventType, final Location location) {
        fEventType = eventType;
        if (location != null) {
            fLocation = new ImmutableLocation(location);
        }
        else {
            fLocation = EmptyLocation.getInstance();
        }
    }
    public final int getEventType() {
        return fEventType;
    }
    public final Location getLocation() {
        return fLocation;
    }
    public final boolean isStartElement() {
        return START_ELEMENT == fEventType;
    }
    public final boolean isAttribute() {
        return ATTRIBUTE == fEventType;
    }
    public final boolean isNamespace() {
        return NAMESPACE == fEventType;
    }
    public final boolean isEndElement() {
        return END_ELEMENT == fEventType;
    }
    public final boolean isEntityReference() {
        return ENTITY_REFERENCE == fEventType;
    }
    public final boolean isProcessingInstruction() {
        return PROCESSING_INSTRUCTION == fEventType;
    }
    public final boolean isCharacters() {
        return CHARACTERS == fEventType ||
            CDATA == fEventType ||
            SPACE == fEventType;
    }
    public final boolean isStartDocument() {
        return START_DOCUMENT == fEventType;
    }
    public final boolean isEndDocument() {
        return END_DOCUMENT == fEventType;
    }
    public final StartElement asStartElement() {
        return (StartElement) this;
    }
    public final EndElement asEndElement() {
        return (EndElement) this;
    }
    public final Characters asCharacters() {
        return (Characters) this;
    }
    public final QName getSchemaType() {
        return null;
    }
    public final String toString() {
        final StringWriter writer = new StringWriter();
        try {
            writeAsEncodedUnicode(writer);
        }
        catch (XMLStreamException xse) {}
        return writer.toString();
    }
}
