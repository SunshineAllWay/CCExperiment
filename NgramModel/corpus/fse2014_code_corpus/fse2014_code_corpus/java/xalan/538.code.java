package org.apache.xalan.xsltc.trax;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import org.apache.xalan.xsltc.dom.SAXImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
public class DOM2SAX implements XMLReader, Locator {
    private final static String EMPTYSTRING = "";
    private static final String XMLNS_PREFIX = "xmlns";
    private Node _dom = null;
    private ContentHandler _sax = null;
    private LexicalHandler _lex = null;
    private SAXImpl _saxImpl = null;
    private Hashtable _nsPrefixes = new Hashtable();
    public DOM2SAX(Node root) {
	_dom = root;
    }
    public ContentHandler getContentHandler() { 
	return _sax;
    }
    public void setContentHandler(ContentHandler handler) throws 
	NullPointerException 
    {
	_sax = handler;
	if (handler instanceof LexicalHandler) {
	    _lex = (LexicalHandler) handler;
	}
	if (handler instanceof SAXImpl) {
	    _saxImpl = (SAXImpl)handler;
	}
    }
    private boolean startPrefixMapping(String prefix, String uri) 
	throws SAXException 
    {
	boolean pushed = true;
	Stack uriStack = (Stack) _nsPrefixes.get(prefix);
	if (uriStack != null) {
	    if (uriStack.isEmpty()) {
		_sax.startPrefixMapping(prefix, uri);
		uriStack.push(uri);
	    }
	    else {
		final String lastUri = (String) uriStack.peek();
		if (!lastUri.equals(uri)) {
		    _sax.startPrefixMapping(prefix, uri);
		    uriStack.push(uri);
		}
		else {
		    pushed = false;
		}
	    }	
	}
	else {
	    _sax.startPrefixMapping(prefix, uri);
	    _nsPrefixes.put(prefix, uriStack = new Stack());
	    uriStack.push(uri);
	}
	return pushed;
    }
    private void endPrefixMapping(String prefix) 
	throws SAXException
    {
	final Stack uriStack = (Stack) _nsPrefixes.get(prefix);
	if (uriStack != null) {
	    _sax.endPrefixMapping(prefix);
	    uriStack.pop();
	}
    }
    private static String getLocalName(Node node) {
	final String localName = node.getLocalName();
	if (localName == null) {
	    final String qname = node.getNodeName();
	    final int col = qname.lastIndexOf(':');
	    return (col > 0) ? qname.substring(col + 1) : qname;
	}
	return localName;
    }
    public void parse(InputSource unused) throws IOException, SAXException {
        parse(_dom);
    }
    public void parse() throws IOException, SAXException {
	if (_dom != null) {
	    boolean isIncomplete = 
		(_dom.getNodeType() != org.w3c.dom.Node.DOCUMENT_NODE);
	    if (isIncomplete) {
		_sax.startDocument();
		parse(_dom);
		_sax.endDocument();
	    }
	    else {
		parse(_dom);
	    }
	}
    }
    private void parse(Node node) throws IOException, SAXException {
        Node first = null;
 	if (node == null) return;
        switch (node.getNodeType()) {
	case Node.ATTRIBUTE_NODE:         
	case Node.DOCUMENT_FRAGMENT_NODE:
	case Node.DOCUMENT_TYPE_NODE :
	case Node.ENTITY_NODE :
	case Node.ENTITY_REFERENCE_NODE:
	case Node.NOTATION_NODE :
	    break;
	case Node.CDATA_SECTION_NODE:
	    final String cdata = node.getNodeValue();
	    if (_lex != null) {
		_lex.startCDATA();
	        _sax.characters(cdata.toCharArray(), 0, cdata.length());
		_lex.endCDATA();
 	    } 
	    else {
	        _sax.characters(cdata.toCharArray(), 0, cdata.length());
	    }	
	    break;
	case Node.COMMENT_NODE:           
	    if (_lex != null) {
		final String value = node.getNodeValue();
		_lex.comment(value.toCharArray(), 0, value.length());
	    }
	    break;
	case Node.DOCUMENT_NODE:
	    _sax.setDocumentLocator(this);
	    _sax.startDocument();
	    Node next = node.getFirstChild();
	    while (next != null) {
		parse(next);
		next = next.getNextSibling();
	    }
	    _sax.endDocument();
	    break;
	case Node.ELEMENT_NODE:
	    String prefix;
	    List pushedPrefixes = new ArrayList();
	    final AttributesImpl attrs = new AttributesImpl();
	    final NamedNodeMap map = node.getAttributes();
	    final int length = map.getLength();
	    for (int i = 0; i < length; i++) {
		final Node attr = map.item(i);
		final String qnameAttr = attr.getNodeName();
		if (qnameAttr.startsWith(XMLNS_PREFIX)) {
		    final String uriAttr = attr.getNodeValue();
		    final int colon = qnameAttr.lastIndexOf(':');
		    prefix = (colon > 0) ? qnameAttr.substring(colon + 1) : EMPTYSTRING;
		    if (startPrefixMapping(prefix, uriAttr)) {
			pushedPrefixes.add(prefix);
		    }
		}
	    }
	    for (int i = 0; i < length; i++) {
		final Node attr = map.item(i);
		final String qnameAttr = attr.getNodeName();
		if (!qnameAttr.startsWith(XMLNS_PREFIX)) {
		    final String uriAttr = attr.getNamespaceURI();
		    final String localNameAttr = getLocalName(attr);
		    if (uriAttr != null) {	
			final int colon = qnameAttr.lastIndexOf(':');
			prefix = (colon > 0) ? qnameAttr.substring(0, colon) : EMPTYSTRING;
			if (startPrefixMapping(prefix, uriAttr)) {
			    pushedPrefixes.add(prefix);
			}
		    }
		    attrs.addAttribute(attr.getNamespaceURI(), getLocalName(attr), 
			qnameAttr, "CDATA", attr.getNodeValue());
		}
	    }
	    final String qname = node.getNodeName();
	    final String uri = node.getNamespaceURI();
	    final String localName = getLocalName(node);
	    if (uri != null) {	
		final int colon = qname.lastIndexOf(':');
		prefix = (colon > 0) ? qname.substring(0, colon) : EMPTYSTRING;
		if (startPrefixMapping(prefix, uri)) {
		    pushedPrefixes.add(prefix);
		}
	    }
	    if (_saxImpl != null) {
	        _saxImpl.startElement(uri, localName, qname, attrs, node);
	    }
	    else {
	        _sax.startElement(uri, localName, qname, attrs);
	    }
	    next = node.getFirstChild();
	    while (next != null) {
		parse(next);
		next = next.getNextSibling();
	    }
	    _sax.endElement(uri, localName, qname);
	    final int nPushedPrefixes = pushedPrefixes.size();
	    for (int i = 0; i < nPushedPrefixes; i++) {
		endPrefixMapping((String) pushedPrefixes.get(i));
	    }
	    break;
	case Node.PROCESSING_INSTRUCTION_NODE:
	    _sax.processingInstruction(node.getNodeName(),
				       node.getNodeValue());
	    break;
	case Node.TEXT_NODE:
	    final String data = node.getNodeValue();
	    _sax.characters(data.toCharArray(), 0, data.length());
	    break;
	}
    }
    public DTDHandler getDTDHandler() { 
	return null;
    }
    public ErrorHandler getErrorHandler() {
	return null;
    }
    public boolean getFeature(String name) throws SAXNotRecognizedException,
	SAXNotSupportedException
    {
	return false;
    }
    public void setFeature(String name, boolean value) throws 
	SAXNotRecognizedException, SAXNotSupportedException 
    {
    }
    public void parse(String sysId) throws IOException, SAXException {
	throw new IOException("This method is not yet implemented.");
    }
    public void setDTDHandler(DTDHandler handler) throws NullPointerException {
    }
    public void setEntityResolver(EntityResolver resolver) throws 
	NullPointerException 
    {
    }
    public EntityResolver getEntityResolver() {
	return null;
    }
    public void setErrorHandler(ErrorHandler handler) throws 
	NullPointerException
    {
    }
    public void setProperty(String name, Object value) throws
	SAXNotRecognizedException, SAXNotSupportedException {
    }
    public Object getProperty(String name) throws SAXNotRecognizedException,
	SAXNotSupportedException
    {
	return null;
    }
    public int getColumnNumber() { 
	return 0; 
    }
    public int getLineNumber() { 
	return 0; 
    }
    public String getPublicId() { 
	return null; 
    }
    public String getSystemId() { 
	return null; 
    }
    private String getNodeTypeFromCode(short code) {
	String retval = null;
	switch (code) {
	case Node.ATTRIBUTE_NODE : 
	    retval = "ATTRIBUTE_NODE"; break; 
	case Node.CDATA_SECTION_NODE :
	    retval = "CDATA_SECTION_NODE"; break; 
	case Node.COMMENT_NODE :
	    retval = "COMMENT_NODE"; break; 
	case Node.DOCUMENT_FRAGMENT_NODE :
	    retval = "DOCUMENT_FRAGMENT_NODE"; break; 
	case Node.DOCUMENT_NODE :
	    retval = "DOCUMENT_NODE"; break; 
	case Node.DOCUMENT_TYPE_NODE :
	    retval = "DOCUMENT_TYPE_NODE"; break; 
	case Node.ELEMENT_NODE :
	    retval = "ELEMENT_NODE"; break; 
	case Node.ENTITY_NODE :
	    retval = "ENTITY_NODE"; break; 
	case Node.ENTITY_REFERENCE_NODE :
	    retval = "ENTITY_REFERENCE_NODE"; break; 
	case Node.NOTATION_NODE :
	    retval = "NOTATION_NODE"; break; 
	case Node.PROCESSING_INSTRUCTION_NODE :
	    retval = "PROCESSING_INSTRUCTION_NODE"; break; 
	case Node.TEXT_NODE:
	    retval = "TEXT_NODE"; break; 
        }
	return retval;
    }
}
