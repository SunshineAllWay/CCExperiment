package org.apache.batik.dom;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
public class GenericDOMImplementation extends AbstractDOMImplementation {
    protected static final DOMImplementation DOM_IMPLEMENTATION =
        new GenericDOMImplementation();
    public GenericDOMImplementation() {
    }
    public static DOMImplementation getDOMImplementation() {
        return DOM_IMPLEMENTATION;
    }
    public Document createDocument(String namespaceURI,
                                   String qualifiedName,
                                   DocumentType doctype) throws DOMException {
        Document result = new GenericDocument(doctype, this);
        result.appendChild(result.createElementNS(namespaceURI,
                                                  qualifiedName));
        return result;
    }
    public DocumentType createDocumentType(String qualifiedName,
                                           String publicId,
                                           String systemId) {
        if (qualifiedName == null) {
            qualifiedName = "";
        }
        int test = XMLUtilities.testXMLQName(qualifiedName);
        if ((test & XMLUtilities.IS_XML_10_NAME) == 0) {
            throw new DOMException
                (DOMException.INVALID_CHARACTER_ERR,
                 formatMessage("xml.name",
                               new Object[] { qualifiedName }));
        }
        if ((test & XMLUtilities.IS_XML_10_QNAME) == 0) {
            throw new DOMException
                (DOMException.INVALID_CHARACTER_ERR,
                 formatMessage("invalid.qname",
                               new Object[] { qualifiedName }));
        }
        return new GenericDocumentType(qualifiedName, publicId, systemId);
    }
}
