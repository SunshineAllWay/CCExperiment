package org.apache.xerces.xni.parser;
import org.apache.xerces.xni.XNIException;
public class XMLConfigurationException
    extends XNIException {
    static final long serialVersionUID = -5437427404547669188L;
    public static final short NOT_RECOGNIZED = 0;
    public static final short NOT_SUPPORTED = 1;
    protected short fType;
    protected String fIdentifier;
    public XMLConfigurationException(short type, String identifier) {
        super(identifier);
        fType = type;
        fIdentifier = identifier;
    } 
    public XMLConfigurationException(short type, String identifier,
                                     String message) {
        super(message);
        fType = type;
        fIdentifier = identifier;
    } 
    public short getType() {
        return fType;
    } 
    public String getIdentifier() {
        return fIdentifier;
    } 
} 
