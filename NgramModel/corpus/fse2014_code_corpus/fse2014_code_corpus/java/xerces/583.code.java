package org.apache.xerces.stax;
import javax.xml.stream.Location;
public class ImmutableLocation implements Location {
    private final int fCharacterOffset;
    private final int fColumnNumber;
    private final int fLineNumber;
    private final String fPublicId;
    private final String fSystemId;
    public ImmutableLocation(Location location) {
        this(location.getCharacterOffset(), location.getColumnNumber(), 
                location.getLineNumber(), location.getPublicId(), 
                location.getSystemId());
    }
    public ImmutableLocation(int characterOffset, int columnNumber, int lineNumber, String publicId, String systemId) {
        fCharacterOffset = characterOffset;
        fColumnNumber = columnNumber;
        fLineNumber = lineNumber;
        fPublicId = publicId;
        fSystemId = systemId;
    }
    public int getCharacterOffset() {
        return fCharacterOffset;
    }
    public int getColumnNumber() {
        return fColumnNumber;
    }
    public int getLineNumber() {
        return fLineNumber;
    }
    public String getPublicId() {
        return fPublicId;
    }
    public String getSystemId() {
        return fSystemId;
    }
}
