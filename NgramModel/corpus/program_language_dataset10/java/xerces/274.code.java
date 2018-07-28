package org.apache.xerces.dom3.as;
public interface DOMImplementationAS {
    public ASModel createAS(boolean isNamespaceAware);
    public DOMASBuilder createDOMASBuilder();
    public DOMASWriter createDOMASWriter();
}
