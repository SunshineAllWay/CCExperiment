package org.apache.html.dom;
import org.w3c.dom.html.HTMLAnchorElement;
public class HTMLAnchorElementImpl
    extends HTMLElementImpl
    implements HTMLAnchorElement
{
    private static final long serialVersionUID = -140558580924061847L;
    public String getAccessKey()
    {
        String    accessKey;
        accessKey = getAttribute( "accesskey" );
        if ( accessKey != null && accessKey.length() > 1 )
            accessKey = accessKey.substring( 0, 1 );
        return accessKey;
    }
    public void setAccessKey( String accessKey )
    {
        if ( accessKey != null && accessKey.length() > 1 )
            accessKey = accessKey.substring( 0, 1 );
        setAttribute( "accesskey", accessKey );
    }
    public String getCharset()
    {
        return getAttribute( "charset" );
    }
    public void setCharset( String charset )
    {
        setAttribute( "charset", charset );
    }
    public String getCoords()
    {
        return getAttribute( "coords" );
    }
    public void setCoords( String coords )
    {
        setAttribute( "coords", coords );
    }
    public String getHref()
    {
        return getAttribute( "href" );
    }
    public void setHref( String href )
    {
        setAttribute( "href", href );
    }
    public String getHreflang()
    {
        return getAttribute( "hreflang" );
    }
    public void setHreflang( String hreflang )
    {
        setAttribute( "hreflang", hreflang );
    }
    public String getName()
    {
        return getAttribute( "name" );
    }
    public void setName( String name )
    {
        setAttribute( "name", name );
    }
    public String getRel()
    {
        return getAttribute( "rel" );
    }
    public void setRel( String rel )
    {
        setAttribute( "rel", rel );
    }
    public String getRev()
    {
        return getAttribute( "rev" );
    }
    public void setRev( String rev )
    {
        setAttribute( "rev", rev );
    }
    public String getShape()
    {
        return capitalize( getAttribute( "shape" ) );
    }
    public void setShape( String shape )
    {
        setAttribute( "shape", shape );
    }
    public int getTabIndex()
    {
        return this.getInteger( getAttribute( "tabindex" ) );
    }
    public void setTabIndex( int tabIndex )
    {
        setAttribute( "tabindex", String.valueOf( tabIndex ) );
    }
    public String getTarget()
    {
        return getAttribute( "target" );
    }
    public void setTarget( String target )
    {
        setAttribute( "target", target );
    }
    public String getType()
    {
        return getAttribute( "type" );
    }
    public void setType( String type )
    {
        setAttribute( "type", type );
    }
    public void blur()
    {
    }
    public void focus()
    {
    }
    public HTMLAnchorElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
