package org.apache.xerces.xni.parser;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
public class XMLParseException
    extends XNIException {
    static final long serialVersionUID = 1732959359448549967L;
    protected String fPublicId;
    protected String fLiteralSystemId;
    protected String fExpandedSystemId;
    protected String fBaseSystemId;
    protected int fLineNumber = -1;
    protected int fColumnNumber = -1;
    protected int fCharacterOffset = -1;
    public XMLParseException(XMLLocator locator, String message) {
        super(message);
        if (locator != null) {
            fPublicId = locator.getPublicId();
            fLiteralSystemId = locator.getLiteralSystemId();
            fExpandedSystemId = locator.getExpandedSystemId();
            fBaseSystemId = locator.getBaseSystemId();
            fLineNumber = locator.getLineNumber();
            fColumnNumber = locator.getColumnNumber();
            fCharacterOffset = locator.getCharacterOffset();
        }
    } 
    public XMLParseException(XMLLocator locator,
                             String message, Exception exception) {
        super(message, exception);
        if (locator != null) {
            fPublicId = locator.getPublicId();
            fLiteralSystemId = locator.getLiteralSystemId();
            fExpandedSystemId = locator.getExpandedSystemId();
            fBaseSystemId = locator.getBaseSystemId();
            fLineNumber = locator.getLineNumber();
            fColumnNumber = locator.getColumnNumber();
            fCharacterOffset = locator.getCharacterOffset();
        }
    } 
    public String getPublicId() {
        return fPublicId;
    } 
    public String getExpandedSystemId() {
        return fExpandedSystemId;
    } 
    public String getLiteralSystemId() {
        return fLiteralSystemId;
    } 
    public String getBaseSystemId() {
        return fBaseSystemId;
    } 
    public int getLineNumber() {
        return fLineNumber;
    } 
    public int getColumnNumber() {
        return fColumnNumber;
    } 
    public int getCharacterOffset() {
        return fCharacterOffset;
    } 
    public String toString() {
        StringBuffer str = new StringBuffer();
        if (fPublicId != null) {
            str.append(fPublicId);
        }
        str.append(':');
        if (fLiteralSystemId != null) {
            str.append(fLiteralSystemId);
        }
        str.append(':');
        if (fExpandedSystemId != null) {
            str.append(fExpandedSystemId);
        }
        str.append(':');
        if (fBaseSystemId != null) {
            str.append(fBaseSystemId);
        }
        str.append(':');
        str.append(fLineNumber);
        str.append(':');
        str.append(fColumnNumber);
        str.append(':');
        str.append(fCharacterOffset);
        str.append(':');
        String message = getMessage();
        if (message == null) {
            Exception exception = getException();
            if (exception != null) {
                message = exception.getMessage();
            }
        }
        if (message != null) {
            str.append(message);
        }
        return str.toString();
    } 
} 
