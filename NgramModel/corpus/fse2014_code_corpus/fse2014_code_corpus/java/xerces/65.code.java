package org.apache.html.dom;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLDOMImplementation;
import org.w3c.dom.html.HTMLDocument;
public class HTMLDOMImplementationImpl
    extends DOMImplementationImpl
    implements HTMLDOMImplementation
{
    private static final HTMLDOMImplementation _instance = new HTMLDOMImplementationImpl();
    private HTMLDOMImplementationImpl()
    {
    }
    public final HTMLDocument createHTMLDocument( String title )
        throws DOMException
    {
	HTMLDocument doc;
	if ( title == null )
	    throw new NullPointerException( "HTM014 Argument 'title' is null." );
	doc = new HTMLDocumentImpl();
	doc.setTitle( title );
	return doc;
    }
    public static HTMLDOMImplementation getHTMLDOMImplementation()
    {
	return _instance;
    }
}
