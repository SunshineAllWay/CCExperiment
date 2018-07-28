package org.apache.xerces.util;
import org.apache.xerces.xni.XMLResourceIdentifier;
public class XMLResourceIdentifierImpl
    implements XMLResourceIdentifier {
    protected String fPublicId;
    protected String fLiteralSystemId;
    protected String fBaseSystemId;
    protected String fExpandedSystemId;
    protected String fNamespace;
    public XMLResourceIdentifierImpl() {} 
    public XMLResourceIdentifierImpl(String publicId,
                                     String literalSystemId, String baseSystemId,
                                     String expandedSystemId) {
        setValues(publicId, literalSystemId, baseSystemId,
                  expandedSystemId, null);
    } 
    public XMLResourceIdentifierImpl(String publicId, String literalSystemId,
                                     String baseSystemId, String expandedSystemId,
                                     String namespace) {
        setValues(publicId, literalSystemId, baseSystemId,
                  expandedSystemId, namespace);
    } 
    public void setValues(String publicId, String literalSystemId,
                          String baseSystemId, String expandedSystemId) {
        setValues(publicId, literalSystemId, baseSystemId,
                  expandedSystemId, null);
    } 
    public void setValues(String publicId, String literalSystemId,
                          String baseSystemId, String expandedSystemId,
                          String namespace) {
        fPublicId = publicId;
        fLiteralSystemId = literalSystemId;
        fBaseSystemId = baseSystemId;
        fExpandedSystemId = expandedSystemId;
        fNamespace = namespace;
    } 
    public void clear() {
        fPublicId = null;
        fLiteralSystemId = null;
        fBaseSystemId = null;
        fExpandedSystemId = null;
        fNamespace = null;
    } 
    public void setPublicId(String publicId) {
        fPublicId = publicId;
    } 
    public void setLiteralSystemId(String literalSystemId) {
        fLiteralSystemId = literalSystemId;
    } 
    public void setBaseSystemId(String baseSystemId) {
        fBaseSystemId = baseSystemId;
    } 
    public void setExpandedSystemId(String expandedSystemId) {
        fExpandedSystemId = expandedSystemId;
    } 
    public void setNamespace(String namespace) {
        fNamespace = namespace;
    } 
    public String getPublicId() {
        return fPublicId;
    } 
    public String getLiteralSystemId() {
        return fLiteralSystemId;
    } 
    public String getBaseSystemId() {
        return fBaseSystemId;
    } 
    public String getExpandedSystemId() {
        return fExpandedSystemId;
    } 
    public String getNamespace() {
        return fNamespace;
    } 
    public int hashCode() {
        int code = 0;
        if (fPublicId != null) {
            code += fPublicId.hashCode();
        }
        if (fLiteralSystemId != null) {
            code += fLiteralSystemId.hashCode();
        }
        if (fBaseSystemId != null) {
            code += fBaseSystemId.hashCode();
        }
        if (fExpandedSystemId != null) {
            code += fExpandedSystemId.hashCode();
        }
        if (fNamespace != null) {
            code += fNamespace.hashCode();
        }
        return code;
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
        if (fBaseSystemId != null) {
            str.append(fBaseSystemId);
        }
        str.append(':');
        if (fExpandedSystemId != null) {
            str.append(fExpandedSystemId);
        }
        str.append(':');
        if (fNamespace != null) {
            str.append(fNamespace);
        }
        return str.toString();
    } 
} 
