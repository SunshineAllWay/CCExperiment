package org.apache.xml.serialize;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xerces.dom.DOMErrorImpl;
import org.apache.xerces.dom.DOMLocatorImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.util.XMLChar;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
public abstract class BaseMarkupSerializer
    implements ContentHandler, DocumentHandler, LexicalHandler,
               DTDHandler, DeclHandler, DOMSerializer, Serializer
{
    protected short features = 0xFFFFFFFF;
    protected DOMErrorHandler fDOMErrorHandler;
    protected final DOMErrorImpl fDOMError = new DOMErrorImpl();
    protected LSSerializerFilter fDOMFilter;
    protected EncodingInfo _encodingInfo;
    private ElementState[]  _elementStates;
    private int             _elementStateCount;
    private Vector          _preRoot;
    protected boolean       _started;
    private boolean         _prepared;
    protected Hashtable     _prefixes;
    protected String        _docTypePublicId;
    protected String        _docTypeSystemId;
    protected OutputFormat   _format;
    protected Printer       _printer;
    protected boolean       _indenting;
    protected final StringBuffer fStrBuffer = new StringBuffer(40);
    private Writer          _writer;
    private OutputStream    _output;
    protected Node fCurrentNode = null;
    protected BaseMarkupSerializer( OutputFormat format )
    {
        int i;
        _elementStates = new ElementState[ 10 ];
        for ( i = 0 ; i < _elementStates.length ; ++i )
            _elementStates[ i ] = new ElementState();
        _format = format;
    }
    public DocumentHandler asDocumentHandler()
        throws IOException
    {
        prepare();
        return this;
    }
    public ContentHandler asContentHandler()
        throws IOException
    {
        prepare();
        return this;
    }
    public DOMSerializer asDOMSerializer()
        throws IOException
    {
        prepare();
        return this;
    }
    public void setOutputByteStream( OutputStream output )
    {
        if ( output == null ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                                                           "ArgumentIsNull", new Object[]{"output"});
            throw new NullPointerException(msg);
        }
        _output = output;
        _writer = null;
        reset();
    }
    public void setOutputCharStream( Writer writer )
    {
        if ( writer == null ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                                                           "ArgumentIsNull", new Object[]{"writer"});
            throw new NullPointerException(msg);
        }
        _writer = writer;
        _output = null;
        reset();
    }
    public void setOutputFormat( OutputFormat format )
    {
        if ( format == null ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                                                           "ArgumentIsNull", new Object[]{"format"});
            throw new NullPointerException(msg);
        }
        _format = format;
        reset();
    }
    public boolean reset()
    {
        if ( _elementStateCount > 1 ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                                                           "ResetInMiddle", null);
            throw new IllegalStateException(msg);
        }
        _prepared = false;
        fCurrentNode = null;
        fStrBuffer.setLength(0);
        return true;
    }
    protected void cleanup() {
        fCurrentNode = null;
    }
    protected void prepare()
        throws IOException
    {
        if ( _prepared )
            return;
        if ( _writer == null && _output == null ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                                                           "NoWriterSupplied", null);
            throw new IOException(msg);
        }
        _encodingInfo = _format.getEncodingInfo();
        if ( _output != null ) {
            _writer = _encodingInfo.getWriter(_output);
        }
        if ( _format.getIndenting() ) {
            _indenting = true;
            _printer = new IndentPrinter( _writer, _format );
        } else {
            _indenting = false;
            _printer = new Printer( _writer, _format );
        }
        ElementState state;
        _elementStateCount = 0;
        state = _elementStates[ 0 ];
        state.namespaceURI = null;
        state.localName = null;
        state.rawName = null;
        state.preserveSpace = _format.getPreserveSpace();
        state.empty = true;
        state.afterElement = false;
        state.afterComment = false;
        state.doCData = state.inCData = false;
        state.prefixes = null;
        _docTypePublicId = _format.getDoctypePublic();
        _docTypeSystemId = _format.getDoctypeSystem();
        _started = false;
        _prepared = true;
    }
    public void serialize( Element elem )
        throws IOException
    {
        reset();
        prepare();
        serializeNode( elem );
        cleanup();
        _printer.flush();
        if ( _printer.getException() != null )
            throw _printer.getException();
    }
    public void serialize( DocumentFragment frag )
        throws IOException
    {
        reset();
        prepare();
        serializeNode( frag );
        cleanup();
        _printer.flush();
        if ( _printer.getException() != null )
            throw _printer.getException();
    }
    public void serialize( Document doc )
        throws IOException
    {
        reset();
        prepare();
        serializeNode( doc );
        serializePreRoot();
        cleanup();
        _printer.flush();
        if ( _printer.getException() != null )
            throw _printer.getException();
    }
    public void startDocument()
        throws SAXException
    {
        try {
            prepare();
        } catch ( IOException except ) {
            throw new SAXException( except.toString() );
        }
    }
    public void characters( char[] chars, int start, int length )
        throws SAXException
    {
        ElementState state;
        try {
        state = content();
        if ( state.inCData || state.doCData ) {
            int          saveIndent;
            if ( ! state.inCData ) {
                _printer.printText( "<![CDATA[" );
                state.inCData = true;
            }
            saveIndent = _printer.getNextIndent();
            _printer.setNextIndent( 0 );
            char ch;
            final int end = start + length;
            for ( int index = start ; index < end; ++index ) {
                ch = chars[index];
                if ( ch == ']' && index + 2 < end &&
                     chars[ index + 1 ] == ']' && chars[ index + 2 ] == '>' ) {
                    _printer.printText("]]]]><![CDATA[>");
                    index +=2; 
                    continue;
                }
                if (!XMLChar.isValid(ch)) {
                    if (++index < end) {
                        surrogates(ch, chars[index], true);
                    } 
                    else {
                        fatalError("The character '"+ch+"' is an invalid XML character"); 
                    }
                    continue;
                }
                if ( ( ch >= ' ' && _encodingInfo.isPrintable(ch) && ch != 0x7F ) ||
                    ch == '\n' || ch == '\r' || ch == '\t' ) {
                    _printer.printText(ch);
                } 
                else {
                    _printer.printText("]]>&#x");                        
                    _printer.printText(Integer.toHexString(ch));                        
                    _printer.printText(";<![CDATA[");
                }
            }
            _printer.setNextIndent( saveIndent );
        } else {
            int saveIndent;
            if ( state.preserveSpace ) {
                saveIndent = _printer.getNextIndent();
                _printer.setNextIndent( 0 );
                printText( chars, start, length, true, state.unescaped );
                _printer.setNextIndent( saveIndent );
            } else {
                printText( chars, start, length, false, state.unescaped );
            }
        }
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void ignorableWhitespace( char[] chars, int start, int length )
        throws SAXException
    {
        int i;
        try {
        content();
        if ( _indenting ) {
            _printer.setThisIndent( 0 );
            for ( i = start ; length-- > 0 ; ++i )
                _printer.printText( chars[ i ] );
        }
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public final void processingInstruction( String target, String code )
        throws SAXException
    {
        try {
            processingInstructionIO( target, code );
        } catch ( IOException except ) {
        throw new SAXException( except );
        }
    }
    public void processingInstructionIO( String target, String code )
        throws IOException
    {
        int          index;
        ElementState state;
        state = content();
        index = target.indexOf( "?>" );
        if ( index >= 0 )
            fStrBuffer.append( "<?" ).append( target.substring( 0, index ) );
        else
            fStrBuffer.append( "<?" ).append( target );
        if ( code != null ) {
            fStrBuffer.append( ' ' );
            index = code.indexOf( "?>" );
            if ( index >= 0 )
                fStrBuffer.append( code.substring( 0, index ) );
            else
                fStrBuffer.append( code );
        }
        fStrBuffer.append( "?>" );
        if ( isDocumentState() ) {
            if ( _preRoot == null )
                _preRoot = new Vector();
            _preRoot.addElement( fStrBuffer.toString() );
        } else {
            _printer.indent();
            printText( fStrBuffer.toString(), true, true );
            _printer.unindent();
            if ( _indenting )
            state.afterElement = true;
        }
        fStrBuffer.setLength(0);
    }
    public void comment( char[] chars, int start, int length )
        throws SAXException
    {
        try {
        comment( new String( chars, start, length ) );
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }
    public void comment( String text )
        throws IOException
    {
        int          index;
        ElementState state;
        if ( _format.getOmitComments() )
            return;
        state  = content();
        index = text.indexOf( "-->" );
        if ( index >= 0 )
            fStrBuffer.append( "<!--" ).append( text.substring( 0, index ) ).append( "-->" );
        else
            fStrBuffer.append( "<!--" ).append( text ).append( "-->" );
        if ( isDocumentState() ) {
            if ( _preRoot == null )
                _preRoot = new Vector();
            _preRoot.addElement( fStrBuffer.toString() );
        } else {
            if ( _indenting && ! state.preserveSpace)
                _printer.breakLine();
						_printer.indent();
            printText( fStrBuffer.toString(), true, true );
						_printer.unindent();
            if ( _indenting )
                state.afterElement = true;
        }
        fStrBuffer.setLength(0);
	state.afterComment = true;
	state.afterElement = false;
    }
    public void startCDATA()
    {
        ElementState state;
        state = getElementState();
        state.doCData = true;
    }
    public void endCDATA()
    {
        ElementState state;
        state = getElementState();
        state.doCData = false;
    }
    public void startNonEscaping()
    {
        ElementState state;
        state = getElementState();
        state.unescaped = true;
    }
    public void endNonEscaping()
    {
        ElementState state;
        state = getElementState();
        state.unescaped = false;
    }
    public void startPreserving()
    {
        ElementState state;
        state = getElementState();
        state.preserveSpace = true;
    }
    public void endPreserving()
    {
        ElementState state;
        state = getElementState();
        state.preserveSpace = false;
    }
    public void endDocument()
        throws SAXException
    {
        try {
        serializePreRoot();
        _printer.flush();
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }
    public void startEntity( String name )
    {
    }
    public void endEntity( String name )
    {
    }
    public void setDocumentLocator( Locator locator )
    {
    }
    public void skippedEntity ( String name )
        throws SAXException
    {
        try {
        endCDATA();
        content();
        _printer.printText( '&' );
        _printer.printText( name );
        _printer.printText( ';' );
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }
    public void startPrefixMapping( String prefix, String uri )
        throws SAXException
    {
        if ( _prefixes == null )
            _prefixes = new Hashtable();
        _prefixes.put( uri, prefix == null ? "" : prefix );
    }
    public void endPrefixMapping( String prefix )
        throws SAXException
    {
    }
    public final void startDTD( String name, String publicId, String systemId )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _docTypePublicId = publicId;
        _docTypeSystemId = systemId;
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void endDTD()
    {
    }
    public void elementDecl( String name, String model )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _printer.printText( "<!ELEMENT " );
        _printer.printText( name );
        _printer.printText( ' ' );
        _printer.printText( model );
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void attributeDecl( String eName, String aName, String type,
                               String valueDefault, String value )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _printer.printText( "<!ATTLIST " );
        _printer.printText( eName );
        _printer.printText( ' ' );
        _printer.printText( aName );
        _printer.printText( ' ' );
        _printer.printText( type );
        if ( valueDefault != null ) {
            _printer.printText( ' ' );
            _printer.printText( valueDefault );
        }
        if ( value != null ) {
            _printer.printText( " \"" );
            printEscaped( value );
            _printer.printText( '"' );
        }
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }
    public void internalEntityDecl( String name, String value )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _printer.printText( "<!ENTITY " );
        _printer.printText( name );
        _printer.printText( " \"" );
        printEscaped( value );
        _printer.printText( "\">" );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void externalEntityDecl( String name, String publicId, String systemId )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        unparsedEntityDecl( name, publicId, systemId, null );
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void unparsedEntityDecl( String name, String publicId,
                                    String systemId, String notationName )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        if ( publicId == null ) {
            _printer.printText( "<!ENTITY " );
            _printer.printText( name );
            _printer.printText( " SYSTEM " );
            printDoctypeURL( systemId );
        } else {
            _printer.printText( "<!ENTITY " );
            _printer.printText( name );
            _printer.printText( " PUBLIC " );
            printDoctypeURL( publicId );
            _printer.printText( ' ' );
            printDoctypeURL( systemId );
        }
        if ( notationName != null ) {
            _printer.printText( " NDATA " );
            _printer.printText( notationName );
        }
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }
    public void notationDecl( String name, String publicId, String systemId )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        if ( publicId != null ) {
            _printer.printText( "<!NOTATION " );
            _printer.printText( name );
            _printer.printText( " PUBLIC " );
            printDoctypeURL( publicId );
            if ( systemId != null ) {
                _printer.printText( ' ' );
                printDoctypeURL( systemId );
            }
        } else {
            _printer.printText( "<!NOTATION " );
            _printer.printText( name );
            _printer.printText( " SYSTEM " );
            printDoctypeURL( systemId );
        }
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    protected void serializeNode( Node node )
        throws IOException
    {
        fCurrentNode = node;
        switch ( node.getNodeType() ) {
        case Node.TEXT_NODE : {
            String text;
            text = node.getNodeValue();
            if ( text != null ) {
                if (fDOMFilter !=null && 
                    (fDOMFilter.getWhatToShow() & NodeFilter.SHOW_TEXT)!= 0) {
                    short code = fDOMFilter.acceptNode(node);
                    switch (code) {
                        case NodeFilter.FILTER_REJECT:
                        case NodeFilter.FILTER_SKIP: { 
                            break;
                        }
                        default: {
                            characters(text);
                        }
                    }
                }
                else if ( !_indenting || getElementState().preserveSpace
                     || (text.replace('\n',' ').trim().length() != 0))
                    characters( text );
            }                
            break;
        }
        case Node.CDATA_SECTION_NODE : {
            String text = node.getNodeValue();
            if ((features & DOMSerializerImpl.CDATA) != 0) {
                if (text != null) {
                    if (fDOMFilter != null
                        && (fDOMFilter.getWhatToShow()
                            & NodeFilter.SHOW_CDATA_SECTION)
                            != 0) {
                        short code = fDOMFilter.acceptNode(node);
                        switch (code) {
                            case NodeFilter.FILTER_REJECT :
                            case NodeFilter.FILTER_SKIP :
                                {
                                    return;
                                }
                            default :
                                {
                                }
                        }
                    }
                    startCDATA();
                    characters(text);
                    endCDATA();
                }
            } else {
                characters(text);
            }
            break;
        }
        case Node.COMMENT_NODE : {
            String text;
            if ( ! _format.getOmitComments() ) {
                text = node.getNodeValue();
                if ( text != null ) {
                    if (fDOMFilter !=null && 
                          (fDOMFilter.getWhatToShow() & NodeFilter.SHOW_COMMENT)!= 0) {
                          short code = fDOMFilter.acceptNode(node);
                          switch (code) {
                              case NodeFilter.FILTER_REJECT:
                              case NodeFilter.FILTER_SKIP: { 
                                  return;
                              }
                              default: {
                              }
                          }                      
                    }
                    comment( text );
                }                    
            }
            break;
        }
        case Node.ENTITY_REFERENCE_NODE : {
            Node         child;
            endCDATA();
            content();
            if (((features & DOMSerializerImpl.ENTITIES) != 0)
                || (node.getFirstChild() == null)) {
                if (fDOMFilter !=null && 
                      (fDOMFilter.getWhatToShow() & NodeFilter.SHOW_ENTITY_REFERENCE)!= 0) {
                      short code = fDOMFilter.acceptNode(node);
                      switch (code) {
                        case NodeFilter.FILTER_REJECT:{
                            return; 
                          }
                          case NodeFilter.FILTER_SKIP: { 
                              child = node.getFirstChild();
                              while ( child != null ) {
                                  serializeNode( child );
                                  child = child.getNextSibling();
                              }
                              return;
                          }
                          default: {
                          }
                      }
                  }         
                checkUnboundNamespacePrefixedNode(node);
                _printer.printText("&");
                _printer.printText(node.getNodeName());
                _printer.printText(";");
            }
            else {
                child = node.getFirstChild();
                while ( child != null ) {
                    serializeNode( child );
                    child = child.getNextSibling();
                }
            }
            break;
        }
        case Node.PROCESSING_INSTRUCTION_NODE : {
            if (fDOMFilter !=null && 
                  (fDOMFilter.getWhatToShow() & NodeFilter.SHOW_PROCESSING_INSTRUCTION)!= 0) {
                  short code = fDOMFilter.acceptNode(node);
                  switch (code) {
                    case NodeFilter.FILTER_REJECT:                      
                    case NodeFilter.FILTER_SKIP: { 
                          return;  
                    }
                    default: { 
                    }
                  }
            }
            processingInstructionIO( node.getNodeName(), node.getNodeValue() );
            break;
        }
        case Node.ELEMENT_NODE :  {
            if (fDOMFilter !=null && 
                  (fDOMFilter.getWhatToShow() & NodeFilter.SHOW_ELEMENT)!= 0) {
                  short code = fDOMFilter.acceptNode(node);
                  switch (code) {
                    case NodeFilter.FILTER_REJECT: {
                        return;                     
                    }
                    case NodeFilter.FILTER_SKIP: { 
                        Node child = node.getFirstChild();
                        while ( child != null ) {
                            serializeNode( child );
                            child = child.getNextSibling();
                        }
                        return;  
                    }
                    default: { 
                    }
                  }
            }
            serializeElement( (Element) node );
            break;
        }
        case Node.DOCUMENT_NODE : {
            DocumentType      docType;
            docType = ( (Document) node ).getDoctype();
            if (docType != null) {
                try {
                    String internal;
                    _printer.enterDTD();
                    _docTypePublicId = docType.getPublicId();
                    _docTypeSystemId = docType.getSystemId();
                    internal = docType.getInternalSubset();
                    if ( internal != null && internal.length() > 0 )
                        _printer.printText( internal );
                    endDTD();
                }
                catch (NoSuchMethodError nsme) {
                    Class docTypeClass = docType.getClass();
                    String docTypePublicId = null;
                    String docTypeSystemId = null;
                    try {
                        java.lang.reflect.Method getPublicId = docTypeClass.getMethod("getPublicId", (Class[]) null);
                        if (getPublicId.getReturnType().equals(String.class)) {
                            docTypePublicId = (String)getPublicId.invoke(docType, (Object[]) null);
                        }
                    }
                    catch (Exception e) {
                    }
                    try {
                        java.lang.reflect.Method getSystemId = docTypeClass.getMethod("getSystemId", (Class[]) null);
                        if (getSystemId.getReturnType().equals(String.class)) {
                            docTypeSystemId = (String)getSystemId.invoke(docType, (Object[]) null);
                        }
                    }
                    catch (Exception e) {
                    }
                    _printer.enterDTD();
                    _docTypePublicId = docTypePublicId;
                    _docTypeSystemId = docTypeSystemId;
                    endDTD();
                }
            }
        }
        case Node.DOCUMENT_FRAGMENT_NODE : {
            Node         child;
            child = node.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            break;
        }
        default:
            break;
        }
    }
    protected ElementState content()
        throws IOException
    {
        ElementState state;
        state = getElementState();
        if ( ! isDocumentState() ) {
            if ( state.inCData && ! state.doCData ) {
                _printer.printText( "]]>" );
                state.inCData = false;
            }
            if ( state.empty ) {
                _printer.printText( '>' );
                state.empty = false;
            }
            state.afterElement = false;
            state.afterComment = false;
        }
        return state;
    }
    protected void characters( String text )
        throws IOException
    {
        ElementState state;
        state = content();
        if ( state.inCData || state.doCData ) {
            if ( ! state.inCData ) {
                _printer.printText("<![CDATA[");
                state.inCData = true;
            }
            int saveIndent = _printer.getNextIndent();
            _printer.setNextIndent( 0 );
            printCDATAText( text);
            _printer.setNextIndent( saveIndent );
        } else {
            int saveIndent;
            if ( state.preserveSpace ) {
                saveIndent = _printer.getNextIndent();
                _printer.setNextIndent( 0 );
                printText( text, true, state.unescaped );
                _printer.setNextIndent( saveIndent );
            } else {
                printText( text, false, state.unescaped );
            }
        }
    }
    protected abstract String getEntityRef( int ch );
    protected abstract void serializeElement( Element elem )
        throws IOException;
    protected void serializePreRoot()
        throws IOException
    {
        int i;
        if ( _preRoot != null ) {
            for ( i = 0 ; i < _preRoot.size() ; ++i ) {
                printText( (String) _preRoot.elementAt( i ), true, true );
                if ( _indenting )
                _printer.breakLine();
            }
            _preRoot.removeAllElements();
        }
    }
    protected void printCDATAText( String text ) throws IOException {
        int length = text.length();
        char ch;
        for ( int index = 0 ; index <  length; ++index ) {
            ch = text.charAt( index );            
            if (ch == ']'
                && index + 2 < length
                && text.charAt(index + 1) == ']'
                && text.charAt(index + 2) == '>') { 
                if (fDOMErrorHandler != null) {
                    if ((features & DOMSerializerImpl.SPLITCDATA) == 0) {
                        String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.SERIALIZER_DOMAIN,
                            "EndingCDATA",
                            null);    
                        if ((features & DOMSerializerImpl.WELLFORMED) != 0) {
                            modifyDOMError(msg, DOMError.SEVERITY_FATAL_ERROR, "wf-invalid-character", fCurrentNode);
                            fDOMErrorHandler.handleError(fDOMError);
                            throw new LSException(LSException.SERIALIZE_ERR, msg);
                        }
                        modifyDOMError(msg, DOMError.SEVERITY_ERROR, "cdata-section-not-splitted", fCurrentNode);
                        if (!fDOMErrorHandler.handleError(fDOMError)) {
                            throw new LSException(LSException.SERIALIZE_ERR, msg);
                        }                        
                    } else {
                        String msg =
                            DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.SERIALIZER_DOMAIN,
                                "SplittingCDATA",
                                null);
                        modifyDOMError(
                            msg,
                            DOMError.SEVERITY_WARNING,
                            null, fCurrentNode);
                        fDOMErrorHandler.handleError(fDOMError);
                    }
                }
                _printer.printText("]]]]><![CDATA[>");
                index += 2;
                continue;
            }
            if (!XMLChar.isValid(ch)) {
                if (++index <length) {
                    surrogates(ch, text.charAt(index), true);
                } 
                else {
                    fatalError("The character '"+ch+"' is an invalid XML character"); 
                }
                continue;
            }
            if ( ( ch >= ' ' && _encodingInfo.isPrintable(ch) && ch != 0x7F ) ||
                 ch == '\n' || ch == '\r' || ch == '\t' ) {
                _printer.printText(ch);
            } 
            else {
                _printer.printText("]]>&#x");                        
                _printer.printText(Integer.toHexString(ch));                        
                _printer.printText(";<![CDATA[");
            }
        }
    }
    protected void surrogates(int high, int low, boolean inContent) throws IOException{
        if (XMLChar.isHighSurrogate(high)) {
            if (!XMLChar.isLowSurrogate(low)) {
                fatalError("The character '"+(char)low+"' is an invalid XML character"); 
            }
            else {
                int supplemental = XMLChar.supplemental((char)high, (char)low);
                if (!XMLChar.isValid(supplemental)) {
                    fatalError("The character '"+(char)supplemental+"' is an invalid XML character"); 
                }
                else {
                    if (inContent && content().inCData) {
                        _printer.printText("]]>&#x");                        
                        _printer.printText(Integer.toHexString(supplemental));                        
                        _printer.printText(";<![CDATA[");
                    }  
                    else {
                        printHex(supplemental);
                    }
                }
            }
        } else {
            fatalError("The character '"+(char)high+"' is an invalid XML character"); 
        }
    }
    protected void printText( char[] chars, int start, int length,
                                    boolean preserveSpace, boolean unescaped )
        throws IOException
    {
        if ( preserveSpace ) {
            while ( length-- > 0 ) {
                char ch = chars[ start ];
                ++start;
                if ( ch == '\n' || ch == '\r' || unescaped ) {
                    _printer.printText( ch );
                } 
                else {
                    printEscaped( ch );
                }
            }
        } else {
            while ( length-- > 0 ) {
                char ch = chars[ start ];
                ++start;
                if ( ch == ' ' || ch == '\f' || ch == '\t' || ch == '\n' || ch == '\r' ) {
                    _printer.printSpace();
                } 
                else if ( unescaped ) {
                    _printer.printText( ch );
                } 
                else {
                    printEscaped( ch );
                }
            }
        }
    }
    protected void printText( String text, boolean preserveSpace, boolean unescaped )
        throws IOException
    {
        int index;
        char ch;
        if ( preserveSpace ) {
            for ( index = 0 ; index < text.length() ; ++index ) {
                ch = text.charAt( index );
                if ( ch == '\n' || ch == '\r' || unescaped )
                    _printer.printText( ch );
                else
                    printEscaped( ch );
            }
        } else {
            for ( index = 0 ; index < text.length() ; ++index ) {
                ch = text.charAt( index );
                if ( ch == ' ' || ch == '\f' || ch == '\t' || ch == '\n' || ch == '\r' ) {
                    _printer.printSpace();
                }
                else if ( unescaped ) {
                    _printer.printText( ch );
                }
                else {
                    printEscaped( ch );
                }
            }
        }
    }
    protected void printDoctypeURL( String url )
        throws IOException
    {
        int                i;
        _printer.printText( '"' );
        for( i = 0 ; i < url.length() ; ++i ) {
            if ( url.charAt( i ) == '"' ||  url.charAt( i ) < 0x20 || url.charAt( i ) > 0x7F ) {
                _printer.printText( '%' );
                _printer.printText( Integer.toHexString( url.charAt( i ) ) );
            } else
                _printer.printText( url.charAt( i ) );
        }
        _printer.printText( '"' );
    }
    protected void printEscaped( int ch )
        throws IOException
    {
        String charRef;
        charRef = getEntityRef( ch );
        if ( charRef != null ) {
            _printer.printText( '&' );
            _printer.printText( charRef );
            _printer.printText( ';' );
        } else if ( ( ch >= ' ' && _encodingInfo.isPrintable((char)ch) && ch != 0x7F ) ||
                    ch == '\n' || ch == '\r' || ch == '\t' ) {
            if (ch < 0x10000) {
                _printer.printText((char)ch );
            } else {
                _printer.printText((char)(((ch-0x10000)>>10)+0xd800));
                _printer.printText((char)(((ch-0x10000)&0x3ff)+0xdc00));
            }
        } else {
			printHex(ch);
        }
    }
	 final void printHex( int ch) throws IOException {	
		 _printer.printText( "&#x" );
		 _printer.printText(Integer.toHexString(ch));
		 _printer.printText( ';' );
	 }
    protected void printEscaped( String source )
        throws IOException
    {
        for ( int i = 0 ; i < source.length() ; ++i ) {
            int ch = source.charAt(i);
            if ((ch & 0xfc00) == 0xd800 && i+1 < source.length()) {
                int lowch = source.charAt(i+1);
                if ((lowch & 0xfc00) == 0xdc00) {
                    ch = 0x10000 + ((ch-0xd800)<<10) + lowch-0xdc00;
                    i++;
                }
            }
            printEscaped(ch);
        }
    }
    protected ElementState getElementState()
    {
        return _elementStates[ _elementStateCount ];
    }
    protected ElementState enterElementState( String namespaceURI, String localName,
                                              String rawName, boolean preserveSpace )
    {
        ElementState state;
        if ( _elementStateCount + 1 == _elementStates.length ) {
            ElementState[] newStates;
            newStates = new ElementState[ _elementStates.length + 10 ];
            for ( int i = 0 ; i < _elementStates.length ; ++i )
                newStates[ i ] = _elementStates[ i ];
            for ( int i = _elementStates.length ; i < newStates.length ; ++i )
                newStates[ i ] = new ElementState();
            _elementStates = newStates;
        }
        ++_elementStateCount;
        state = _elementStates[ _elementStateCount ];
        state.namespaceURI = namespaceURI;
        state.localName = localName;
        state.rawName = rawName;
        state.preserveSpace = preserveSpace;
        state.empty = true;
        state.afterElement = false;
        state.afterComment = false;
        state.doCData = state.inCData = false;
        state.unescaped = false;
        state.prefixes = _prefixes;
        _prefixes = null;
        return state;
    }
    protected ElementState leaveElementState()
    {
        if ( _elementStateCount > 0 ) {
            _prefixes = null;
            -- _elementStateCount;
            return _elementStates[ _elementStateCount ];
        }
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "Internal", null);
        throw new IllegalStateException(msg);
    }
    protected boolean isDocumentState() {
        return _elementStateCount == 0;
    }
    final void clearDocumentState() {
        _elementStateCount = 0;
    }
    protected String getPrefix( String namespaceURI )
    {
        String    prefix;
        if ( _prefixes != null ) {
            prefix = (String) _prefixes.get( namespaceURI );
            if ( prefix != null )
                return prefix;
        }
        if ( _elementStateCount == 0 ) {
            return null;
        }
        for ( int i = _elementStateCount ; i > 0 ; --i ) {
            if ( _elementStates[ i ].prefixes != null ) {
                prefix = (String) _elementStates[ i ].prefixes.get( namespaceURI );
                if ( prefix != null )
                    return prefix;
            }
        }
        return null;
    }
    protected DOMError modifyDOMError(String message, short severity, String type, Node node){
            fDOMError.reset();
            fDOMError.fMessage = message;
            fDOMError.fType = type;
            fDOMError.fSeverity = severity;
            fDOMError.fLocator = new DOMLocatorImpl(-1, -1, -1, node, null);
            return fDOMError;
    }
    protected void fatalError(String message) throws IOException{
        if (fDOMErrorHandler != null) {
            modifyDOMError(message, DOMError.SEVERITY_FATAL_ERROR, null, fCurrentNode);
            fDOMErrorHandler.handleError(fDOMError);
        } 
        else {
            throw new IOException(message);
        }
    }
	 protected void checkUnboundNamespacePrefixedNode (Node node) throws IOException{
	 }
}
