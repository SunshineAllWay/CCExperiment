package org.apache.xerces.dom3.as;
import org.w3c.dom.DOMException;
public interface DocumentAS {
    public ASModel getActiveASModel();
    public void setActiveASModel(ASModel activeASModel);
    public ASObjectList getBoundASModels();
    public void setBoundASModels(ASObjectList boundASModels);
    public ASModel getInternalAS();
    public void setInternalAS(ASModel as);
    public void addAS(ASModel as);
    public void removeAS(ASModel as);
    public ASElementDeclaration getElementDeclaration()
                                                      throws DOMException;
    public void validate()
                         throws DOMASException;
}
