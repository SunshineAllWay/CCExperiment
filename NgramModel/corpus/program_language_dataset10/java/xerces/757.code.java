package org.apache.xml.serialize;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
public class XMLSerializer
extends BaseMarkupSerializer {
    protected static final boolean DEBUG = false;
    protected NamespaceSupport fNSBinder;
    protected NamespaceSupport fLocalNSBinder;
    protected SymbolTable fSymbolTable;    
    protected final static String PREFIX = "NS";
    protected boolean fNamespaces = false;
    protected boolean fNamespacePrefixes = true;
    private boolean fPreserveSpace;
    public XMLSerializer() {
        super( new OutputFormat( Method.XML, null, false ) );
    }
    public XMLSerializer( OutputFormat format ) {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        _format.setMethod( Method.XML );
    }
    public XMLSerializer( Writer writer, OutputFormat format ) {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        _format.setMethod( Method.XML );
        setOutputCharStream( writer );
    }
    public XMLSerializer( OutputStream output, OutputFormat format ) {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        _format.setMethod( Method.XML );
        setOutputByteStream( output );
    }
    public void setOutputFormat( OutputFormat format ) {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.XML, null, false ) );
    }
    public void setNamespaces (boolean namespaces){
        fNamespaces = namespaces;
        if (fNSBinder == null) {
            fNSBinder = new NamespaceSupport();
            fLocalNSBinder = new NamespaceSupport();
            fSymbolTable = new SymbolTable();
        }
    }
    public void startElement( String namespaceURI, String localName,
                              String rawName, Attributes attrs )
    throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        if (DEBUG) {
            System.out.println("==>startElement("+namespaceURI+","+localName+
                               ","+rawName+")");
        }
        try {
            if (_printer == null) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "NoWriterSupplied", null);
                throw new IllegalStateException(msg);
            }
            state = getElementState();
            if (isDocumentState()) {
                if (! _started)
                    startDocument( ( localName == null || localName.length() == 0 ) ? rawName : localName );
            } else {
                if (state.empty)
                    _printer.printText( '>' );
                if (state.inCData) {
                    _printer.printText( "]]>" );
                    state.inCData = false;
                }
                if (_indenting && ! state.preserveSpace &&
                    ( state.empty || state.afterElement || state.afterComment))
                    _printer.breakLine();
            }
            preserveSpace = state.preserveSpace;
            attrs = extractNamespaces(attrs);
            if (rawName == null || rawName.length() == 0) {
                if (localName == null) {
                    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "NoName", null);
                    throw new SAXException(msg);
                }
                if (namespaceURI != null && ! namespaceURI.equals( "" )) {
                    String prefix;
                    prefix = getPrefix( namespaceURI );
                    if (prefix != null && prefix.length() > 0) {
                        rawName = prefix + ":" + localName;
                    } 
                    else {
                        rawName = localName;
                    }
                } 
                else {
                    rawName = localName;
                }
            }
            _printer.printText( '<' );
            _printer.printText( rawName );
            _printer.indent();
            if (attrs != null) {
                for (i = 0 ; i < attrs.getLength() ; ++i) {
                    _printer.printSpace();
                    name = attrs.getQName( i );
                    if (name != null && name.length() == 0) {
                        String prefix;
                        String attrURI;
                        name = attrs.getLocalName( i );
                        attrURI = attrs.getURI( i );
                        if (( attrURI != null && attrURI.length() != 0 ) &&
                            ( namespaceURI == null || namespaceURI.length() == 0 ||
                              ! attrURI.equals( namespaceURI ) )) {
                            prefix = getPrefix( attrURI );
                            if (prefix != null && prefix.length() > 0)
                                name = prefix + ":" + name;
                        }
                    }
                    value = attrs.getValue( i );
                    if (value == null)
                        value = "";
                    _printer.printText( name );
                    _printer.printText( "=\"" );
                    printEscaped( value );
                    _printer.printText( '"' );
                    if (name.equals( "xml:space" )) {
                        if (value.equals( "preserve" ))
                            preserveSpace = true;
                        else
                            preserveSpace = _format.getPreserveSpace();
                    }
                }
            }
            if (_prefixes != null) {
                Iterator entries = _prefixes.entrySet().iterator();
                while (entries.hasNext()) {
                    _printer.printSpace();
                    Map.Entry entry = (Map.Entry) entries.next();
                    value = (String) entry.getKey();
                    name = (String) entry.getValue();
                    if (name.length() == 0) {
                        _printer.printText( "xmlns=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    } 
                    else {
                        _printer.printText( "xmlns:" );
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                }
            }
            state = enterElementState( namespaceURI, localName, rawName, preserveSpace );
            name = ( localName == null || localName.length() == 0 ) ? rawName : namespaceURI + "^" + localName;
            state.doCData = _format.isCDataElement( name );
            state.unescaped = _format.isNonEscapingElement( name );
        } catch (IOException except) {
            throw new SAXException( except );
        }
    }
    public void endElement( String namespaceURI, String localName,
                            String rawName )
    throws SAXException
    {
        try {
            endElementIO( namespaceURI, localName, rawName );
        } catch (IOException except) {
            throw new SAXException( except );
        }
    }
    public void endElementIO( String namespaceURI, String localName,
                              String rawName )
    throws IOException
    {
        ElementState state;
        if (DEBUG) {
            System.out.println("==>endElement: " +rawName);
        }
        _printer.unindent();
        state = getElementState();
        if (state.empty) {
            _printer.printText( "/>" );
        } else {
            if (state.inCData)
                _printer.printText( "]]>" );
            if (_indenting && ! state.preserveSpace && (state.afterElement || state.afterComment))
                _printer.breakLine();
            _printer.printText( "</" );
            _printer.printText( state.rawName );
            _printer.printText( '>' );
        }
        state = leaveElementState();
        state.afterElement = true;
        state.afterComment = false;
        state.empty = false;
        if (isDocumentState())
            _printer.flush();
    }
    public void startElement( String tagName, AttributeList attrs )
    throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        if (DEBUG) {
            System.out.println("==>startElement("+tagName+")");
        }
        try {
            if (_printer == null) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "NoWriterSupplied", null);
                throw new IllegalStateException(msg);
            }
            state = getElementState();
            if (isDocumentState()) {
                if (! _started)
                    startDocument( tagName );
            } else {
                if (state.empty)
                    _printer.printText( '>' );
                if (state.inCData) {
                    _printer.printText( "]]>" );
                    state.inCData = false;
                }
                if (_indenting && ! state.preserveSpace &&
                    ( state.empty || state.afterElement || state.afterComment))
                    _printer.breakLine();
            }
            preserveSpace = state.preserveSpace;
            _printer.printText( '<' );
            _printer.printText( tagName );
            _printer.indent();
            if (attrs != null) {
                for (i = 0 ; i < attrs.getLength() ; ++i) {
                    _printer.printSpace();
                    name = attrs.getName( i );
                    value = attrs.getValue( i );
                    if (value != null) {
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                    if (name.equals( "xml:space" )) {
                        if (value.equals( "preserve" ))
                            preserveSpace = true;
                        else
                            preserveSpace = _format.getPreserveSpace();
                    }
                }
            }
            state = enterElementState( null, null, tagName, preserveSpace );
            state.doCData = _format.isCDataElement( tagName );
            state.unescaped = _format.isNonEscapingElement( tagName );
        } catch (IOException except) {
            throw new SAXException( except );
        }
    }
    public void endElement( String tagName )
    throws SAXException
    {
        endElement( null, null, tagName );
    }
    protected void startDocument( String rootTagName )
    throws IOException
    {
        int    i;
        String dtd;
        dtd = _printer.leaveDTD();
        if (! _started) {
            if (! _format.getOmitXMLDeclaration()) {
                StringBuffer    buffer;
                buffer = new StringBuffer( "<?xml version=\"" );
                if (_format.getVersion() != null)
                    buffer.append( _format.getVersion() );
                else
                    buffer.append( "1.0" );
                buffer.append( '"' );
                String format_encoding =  _format.getEncoding();
                if (format_encoding != null) {
                    buffer.append( " encoding=\"" );
                    buffer.append( format_encoding );
                    buffer.append( '"' );
                }
                if (_format.getStandalone() && _docTypeSystemId == null &&
                    _docTypePublicId == null)
                    buffer.append( " standalone=\"yes\"" );
                buffer.append( "?>" );
                _printer.printText( buffer );
                _printer.breakLine();
            }
            if (! _format.getOmitDocumentType()) {
                if (_docTypeSystemId != null) {
                    _printer.printText( "<!DOCTYPE " );
                    _printer.printText( rootTagName );
                    if (_docTypePublicId != null) {
                        _printer.printText( " PUBLIC " );
                        printDoctypeURL( _docTypePublicId );
                        if (_indenting) {
                            _printer.breakLine();
                            for (i = 0 ; i < 18 + rootTagName.length() ; ++i)
                                _printer.printText( " " );
                        } else
                            _printer.printText( " " );
                        printDoctypeURL( _docTypeSystemId );
                    } else {
                        _printer.printText( " SYSTEM " );
                        printDoctypeURL( _docTypeSystemId );
                    }
                    if (dtd != null && dtd.length() > 0) {
                        _printer.printText( " [" );
                        printText( dtd, true, true );
                        _printer.printText( ']' );
                    }
                    _printer.printText( ">" );
                    _printer.breakLine();
                } else if (dtd != null && dtd.length() > 0) {
                    _printer.printText( "<!DOCTYPE " );
                    _printer.printText( rootTagName );
                    _printer.printText( " [" );
                    printText( dtd, true, true );
                    _printer.printText( "]>" );
                    _printer.breakLine();
                }
            }
        }
        _started = true;
        serializePreRoot();
    }
    protected void serializeElement( Element elem )
    throws IOException
    {
        Attr         attr;
        NamedNodeMap attrMap;
        int          i;
        Node         child;
        ElementState state;
        String       name;
        String       value;
        String       tagName;
        String prefix, localUri;
        String uri;
        if (fNamespaces) {
            fLocalNSBinder.reset();
            fNSBinder.pushContext();
        }
        if (DEBUG) {
            System.out.println("==>startElement: " +elem.getNodeName() +" ns="+elem.getNamespaceURI());
        }
        tagName = elem.getTagName();
        state = getElementState();
        if (isDocumentState()) {
            if (! _started) {
                startDocument( tagName);
            }
        } else {
            if (state.empty)
                _printer.printText( '>' );
            if (state.inCData) {
                _printer.printText( "]]>" );
                state.inCData = false;
            }
            if (_indenting && ! state.preserveSpace &&
                ( state.empty || state.afterElement || state.afterComment))
                _printer.breakLine();
        }
        fPreserveSpace = state.preserveSpace;
        int length = 0;
        attrMap = null;
        if (elem.hasAttributes()) {
            attrMap = elem.getAttributes();
            length = attrMap.getLength();
        }
        if (!fNamespaces) { 
            _printer.printText( '<' );
            _printer.printText( tagName );
            _printer.indent();
            for ( i = 0 ; i < length ; ++i ) {
                attr = (Attr) attrMap.item( i );
                name = attr.getName();
                value = attr.getValue();
                if ( value == null )
                    value = "";
                printAttribute (name, value, attr.getSpecified(), attr);
            }
        } else { 
            for (i = 0;i < length;i++) {
                attr = (Attr) attrMap.item( i );
                uri = attr.getNamespaceURI();
                if (uri != null && uri.equals(NamespaceContext.XMLNS_URI)) {
                    value = attr.getNodeValue();
                    if (value == null) {
                        value=XMLSymbols.EMPTY_STRING;
                    }
                    if (value.equals(NamespaceContext.XMLNS_URI)) {
                        if (fDOMErrorHandler != null) {
                            String msg = DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.XML_DOMAIN,"CantBindXMLNS",null );
                            modifyDOMError(msg,  DOMError.SEVERITY_ERROR, null, attr);
                            boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                            if (!continueProcess) {
            	                throw new RuntimeException( 
            	                    DOMMessageFormatter.formatMessage(
            	                    DOMMessageFormatter.SERIALIZER_DOMAIN,
            	                    "SerializationStopped", null));
                            }
                        }
                    } else {
                        prefix = attr.getPrefix();
                        prefix = (prefix == null || 
                                  prefix.length() == 0) ? XMLSymbols.EMPTY_STRING :fSymbolTable.addSymbol(prefix);
                        String localpart = fSymbolTable.addSymbol( attr.getLocalName());
                        if (prefix == XMLSymbols.PREFIX_XMLNS) { 
                            value = fSymbolTable.addSymbol(value);
                            if (value.length() != 0) {
                                fNSBinder.declarePrefix(localpart, value);
                            } else {
                            }
                            continue;
                        }
                        value = fSymbolTable.addSymbol(value);
                        fNSBinder.declarePrefix(XMLSymbols.EMPTY_STRING, value);
                        continue;
                    }  
                } 
            }  
            uri = elem.getNamespaceURI();            
            prefix = elem.getPrefix();
            if ((uri !=null && prefix !=null ) && uri.length() == 0 && prefix.length()!=0) {
                prefix = null; 
                _printer.printText( '<' );
                _printer.printText( elem.getLocalName() );
                _printer.indent();
            } else {
                _printer.printText( '<' );
                _printer.printText( tagName );
                _printer.indent();
            }
            if (uri != null) {  
                uri = fSymbolTable.addSymbol(uri);
                prefix = (prefix == null || 
                          prefix.length() == 0) ? XMLSymbols.EMPTY_STRING :fSymbolTable.addSymbol(prefix);
                if (fNSBinder.getURI(prefix) == uri) {
                } else {
                    if (fNamespacePrefixes) {
                        printNamespaceAttr(prefix, uri);
                    }
                    fLocalNSBinder.declarePrefix(prefix, uri);
                    fNSBinder.declarePrefix(prefix, uri);
                }
            } else { 
                if (elem.getLocalName() == null) {
                    if (fDOMErrorHandler != null) {
                        String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "NullLocalElementName", 
                            new Object[]{elem.getNodeName()});
                        modifyDOMError(msg,DOMError.SEVERITY_ERROR, null, elem);
                        boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                        if (!continueProcess) {
                           throw new RuntimeException( 
            	               DOMMessageFormatter.formatMessage(
            	               DOMMessageFormatter.SERIALIZER_DOMAIN,
            	               "SerializationStopped", null));
                        }
                    }
                } else { 
                    uri = fNSBinder.getURI(XMLSymbols.EMPTY_STRING);
                    if (uri !=null && uri.length() > 0) {
                        if (fNamespacePrefixes) {
                            printNamespaceAttr(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
                        }
                        fLocalNSBinder.declarePrefix(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
                        fNSBinder.declarePrefix(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
                    }
                }
            }
            for (i = 0; i < length; i++) {
                attr = (Attr) attrMap.item( i );
                value = attr.getValue();
                name = attr.getNodeName();  
                uri = attr.getNamespaceURI();
                if (uri !=null && uri.length() == 0) {
                    uri=null;
                    name=attr.getLocalName();
                }
                if (DEBUG) {
                    System.out.println("==>process attribute: "+attr.getNodeName());
                }
                if (value == null) {
                    value=XMLSymbols.EMPTY_STRING;
                }
                if (uri != null) {  
                    prefix = attr.getPrefix();
                    prefix = prefix == null ? XMLSymbols.EMPTY_STRING :fSymbolTable.addSymbol(prefix);
                    String localpart = fSymbolTable.addSymbol( attr.getLocalName());
                    if (uri != null && uri.equals(NamespaceContext.XMLNS_URI)) {
                        prefix = attr.getPrefix();
                        prefix = (prefix == null || 
                                  prefix.length() == 0) ? XMLSymbols.EMPTY_STRING : fSymbolTable.addSymbol(prefix);
                        localpart = fSymbolTable.addSymbol( attr.getLocalName());
                        if (prefix == XMLSymbols.PREFIX_XMLNS) { 
                            localUri = fLocalNSBinder.getURI(localpart);  
                            value = fSymbolTable.addSymbol(value);
                            if (value.length() != 0 ) {
                                if (localUri == null) {
                                    if (fNamespacePrefixes) {
                                        printNamespaceAttr(localpart, value);
                                    }
                                    fLocalNSBinder.declarePrefix(localpart, value);
                                }
                            } else {
                            }
                            continue;
                        }
                        uri = fNSBinder.getURI(XMLSymbols.EMPTY_STRING);
                        localUri= fLocalNSBinder.getURI(XMLSymbols.EMPTY_STRING);
                        value = fSymbolTable.addSymbol(value);
                        if (localUri == null ) {
                            if (fNamespacePrefixes) {
                                printNamespaceAttr(XMLSymbols.EMPTY_STRING, value);
                            }
                        }
                        continue;
                    }
                    uri = fSymbolTable.addSymbol(uri);
                    String declaredURI =  fNSBinder.getURI(prefix);
                    if (prefix == XMLSymbols.EMPTY_STRING || declaredURI != uri) {
                        name  = attr.getNodeName();
                        String declaredPrefix = fNSBinder.getPrefix(uri);
                        if (declaredPrefix !=null && declaredPrefix !=XMLSymbols.EMPTY_STRING) {
                            prefix = declaredPrefix;
                            name=prefix+":"+localpart;
                        } else {
                            if (DEBUG) {
                                System.out.println("==> cound not find prefix for the attribute: " +prefix);
                            }
                            if (prefix != XMLSymbols.EMPTY_STRING && fLocalNSBinder.getURI(prefix) == null) {
                            } else {
                                int counter = 1;
                                prefix = fSymbolTable.addSymbol(PREFIX + counter++);
                                while (fLocalNSBinder.getURI(prefix)!=null) {
                                    prefix = fSymbolTable.addSymbol(PREFIX +counter++);
                                }
                                name=prefix+":"+localpart;
                            }
                            if (fNamespacePrefixes) {
                                printNamespaceAttr(prefix, uri);
                            }
                            value = fSymbolTable.addSymbol(value);
                            fLocalNSBinder.declarePrefix(prefix, value);
                            fNSBinder.declarePrefix(prefix, uri);
                        }                        
                    }
                    printAttribute (name, (value==null)?XMLSymbols.EMPTY_STRING:value, attr.getSpecified(), attr);
                } else { 
                    if (attr.getLocalName() == null) {
                        if (fDOMErrorHandler != null) {
                            String msg = DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN, 
                                "NullLocalAttrName", new Object[]{attr.getNodeName()});                            
                            modifyDOMError(msg, DOMError.SEVERITY_ERROR, null, attr);
                            boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                            if (!continueProcess) {
                                throw new RuntimeException( 
            	                   DOMMessageFormatter.formatMessage(
            	                   DOMMessageFormatter.SERIALIZER_DOMAIN,
            	                   "SerializationStopped", null));
                            }
                        }
                        printAttribute (name, value, attr.getSpecified(), attr);
                    } else { 
                        printAttribute (name, value, attr.getSpecified(), attr);
                    }
                }
            } 
        }
        if (elem.hasChildNodes()) {
            state = enterElementState( null, null, tagName, fPreserveSpace );
            state.doCData = _format.isCDataElement( tagName );
            state.unescaped = _format.isNonEscapingElement( tagName );
            child = elem.getFirstChild();
            while (child != null) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            if (fNamespaces) {
                fNSBinder.popContext();
            }
            endElementIO( null, null, tagName );
        } else {
            if (DEBUG) {
                System.out.println("==>endElement: " +elem.getNodeName());
            }
            if (fNamespaces) {
                fNSBinder.popContext();
            }
            _printer.unindent();
            _printer.printText( "/>" );
            state.afterElement = true;
            state.afterComment = false;
            state.empty = false;
            if (isDocumentState())
                _printer.flush();
        }
    }
    private void printNamespaceAttr(String prefix, String uri) throws IOException{
        _printer.printSpace();
        if (prefix == XMLSymbols.EMPTY_STRING) {
            if (DEBUG) {
                System.out.println("=>add xmlns=\""+uri+"\" declaration");
            }
            _printer.printText( XMLSymbols.PREFIX_XMLNS );
        } else {
            if (DEBUG) {
                System.out.println("=>add xmlns:"+prefix+"=\""+uri+"\" declaration");
            }
            _printer.printText( "xmlns:"+prefix );
        }
        _printer.printText( "=\"" );
        printEscaped( uri );
        _printer.printText( '"' );
    }
    private void printAttribute (String name, String value, boolean isSpecified, Attr attr) throws IOException{
        if (isSpecified || (features & DOMSerializerImpl.DISCARDDEFAULT) == 0) {
            if (fDOMFilter !=null && 
                (fDOMFilter.getWhatToShow() & NodeFilter.SHOW_ATTRIBUTE)!= 0) {
                short code = fDOMFilter.acceptNode(attr);
                switch (code) {
                    case NodeFilter.FILTER_REJECT:
                    case NodeFilter.FILTER_SKIP: { 
                        return;
                    }
                    default: {
                    }
                }
            }
            _printer.printSpace();
            _printer.printText( name );
            _printer.printText( "=\"" );
            printEscaped( value );
            _printer.printText( '"' );
        }
        if (name.equals( "xml:space" )) {
            if (value.equals( "preserve" ))
                fPreserveSpace = true;
            else
                fPreserveSpace = _format.getPreserveSpace();
        }
    }
    protected String getEntityRef( int ch ) {
        switch (ch) {
        case '<':
            return "lt";
        case '>':
            return "gt";
        case '"':
            return "quot";
        case '\'':
            return "apos";
        case '&':
            return "amp";
        }
        return null;
    }
    private Attributes extractNamespaces( Attributes attrs )
    throws SAXException
    {
        AttributesImpl attrsOnly;
        String         rawName;
        int            i;
        int            length;
        if (attrs == null) {
            return null;
        }
        length = attrs.getLength();
        attrsOnly = new AttributesImpl( attrs );
        for (i = length - 1 ; i >= 0 ; --i) {
            rawName = attrsOnly.getQName( i );
            if (rawName.startsWith( "xmlns" )) {
                if (rawName.length() == 5) {
                    startPrefixMapping( "", attrs.getValue( i ) );
                    attrsOnly.removeAttribute( i );
                } else if (rawName.charAt(5) == ':') {
                    startPrefixMapping(rawName.substring(6), attrs.getValue(i));
                    attrsOnly.removeAttribute( i );
                }
            }
        }
        return attrsOnly;
    }
    protected void printEscaped(String source) throws IOException {
        int length = source.length();
        for (int i = 0; i < length; ++i) {
            int ch = source.charAt(i);
            if (!XMLChar.isValid(ch)) {
                if (++i < length) {
                    surrogates(ch, source.charAt(i), false);
                } else {
                    fatalError("The character '" + (char) ch + "' is an invalid XML character");
                }
                continue;
            }
            if (ch == '\n' || ch == '\r' || ch == '\t') {
                printHex(ch);
            } else if (ch == '<') {
                _printer.printText("&lt;");
            } else if (ch == '&') {
                _printer.printText("&amp;");
            } else if (ch == '"') {
                _printer.printText("&quot;");
            } else if ((ch >= ' ' && _encodingInfo.isPrintable((char) ch))) {
                _printer.printText((char) ch);
            } else {
                printHex(ch);
            }
        }
    }
    protected void printXMLChar( int ch) throws IOException {
    	if (ch == '\r') {
			printHex(ch);
    	} else if ( ch == '<') {
            _printer.printText("&lt;");
        } else if (ch == '&') {
            _printer.printText("&amp;");
        } else if (ch == '>'){
			_printer.printText("&gt;");        	
        } else if ( ch == '\n' ||  ch == '\t' ||
                    ( ch >= ' ' && _encodingInfo.isPrintable((char)ch))) {
            _printer.printText((char)ch);
        } else {
			printHex(ch);
        }
    }
    protected void printText( String text, boolean preserveSpace, boolean unescaped )
    throws IOException {
        int index;
        char ch;
        int length = text.length();
        if ( preserveSpace ) {
            for ( index = 0 ; index < length ; ++index ) {
                ch = text.charAt( index );
                if (!XMLChar.isValid(ch)) {
                    if (++index <length) {
                        surrogates(ch, text.charAt(index), true);
                    } else {
                        fatalError("The character '"+ch+"' is an invalid XML character"); 
                    }
                    continue;
                }
                if ( unescaped ) {
                    _printer.printText( ch );
                } else
                    printXMLChar( ch );
            }
        } else {
            for ( index = 0 ; index < length ; ++index ) {
                ch = text.charAt( index );
                if (!XMLChar.isValid(ch)) {
                    if (++index <length) {
                        surrogates(ch, text.charAt(index), true);
                    } else {
                        fatalError("The character '"+ch+"' is an invalid XML character"); 
                    }
                    continue;
                }
				if ( unescaped )
                    _printer.printText( ch );
                else
                    printXMLChar( ch);
            }
        }
    }
    protected void printText( char[] chars, int start, int length,
                              boolean preserveSpace, boolean unescaped ) throws IOException {
        if ( preserveSpace ) {
            while ( length-- > 0 ) {
                char ch = chars[start++];
                if (!XMLChar.isValid(ch)) {
                    if ( length-- > 0 ) {
                        surrogates(ch, chars[start++], true);
                    } 
                    else {
                        fatalError("The character '"+ch+"' is an invalid XML character"); 
                    }
                    continue;
                }
                if ( unescaped ) {
                    _printer.printText( ch );
                } 
                else {
                    printXMLChar( ch );
                }
            }
        } else {
            while ( length-- > 0 ) {
                char ch = chars[start++];
                if (!XMLChar.isValid(ch)) {
                    if ( length-- > 0 ) {
                        surrogates(ch, chars[start++], true);
                    } 
                    else {
                        fatalError("The character '"+ch+"' is an invalid XML character"); 
                    }
                    continue;
                }
                if ( unescaped ) {
                    _printer.printText( ch );
                } 
                else {
                    printXMLChar( ch );
                }
            }
        }
    }
	protected void checkUnboundNamespacePrefixedNode (Node node) throws IOException{
		if (fNamespaces) {
   			if (DEBUG) {
			    System.out.println("==>serializeNode("+node.getNodeName()+") [Entity Reference - Namespaces on]");
				System.out.println("==>Declared Prefix Count: " + fNSBinder.getDeclaredPrefixCount());
				System.out.println("==>Node Name: " + node.getNodeName());
				System.out.println("==>First Child Node Name: " + node.getFirstChild().getNodeName());
				System.out.println("==>First Child Node Prefix: " + node.getFirstChild().getPrefix());
				System.out.println("==>First Child Node NamespaceURI: " + node.getFirstChild().getNamespaceURI());			
			}
			Node child, next;
	        for (child = node.getFirstChild(); child != null; child = next) {
	            next = child.getNextSibling();
			    if (DEBUG) {
			        System.out.println("==>serializeNode("+child.getNodeName()+") [Child Node]");
			        System.out.println("==>serializeNode("+child.getPrefix()+") [Child Node Prefix]");
	            }    
		 	    String prefix = child.getPrefix();
                prefix = (prefix == null || 
                        prefix.length() == 0) ? XMLSymbols.EMPTY_STRING : fSymbolTable.addSymbol(prefix);
		 	    if (fNSBinder.getURI(prefix) == null && prefix != null) {
					fatalError("The replacement text of the entity node '" 
								+ node.getNodeName()  
								+ "' contains an element node '" 
								+ child.getNodeName() 
								+ "' with an undeclared prefix '" 
								+ prefix + "'.");
		 	    }	
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap attrs = child.getAttributes();
					for (int i = 0; i< attrs.getLength(); i++ ) {
				 	    String attrPrefix = attrs.item(i).getPrefix();
                        attrPrefix = (attrPrefix == null || 
                                attrPrefix.length() == 0) ? XMLSymbols.EMPTY_STRING : fSymbolTable.addSymbol(attrPrefix);
				 	    if (fNSBinder.getURI(attrPrefix) == null && attrPrefix != null) {
							fatalError("The replacement text of the entity node '" 
										+ node.getNodeName()  
										+ "' contains an element node '" 
										+ child.getNodeName() 
										+ "' with an attribute '" 
										+ attrs.item(i).getNodeName() 										
										+ "' an undeclared prefix '" 
										+ attrPrefix + "'.");
				 	    }	
					}	
				}
				if (child.hasChildNodes()) {
					checkUnboundNamespacePrefixedNode(child);
				}	
	        }
		}    
	}	
    public boolean reset() {
        super.reset();
        if (fNSBinder != null){
            fNSBinder.reset();
            fNSBinder.declarePrefix(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
        }
        return true;
    }
}
