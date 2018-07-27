package org.apache.batik.dom.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.batik.util.HaltingThread;
import org.apache.batik.util.XMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SAXDocumentFactory
    extends    DefaultHandler
    implements LexicalHandler,
               DocumentFactory {
    protected DOMImplementation implementation;
    protected String parserClassName;
    protected XMLReader parser;
    protected Document document;
    protected DocumentDescriptor documentDescriptor;
    protected boolean createDocumentDescriptor;
    protected Node currentNode;
    protected Locator locator;
    protected StringBuffer stringBuffer = new StringBuffer();
    protected DocumentType doctype;
    protected boolean stringContent;
    protected boolean inDTD;
    protected boolean inCDATA;
    protected boolean inProlog;
    protected boolean isValidating;
    protected boolean isStandalone;
    protected String xmlVersion;
    protected HashTableStack namespaces;
    protected ErrorHandler errorHandler;
    protected interface PreInfo {
        Node createNode(Document doc);
    }
    static class ProcessingInstructionInfo implements PreInfo {
        public String target, data;
        public ProcessingInstructionInfo(String target, String data) {
            this.target = target;
            this.data = data;
        }
        public Node createNode(Document doc) {
            return doc.createProcessingInstruction(target, data);
        }
    }
    static class CommentInfo implements PreInfo {
        public String comment;
        public CommentInfo(String comment) {
            this.comment = comment;
        }
        public Node createNode(Document doc) {
            return doc.createComment(comment);
        }
    }
    static class CDataInfo implements PreInfo {
        public String cdata;
        public CDataInfo(String cdata) {
            this.cdata = cdata;
        }
        public Node createNode(Document doc) {
            return doc.createCDATASection(cdata);
        }
    }
    static class TextInfo implements PreInfo {
        public String text;
        public TextInfo(String text) {
            this.text = text;
        }
        public Node createNode(Document doc) {
            return doc.createTextNode(text);
        }
    }
    protected List preInfo;
    public SAXDocumentFactory(DOMImplementation impl,
                              String parser) {
        implementation           = impl;
        parserClassName          = parser;
    }
    public SAXDocumentFactory(DOMImplementation impl,
                              String parser,
                              boolean dd) {
        implementation           = impl;
        parserClassName          = parser;
        createDocumentDescriptor = dd;
    }
    public Document createDocument(String ns, String root, String uri)
        throws IOException {
        return createDocument(ns, root, uri, new InputSource(uri));
    }
    public Document createDocument(String uri)
        throws IOException {
        return createDocument(new InputSource(uri));
    }
    public Document createDocument(String ns, String root, String uri,
                                   InputStream is) throws IOException {
        InputSource inp = new InputSource(is);
        inp.setSystemId(uri);
        return createDocument(ns, root, uri, inp);
    }
    public Document createDocument(String uri, InputStream is)
        throws IOException {
        InputSource inp = new InputSource(is);
        inp.setSystemId(uri);
        return createDocument(inp);
    }
    public Document createDocument(String ns, String root, String uri,
                                   Reader r) throws IOException {
        InputSource inp = new InputSource(r);
        inp.setSystemId(uri);
        return createDocument(ns, root, uri, inp);
    }
    public Document createDocument(String ns, String root, String uri,
                                   XMLReader r) throws IOException {
        r.setContentHandler(this);
        r.setDTDHandler(this);
        r.setEntityResolver(this);
        try {
            r.parse(uri);
        } catch (SAXException e) {
            Exception ex = e.getException();
            if (ex != null && ex instanceof InterruptedIOException) {
                throw (InterruptedIOException) ex;
            }
            throw new SAXIOException(e);
        }
        currentNode = null;
        Document ret = document;
        document = null;
        doctype = null;
        return ret;
    }
    public Document createDocument(String uri, Reader r) throws IOException {
        InputSource inp = new InputSource(r);
        inp.setSystemId(uri);
        return createDocument(inp);
    }
    protected Document createDocument(String ns, String root, String uri,
                                      InputSource is)
        throws IOException {
        Document ret = createDocument(is);
        Element docElem = ret.getDocumentElement();
        String lname = root;
        String nsURI = ns;
        if (ns == null) {
            int idx = lname.indexOf(':');
            String nsp = (idx == -1 || idx == lname.length()-1)
                ? ""
                : lname.substring(0, idx);
            nsURI = namespaces.get(nsp);
            if (idx != -1 && idx != lname.length()-1) {
                lname = lname.substring(idx+1);
            }
        }
        String docElemNS = docElem.getNamespaceURI();
        if ((docElemNS != nsURI) &&
            ((docElemNS == null) || (!docElemNS.equals(nsURI))))
            throw new IOException
                ("Root element namespace does not match that requested:\n" +
                 "Requested: " + nsURI + "\n" +
                 "Found: " + docElemNS);
        if (docElemNS != null) {
            if (!docElem.getLocalName().equals(lname))
                throw new IOException
                    ("Root element does not match that requested:\n" +
                     "Requested: " + lname + "\n" +
                     "Found: " + docElem.getLocalName());
        } else {
            if (!docElem.getNodeName().equals(lname))
                throw new IOException
                    ("Root element does not match that requested:\n" +
                     "Requested: " + lname + "\n" +
                     "Found: " + docElem.getNodeName());
        }
        return ret;
    }
    static SAXParserFactory saxFactory;
    static {
        saxFactory = SAXParserFactory.newInstance();
    }
    protected Document createDocument(InputSource is)
        throws IOException {
        try {
            if (parserClassName != null) {
                parser = XMLReaderFactory.createXMLReader(parserClassName);
            } else {
                SAXParser saxParser;
                try {
                    saxParser = saxFactory.newSAXParser();
                } catch (ParserConfigurationException pce) {
                    throw new IOException("Could not create SAXParser: "
                            + pce.getMessage());
                }
                parser = saxParser.getXMLReader();
            }
            parser.setContentHandler(this);
            parser.setDTDHandler(this);
            parser.setEntityResolver(this);
            parser.setErrorHandler((errorHandler == null) ?
                                   this : errorHandler);
            parser.setFeature("http://xml.org/sax/features/namespaces",
                              true);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              true);
            parser.setFeature("http://xml.org/sax/features/validation",
                              isValidating);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler",
                               this);
            parser.parse(is);
        } catch (SAXException e) {
            Exception ex = e.getException();
            if (ex != null && ex instanceof InterruptedIOException) {
                throw (InterruptedIOException)ex;
            }
            throw new SAXIOException(e);
        }
        currentNode  = null;
        Document ret = document;
        document     = null;
        doctype      = null;
        locator      = null;
        parser       = null;
        return ret;
    }
    public DocumentDescriptor getDocumentDescriptor() {
        return documentDescriptor;
    }
    public void setDocumentLocator(Locator l) {
        locator = l;
    }
    public void setValidating(boolean isValidating) {
        this.isValidating = isValidating;
    }
    public boolean isValidating() {
        return isValidating;
    }
    public void setErrorHandler(ErrorHandler eh) {
        errorHandler = eh;
    }
    public DOMImplementation getDOMImplementation(String ver) {
        return implementation;
    }
    public void fatalError(SAXParseException ex) throws SAXException {
        throw ex;
    }
    public void error(SAXParseException ex) throws SAXException {
        throw ex;
    }
    public void warning(SAXParseException ex) throws SAXException {
    }
    public void startDocument() throws SAXException {
        preInfo    = new LinkedList();
        namespaces = new HashTableStack();
        namespaces.put("xml", XMLSupport.XML_NAMESPACE_URI);
        namespaces.put("xmlns", XMLSupport.XMLNS_NAMESPACE_URI);
        namespaces.put("", null);
        inDTD        = false;
        inCDATA      = false;
        inProlog     = true;
        currentNode  = null;
        document     = null;
        doctype      = null;
        isStandalone = false;
        xmlVersion   = XMLConstants.XML_VERSION_10;
        stringBuffer.setLength(0);
        stringContent = false;
        if (createDocumentDescriptor) {
            documentDescriptor = new DocumentDescriptor();
        } else {
            documentDescriptor = null;
        }
    }
    public void startElement(String     uri,
                             String     localName,
                             String     rawName,
                             Attributes attributes) throws SAXException {
        if (HaltingThread.hasBeenHalted()) {
            throw new SAXException(new InterruptedIOException());
        }
        if (inProlog) {
            inProlog = false;
            if (parser != null) {
                try {
                    isStandalone = parser.getFeature
                        ("http://xml.org/sax/features/is-standalone");
                } catch (SAXNotRecognizedException ex) {
                }
                try {
                    xmlVersion = (String) parser.getProperty
                        ("http://xml.org/sax/properties/document-xml-version");
                } catch (SAXNotRecognizedException ex) {
                }
            }
        }
        int len = attributes.getLength();
        namespaces.push();
        String version = null;
        for (int i = 0; i < len; i++) {
            String aname = attributes.getQName(i);
            int slen = aname.length();
            if (slen < 5)
                continue;
            if (aname.equals("version")) {
                version = attributes.getValue(i);
                continue;
            }
            if (!aname.startsWith("xmlns"))
                continue;
            if (slen == 5) {
                String ns = attributes.getValue(i);
                if (ns.length() == 0)
                    ns = null;
                namespaces.put("", ns);
            } else if (aname.charAt(5) == ':') {
                String ns = attributes.getValue(i);
                if (ns.length() == 0) {
                    ns = null;
                }
                namespaces.put(aname.substring(6), ns);
            }
        }
        appendStringData();
        Element e;
        int idx = rawName.indexOf(':');
        String nsp = (idx == -1 || idx == rawName.length()-1)
            ? ""
            : rawName.substring(0, idx);
        String nsURI = namespaces.get(nsp);
        if (currentNode == null) {
            implementation = getDOMImplementation(version);
            document = implementation.createDocument(nsURI, rawName, doctype);
            Iterator i = preInfo.iterator();
            currentNode = e = document.getDocumentElement();
            while (i.hasNext()) {
                PreInfo pi = (PreInfo)i.next();
                Node n = pi.createNode(document);
                document.insertBefore(n, e);
            }
            preInfo = null;
        } else {
            e = document.createElementNS(nsURI, rawName);
            currentNode.appendChild(e);
            currentNode = e;
        }
        if (createDocumentDescriptor && locator != null) {
            documentDescriptor.setLocation(e,
                                           locator.getLineNumber(),
                                           locator.getColumnNumber());
        }
        for (int i = 0; i < len; i++) {
            String aname = attributes.getQName(i);
            if (aname.equals("xmlns")) {
                e.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI,
                                 aname,
                                 attributes.getValue(i));
            } else {
                idx = aname.indexOf(':');
                nsURI = (idx == -1)
                    ? null
                    : namespaces.get(aname.substring(0, idx));
                e.setAttributeNS(nsURI, aname, attributes.getValue(i));
            }
        }
    }
    public void endElement(String uri, String localName, String rawName)
        throws SAXException {
        appendStringData(); 
        if (currentNode != null)
            currentNode = currentNode.getParentNode();
        namespaces.pop();
    }
    public void appendStringData() {
        if (!stringContent) return;
        String str = stringBuffer.toString();
        stringBuffer.setLength(0); 
        stringContent = false;
        if (currentNode == null) {
            if (inCDATA) preInfo.add(new CDataInfo(str));
            else         preInfo.add(new TextInfo(str));
        } else {
            Node n;
            if (inCDATA) n = document.createCDATASection(str);
            else         n = document.createTextNode(str);
            currentNode.appendChild(n);
        }
    }
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        stringBuffer.append(ch, start, length);
        stringContent = true;
    }
    public void ignorableWhitespace(char[] ch,
                                    int start,
                                    int length)
        throws SAXException {
        stringBuffer.append(ch, start, length);
        stringContent = true;
    }
    public void processingInstruction(String target, String data)
        throws SAXException {
        if (inDTD)
            return;
        appendStringData(); 
        if (currentNode == null)
            preInfo.add(new ProcessingInstructionInfo(target, data));
        else
            currentNode.appendChild
                (document.createProcessingInstruction(target, data));
    }
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {
        appendStringData(); 
        doctype = implementation.createDocumentType(name, publicId, systemId);
        inDTD = true;
    }
    public void endDTD() throws SAXException {
        inDTD = false;
    }
    public void startEntity(String name) throws SAXException {
    }
    public void endEntity(String name) throws SAXException {
    }
    public void startCDATA() throws SAXException {
        appendStringData(); 
        inCDATA       = true;
        stringContent = true; 
    }
    public void endCDATA() throws SAXException {
        appendStringData(); 
        inCDATA = false;
    }
    public void comment(char[] ch, int start, int length) throws SAXException {
        if (inDTD) return;
        appendStringData();
        String str = new String(ch, start, length);
        if (currentNode == null) {
            preInfo.add(new CommentInfo(str));
        } else {
            currentNode.appendChild(document.createComment(str));
        }
    }
}
