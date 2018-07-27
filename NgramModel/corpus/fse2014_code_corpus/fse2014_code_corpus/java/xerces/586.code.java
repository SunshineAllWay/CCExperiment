package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import org.apache.xerces.util.XMLChar;
public final class CharactersImpl extends XMLEventImpl implements Characters {
    private final String fData;
    public CharactersImpl(final String data, final int eventType, final Location location) {
        super(eventType, location);
        fData = (data != null) ? data : "";
    }
    public String getData() {
        return fData;
    }
    public boolean isWhiteSpace() {
        final int length = fData != null ? fData.length() : 0;
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (!XMLChar.isSpace(fData.charAt(i))) {
                return false;
            }
        }
        return true; 
    }
    public boolean isCData() {
        return CDATA == getEventType();
    }
    public boolean isIgnorableWhiteSpace() {
        return SPACE == getEventType();
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write(fData);
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
