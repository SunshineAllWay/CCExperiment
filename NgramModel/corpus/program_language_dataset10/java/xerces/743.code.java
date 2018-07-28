package org.apache.xml.serialize;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
public class HTMLSerializer
    extends BaseMarkupSerializer
{
    private boolean _xhtml;
    public static final String XHTMLNamespace = "http://www.w3.org/1999/xhtml";
    private String fUserXHTMLNamespace = null;
    protected HTMLSerializer( boolean xhtml, OutputFormat format )
    {
        super( format );
        _xhtml = xhtml;
    }
    public HTMLSerializer()
    {
        this( false, new OutputFormat( Method.HTML, "ISO-8859-1", false ) );
    }
    public HTMLSerializer( OutputFormat format )
    {
        this( false, format != null ? format : new OutputFormat( Method.HTML, "ISO-8859-1", false ) );
    }
    public HTMLSerializer( Writer writer, OutputFormat format )
    {
        this( false, format != null ? format : new OutputFormat( Method.HTML, "ISO-8859-1", false ) );
        setOutputCharStream( writer );
    }
    public HTMLSerializer( OutputStream output, OutputFormat format )
    {
        this( false, format != null ? format : new OutputFormat( Method.HTML, "ISO-8859-1", false ) );
        setOutputByteStream( output );
    }
    public void setOutputFormat( OutputFormat format )
    {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.HTML, "ISO-8859-1", false ) );
    }
    public void setXHTMLNamespace(String newNamespace) {
        fUserXHTMLNamespace = newNamespace;
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
        String       htmlName;
        boolean      addNSAttr = false;
        try {
            if ( _printer == null )
            	throw new IllegalStateException( 
				    DOMMessageFormatter.formatMessage(
				    DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "NoWriterSupplied", null));
            state = getElementState();
            if ( isDocumentState() ) {
                if ( ! _started )
                    startDocument( (localName == null || localName.length() == 0) 
                        ? rawName : localName );
            } else {
                if ( state.empty )
                    _printer.printText( '>' );
                if ( _indenting && ! state.preserveSpace &&
                     ( state.empty || state.afterElement ) )
                    _printer.breakLine();
            }
            preserveSpace = state.preserveSpace;
            boolean hasNamespaceURI = (namespaceURI != null && namespaceURI.length() != 0);
            if ( rawName == null || rawName.length() == 0) {
                rawName = localName;
                if ( hasNamespaceURI ) {
                    String prefix;
                    prefix = getPrefix( namespaceURI );
                    if ( prefix != null && prefix.length() != 0 )
                        rawName = prefix + ":" + localName;
                }
                addNSAttr = true;
            }
            if ( !hasNamespaceURI )
                htmlName = rawName;
            else {
                if ( namespaceURI.equals( XHTMLNamespace ) ||
                        (fUserXHTMLNamespace != null && fUserXHTMLNamespace.equals(namespaceURI)) )
                    htmlName = localName;
                else
                    htmlName = null;
            }
            _printer.printText( '<' );
            if ( _xhtml )
                _printer.printText( rawName.toLowerCase(Locale.ENGLISH) );
            else
                _printer.printText( rawName );
            _printer.indent();
            if ( attrs != null ) {
                for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                    _printer.printSpace();
                    name = attrs.getQName( i ).toLowerCase(Locale.ENGLISH);
                    value = attrs.getValue( i );
                    if ( _xhtml || hasNamespaceURI ) {
                        if ( value == null ) {
                            _printer.printText( name );
                            _printer.printText( "=\"\"" );
                        } else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    } else {
                        if ( value == null ) {
                            value = "";
                        }
                        if ( !_format.getPreserveEmptyAttributes() && value.length() == 0 )
                            _printer.printText( name );
                        else if ( HTMLdtd.isURI( rawName, name ) ) {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            _printer.printText( escapeURI( value ) );
                            _printer.printText( '"' );
                        } else if ( HTMLdtd.isBoolean( rawName, name ) )
                            _printer.printText( name );
                        else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    }
                }
            }
            if ( htmlName != null && HTMLdtd.isPreserveSpace( htmlName ) )
                preserveSpace = true;
            if ( addNSAttr ) {
                Iterator entries = _prefixes.entrySet().iterator();
                while (entries.hasNext()) {
                    _printer.printSpace();
                    Map.Entry entry = (Map.Entry) entries.next();
                    value = (String) entry.getKey();
                    name = (String) entry.getValue();
                    if ( name.length() == 0 ) {
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
            if ( htmlName != null && ( htmlName.equalsIgnoreCase( "A" ) ||
                                       htmlName.equalsIgnoreCase( "TD" ) ) ) {
                state.empty = false;
                _printer.printText( '>' );
            }
            if ( htmlName != null && ( rawName.equalsIgnoreCase( "SCRIPT" ) ||
                                       rawName.equalsIgnoreCase( "STYLE" ) ) ) {
                if ( _xhtml ) {
                    state.doCData = true;
                } else {
                    state.unescaped = true;
                }
            }
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void endElement( String namespaceURI, String localName,
                            String rawName )
        throws SAXException
    {
        try {
            endElementIO( namespaceURI, localName, rawName );
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void endElementIO( String namespaceURI, String localName,
                              String rawName )
        throws IOException
    {
        ElementState state;
        String       htmlName;
        _printer.unindent();
        state = getElementState();
        if ( state.namespaceURI == null || state.namespaceURI.length() == 0 )
            htmlName = state.rawName;
        else {
            if ( state.namespaceURI.equals( XHTMLNamespace ) ||
                        (fUserXHTMLNamespace != null && fUserXHTMLNamespace.equals(state.namespaceURI)) )
                htmlName = state.localName;
            else
                htmlName = null;
        }
        if ( _xhtml) {
            if ( state.empty ) {
                _printer.printText( " />" );
            } else {
                if ( state.inCData )
                    _printer.printText( "]]>" );
                _printer.printText( "</" );
                _printer.printText( state.rawName.toLowerCase(Locale.ENGLISH) );
                _printer.printText( '>' );
            }
        } else {
            if ( state.empty )
                _printer.printText( '>' );
            if ( htmlName == null || ! HTMLdtd.isOnlyOpening( htmlName ) ) {
                if ( _indenting && ! state.preserveSpace && state.afterElement )
                    _printer.breakLine();
                if ( state.inCData )
                    _printer.printText( "]]>" );
                _printer.printText( "</" );
                _printer.printText( state.rawName );
                _printer.printText( '>' );
            }
        }
        state = leaveElementState();
        if ( htmlName == null || ( ! htmlName.equalsIgnoreCase( "A" ) &&
                                   ! htmlName.equalsIgnoreCase( "TD" ) ) )
            state.afterElement = true;
        state.empty = false;
        if ( isDocumentState() )
            _printer.flush();
    }
    public void characters( char[] chars, int start, int length )
        throws SAXException
    {
        ElementState state;
        try {
            state = content();
            state.doCData = false;
            super.characters( chars, start, length );
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }
    public void startElement( String tagName, AttributeList attrs )
        throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        try {
            if ( _printer == null )
                throw new IllegalStateException( 
				    DOMMessageFormatter.formatMessage(
				    DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "NoWriterSupplied", null));
            state = getElementState();
            if ( isDocumentState() ) {
                if ( ! _started )
                    startDocument( tagName );
            } else {
                if ( state.empty )
                    _printer.printText( '>' );
                if ( _indenting && ! state.preserveSpace &&
                     ( state.empty || state.afterElement ) )
                    _printer.breakLine();
            }
            preserveSpace = state.preserveSpace;
            _printer.printText( '<' );
            if ( _xhtml )
                _printer.printText( tagName.toLowerCase(Locale.ENGLISH) );
            else
                _printer.printText( tagName );
            _printer.indent();
            if ( attrs != null ) {
                for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                    _printer.printSpace();
                    name = attrs.getName( i ).toLowerCase(Locale.ENGLISH);
                    value = attrs.getValue( i );
                    if ( _xhtml ) {
                        if ( value == null ) {
                            _printer.printText( name );
                            _printer.printText( "=\"\"" );
                        } else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    } else {
                        if ( value == null ) {
                            value = "";
                        }
                        if ( !_format.getPreserveEmptyAttributes() && value.length() == 0 )
                            _printer.printText( name );
                        else if ( HTMLdtd.isURI( tagName, name ) ) {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            _printer.printText( escapeURI( value ) );
                            _printer.printText( '"' );
                        } else if ( HTMLdtd.isBoolean( tagName, name ) )
                            _printer.printText( name );
                        else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    }
                }
            }
            if ( HTMLdtd.isPreserveSpace( tagName ) )
                preserveSpace = true;
            state = enterElementState( null, null, tagName, preserveSpace );
            if ( tagName.equalsIgnoreCase( "A" ) || tagName.equalsIgnoreCase( "TD" ) ) {
                state.empty = false;
                _printer.printText( '>' );
            }
            if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
                 tagName.equalsIgnoreCase( "STYLE" ) ) {
                if ( _xhtml ) {
                    state.doCData = true;
                } else {
                    state.unescaped = true;
                }
            }
        } catch ( IOException except ) {
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
        _printer.leaveDTD();
        if ( ! _started ) {
            if ( _docTypePublicId == null && _docTypeSystemId == null ) {
                if ( _xhtml ) {
                    _docTypePublicId = HTMLdtd.XHTMLPublicId;
                    _docTypeSystemId = HTMLdtd.XHTMLSystemId;
                } else {
                    _docTypePublicId = HTMLdtd.HTMLPublicId;
                    _docTypeSystemId = HTMLdtd.HTMLSystemId;
                }
            }
            if ( ! _format.getOmitDocumentType() ) {
                if ( _docTypePublicId != null && ( ! _xhtml || _docTypeSystemId != null )  ) {
                    if (_xhtml) {
                        _printer.printText( "<!DOCTYPE html PUBLIC " );
                    }
                    else {
                        _printer.printText( "<!DOCTYPE HTML PUBLIC " );
                    }
                    printDoctypeURL( _docTypePublicId );
                    if ( _docTypeSystemId != null ) {
                        if ( _indenting ) {
                            _printer.breakLine();
                            _printer.printText( "                      " );
                        } else
                        _printer.printText( ' ' );
                        printDoctypeURL( _docTypeSystemId );
                    }
                    _printer.printText( '>' );
                    _printer.breakLine();
                } else if ( _docTypeSystemId != null ) {
                    if (_xhtml) {
                        _printer.printText( "<!DOCTYPE html SYSTEM " );
                    }
                    else {
                        _printer.printText( "<!DOCTYPE HTML SYSTEM " );
                    }
                    printDoctypeURL( _docTypeSystemId );
                    _printer.printText( '>' );
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
        boolean      preserveSpace;
        String       name;
        String       value;
        String       tagName;
        tagName = elem.getTagName();
        state = getElementState();
        if ( isDocumentState() ) {
            if ( ! _started )
                startDocument( tagName );
        } else {
            if ( state.empty )
                _printer.printText( '>' );
            if ( _indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement ) )
                _printer.breakLine();
        }
        preserveSpace = state.preserveSpace;
        _printer.printText( '<' );
        if ( _xhtml )
            _printer.printText( tagName.toLowerCase(Locale.ENGLISH) );
        else
            _printer.printText( tagName );
        _printer.indent();
        attrMap = elem.getAttributes();
        if ( attrMap != null ) {
            for ( i = 0 ; i < attrMap.getLength() ; ++i ) {
                attr = (Attr) attrMap.item( i );
                name = attr.getName().toLowerCase(Locale.ENGLISH);
                value = attr.getValue();
                if ( attr.getSpecified() ) {
                    _printer.printSpace();
                    if ( _xhtml ) {
                        if ( value == null ) {
                            _printer.printText( name );
                            _printer.printText( "=\"\"" );
                        } else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    } else {
                        if ( value == null ) {
                            value = "";
                        }
                        if ( !_format.getPreserveEmptyAttributes() && value.length() == 0 )
                            _printer.printText( name );
                        else if ( HTMLdtd.isURI( tagName, name ) ) {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            _printer.printText( escapeURI( value ) );
                            _printer.printText( '"' );
                        } else if ( HTMLdtd.isBoolean( tagName, name ) )
                            _printer.printText( name );
                        else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    }
                }
            }
        }
        if ( HTMLdtd.isPreserveSpace( tagName ) )
            preserveSpace = true;
        if ( elem.hasChildNodes() || ! HTMLdtd.isEmptyTag( tagName ) ) {
            state = enterElementState( null, null, tagName, preserveSpace );
            if ( tagName.equalsIgnoreCase( "A" ) || tagName.equalsIgnoreCase( "TD" ) ) {
                state.empty = false;
                _printer.printText( '>' );
            }
            if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
                 tagName.equalsIgnoreCase( "STYLE" ) ) {
                if ( _xhtml ) {
                    state.doCData = true;
                } else {
                    state.unescaped = true;
                }
            }
            child = elem.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            endElementIO( null, null, tagName );
        } else {
            _printer.unindent();
            if ( _xhtml )
                _printer.printText( " />" );
            else
                _printer.printText( '>' );
            state.afterElement = true;
            state.empty = false;
            if ( isDocumentState() )
                _printer.flush();
        }
    }
    protected void characters( String text )
        throws IOException
    {
        content();
        super.characters( text );
    }
    protected String getEntityRef( int ch )
    {
        return HTMLdtd.fromChar( ch );
    }
    protected String escapeURI( String uri )
    {
        int index;
        index = uri.indexOf( "\"" );
        if ( index >= 0 ) {
            return uri.substring( 0, index );
        }
        return uri;
    }
}
