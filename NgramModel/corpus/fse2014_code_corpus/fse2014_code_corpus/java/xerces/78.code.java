package org.apache.html.dom;
import org.w3c.dom.html.HTMLImageElement;
public class HTMLImageElementImpl
    extends HTMLElementImpl
    implements HTMLImageElement
{
    private static final long serialVersionUID = 1424360710977241315L;
    public String getLowSrc()
    {
        return getAttribute( "lowsrc" );
    }
    public void setLowSrc( String lowSrc )
    {
        setAttribute( "lowsrc", lowSrc );
    }
       public String getSrc()
    {
        return getAttribute( "src" );
    }
    public void setSrc( String src )
    {
        setAttribute( "src", src );
    }
      public String getName()
    {
        return getAttribute( "name" );
    }
    public void setName( String name )
    {
        setAttribute( "name", name );
    }
    public String getAlign()
    {
        return capitalize( getAttribute( "align" ) );
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
    public String getBorder()
    {
        return getAttribute( "border" );
    }
    public void setBorder( String border )
    {
        setAttribute( "border", border );
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
        return getAttribute( "hspace" );
    }
    public void setHspace( String hspace )
    {
        setAttribute( "hspace", hspace );
    }
    public boolean getIsMap()
    {
        return getBinary( "ismap" );
    }
    public void setIsMap( boolean isMap )
    {
        setAttribute( "ismap", isMap );
    }
    public String getLongDesc()
    {
        return getAttribute( "longdesc" );
    }
    public void setLongDesc( String longDesc )
    {
        setAttribute( "longdesc", longDesc );
    }
    public String getUseMap()
    {
        return getAttribute( "useMap" );
    }
    public void setUseMap( String useMap )
    {
        setAttribute( "useMap", useMap );
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
    public HTMLImageElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
