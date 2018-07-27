package org.apache.html.dom;
import org.w3c.dom.html.HTMLLabelElement;
public class HTMLLabelElementImpl
    extends HTMLElementImpl
    implements HTMLLabelElement, HTMLFormControl
{
    private static final long serialVersionUID = 5774388295313199380L;
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
       public String getHtmlFor()
    {
        return getAttribute( "for" );
    }
    public void setHtmlFor( String htmlFor )
    {
        setAttribute( "for", htmlFor );
    }
    public HTMLLabelElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
}
