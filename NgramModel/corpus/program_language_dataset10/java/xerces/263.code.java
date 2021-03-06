package org.apache.xerces.dom3.as;
import org.w3c.dom.DOMException;
public interface ASModel extends ASObject {
    public boolean getIsNamespaceAware();
    public short getUsageLocation();
    public String getAsLocation();
    public void setAsLocation(String asLocation);
    public String getAsHint();
    public void setAsHint(String asHint);
    public ASNamedObjectMap getElementDeclarations();
    public ASNamedObjectMap getAttributeDeclarations();
    public ASNamedObjectMap getNotationDeclarations();
    public ASNamedObjectMap getEntityDeclarations();
    public ASNamedObjectMap getContentModelDeclarations();
    public void addASModel(ASModel abstractSchema);
    public ASObjectList getASModels();
    public void removeAS(ASModel as);
    public boolean validate();
    public ASElementDeclaration createASElementDeclaration(String namespaceURI, 
                                                           String name)
                                                           throws DOMException;
    public ASAttributeDeclaration createASAttributeDeclaration(String namespaceURI, 
                                                               String name)
                                                               throws DOMException;
    public ASNotationDeclaration createASNotationDeclaration(String namespaceURI, 
                                                             String name, 
                                                             String systemId, 
                                                             String publicId)
                                                             throws DOMException;
    public ASEntityDeclaration createASEntityDeclaration(String name)
                                                         throws DOMException;
    public ASContentModel createASContentModel(int minOccurs, 
                                               int maxOccurs, 
                                               short operator)
                                               throws DOMASException;
}
