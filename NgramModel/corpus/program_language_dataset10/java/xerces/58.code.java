package org.apache.html.dom;
import java.util.Vector;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.ProcessingInstructionImpl;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
public class HTMLBuilder
    implements DocumentHandler
{
    protected HTMLDocumentImpl    _document;
    protected ElementImpl        _current;
    private boolean         _ignoreWhitespace = true;
    private boolean         _done = true;
    protected Vector         _preRootNodes;
    public void startDocument()
        throws SAXException
    {
        if ( ! _done )
	    throw new SAXException( "HTM001 State error: startDocument fired twice on one builder." );
	_document = null;
	_done = false;
    }
    public void endDocument()
        throws SAXException
    {
        if ( _document == null )
            throw new SAXException( "HTM002 State error: document never started or missing document element." );
	if ( _current != null )
	    throw new SAXException( "HTM003 State error: document ended before end of document element." );
        _current = null;
	_done = true;
    }
    public synchronized void startElement( String tagName, AttributeList attrList )
        throws SAXException
    {
        ElementImpl elem;
        int         i;
	if ( tagName == null )
	    throw new SAXException( "HTM004 Argument 'tagName' is null." );
	if ( _document == null )
	{
	    _document = new HTMLDocumentImpl();
	    elem = (ElementImpl) _document.getDocumentElement();
	    _current = elem;
	    if ( _current == null )
		throw new SAXException( "HTM005 State error: Document.getDocumentElement returns null." );
	    if ( _preRootNodes != null )
	    {
		for ( i = _preRootNodes.size() ; i-- > 0 ; )
		    _document.insertBefore( (Node) _preRootNodes.elementAt( i ), elem );
		_preRootNodes = null;
	    }
	}
	else
	{
	    if ( _current == null )
		throw new SAXException( "HTM006 State error: startElement called after end of document element." );
	    elem = (ElementImpl) _document.createElement( tagName );
	    _current.appendChild( elem );
	    _current = elem;
	}
        if ( attrList != null )
        {
            for ( i = 0 ; i < attrList.getLength() ; ++ i )
                elem.setAttribute( attrList.getName( i ), attrList.getValue( i ) );
        }
    }
    public void endElement( String tagName )
        throws SAXException
    {
        if ( _current == null )
            throw new SAXException( "HTM007 State error: endElement called with no current node." );
	if ( ! _current.getNodeName().equalsIgnoreCase( tagName ))
	    throw new SAXException( "HTM008 State error: mismatch in closing tag name " + tagName + "\n" + tagName);
	if ( _current.getParentNode() == _current.getOwnerDocument() )
	    _current = null;
	else
	    _current = (ElementImpl) _current.getParentNode();
    }
    public void characters( String text )
        throws SAXException
    {
	if ( _current == null )
            throw new SAXException( "HTM009 State error: character data found outside of root element." );
        _current.appendChild( _document.createTextNode(text) );
    }
    public void characters( char[] text, int start, int length )
        throws SAXException
    {
	if ( _current == null )
            throw new SAXException( "HTM010 State error: character data found outside of root element." );
        _current.appendChild( _document.createTextNode(new String(text, start, length)) );
    }
    public void ignorableWhitespace( char[] text, int start, int length )
        throws SAXException
    {        
        if ( ! _ignoreWhitespace )
            _current.appendChild( _document.createTextNode(new String(text, start, length)) );
     }
    public void processingInstruction( String target, String instruction )
        throws SAXException
    {        
        if ( _current == null && _document == null )
	{
	    if ( _preRootNodes == null )
		_preRootNodes = new Vector();
	    _preRootNodes.addElement( new ProcessingInstructionImpl( null, target, instruction ) );
	}
	else
        if ( _current == null && _document != null )
            _document.appendChild( _document.createProcessingInstruction(target, instruction) );
	else
        _current.appendChild( _document.createProcessingInstruction(target, instruction) );
    }
    public HTMLDocument getHTMLDocument()
    {
        return _document;
    }
    public void setDocumentLocator( Locator locator )
    {
    }
}
