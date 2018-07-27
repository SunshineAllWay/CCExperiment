package org.apache.xerces.jaxp.validation;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.transform.stax.StAXResult;
import org.apache.xerces.xni.XMLDocumentHandler;
interface StAXDocumentHandler extends XMLDocumentHandler {
    public void setStAXResult(StAXResult result);
    public void startDocument(XMLStreamReader reader) throws XMLStreamException;
    public void endDocument(XMLStreamReader reader) throws XMLStreamException;
    public void comment(XMLStreamReader reader) throws XMLStreamException;
    public void processingInstruction(XMLStreamReader reader) throws XMLStreamException;
    public void entityReference(XMLStreamReader reader) throws XMLStreamException;
    public void startDocument(StartDocument event) throws XMLStreamException;
    public void endDocument(EndDocument event) throws XMLStreamException;
    public void doctypeDecl(DTD event) throws XMLStreamException;
    public void characters(Characters event) throws XMLStreamException;
    public void cdata(Characters event) throws XMLStreamException;
    public void comment(Comment event) throws XMLStreamException;
    public void processingInstruction(ProcessingInstruction event) throws XMLStreamException;
    public void entityReference(EntityReference event) throws XMLStreamException;
    public void setIgnoringCharacters(boolean ignore);
} 
