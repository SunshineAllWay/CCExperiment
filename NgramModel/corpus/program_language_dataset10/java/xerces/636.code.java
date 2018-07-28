package org.apache.xerces.util;
import org.apache.xerces.impl.XMLEntityDescription;
public class XMLEntityDescriptionImpl 
    extends XMLResourceIdentifierImpl
    implements XMLEntityDescription {
    public XMLEntityDescriptionImpl() {} 
    public XMLEntityDescriptionImpl(String entityName, String publicId, String literalSystemId, 
                                    String baseSystemId, String expandedSystemId) {
        setDescription(entityName, publicId, literalSystemId, baseSystemId, expandedSystemId);
    } 
    public XMLEntityDescriptionImpl(String entityName, String publicId, String literalSystemId,
                                    String baseSystemId, String expandedSystemId, String namespace) {
        setDescription(entityName, publicId, literalSystemId, baseSystemId, expandedSystemId, namespace);
    } 
    protected String fEntityName;
    public void setEntityName(String name) {
        fEntityName = name;
    } 
    public String getEntityName() {
        return fEntityName;
    } 
    public void setDescription(String entityName, String publicId, String literalSystemId,
                               String baseSystemId, String expandedSystemId) {
        setDescription(entityName, publicId, literalSystemId, baseSystemId, expandedSystemId, null);
    } 
    public void setDescription(String entityName, String publicId, String literalSystemId,
                               String baseSystemId, String expandedSystemId, String namespace) {
        fEntityName = entityName;
        setValues(publicId, literalSystemId, baseSystemId, expandedSystemId, namespace);
    } 
    public void clear() {
        super.clear();
        fEntityName = null;
    } 
    public int hashCode() {
        int code = super.hashCode();
        if (fEntityName != null) {
            code += fEntityName.hashCode();
        }
        return code;
    } 
    public String toString() {
        StringBuffer str = new StringBuffer();
        if (fEntityName != null) {
            str.append(fEntityName);
        }
        str.append(':');
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
