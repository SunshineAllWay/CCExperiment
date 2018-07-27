package org.apache.html.dom;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLScriptElement;
public class HTMLScriptElementImpl
    extends HTMLElementImpl
    implements HTMLScriptElement
{
    private static final long serialVersionUID = 5090330049085326558L;
    public String getText()
    {
        Node child;
        StringBuffer text = new StringBuffer();
        child = getFirstChild();
        while ( child != null )
        {
            if ( child instanceof Text ) {
                text.append(( (Text) child ).getData());
            }
            child = child.getNextSibling();
        }
        return text.toString();
    }
    public void setText( String text )
    {
        Node    child;
        Node    next;
        child = getFirstChild();
        while ( child != null )
        {
            next = child.getNextSibling();
            removeChild( child );
            child = next;
        }
        insertBefore( getOwnerDocument().createTextNode( text ), getFirstChild() );
    }
       public String getHtmlFor()
    {
        return getAttribute( "for" );
    }
    public void setHtmlFor( String htmlFor )
    {
        setAttribute( "for", htmlFor );
    }
       public String getEvent()
    {
        return getAttribute( "event" );
    }
    public void setEvent( String event )
    {
        setAttribute( "event", event );
    }
       public String getCharset()
    {
        return getAttribute( "charset" );
    }
    public void setCharset( String charset )
    {
        setAttribute( "charset", charset );
    }
    public boolean getDefer()
    {
        return getBinary( "defer" );
    }
    public void setDefer( boolean defer )
    {
        setAttribute( "defer", defer );
    }
       public String getSrc()
    {
        return getAttribute( "src" );
    }
    public void setSrc( String src )
    {
        setAttribute( "src", src );
    }
    public String getType()
    {
        return getAttribute( "type" );
    }
    public void setType( String type )
    {
        setAttribute( "type", type );
    }
    public HTMLScriptElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
