package org.apache.html.dom;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLMapElement;
public class HTMLMapElementImpl
    extends HTMLElementImpl
    implements HTMLMapElement
{
    private static final long serialVersionUID = 7520887584251976392L;
    public HTMLCollection getAreas()
    {
        if ( _areas == null )
            _areas = new HTMLCollectionImpl( this, HTMLCollectionImpl.AREA );
        return _areas;
    }
      public String getName()
    {
        return getAttribute( "name" );
    }
    public void setName( String name )
    {
        setAttribute( "name", name );
    }
    public Node cloneNode( boolean deep )
    {
        HTMLMapElementImpl clonedNode = (HTMLMapElementImpl)super.cloneNode( deep );
        clonedNode._areas = null;
        return clonedNode;
    }
    public HTMLMapElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
    private HTMLCollection    _areas;
}
