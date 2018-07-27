package org.apache.html.dom;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLTitleElement;
public class HTMLTitleElementImpl
    extends HTMLElementImpl
    implements HTMLTitleElement
{
    private static final long serialVersionUID = 879646303512367875L;
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
    public HTMLTitleElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
