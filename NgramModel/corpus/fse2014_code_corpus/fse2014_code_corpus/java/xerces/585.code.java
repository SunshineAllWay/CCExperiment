package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
public class AttributeImpl extends XMLEventImpl implements Attribute {
    private final boolean fIsSpecified;
    private final QName fName;
    private final String fValue;
    private final String fDtdType;
    public AttributeImpl(final QName name, final String value, final String dtdType, final boolean isSpecified, final Location location) {
        this(ATTRIBUTE, name, value, dtdType, isSpecified, location);
    }
    protected AttributeImpl(final int type, final QName name, final String value, final String dtdType, final boolean isSpecified, final Location location) {
        super(type, location);
        fName = name;
        fValue = value;
        fDtdType = dtdType;
        fIsSpecified = isSpecified;
    }
    public final QName getName() {
        return fName;
    }
    public final String getValue() {
        return fValue;
    }
    public final String getDTDType() {
        return fDtdType;
    }
    public final boolean isSpecified() {
        return fIsSpecified;
    }
    public final void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            String prefix = fName.getPrefix();
            if (prefix != null && prefix.length() > 0) {
                writer.write(prefix);
                writer.write(':');
            }
            writer.write(fName.getLocalPart());
            writer.write("=\"");
            writer.write(fValue);
            writer.write('"');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
