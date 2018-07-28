package org.apache.html.dom;
import java.util.Locale;
import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFormElement;
public class HTMLElementImpl
    extends ElementImpl
    implements HTMLElement
{
    private static final long serialVersionUID = 5283925246324423495L;
    public HTMLElementImpl( HTMLDocumentImpl owner, String tagName ) {
        super( owner, tagName.toUpperCase(Locale.ENGLISH) );
    }
    public String getId() {
        return getAttribute( "id" );
    }
    public void setId( String id ) {
        setAttribute( "id", id );
    }   
    public String getTitle() {
        return getAttribute( "title" );
    }
    public void setTitle( String title ) {
        setAttribute( "title", title );
    }
    public String getLang() {
        return getAttribute( "lang" );
    }  
    public void setLang( String lang ) {
        setAttribute( "lang", lang );
    }
    public String getDir() {
        return getAttribute( "dir" );
    }
    public void setDir( String dir ) {
        setAttribute( "dir", dir );
    }
    public String getClassName() {
        return getAttribute( "class" );
    }
    public void setClassName( String className ) {
        setAttribute( "class", className );
    }
    int getInteger( String value ) {
        try {
            return Integer.parseInt( value );
        }
        catch ( NumberFormatException except ) {
            return 0;
        }
    }
    boolean getBinary( String name ) {
        return ( getAttributeNode( name ) != null );
    }
    void setAttribute( String name, boolean value ) {
        if ( value ) {
            setAttribute( name, name );
        }
        else {
            removeAttribute( name );
        }
    }
    public Attr getAttributeNode( String attrName ) {
        return super.getAttributeNode( attrName.toLowerCase(Locale.ENGLISH) );
    }
    public Attr getAttributeNodeNS( String namespaceURI,
            String localName ) {
        if ( namespaceURI != null && namespaceURI.length() > 0 ) {
            return super.getAttributeNodeNS( namespaceURI, localName );
        }
        return super.getAttributeNode( localName.toLowerCase(Locale.ENGLISH) );
    }
    public String getAttribute( String attrName ) {
        return super.getAttribute( attrName.toLowerCase(Locale.ENGLISH) );
    }
    public String getAttributeNS( String namespaceURI,
            String localName ) {
        if ( namespaceURI != null && namespaceURI.length() > 0 ) {
            return super.getAttributeNS( namespaceURI, localName );
        }
        return super.getAttribute( localName.toLowerCase(Locale.ENGLISH) );
    }
    public final NodeList getElementsByTagName( String tagName ) {
        return super.getElementsByTagName( tagName.toUpperCase(Locale.ENGLISH) );
    }
    public final NodeList getElementsByTagNameNS( String namespaceURI,
            String localName ) {
        if ( namespaceURI != null && namespaceURI.length() > 0 ) {
            return super.getElementsByTagNameNS( namespaceURI, localName.toUpperCase(Locale.ENGLISH) );
        }
        return super.getElementsByTagName( localName.toUpperCase(Locale.ENGLISH) );
    }
    String capitalize( String value ) {
        char[]    chars;
        int        i;
        chars = value.toCharArray();
        if ( chars.length > 0 ) {
            chars[ 0 ] = Character.toUpperCase( chars[ 0 ] );
            for ( i = 1 ; i < chars.length ; ++i ) {
                chars[ i ] = Character.toLowerCase( chars[ i ] );
            }
            return String.valueOf( chars );
        }
        return value;
    }
    String getCapitalized( String name ) {
        String    value;
        char[]    chars;
        int        i;
        value = getAttribute( name );
        if ( value != null ) {
            chars = value.toCharArray();
            if ( chars.length > 0 ) {
                chars[ 0 ] = Character.toUpperCase( chars[ 0 ] );
                for ( i = 1 ; i < chars.length ; ++i ) {
                    chars[ i ] = Character.toLowerCase( chars[ i ] );
                }
                return String.valueOf( chars );
            }
        }
        return value;
    }
    public HTMLFormElement getForm() {
        Node parent = getParentNode(); 
        while ( parent != null ) {
            if ( parent instanceof HTMLFormElement ) {
                return (HTMLFormElement) parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }
}
