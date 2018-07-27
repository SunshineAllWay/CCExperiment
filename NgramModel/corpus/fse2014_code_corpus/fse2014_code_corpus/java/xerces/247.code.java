package org.apache.xerces.dom;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
public class PSVIDOMImplementationImpl extends DOMImplementationImpl {
    static final PSVIDOMImplementationImpl singleton = new PSVIDOMImplementationImpl();
    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }  
    public boolean hasFeature(String feature, String version) {
        return super.hasFeature(feature, version) ||
               feature.equalsIgnoreCase("psvi");
    } 
    protected CoreDocumentImpl createDocument(DocumentType doctype) {
        return new PSVIDocumentImpl(doctype);
    }
} 
