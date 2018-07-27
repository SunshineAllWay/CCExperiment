package org.apache.html.dom;
import org.w3c.dom.html.HTMLAppletElement;
public class HTMLAppletElementImpl
    extends HTMLElementImpl
    implements HTMLAppletElement
{
    private static final long serialVersionUID = 8375794094117740967L;
    public String getAlign()
    {
        return getAttribute( "align" );
    }
    public void setAlign( String align )
    {
        setAttribute( "align", align );
    }
    public String getAlt()
    {
        return getAttribute( "alt" );
    }
    public void setAlt( String alt )
    {
        setAttribute( "alt", alt );
    }
    public String getArchive()
    {
        return getAttribute( "archive" );
    }
    public void setArchive( String archive )
    {
        setAttribute( "archive", archive );
    }
    public String getCode()
    {
        return getAttribute( "code" );
    }
    public void setCode( String code )
    {
        setAttribute( "code", code );
    }
    public String getCodeBase()
    {
        return getAttribute( "codebase" );
    }
    public void setCodeBase( String codeBase )
    {
        setAttribute( "codebase", codeBase );
    }
    public String getHeight()
    {
        return getAttribute( "height" );
    }
    public void setHeight( String height )
    {
        setAttribute( "height", height );
    }
    public String getHspace()
    {
        return getAttribute( "height" );
    }
    public void setHspace( String height )
    {
        setAttribute( "height", height );
    }
    public String getName()
    {
        return getAttribute( "name" );
    }
    public void setName( String name )
    {
        setAttribute( "name", name );
    }
    public String getObject()
    {
        return getAttribute( "object" );
    }
    public void setObject( String object )
    {
        setAttribute( "object", object );
    }
    public String getVspace()
    {
        return getAttribute( "vspace" );
    }
    public void setVspace( String vspace )
    {
        setAttribute( "vspace", vspace );
    }
    public String getWidth()
    {
        return getAttribute( "width" );
    }
    public void setWidth( String width )
    {
        setAttribute( "width", width );
    }
    public HTMLAppletElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
