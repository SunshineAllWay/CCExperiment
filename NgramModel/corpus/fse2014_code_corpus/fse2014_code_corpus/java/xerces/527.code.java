package org.apache.xerces.jaxp.validation;
import javax.xml.transform.dom.DOMResult;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
interface DOMDocumentHandler extends XMLDocumentHandler {
    public void setDOMResult(DOMResult result);
    public void doctypeDecl(DocumentType node) throws XNIException;
    public void characters(Text node) throws XNIException;
    public void cdata(CDATASection node) throws XNIException;
    public void comment(Comment node) throws XNIException;
    public void processingInstruction(ProcessingInstruction node) throws XNIException;
    public void setIgnoringCharacters(boolean ignore);
}
