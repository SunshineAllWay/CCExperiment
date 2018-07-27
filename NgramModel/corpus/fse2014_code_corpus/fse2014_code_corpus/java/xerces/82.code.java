package org.apache.html.dom;
import org.w3c.dom.html.HTMLLegendElement;
public class HTMLLegendElementImpl
    extends HTMLElementImpl
    implements HTMLLegendElement
{
    private static final long serialVersionUID = -621849164029630762L;
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
    public String getAlign()
    {
        return getAttribute( "align" );
    }
    public void setAlign( String align )
    {
        setAttribute( "align", align );
    }
    public HTMLLegendElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
