package org.apache.xerces.dom;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.RevalidationHandler;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XML11Char;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
public class DOMNormalizer implements XMLDocumentHandler {
    protected final static boolean DEBUG_ND = false;
    protected final static boolean DEBUG = false;
    protected final static boolean DEBUG_EVENTS = false;
    protected final static String PREFIX = "NS";
    protected DOMConfigurationImpl fConfiguration = null;
    protected CoreDocumentImpl fDocument = null;
    protected final XMLAttributesProxy fAttrProxy = new XMLAttributesProxy();
    protected final QName fQName = new QName();
    protected RevalidationHandler fValidationHandler;
    protected SymbolTable fSymbolTable;
    protected DOMErrorHandler fErrorHandler;
    private final DOMErrorImpl fError = new DOMErrorImpl();
    protected boolean fNamespaceValidation = false;
    protected boolean fPSVI = false;
    protected final NamespaceContext fNamespaceContext = new NamespaceSupport();
    protected final NamespaceContext fLocalNSBinder = new NamespaceSupport();
    protected final ArrayList fAttributeList = new ArrayList(5);
    protected final DOMLocatorImpl fLocator = new DOMLocatorImpl();
    protected Node fCurrentNode = null;
    private final QName fAttrQName = new QName();
    final XMLString fNormalizedValue = new XMLString(new char[16], 0, 0);
    public static final RuntimeException abort = new RuntimeException();
    public static final XMLString EMPTY_STRING = new XMLString();
    private boolean fAllWhitespace = false;
    public DOMNormalizer(){}
    protected void normalizeDocument(CoreDocumentImpl document, DOMConfigurationImpl config) {
        fDocument = document;
        fConfiguration = config;
        fAllWhitespace = false;
        fNamespaceValidation = false;
        String xmlVersion = fDocument.getXmlVersion();
        String schemaType = null;
        String [] schemaLocations = null;
        fSymbolTable = (SymbolTable) fConfiguration.getProperty(DOMConfigurationImpl.SYMBOL_TABLE);
        fNamespaceContext.reset();
        fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, null);
        if ((fConfiguration.features & DOMConfigurationImpl.VALIDATE) != 0) {
            String schemaLang = (String)fConfiguration.getProperty(DOMConfigurationImpl.JAXP_SCHEMA_LANGUAGE);
            if (schemaLang != null && schemaLang.equals(Constants.NS_XMLSCHEMA)) {
                schemaType = XMLGrammarDescription.XML_SCHEMA;
                fValidationHandler = CoreDOMImplementationImpl.singleton.getValidator(schemaType, xmlVersion);
                fConfiguration.setFeature(DOMConfigurationImpl.SCHEMA, true);
                fConfiguration.setFeature(DOMConfigurationImpl.SCHEMA_FULL_CHECKING, true);
                fNamespaceValidation = true;              
                fPSVI = ((fConfiguration.features & DOMConfigurationImpl.PSVI) !=0)?true:false;       
            }
            else {
                schemaType = XMLGrammarDescription.XML_DTD;
                if (schemaLang != null) {
                    schemaLocations = (String []) fConfiguration.getProperty(DOMConfigurationImpl.JAXP_SCHEMA_SOURCE);
                }
                fConfiguration.setDTDValidatorFactory(xmlVersion);
                fValidationHandler = CoreDOMImplementationImpl.singleton.getValidator(schemaType, xmlVersion);
                fPSVI = false;
            }
            fConfiguration.setFeature(DOMConfigurationImpl.XERCES_VALIDATION, true);       
            fDocument.clearIdentifiers();
            if (fValidationHandler != null) {
                ((XMLComponent) fValidationHandler).reset(fConfiguration);
            }
        }
        else {
            fValidationHandler = null;
        }
        fErrorHandler = (DOMErrorHandler) fConfiguration.getParameter(Constants.DOM_ERROR_HANDLER);
        if (fValidationHandler != null) {
            fValidationHandler.setDocumentHandler(this);
            fValidationHandler.startDocument(
                    new SimpleLocator(fDocument.fDocumentURI, fDocument.fDocumentURI,
                            -1, -1 ), fDocument.encoding, fNamespaceContext, null);
            fValidationHandler.xmlDecl(fDocument.getXmlVersion(), 
                    fDocument.getXmlEncoding(), fDocument.getXmlStandalone() ? "yes" : "no", null);
        }
        try {
            if (schemaType == XMLGrammarDescription.XML_DTD) {
                processDTD(xmlVersion, schemaLocations != null ? schemaLocations[0] : null);
            }            
            Node kid, next;
            for (kid = fDocument.getFirstChild(); kid != null; kid = next) {
                next = kid.getNextSibling();
                kid = normalizeNode(kid);
                if (kid != null) { 
                    next = kid;
                }
            }
            if (fValidationHandler != null) {
                fValidationHandler.endDocument(null);
                fValidationHandler.setDocumentHandler(null);
                CoreDOMImplementationImpl.singleton.releaseValidator(schemaType, xmlVersion, fValidationHandler);
                fValidationHandler = null;
            }
        }
        catch (RuntimeException e) {
            if (fValidationHandler != null) {
                fValidationHandler.setDocumentHandler(null);
                CoreDOMImplementationImpl.singleton.releaseValidator(schemaType, xmlVersion, fValidationHandler);
                fValidationHandler = null;
            }
            if (e == abort) {
                return; 
            }
            throw e; 
        }
    }
    protected Node normalizeNode (Node node){
        int type = node.getNodeType();
        boolean wellformed;
        fLocator.fRelatedNode=node;
        switch (type) {
        case Node.DOCUMENT_TYPE_NODE: {
                if (DEBUG_ND) {
                    System.out.println("==>normalizeNode:{doctype}");
                }
                break;
            }
        case Node.ELEMENT_NODE: {  
                if (DEBUG_ND) {
                    System.out.println("==>normalizeNode:{element} "+node.getNodeName());
                }
                if (fDocument.errorChecking) {
                    if ( ((fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0) && 
                            fDocument.isXMLVersionChanged()){
                        if (fNamespaceValidation){
                            wellformed = CoreDocumentImpl.isValidQName(node.getPrefix() , node.getLocalName(), fDocument.isXML11Version());
                        }
                        else {
                            wellformed = CoreDocumentImpl.isXMLName(node.getNodeName() , fDocument.isXML11Version());
                        }
                        if (!wellformed){
                            String msg = DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN, 
                                    "wf-invalid-character-in-node-name", 
                                    new Object[]{"Element", node.getNodeName()});
                            reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR, 
                            "wf-invalid-character-in-node-name");                       
                        }
                    }
                }
                fNamespaceContext.pushContext();
                fLocalNSBinder.reset();
                ElementImpl elem = (ElementImpl)node;
                if (elem.needsSyncChildren()) {
                    elem.synchronizeChildren();
                }
                AttributeMap attributes = (elem.hasAttributes()) ? (AttributeMap) elem.getAttributes() : null; 
                if ((fConfiguration.features & DOMConfigurationImpl.NAMESPACES) !=0) {
                    namespaceFixUp(elem, attributes);
                    if ((fConfiguration.features & DOMConfigurationImpl.NSDECL) == 0 && attributes != null ) {
                        for (int i = 0; i < attributes.getLength(); ++i) {
                            Attr att = (Attr)attributes.getItem(i);
                            if (XMLSymbols.PREFIX_XMLNS.equals(att.getPrefix()) ||
                                XMLSymbols.PREFIX_XMLNS.equals(att.getName())) {
                                elem.removeAttributeNode(att);
                                --i;
                            }
                        }
                    }  
                } else {
                    if ( attributes!=null ) {
                        for ( int i=0; i<attributes.getLength(); ++i ) {
                            Attr attr = (Attr)attributes.item(i);
                            attr.normalize();
                            if (fDocument.errorChecking && ((fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0)){
                                    isAttrValueWF(fErrorHandler, fError, fLocator, attributes, attr, attr.getValue(), fDocument.isXML11Version());
                                if (fDocument.isXMLVersionChanged()) {
                                    if (fNamespaceValidation){
                                        wellformed = CoreDocumentImpl.isValidQName(node.getPrefix(), node.getLocalName(), fDocument.isXML11Version());
                                    }
                                    else {
                                        wellformed = CoreDocumentImpl.isXMLName(node.getNodeName(), fDocument.isXML11Version());
                                    }
                                    if (!wellformed) {
				                            String msg = DOMMessageFormatter.formatMessage(
				                              DOMMessageFormatter.DOM_DOMAIN, 
				                              "wf-invalid-character-in-node-name", 
				                               new Object[]{"Attr",node.getNodeName()});
				                            reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR, 
				                                "wf-invalid-character-in-node-name");
                                    }
                                }           
                            }
                        }                                                        
                    }
                }
                if (fValidationHandler != null) {
                    fAttrProxy.setAttributes(attributes, fDocument, elem);
                    updateQName(elem, fQName); 
                    fConfiguration.fErrorHandlerWrapper.fCurrentNode = node;
                    fCurrentNode = node;
                    fValidationHandler.startElement(fQName, fAttrProxy, null);
                }
                Node kid, next;
                for (kid = elem.getFirstChild(); kid != null; kid = next) {
                    next = kid.getNextSibling();
                    kid = normalizeNode(kid);
                    if (kid != null) {
                        next = kid;  
                    }
                } 
                if (DEBUG_ND) {
                    System.out.println("***The children of {"+node.getNodeName()+"} are normalized");
                    for (kid = elem.getFirstChild(); kid != null; kid = next) {
                        next = kid.getNextSibling();
                        System.out.println(kid.getNodeName() +"["+kid.getNodeValue()+"]");
                    }
                }
                if (fValidationHandler != null) {
                    updateQName(elem, fQName); 
                    fConfiguration.fErrorHandlerWrapper.fCurrentNode = node;
                    fCurrentNode = node;
                    fValidationHandler.endElement(fQName, null);
                }
                fNamespaceContext.popContext();
                break;
            }
        case Node.COMMENT_NODE: {  
                if (DEBUG_ND) {
                    System.out.println("==>normalizeNode:{comments}");
                }
                if ((fConfiguration.features & DOMConfigurationImpl.COMMENTS) == 0) {
                    Node prevSibling = node.getPreviousSibling();
                    Node parent = node.getParentNode();
                    parent.removeChild(node);
                    if (prevSibling != null && prevSibling.getNodeType() == Node.TEXT_NODE) {
                        Node nextSibling = prevSibling.getNextSibling();
                        if (nextSibling != null && nextSibling.getNodeType() == Node.TEXT_NODE) {
                            ((TextImpl)nextSibling).insertData(0, prevSibling.getNodeValue());
                            parent.removeChild(prevSibling);
                            return nextSibling;
                        }
                    }
                }
                else {
                    if (fDocument.errorChecking && ((fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0)){
                        String commentdata = ((Comment)node).getData();
                        isCommentWF(fErrorHandler, fError, fLocator, commentdata, fDocument.isXML11Version());                        
                    }
                    if (fValidationHandler != null) {
                        fValidationHandler.comment(EMPTY_STRING, null);
                    }         
                }
				break;
            }
        case Node.ENTITY_REFERENCE_NODE: { 
                if (DEBUG_ND) {
                    System.out.println("==>normalizeNode:{entityRef} "+node.getNodeName());
                }
                if ((fConfiguration.features & DOMConfigurationImpl.ENTITIES) == 0) {
                    Node prevSibling = node.getPreviousSibling();
                    Node parent = node.getParentNode();
                    ((EntityReferenceImpl)node).setReadOnly(false, true);
                    expandEntityRef (parent, node);
                    parent.removeChild(node);
                    Node next = (prevSibling != null)?prevSibling.getNextSibling():parent.getFirstChild();
                    if (prevSibling != null && next != null && prevSibling.getNodeType() == Node.TEXT_NODE && 
                        next.getNodeType() == Node.TEXT_NODE) {
                        return prevSibling;  
                    }
                    return next;
                } else {
                    if (fDocument.errorChecking && ((fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0) && 
                        fDocument.isXMLVersionChanged()){
                            CoreDocumentImpl.isXMLName(node.getNodeName() , fDocument.isXML11Version());                    
                    }
                }
                break;
            }
        case Node.CDATA_SECTION_NODE: {
                if (DEBUG_ND) {
                    System.out.println("==>normalizeNode:{cdata}");
                }
                if ((fConfiguration.features & DOMConfigurationImpl.CDATA) == 0) {
                    Node prevSibling = node.getPreviousSibling();
                    if (prevSibling != null && prevSibling.getNodeType() == Node.TEXT_NODE){
                        ((Text)prevSibling).appendData(node.getNodeValue());
                        node.getParentNode().removeChild(node);
                        return prevSibling; 
                    }
                    else {
                        Text text = fDocument.createTextNode(node.getNodeValue());
                        Node parent = node.getParentNode();
                        node = parent.replaceChild(text, node);
                        return text;  
                    }
                }
                if (fValidationHandler != null) {
                    fConfiguration.fErrorHandlerWrapper.fCurrentNode = node;
                    fCurrentNode = node;
                    fValidationHandler.startCDATA(null);
                    fValidationHandler.characterData(node.getNodeValue(), null);
                    fValidationHandler.endCDATA(null);
                }
                String value = node.getNodeValue();
                if ((fConfiguration.features & DOMConfigurationImpl.SPLITCDATA) != 0) {
                    int index;
                    Node parent = node.getParentNode();
                    if (fDocument.errorChecking) {
                        isXMLCharWF(fErrorHandler, fError, fLocator, node.getNodeValue(), fDocument.isXML11Version());
                    }
                    while ( (index=value.indexOf("]]>")) >= 0 ) {
                        node.setNodeValue(value.substring(0, index+2));
                        value = value.substring(index +2);
                        Node firstSplitNode = node;
                        Node newChild = fDocument.createCDATASection(value);
                        parent.insertBefore(newChild, node.getNextSibling());
                        node = newChild;                      
                        fLocator.fRelatedNode = firstSplitNode;
                        String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, 
                            "cdata-sections-splitted", 
                             null);
                        reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_WARNING, 
                            "cdata-sections-splitted");
                    }
                }
                else if (fDocument.errorChecking) {
                    isCDataWF(fErrorHandler, fError, fLocator, value, fDocument.isXML11Version());
                }
                break;
            }
        case Node.TEXT_NODE: { 
                if (DEBUG_ND) {
                    System.out.println("==>normalizeNode(text):{"+node.getNodeValue()+"}");
                }
                Node next = node.getNextSibling();
                if ( next!=null && next.getNodeType() == Node.TEXT_NODE ) {
                    ((Text)node).appendData(next.getNodeValue());
                    node.getParentNode().removeChild( next );
                    return node; 
                } else if (node.getNodeValue().length()==0) {
                    node.getParentNode().removeChild( node );
                } else {                    
                    short nextType = (next != null)?next.getNodeType():-1;
                    if (nextType == -1 || !(((fConfiguration.features & DOMConfigurationImpl.ENTITIES) == 0 &&
                            nextType == Node.ENTITY_NODE) ||
                            ((fConfiguration.features & DOMConfigurationImpl.COMMENTS) == 0 &&
                                    nextType == Node.COMMENT_NODE) ||
                                    ((fConfiguration.features & DOMConfigurationImpl.CDATA) == 0) &&
                                    nextType == Node.CDATA_SECTION_NODE)) {
                        if (fDocument.errorChecking && ((fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0) ){
                            isXMLCharWF(fErrorHandler, fError, fLocator, node.getNodeValue(), fDocument.isXML11Version());
                        }                              
                        if (fValidationHandler != null) {
                            fConfiguration.fErrorHandlerWrapper.fCurrentNode = node;
                            fCurrentNode = node;
                            fValidationHandler.characterData(node.getNodeValue(), null);
                            if (!fNamespaceValidation) {
                                if (fAllWhitespace) {
                                    fAllWhitespace = false;
                                    ((TextImpl)node).setIgnorableWhitespace(true);
                                }
                                else {
                                    ((TextImpl)node).setIgnorableWhitespace(false);
                                }
                            }
                            if (DEBUG_ND) {
                                System.out.println("=====>characterData(),"+nextType);
                            }
                        }  
                    }
                    else {
                        if (DEBUG_ND) {
                            System.out.println("=====>don't send characters(),"+nextType);
                        }
                    }
                }
                break;
            }        
        case Node.PROCESSING_INSTRUCTION_NODE: {
            if (fDocument.errorChecking && (fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0 ) {
                ProcessingInstruction pinode = (ProcessingInstruction)node ;
                String target = pinode.getTarget();
                if(fDocument.isXML11Version()){
                    wellformed = XML11Char.isXML11ValidName(target);
                }
                else{                
                    wellformed = XMLChar.isValidName(target);
                }
				if (!wellformed) {
				    String msg = DOMMessageFormatter.formatMessage(
				        DOMMessageFormatter.DOM_DOMAIN, 
				        "wf-invalid-character-in-node-name", 
				        new Object[]{"Element", node.getNodeName()});
                    reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR,  
                        "wf-invalid-character-in-node-name");
                }        
                isXMLCharWF(fErrorHandler, fError, fLocator, pinode.getData(), fDocument.isXML11Version());
            }
            if (fValidationHandler != null) {
                fValidationHandler.processingInstruction(((ProcessingInstruction) node).getTarget(), EMPTY_STRING, null);
            } 
        }
        }
        return null;
    }
    private void processDTD(String xmlVersion, String schemaLocation) {
        String rootName = null;
        String publicId = null;
        String systemId = schemaLocation;
        String baseSystemId = fDocument.getDocumentURI();
        String internalSubset = null;
        DocumentType docType = fDocument.getDoctype();
        if (docType != null) {
            rootName = docType.getName();
            publicId = docType.getPublicId();
            if (systemId == null || systemId.length() == 0) {
                systemId = docType.getSystemId();
            }
            internalSubset = docType.getInternalSubset();
        }
        else {
            Element elem = fDocument.getDocumentElement();
            if (elem == null) return;
            rootName = elem.getNodeName();
            if (systemId == null || systemId.length() == 0) return;
        }
        XMLDTDLoader loader = null;
        try {
            fValidationHandler.doctypeDecl(rootName, publicId, systemId, null);
            loader = CoreDOMImplementationImpl.singleton.getDTDLoader(xmlVersion);
            loader.setFeature(DOMConfigurationImpl.XERCES_VALIDATION, true);
            loader.setEntityResolver(fConfiguration.getEntityResolver());
            loader.setErrorHandler(fConfiguration.getErrorHandler());
            loader.loadGrammarWithContext((XMLDTDValidator) fValidationHandler, rootName, 
                    publicId, systemId, baseSystemId, internalSubset);
        }
        catch (IOException e) {   
        }
        finally {
            if (loader != null) {
                CoreDOMImplementationImpl.singleton.releaseDTDLoader(xmlVersion, loader);
            }
        }
    } 
    protected final void expandEntityRef (Node parent, Node reference){
        Node kid, next;
        for (kid = reference.getFirstChild(); kid != null; kid = next) {
            next = kid.getNextSibling();
            parent.insertBefore(kid, reference);
        }
    }
    protected final void namespaceFixUp (ElementImpl element, AttributeMap attributes){
        if (DEBUG) {
            System.out.println("[ns-fixup] element:" +element.getNodeName()+
                               " uri: "+element.getNamespaceURI());
        }
        String value, uri, prefix;
        if (attributes != null) {
            for (int k = 0; k < attributes.getLength(); ++k) {
                Attr attr = (Attr)attributes.getItem(k);
                uri = attr.getNamespaceURI();
                if (uri != null && uri.equals(NamespaceContext.XMLNS_URI)) {
                    value = attr.getNodeValue();
                    if (value == null) {
                        value=XMLSymbols.EMPTY_STRING;
                    }
                    if (fDocument.errorChecking && value.equals(NamespaceContext.XMLNS_URI)) {
                        fLocator.fRelatedNode = attr;
                        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN,"CantBindXMLNS",null );
                        reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR, "CantBindXMLNS");
                    } else {
                        prefix = attr.getPrefix();
                        prefix = (prefix == null || 
                                  prefix.length() == 0) ? XMLSymbols.EMPTY_STRING :fSymbolTable.addSymbol(prefix);
                        String localpart = fSymbolTable.addSymbol( attr.getLocalName());
                        if (prefix == XMLSymbols.PREFIX_XMLNS) { 
                            value = fSymbolTable.addSymbol(value);
                            if (value.length() != 0) {
                                fNamespaceContext.declarePrefix(localpart, value);
                            } else {
                            }
                            continue;
                        } else { 
                            value = fSymbolTable.addSymbol(value);
                            fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, value.length() != 0 ? value : null);
                            continue;
                        }
                    }  
                } 
            }
        }
        uri = element.getNamespaceURI();
        prefix = element.getPrefix();
        if (uri != null) {  
            uri = fSymbolTable.addSymbol(uri);
            prefix = (prefix == null || 
                      prefix.length() == 0) ? XMLSymbols.EMPTY_STRING :fSymbolTable.addSymbol(prefix);
            if (fNamespaceContext.getURI(prefix) == uri) {
            } else {
                addNamespaceDecl(prefix, uri, element);
                fLocalNSBinder.declarePrefix(prefix, uri);
                fNamespaceContext.declarePrefix(prefix, uri);
            }
        } else { 
            if (element.getLocalName() == null) {
                if (fNamespaceValidation) {
                    String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "NullLocalElementName", 
                            new Object[]{element.getNodeName()});
                    reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_FATAL_ERROR, 
                    "NullLocalElementName");
                } else {
                    String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "NullLocalElementName", 
                            new Object[]{element.getNodeName()});
                    reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR, 
                    "NullLocalElementName");
                }
            } else { 
                uri = fNamespaceContext.getURI(XMLSymbols.EMPTY_STRING);
                if (uri !=null && uri.length() > 0) {
                    addNamespaceDecl (XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING, element);
                    fLocalNSBinder.declarePrefix(XMLSymbols.EMPTY_STRING, null);
                    fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, null);
                }
            }
        }
        if (attributes != null) {
            attributes.cloneMap(fAttributeList);
            for (int i = 0; i < fAttributeList.size(); i++) {
                Attr attr = (Attr) fAttributeList.get(i);
                fLocator.fRelatedNode = attr;
                if (DEBUG) {
                    System.out.println("==>[ns-fixup] process attribute: "+attr.getNodeName());
                }
                attr.normalize();                
                value = attr.getValue();           
                uri = attr.getNamespaceURI();
                if (value == null) {
                    value = XMLSymbols.EMPTY_STRING;
                }
                if (fDocument.errorChecking && ((fConfiguration.features & DOMConfigurationImpl.WELLFORMED) != 0)) {
                    isAttrValueWF(fErrorHandler, fError, fLocator, attributes, attr, value, fDocument.isXML11Version());
                    if (fDocument.isXMLVersionChanged()) {
                        boolean wellformed;
                        if (fNamespaceValidation){
                            wellformed = CoreDocumentImpl.isValidQName(attr.getPrefix(), attr.getLocalName(), fDocument.isXML11Version());
                        }
                        else {
                            wellformed = CoreDocumentImpl.isXMLName(attr.getNodeName(), fDocument.isXML11Version());
                        }
                        if (!wellformed) {
                            String msg = DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN, 
                                    "wf-invalid-character-in-node-name", 
                                    new Object[]{"Attr", attr.getNodeName()});
                            reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR, 
                            "wf-invalid-character-in-node-name");
                        }
                    }           
                }
                if (uri != null) {  
                    prefix = attr.getPrefix();
                    prefix = (prefix == null || 
                              prefix.length() == 0) ? XMLSymbols.EMPTY_STRING :fSymbolTable.addSymbol(prefix);
                     fSymbolTable.addSymbol( attr.getLocalName());
                    if (uri != null && uri.equals(NamespaceContext.XMLNS_URI)) {
                        continue;
                    }
                    ((AttrImpl)attr).setIdAttribute(false);
                    uri = fSymbolTable.addSymbol(uri);
                    String declaredURI =  fNamespaceContext.getURI(prefix);
                    if (prefix == XMLSymbols.EMPTY_STRING || declaredURI != uri) {
                        String declaredPrefix = fNamespaceContext.getPrefix(uri);
                        if (declaredPrefix !=null && declaredPrefix !=XMLSymbols.EMPTY_STRING) {
                            prefix = declaredPrefix;
                        } else {
                            if (prefix != XMLSymbols.EMPTY_STRING && fLocalNSBinder.getURI(prefix) == null) {
                            } else {
                                int counter = 1;
                                prefix = fSymbolTable.addSymbol(PREFIX +counter++);
                                while (fLocalNSBinder.getURI(prefix)!=null) {
                                    prefix = fSymbolTable.addSymbol(PREFIX +counter++);
                                }
                            }
                            addNamespaceDecl(prefix, uri, element);
                            value = fSymbolTable.addSymbol(value);
                            fLocalNSBinder.declarePrefix(prefix, value);
                            fNamespaceContext.declarePrefix(prefix, uri);
                        }
                        attr.setPrefix(prefix);
                    }
                } else { 
                    ((AttrImpl)attr).setIdAttribute(false);
                    if (attr.getLocalName() == null) {
                        if (fNamespaceValidation) {
                            String msg = DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN, 
                                "NullLocalAttrName", new Object[]{attr.getNodeName()});
                            reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_FATAL_ERROR, 
                                "NullLocalAttrName");
                        } else {
                            String msg = DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN, 
                                "NullLocalAttrName", new Object[]{attr.getNodeName()});
                            reportDOMError(fErrorHandler, fError, fLocator, msg, DOMError.SEVERITY_ERROR, 
                                "NullLocalAttrName");
                        }
                    } else {
                    }
                }
            }
        } 
    }
    protected final void addNamespaceDecl(String prefix, String uri, ElementImpl element){
        if (DEBUG) {
            System.out.println("[ns-fixup] addNamespaceDecl ["+prefix+"]");
        }
        if (prefix == XMLSymbols.EMPTY_STRING) {
            if (DEBUG) {
                System.out.println("=>add xmlns=\""+uri+"\" declaration");
            }
            element.setAttributeNS(NamespaceContext.XMLNS_URI, XMLSymbols.PREFIX_XMLNS, uri);             
        } else {
            if (DEBUG) {
                System.out.println("=>add xmlns:"+prefix+"=\""+uri+"\" declaration");
            }
            element.setAttributeNS(NamespaceContext.XMLNS_URI, "xmlns:"+prefix, uri); 
        }
    }
    public static final void isCDataWF(DOMErrorHandler errorHandler, DOMErrorImpl error, DOMLocatorImpl locator, 
        String datavalue, boolean isXML11Version)
    {
        if (datavalue == null || (datavalue.length() == 0) ) {
            return;
        }
        char [] dataarray = datavalue.toCharArray(); 
        int datalength = dataarray.length;
        if (isXML11Version) {                    
            int i = 0;
            while(i < datalength){     
                char c = dataarray[i++];                                      
                if ( XML11Char.isXML11Invalid(c) ) {
                    if (XMLChar.isHighSurrogate(c) && i < datalength) {
                        char c2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(c2) && 
                            XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                            continue;
                        }
                    }
                    String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.XML_DOMAIN,
                        "InvalidCharInCDSect",
                        new Object[] { Integer.toString(c, 16)});
                    reportDOMError(
                        errorHandler,
                        error,
                        locator,
                        msg,
                        DOMError.SEVERITY_ERROR,
                        "wf-invalid-character");
                }
                else if (c == ']') {
                    int count = i;
                    if (count < datalength && dataarray[count] == ']') {
                        while (++count < datalength && dataarray[count] == ']') {
                        }
                        if (count < datalength && dataarray[count] == '>') {
                            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN, "CDEndInContent", null);
                            reportDOMError(errorHandler, error, locator,msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                        }
                    }
                }
            }
        } 
        else {                    
            int i = 0;
            while (i < datalength) {   
                char c = dataarray[i++];                         
                if( XMLChar.isInvalid(c) ) {
                    if (XMLChar.isHighSurrogate(c) && i < datalength) {
                        char c2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(c2) && 
                            XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                            continue;
                        }
                    }
                    String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.XML_DOMAIN, 
                        "InvalidCharInCDSect", 
                        new Object[]{Integer.toString(c, 16)});
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                }
                else if (c==']') {
                    int count = i;
                    if ( count< datalength && dataarray[count]==']' ) {
                        while (++count < datalength && dataarray[count]==']' ) {
                        }
                        if ( count < datalength && dataarray[count]=='>' ) {
                            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN, "CDEndInContent", null);
                            reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                        }
                    }
                }
            }            
        } 
    } 
    public static final void isXMLCharWF(DOMErrorHandler errorHandler, DOMErrorImpl error, DOMLocatorImpl locator, 
        String datavalue, boolean isXML11Version)
    {
        if ( datavalue == null || (datavalue.length() == 0) ) {
            return;      
        }
        char [] dataarray = datavalue.toCharArray(); 
        int datalength = dataarray.length;
        if(isXML11Version){                    
            int i = 0 ;
            while (i < datalength) {                            
                if(XML11Char.isXML11Invalid(dataarray[i++])){
                    char ch = dataarray[i-1];
                    if (XMLChar.isHighSurrogate(ch) && i < datalength) {
                        char ch2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(ch2) && 
                            XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            continue;
                        }
                    }
                    String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN, "InvalidXMLCharInDOM", 
                        new Object[]{Integer.toString(dataarray[i-1], 16)});
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, 
                    "wf-invalid-character");
                }
            }
        } 
        else{                    
            int i = 0 ;
            while (i < datalength) {                            
                if( XMLChar.isInvalid(dataarray[i++]) ) {
                    char ch = dataarray[i-1];
                    if (XMLChar.isHighSurrogate(ch) && i < datalength) {
                        char ch2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(ch2) && 
                            XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            continue;
                        }
                    }
                    String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN, "InvalidXMLCharInDOM", 
                        new Object[]{Integer.toString(dataarray[i-1], 16)});
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, 
                    "wf-invalid-character");
                }
            }            
        } 
    } 
    public static final void isCommentWF(DOMErrorHandler errorHandler, DOMErrorImpl error, DOMLocatorImpl locator, 
        String datavalue, boolean isXML11Version)
    {
        if ( datavalue == null || (datavalue.length() == 0) ) {
            return;
        }
        char [] dataarray = datavalue.toCharArray(); 
        int datalength = dataarray.length ;
        if (isXML11Version) {                    
            int i = 0 ;
            while (i < datalength){   
                char c = dataarray[i++];
                if ( XML11Char.isXML11Invalid(c) ) {
                    if (XMLChar.isHighSurrogate(c) && i < datalength) {
                        char c2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(c2) && 
                            XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                            continue;
                        }
                    }
                    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN, 
                        "InvalidCharInComment", 
                        new Object [] {Integer.toString(dataarray[i-1], 16)});
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                }
                else if (c == '-' && i < datalength && dataarray[i] == '-') {
                    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN,
                        "DashDashInComment", null);
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                }
            }
        } 
        else {                    
            int i = 0;
            while (i < datalength){ 
                char c = dataarray[i++];                           
                if( XMLChar.isInvalid(c) ){
                    if (XMLChar.isHighSurrogate(c) && i < datalength) {
                        char c2 = dataarray[i++];
                        if (XMLChar.isLowSurrogate(c2) && 
                            XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                            continue;
                        }
                    }
                    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN,
                        "InvalidCharInComment", new Object [] {Integer.toString(dataarray[i-1], 16)});
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                }  
                else if (c == '-' && i<datalength && dataarray[i]=='-'){
                    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.XML_DOMAIN,
                        "DashDashInComment", null);
                    reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, "wf-invalid-character");
                }                                      
            }
        } 
    } 
    public static final void isAttrValueWF(DOMErrorHandler errorHandler, DOMErrorImpl error, 
            DOMLocatorImpl locator, NamedNodeMap attributes, Attr a, String value, boolean xml11Version) {
        if (a instanceof AttrImpl && ((AttrImpl)a).hasStringValue()) {
            isXMLCharWF(errorHandler, error, locator, value, xml11Version);
        } else {
        	NodeList children = a.getChildNodes(); 
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                    Document owner = a.getOwnerDocument();
                    Entity ent = null;
                    if (owner != null) {
                        DocumentType docType = owner.getDoctype();
                        if (docType != null) {
                            NamedNodeMap entities = docType.getEntities();
                            ent = (Entity) entities.getNamedItemNS(
                                    "*",
                                    child.getNodeName());
                        }
                    }
                    if (ent == null) {
                        String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "UndeclaredEntRefInAttrValue", 
                            new Object[]{a.getNodeName()});
                        reportDOMError(errorHandler, error, locator, msg, DOMError.SEVERITY_ERROR, 
                            "UndeclaredEntRefInAttrValue");
                    }
                }
                else {
                    isXMLCharWF(errorHandler, error, locator, child.getNodeValue(), xml11Version);
                }
            }
        }
    }
    public static final void reportDOMError(DOMErrorHandler errorHandler, DOMErrorImpl error, DOMLocatorImpl locator, 
                        String message, short severity, String type ) {
        if( errorHandler!=null ) {
            error.reset();
            error.fMessage = message;
            error.fSeverity = severity;
            error.fLocator = locator;
            error.fType = type;
            error.fRelatedData = locator.fRelatedNode;
            if(!errorHandler.handleError(error))
                throw abort;
        }
        if( severity==DOMError.SEVERITY_FATAL_ERROR )
            throw abort;
    }
    protected final void updateQName (Node node, QName qname){
        String prefix    = node.getPrefix();
        String namespace = node.getNamespaceURI();
        String localName = node.getLocalName();
        qname.prefix = (prefix!=null && prefix.length()!=0)?fSymbolTable.addSymbol(prefix):null;
        qname.localpart = (localName != null)?fSymbolTable.addSymbol(localName):null;
        qname.rawname = fSymbolTable.addSymbol(node.getNodeName()); 
        qname.uri =  (namespace != null)?fSymbolTable.addSymbol(namespace):null;
    }
    final String normalizeAttributeValue(String value, Attr attr) {
        if (!attr.getSpecified()){
            return value;
        }
        int end = value.length();
        if (fNormalizedValue.ch.length < end) {
            fNormalizedValue.ch = new char[end];
        }
        fNormalizedValue.length = 0;
        boolean normalized = false;
        for (int i = 0; i < end; i++) {
            char c = value.charAt(i);
            if (c==0x0009 || c==0x000A) {
               fNormalizedValue.ch[fNormalizedValue.length++] = ' '; 
               normalized = true;
            }
            else if(c==0x000D){
               normalized = true;
               fNormalizedValue.ch[fNormalizedValue.length++] = ' ';
               int next = i+1;
               if (next < end && value.charAt(next)==0x000A) i=next; 
            }
            else {
                fNormalizedValue.ch[fNormalizedValue.length++] = c;
            }
        }
        if (normalized){
           value = fNormalizedValue.toString();
           attr.setValue(value);
        }
        return value;
    }
    protected final class XMLAttributesProxy 
    implements XMLAttributes {
        protected AttributeMap fAttributes;
        protected CoreDocumentImpl fDocument;
        protected ElementImpl fElement;
        protected final Vector fDTDTypes = new Vector(5);
        protected final Vector fAugmentations = new Vector(5);
        public void setAttributes(AttributeMap attributes, CoreDocumentImpl doc, ElementImpl elem) {
            fDocument = doc;
            fAttributes = attributes;
            fElement = elem;
            if (attributes != null) {
                int length = attributes.getLength();
                fDTDTypes.setSize(length);
                fAugmentations.setSize(length);
                for (int i = 0; i < length; i++) {
                    fAugmentations.setElementAt(new AugmentationsImpl(), i);
                }
            } 
            else {
                fDTDTypes.setSize(0);
                fAugmentations.setSize(0);
            }
        }
        public int addAttribute(QName qname, String attrType, String attrValue) {
            int index = fElement.getXercesAttribute(qname.uri, qname.localpart);
            if (index < 0) {
                AttrImpl attr = (AttrImpl)
                    ((CoreDocumentImpl) fElement.getOwnerDocument()).createAttributeNS(
                        qname.uri,
                        qname.rawname,
                        qname.localpart);
                attr.setNodeValue(attrValue);
                index = fElement.setXercesAttributeNode(attr);
                fDTDTypes.insertElementAt(attrType, index);
                fAugmentations.insertElementAt(new AugmentationsImpl(), index);
                attr.setSpecified(false);
            }            
            else {
            }
            return index;
        }
        public void removeAllAttributes(){
        }
        public void removeAttributeAt(int attrIndex){
        }
        public int getLength(){
            return(fAttributes != null)?fAttributes.getLength():0;
        }
        public int getIndex(String qName){        
            return -1;
        }
        public int getIndex(String uri, String localPart) {
            return -1;
        }
        public void setName(int attrIndex, QName attrName) {
        }
        public void getName(int attrIndex, QName attrName) {
            if (fAttributes != null) {
                updateQName((Node)fAttributes.getItem(attrIndex), attrName);
            }
        }
        public String getPrefix(int index) {
            if (fAttributes != null) {
                Node node = (Node) fAttributes.getItem(index);
                String prefix = node.getPrefix();
                prefix = (prefix != null && prefix.length() != 0) ? fSymbolTable.addSymbol(prefix) : null;
                return prefix;
            }
            return null;
        }
        public String getURI(int index) {
            if (fAttributes != null) {
                Node node = (Node) fAttributes.getItem(index);
                String namespace = node.getNamespaceURI();
                namespace = (namespace != null) ? fSymbolTable.addSymbol(namespace) : null;
                return namespace;
            }
            return null;
        }
        public String getLocalName(int index) {
            if (fAttributes != null) {
                Node node = (Node) fAttributes.getItem(index);
                String localName = node.getLocalName();
                localName = (localName != null) ? fSymbolTable.addSymbol(localName) : null;
                return localName;
            }
            return null;
        }
        public String getQName(int index) {
            if (fAttributes != null) {
                Node node = (Node) fAttributes.getItem(index);
                String rawname = fSymbolTable.addSymbol(node.getNodeName());
                return rawname;
            }
            return null;
        }
        public void setType(int attrIndex, String attrType) {
            fDTDTypes.setElementAt(attrType, attrIndex);
        }
        public String getType(int index) {
            String type = (String) fDTDTypes.elementAt(index);
            return (type != null) ? getReportableType(type) : "CDATA";
        }
        public String getType(String qName) {
            return "CDATA";
        }
        public String getType(String uri, String localName) {
            return "CDATA";
        }
        private String getReportableType(String type) {
            if (type.charAt(0) == '(') {
                return "NMTOKEN";
            }
            return type;
        }
        public void setValue(int attrIndex, String attrValue) {
            if (fAttributes != null){
                AttrImpl attr = (AttrImpl)fAttributes.getItem(attrIndex);
                boolean specified = attr.getSpecified();
                attr.setValue(attrValue);
                attr.setSpecified(specified);
            }
        }
        public String getValue(int index) {
            return (fAttributes !=null)?fAttributes.item(index).getNodeValue():"";
        }
        public String getValue(String qName){
            return null;
        }
        public String getValue(String uri, String localName){ 
            if (fAttributes != null) {
                Node node =  fAttributes.getNamedItemNS(uri, localName);
                return(node != null)? node.getNodeValue():null;
            }
            return null;
        }
        public void setNonNormalizedValue(int attrIndex, String attrValue){
        }
        public String getNonNormalizedValue(int attrIndex){
            return null;
        }
        public void setSpecified(int attrIndex, boolean specified){
            AttrImpl attr = (AttrImpl)fAttributes.getItem(attrIndex);
            attr.setSpecified(specified);
        }
        public boolean isSpecified(int attrIndex){
            return((Attr)fAttributes.getItem(attrIndex)).getSpecified();
        }
        public Augmentations getAugmentations (int attributeIndex){
            return(Augmentations)fAugmentations.elementAt(attributeIndex);
        }
        public Augmentations getAugmentations (String uri, String localPart){ 
            return null;
        }
        public Augmentations getAugmentations(String qName){
            return null;
        }
        public void setAugmentations(int attrIndex, Augmentations augs) {
            fAugmentations.setElementAt(augs, attrIndex);
        }
    }
    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext namespaceContext,
                              Augmentations augs) 
        throws XNIException{
    }
    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs)
        throws XNIException{
    }
    public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
        throws XNIException{
    }
    public void comment(XMLString text, Augmentations augs) throws XNIException{
    }
    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException{
    }
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        Element currentElement = (Element) fCurrentNode;
        int attrCount = attributes.getLength();
        if (DEBUG_EVENTS) {
            System.out.println("==>startElement: " +element+
                    " attrs.length="+attrCount);
        }
        for (int i = 0; i < attrCount; i++) {
            attributes.getName(i, fAttrQName);
            Attr attr = null;
            attr = currentElement.getAttributeNodeNS(fAttrQName.uri, fAttrQName.localpart);
            if (attr == null) {
                attr = currentElement.getAttributeNode(fAttrQName.rawname);
            }
            AttributePSVI attrPSVI =
                (AttributePSVI) attributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
            if (attrPSVI != null) {
                XSTypeDefinition decl = attrPSVI.getMemberTypeDefinition();
                boolean id = false;
                if (decl != null) {
                    id = ((XSSimpleType)decl).isIDType();
                } 
                else {
                    decl = attrPSVI.getTypeDefinition();
                    if (decl != null) {
                        id = ((XSSimpleType)decl).isIDType(); 
                    }
                }
                if (id) {
                    ((ElementImpl)currentElement).setIdAttributeNode(attr, true);
                }
                if (fPSVI) {
                    ((PSVIAttrNSImpl) attr).setPSVI(attrPSVI);
                }
                ((AttrImpl) attr).setType(decl);
                if ((fConfiguration.features & DOMConfigurationImpl.DTNORMALIZATION) != 0) {
                    final String normalizedValue = attrPSVI.getSchemaNormalizedValue();
                    if (normalizedValue != null) {
                        boolean specified = attr.getSpecified();
                        attr.setValue(normalizedValue);
                        if (!specified) {
                            ((AttrImpl) attr).setSpecified(specified);
                        }
                    }
                }
            }
            else { 
                String type = null;
                boolean isDeclared = Boolean.TRUE.equals(attributes.getAugmentations(i).getItem (Constants.ATTRIBUTE_DECLARED));
                if (isDeclared) {
                    type = attributes.getType(i);
                    if ("ID".equals (type)) {
                        ((ElementImpl) currentElement).setIdAttributeNode(attr, true);
                    }
                }
                ((AttrImpl) attr).setType(type);
            }
        }
    }
	public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
		throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println("==>emptyElement: " +element);
        }
		startElement(element, attributes, augs);
        endElement(element, augs);
	}
    public void startGeneralEntity(String name, 
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException{
    }
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException{
    }
    public void endGeneralEntity(String name, Augmentations augs) throws XNIException{
    }
    public void characters(XMLString text, Augmentations augs) throws XNIException{
    }
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException{
        fAllWhitespace = true;
    }
    public void endElement(QName element, Augmentations augs) throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println("==>endElement: " + element);
        }
        if (augs != null) {
            ElementPSVI elementPSVI = (ElementPSVI) augs.getItem(Constants.ELEMENT_PSVI);
            if (elementPSVI != null) {
                ElementImpl elementNode = (ElementImpl) fCurrentNode;
                if (fPSVI) {
                    ((PSVIElementNSImpl) fCurrentNode).setPSVI(elementPSVI);
                }
                if (elementNode instanceof ElementNSImpl) {
                    XSTypeDefinition type = elementPSVI.getMemberTypeDefinition();
                    if (type == null) {
                        type = elementPSVI.getTypeDefinition();
                    }
                    ((ElementNSImpl) elementNode).setType(type);
                }
                String normalizedValue = elementPSVI.getSchemaNormalizedValue();
                if ((fConfiguration.features & DOMConfigurationImpl.DTNORMALIZATION) != 0) {
                    if (normalizedValue !=null)
                        elementNode.setTextContent(normalizedValue);
                }
                else {
                    String text = elementNode.getTextContent();
                    if (text.length() == 0) {
                        if (normalizedValue !=null)
                            elementNode.setTextContent(normalizedValue);
                    }
                }
                return;
            }
        }
        if (fCurrentNode instanceof ElementNSImpl) { 
            ((ElementNSImpl) fCurrentNode).setType(null);
        }
    }
    public void startCDATA(Augmentations augs) throws XNIException{
    }
    public void endCDATA(Augmentations augs) throws XNIException{
    }
    public void endDocument(Augmentations augs) throws XNIException{
    }
    public void setDocumentSource(XMLDocumentSource source){
    }
    public XMLDocumentSource getDocumentSource(){
        return null;
    }
}  
