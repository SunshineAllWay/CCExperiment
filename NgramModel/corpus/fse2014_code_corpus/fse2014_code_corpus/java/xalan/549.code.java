package org.apache.xalan.xsltc.trax;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.dom.DOMResult;
import org.apache.xalan.xsltc.StripFilter;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.dom.DOMWSFilter;
import org.apache.xalan.xsltc.dom.SAXImpl;
import org.apache.xalan.xsltc.dom.XSLTCDTMManager;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.serializer.SerializationHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
public class TransformerHandlerImpl implements TransformerHandler, DeclHandler {
    private TransformerImpl  _transformer;
    private AbstractTranslet _translet = null;
    private String           _systemId;
    private SAXImpl          _dom = null;
    private ContentHandler   _handler = null;
    private LexicalHandler   _lexHandler = null;
    private DTDHandler       _dtdHandler = null;
    private DeclHandler      _declHandler = null;
    private Result           _result = null;
    private Locator          _locator = null;
    private boolean          _done = false; 
    private boolean _isIdentity = false;
    public TransformerHandlerImpl(TransformerImpl transformer) {
	_transformer = transformer;
	if (transformer.isIdentity()) {
	    _handler = new DefaultHandler();
	    _isIdentity = true;
	}
	else {
	    _translet = _transformer.getTranslet();
	}
    }
    public String getSystemId() {
	return _systemId;
    }
    public void setSystemId(String id) {
	_systemId = id;
    }
    public Transformer getTransformer() {
	return _transformer;
    }
    public void setResult(Result result) throws IllegalArgumentException {
	_result = result;
    if (null == result) {
       ErrorMsg err = new ErrorMsg(ErrorMsg.ER_RESULT_NULL);
       throw new IllegalArgumentException(err.toString()); 
    }    
	if (_isIdentity) {
	    try {
		SerializationHandler outputHandler =
		    _transformer.getOutputHandler(result);
		_transformer.transferOutputProperties(outputHandler);
		_handler = outputHandler;
		_lexHandler = outputHandler;
	    }
	    catch (TransformerException e) {
		_result = null;
	    }
	}
	else if (_done) {
	    try {
		_transformer.setDOM(_dom);
		_transformer.transform(null, _result);
	    }
	    catch (TransformerException e) {
		throw new IllegalArgumentException(e.getMessage());
	    }
	}
    }
    public void characters(char[] ch, int start, int length) 
	throws SAXException 
    {
	_handler.characters(ch, start, length);
    }
    public void startDocument() throws SAXException {
	if (_result == null) {
	    ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_SET_RESULT_ERR);
	    throw new SAXException(err.toString());
	}
        if (!_isIdentity) {
            boolean hasIdCall = (_translet != null) ? _translet.hasIdCall() : false;
            XSLTCDTMManager dtmManager = null;
            try {
                dtmManager = 
                    (XSLTCDTMManager)_transformer.getTransformerFactory()
                                                 .getDTMManagerClass()
                                                 .newInstance();
            } catch (Exception e) {
                throw new SAXException(e);
            }
            DTMWSFilter wsFilter;
            if (_translet != null && _translet instanceof StripFilter) {
                wsFilter = new DOMWSFilter(_translet);
            } else {
                wsFilter = null;
            }            
            _dom = (SAXImpl)dtmManager.getDTM(null, false, wsFilter, true,
                                              false, hasIdCall);         
            _handler = _dom.getBuilder();
            _lexHandler = (LexicalHandler) _handler;
            _dtdHandler = (DTDHandler) _handler;
            _declHandler = (DeclHandler) _handler;  
            _dom.setDocumentURI(_systemId);
            if (_locator != null) {
                _handler.setDocumentLocator(_locator);
            }            
        }
	_handler.startDocument();
    }
    public void endDocument() throws SAXException {
	_handler.endDocument();
	if (!_isIdentity) {
	    if (_result != null) {
		try {
		    _transformer.setDOM(_dom);
		    _transformer.transform(null, _result);
		}
		catch (TransformerException e) {
		    throw new SAXException(e);
		}
	    }
	    _done = true;
	    _transformer.setDOM(_dom);
	}
	if (_isIdentity && _result instanceof DOMResult) {
	    ((DOMResult)_result).setNode(_transformer.getTransletOutputHandlerFactory().getNode());
        }
    }
    public void startElement(String uri, String localName,
			     String qname, Attributes attributes)
	throws SAXException 
    {
	_handler.startElement(uri, localName, qname, attributes);
    }
    public void endElement(String namespaceURI, String localName, String qname)
	throws SAXException 
    {
	_handler.endElement(namespaceURI, localName, qname);
    }
    public void processingInstruction(String target, String data)
	throws SAXException 
    {
	_handler.processingInstruction(target, data);
    }
    public void startCDATA() throws SAXException { 
	if (_lexHandler != null) {
	    _lexHandler.startCDATA();
	}
    }
    public void endCDATA() throws SAXException { 
	if (_lexHandler != null) {
	    _lexHandler.endCDATA();
	}
    }
    public void comment(char[] ch, int start, int length) 
	throws SAXException 
    { 
	if (_lexHandler != null) {
	    _lexHandler.comment(ch, start, length);
	}
    }
    public void ignorableWhitespace(char[] ch, int start, int length)
	throws SAXException 
    {
	_handler.ignorableWhitespace(ch, start, length);
    }
    public void setDocumentLocator(Locator locator) {
        _locator = locator;
        if (_handler != null) {
            _handler.setDocumentLocator(locator);
        }
    }
    public void skippedEntity(String name) throws SAXException {
	_handler.skippedEntity(name);
    }
    public void startPrefixMapping(String prefix, String uri) 
	throws SAXException {
	_handler.startPrefixMapping(prefix, uri);
    }
    public void endPrefixMapping(String prefix) throws SAXException {
	_handler.endPrefixMapping(prefix);
    }
    public void startDTD(String name, String publicId, String systemId) 
	throws SAXException
    { 
	if (_lexHandler != null) {
	    _lexHandler.startDTD(name, publicId, systemId);
	}
    }
    public void endDTD() throws SAXException {
	if (_lexHandler != null) {
	    _lexHandler.endDTD();
	}
    }
    public void startEntity(String name) throws SAXException { 
	if (_lexHandler != null) {
	    _lexHandler.startEntity(name);
	}
    }
    public void endEntity(String name) throws SAXException { 
	if (_lexHandler != null) {
	    _lexHandler.endEntity(name);
	}
    }
    public void unparsedEntityDecl(String name, String publicId, 
	String systemId, String notationName) throws SAXException 
    {
        if (_dtdHandler != null) {
	    _dtdHandler.unparsedEntityDecl(name, publicId, systemId,
                                           notationName);
        }
    }
    public void notationDecl(String name, String publicId, String systemId) 
	throws SAXException
    {
        if (_dtdHandler != null) {
	    _dtdHandler.notationDecl(name, publicId, systemId);
        }
    }
    public void attributeDecl(String eName, String aName, String type, 
	String valueDefault, String value) throws SAXException 
    {
        if (_declHandler != null) {
	    _declHandler.attributeDecl(eName, aName, type, valueDefault, value);
        }
    }
    public void elementDecl(String name, String model) 
	throws SAXException
    {
        if (_declHandler != null) {
	    _declHandler.elementDecl(name, model);
        }
    }
    public void externalEntityDecl(String name, String publicId, String systemId) 
	throws SAXException
    {
        if (_declHandler != null) {
	    _declHandler.externalEntityDecl(name, publicId, systemId);
        }
    }
    public void internalEntityDecl(String name, String value) 
	throws SAXException
    {
        if (_declHandler != null) {
	    _declHandler.internalEntityDecl(name, value);
        }
    }
}
