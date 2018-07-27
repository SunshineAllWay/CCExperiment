package org.apache.xerces.jaxp.validation;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.transform.stax.StAXResult;
import org.apache.xerces.util.JAXPNamespaceContextWrapper;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
final class StAXStreamResultBuilder implements StAXDocumentHandler {
    private XMLStreamWriter fStreamWriter;
    private final JAXPNamespaceContextWrapper fNamespaceContext;
    private boolean fIgnoreChars;
    private boolean fInCDATA;
    private final QName fAttrName = new QName();
    public StAXStreamResultBuilder(JAXPNamespaceContextWrapper context) {
        fNamespaceContext = context;
    }
    public void setStAXResult(StAXResult result) {
        fIgnoreChars = false;
        fInCDATA = false;
        fAttrName.clear();
        fStreamWriter = (result != null) ? result.getXMLStreamWriter() : null;
    }
    public void startDocument(XMLStreamReader reader) throws XMLStreamException {
        String version = reader.getVersion();
        String encoding = reader.getCharacterEncodingScheme();
        fStreamWriter.writeStartDocument(encoding != null ? encoding : "UTF-8",
                version != null ? version : "1.0");
    }
    public void endDocument(XMLStreamReader reader) throws XMLStreamException {
        fStreamWriter.writeEndDocument();
        fStreamWriter.flush();
    }
    public void comment(XMLStreamReader reader) throws XMLStreamException {
        fStreamWriter.writeComment(reader.getText());
    }
    public void processingInstruction(XMLStreamReader reader)
            throws XMLStreamException {
        String data = reader.getPIData();
        if (data != null && data.length() > 0) {
            fStreamWriter.writeProcessingInstruction(reader.getPITarget(), data);
        }
        else {
            fStreamWriter.writeProcessingInstruction(reader.getPITarget());
        }
    }
    public void entityReference(XMLStreamReader reader) throws XMLStreamException {
        fStreamWriter.writeEntityRef(reader.getLocalName());
    }
    public void startDocument(StartDocument event) throws XMLStreamException {
        String version = event.getVersion();
        String encoding = event.getCharacterEncodingScheme();
        fStreamWriter.writeStartDocument(encoding != null ? encoding : "UTF-8",
                version != null ? version : "1.0");
    }
    public void endDocument(EndDocument event) throws XMLStreamException {
        fStreamWriter.writeEndDocument();
        fStreamWriter.flush();
    }
    public void doctypeDecl(DTD event) throws XMLStreamException {
        fStreamWriter.writeDTD(event.getDocumentTypeDeclaration());
    }
    public void characters(Characters event) throws XMLStreamException {
        fStreamWriter.writeCharacters(event.getData());
    }
    public void cdata(Characters event) throws XMLStreamException {
        fStreamWriter.writeCData(event.getData());
    }
    public void comment(Comment event) throws XMLStreamException {
        fStreamWriter.writeComment(event.getText());
    }
    public void processingInstruction(ProcessingInstruction event)
            throws XMLStreamException {
        String data = event.getData();
        if (data != null && data.length() > 0) {
            fStreamWriter.writeProcessingInstruction(event.getTarget(), data);
        }
        else {
            fStreamWriter.writeProcessingInstruction(event.getTarget());
        }
    }
    public void entityReference(EntityReference event) throws XMLStreamException {
        fStreamWriter.writeEntityRef(event.getName());
    }
    public void setIgnoringCharacters(boolean ignore) {
        fIgnoreChars = ignore;
    }
    public void startDocument(XMLLocator locator, String encoding,
            NamespaceContext namespaceContext, Augmentations augs)
            throws XNIException {}
    public void xmlDecl(String version, String encoding, String standalone,
            Augmentations augs) throws XNIException {}
    public void doctypeDecl(String rootElement, String publicId,
            String systemId, Augmentations augs) throws XNIException {}
    public void comment(XMLString text, Augmentations augs) throws XNIException {}
    public void processingInstruction(String target, XMLString data,
            Augmentations augs) throws XNIException {}
    public void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        try {
            if (element.prefix.length() > 0) {
                fStreamWriter.writeStartElement(element.prefix, 
                        element.localpart, element.uri != null ? element.uri : "");
            }
            else if (element.uri != null){
                fStreamWriter.writeStartElement(element.uri, element.localpart);
            }
            else {
                fStreamWriter.writeStartElement(element.localpart);
            }
            int size = fNamespaceContext.getDeclaredPrefixCount();
            final javax.xml.namespace.NamespaceContext nc = fNamespaceContext.getNamespaceContext();
            for (int i = 0; i < size; ++i) {
                String prefix = fNamespaceContext.getDeclaredPrefixAt(i);
                String uri = nc.getNamespaceURI(prefix);
                if (prefix.length() == 0) {
                    fStreamWriter.writeDefaultNamespace(uri != null ? uri : "");
                }
                else {
                    fStreamWriter.writeNamespace(prefix, uri != null ? uri : "");
                }
            }
            size = attributes.getLength();
            for (int i = 0; i < size; ++i) {
                attributes.getName(i, fAttrName);
                if (fAttrName.prefix.length() > 0) {
                    fStreamWriter.writeAttribute(fAttrName.prefix, 
                            fAttrName.uri != null ? fAttrName.uri : "", 
                            fAttrName.localpart, attributes.getValue(i));
                }
                else if (fAttrName.uri != null) {
                    fStreamWriter.writeAttribute(fAttrName.uri, 
                            fAttrName.localpart, attributes.getValue(i));
                }
                else {
                    fStreamWriter.writeAttribute(fAttrName.localpart, attributes.getValue(i));
                }
            }
        }
        catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }
    public void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        startElement(element, attributes, augs);
        endElement(element, augs);
    }
    public void startGeneralEntity(String name,
            XMLResourceIdentifier identifier, String encoding,
            Augmentations augs) throws XNIException {}
    public void textDecl(String version, String encoding, Augmentations augs)
            throws XNIException {}
    public void endGeneralEntity(String name, Augmentations augs)
            throws XNIException {}
    public void characters(XMLString text, Augmentations augs)
        throws XNIException {
        if (!fIgnoreChars) {
            try {
                if (!fInCDATA) {
                    fStreamWriter.writeCharacters(text.ch, text.offset, text.length);
                }
                else {
                    fStreamWriter.writeCData(text.toString());
                }
            }
            catch (XMLStreamException e) {
                throw new XNIException(e);
            }
        }
    }
    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
        characters(text, augs);
    }
    public void endElement(QName element, Augmentations augs)
            throws XNIException {
        try {
            fStreamWriter.writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }
    public void startCDATA(Augmentations augs) throws XNIException {
        fInCDATA = true;
    }
    public void endCDATA(Augmentations augs) throws XNIException {
        fInCDATA = false;
    }
    public void endDocument(Augmentations augs) throws XNIException {}
    public void setDocumentSource(XMLDocumentSource source) {}
    public XMLDocumentSource getDocumentSource() {
        return null;
    }
} 
