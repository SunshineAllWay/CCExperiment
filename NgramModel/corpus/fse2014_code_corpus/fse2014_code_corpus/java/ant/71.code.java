package org.apache.tools.ant;
import java.io.Serializable;
import org.apache.tools.ant.util.FileUtils;
import org.xml.sax.Locator;
public class Location implements Serializable {
    private final String fileName;
    private final int lineNumber;
    private final int columnNumber;
    public static final Location UNKNOWN_LOCATION = new Location();
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Location() {
        this(null, 0, 0);
    }
    public Location(String fileName) {
        this(fileName, 0, 0);
    }
    public Location(Locator loc) {
        this(loc.getSystemId(), loc.getLineNumber(), loc.getColumnNumber());
    }
    public Location(String fileName, int lineNumber, int columnNumber) {
        if (fileName != null && fileName.startsWith("file:")) {
            this.fileName = FILE_UTILS.fromURI(fileName);
        } else {
            this.fileName = fileName;
        }
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public String getFileName() {
        return fileName;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public int getColumnNumber() {
        return columnNumber;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (fileName != null) {
            buf.append(fileName);
            if (lineNumber != 0) {
                buf.append(":");
                buf.append(lineNumber);
            }
            buf.append(": ");
        }
        return buf.toString();
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other.getClass() == getClass())) {
            return false;
        }
        return toString().equals(other.toString());
    }
    public int hashCode() {
        return toString().hashCode();
    }
}
