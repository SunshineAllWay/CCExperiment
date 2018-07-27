package org.apache.xerces.dom;
import java.util.Vector;
import org.apache.xerces.dom3.as.ASAttributeDeclaration;
import org.apache.xerces.dom3.as.ASContentModel;
import org.apache.xerces.dom3.as.ASElementDeclaration;
import org.apache.xerces.dom3.as.ASEntityDeclaration;
import org.apache.xerces.dom3.as.ASModel;
import org.apache.xerces.dom3.as.ASNamedObjectMap;
import org.apache.xerces.dom3.as.ASNotationDeclaration;
import org.apache.xerces.dom3.as.ASObject;
import org.apache.xerces.dom3.as.ASObjectList;
import org.apache.xerces.dom3.as.DOMASException;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.w3c.dom.DOMException;
public class ASModelImpl implements ASModel {
    boolean fNamespaceAware = true;
    protected Vector fASModels;
    protected SchemaGrammar fGrammar = null;
    public ASModelImpl() {
    	fASModels = new Vector();
    }
    public ASModelImpl(boolean isNamespaceAware) {
    	fASModels = new Vector();
        fNamespaceAware = isNamespaceAware;
    }
    public short getAsNodeType() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASModel getOwnerASModel() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setOwnerASModel(ASModel ownerASModel) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public String getNodeName() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setNodeName(String nodeName) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public String getPrefix() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setPrefix(String prefix) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public String getLocalName() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setLocalName(String localName) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public String getNamespaceURI() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setNamespaceURI(String namespaceURI) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASObject cloneASObject(boolean deep) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public boolean getIsNamespaceAware() {
    	return fNamespaceAware;
    }
    public short getUsageLocation() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public String getAsLocation() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setAsLocation(String asLocation) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public String getAsHint() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void setAsHint(String asHint) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public boolean getContainer() {
    	return (fGrammar != null);
    }
    public ASNamedObjectMap getElementDeclarations() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASNamedObjectMap getAttributeDeclarations() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASNamedObjectMap getNotationDeclarations() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASNamedObjectMap getEntityDeclarations() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASNamedObjectMap getContentModelDeclarations() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void addASModel(ASModel abstractSchema) {
    	fASModels.addElement(abstractSchema);
    }
    public ASObjectList getASModels() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void removeAS(ASModel as) {
    	fASModels.removeElement(as);
    }
    public boolean validate() {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void importASObject(ASObject asobject) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public void insertASObject(ASObject asobject) {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASElementDeclaration createASElementDeclaration(String namespaceURI, 
                                                           String name)
                                                           throws DOMException {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASAttributeDeclaration createASAttributeDeclaration(String namespaceURI, 
                                                               String name)
                                                               throws DOMException {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASNotationDeclaration createASNotationDeclaration(String namespaceURI, String name, 
                                                             String systemId, String publicId)
                                                             throws DOMException {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASEntityDeclaration createASEntityDeclaration(String name)
                                                         throws DOMException {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public ASContentModel createASContentModel(int minOccurs, int maxOccurs, 
                                               short operator) throws DOMASException {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }
    public SchemaGrammar getGrammar() {
        return fGrammar;
    }
    public void setGrammar(SchemaGrammar grammar) {
        fGrammar = grammar;
    }
    public Vector getInternalASModels() {
        return fASModels;
    }
}
