package org.apache.xerces.impl.xs.util;
import org.apache.xerces.xni.XMLLocator;
public final class SimpleLocator implements XMLLocator {
    private String lsid;
    private String esid;
    private int line;
    private int column;
    private int charOffset;
    public SimpleLocator() {
    }
    public SimpleLocator(String lsid, String esid, int line, int column) {
        this(lsid, esid, line, column, -1);
    }
    public void setValues(String lsid, String esid, int line, int column) {
        setValues(lsid, esid, line, column, -1);
    }
    public SimpleLocator(String lsid, String esid, int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.lsid = lsid;
        this.esid = esid;
        charOffset = offset;
    }
    public void setValues(String lsid, String esid, int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.lsid = lsid;
        this.esid = esid;
        charOffset = offset;
    }
    public int getLineNumber() {
        return line;
    }
    public int getColumnNumber() {
        return column;
    }
    public int getCharacterOffset() {
        return charOffset;
    }
    public String getPublicId() {
        return null;
    }
    public String getExpandedSystemId() {
        return esid;
    }
    public String getLiteralSystemId() {
        return lsid;
    }
    public String getBaseSystemId() {
        return null;
    }
    public void setColumnNumber(int col) {
        this.column = col;
    }
    public void setLineNumber(int line) {
        this.line = line;
    }
    public void setCharacterOffset(int offset) {
        charOffset = offset;
    }
    public void setBaseSystemId(String systemId) {}
    public void setExpandedSystemId(String systemId) {
        esid = systemId;
    }
    public void setLiteralSystemId(String systemId) {
        lsid = systemId;
    }
    public void setPublicId(String publicId) {}
    public String getEncoding() {
        return null;
    }
    public String getXMLVersion() {
        return null;
    }
}
