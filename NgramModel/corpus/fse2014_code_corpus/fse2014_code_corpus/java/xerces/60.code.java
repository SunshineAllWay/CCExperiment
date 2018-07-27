package org.apache.html.dom;
import java.io.Serializable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLAppletElement;
import org.w3c.dom.html.HTMLAreaElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLImageElement;
import org.w3c.dom.html.HTMLObjectElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLTableCellElement;
import org.w3c.dom.html.HTMLTableRowElement;
import org.w3c.dom.html.HTMLTableSectionElement;
class HTMLCollectionImpl
    implements HTMLCollection, Serializable
{
    private static final long serialVersionUID = 9112122196669185082L;
    static final short        ANCHOR = 1;
    static final short        FORM = 2;
    static final short        IMAGE = 3;
    static final short        APPLET = 4;
    static final short        LINK = 5;
    static final short        OPTION = 6;
    static final short        ROW = 7;
    static final short        ELEMENT = 8;
    static final short        AREA = -1;
    static final short        TBODY = -2;
    static final short        CELL = -3;
    private short            _lookingFor;
    private Element            _topLevel;
    HTMLCollectionImpl( HTMLElement topLevel, short lookingFor )
    {
        if ( topLevel == null )
            throw new NullPointerException( "HTM011 Argument 'topLevel' is null." );
        _topLevel = topLevel;
       _lookingFor = lookingFor;
    }
    public final int getLength()
    {
        return getLength( _topLevel );
    }
    public final Node item( int index )
    {
        if ( index < 0 )
            throw new IllegalArgumentException( "HTM012 Argument 'index' is negative." );
        return item( _topLevel, new CollectionIndex( index ) );
    }
    public final Node namedItem( String name )
    {
        if ( name == null )
            throw new NullPointerException( "HTM013 Argument 'name' is null." );
        return namedItem( _topLevel, name );
    }
    private int getLength( Element topLevel )
    {
        int        length;
        Node    node;
        synchronized ( topLevel )
        {
            length = 0;
            node = topLevel.getFirstChild();
            while ( node != null )
            {
                if ( node instanceof Element )
                {
                    if ( collectionMatch( (Element) node, null ) )
                        ++ length;
                    else if ( recurse() )
                        length += getLength( (Element) node );
                }
                node = node.getNextSibling(); 
            }
        }
        return length;
    }
    private Node item( Element topLevel, CollectionIndex index )
    {
        Node    node;
        Node    result;
        synchronized ( topLevel )
        {
            node = topLevel.getFirstChild();
            while ( node != null )
            {
                if ( node instanceof Element )
                {
                    if ( collectionMatch( (Element) node, null ) )
                    {
                        if ( index.isZero() )
                            return node;
                        index.decrement();
                    } else if ( recurse() )
                    {
                        result = item( (Element) node, index );
                        if ( result != null )
                            return result;
                    }
                }
                node = node.getNextSibling(); 
            }
        }
        return null;
    }
    private  Node namedItem( Element topLevel, String name )
    {
        Node    node;
        Node    result;
        synchronized ( topLevel )
        {
            node = topLevel.getFirstChild();
            while ( node != null )
            {
                if ( node instanceof Element )
                {
                    if ( collectionMatch( (Element) node, name ) )
                        return node;
                    else if ( recurse() )
                    {
                        result = namedItem( (Element) node, name );
                        if ( result != null )
                            return result;
                    }
                }
                node = node.getNextSibling(); 
            }
            return node;
        }
    }
    protected boolean recurse()
    {
        return _lookingFor > 0;
    }
    protected boolean collectionMatch( Element elem, String name )
    {
        boolean    match;
        synchronized ( elem )
        {
            match = false;
            switch ( _lookingFor )
            {
            case ANCHOR:
                match = ( elem instanceof HTMLAnchorElement ) &&
                        elem.getAttribute( "name" ).length() > 0;
                break;
            case FORM:
                match = ( elem instanceof HTMLFormElement );
                break;
            case IMAGE:
                match = ( elem instanceof HTMLImageElement );
                break;
            case APPLET:
                match = ( elem instanceof HTMLAppletElement ) ||
                        ( elem instanceof HTMLObjectElement &&
                          ( "application/java".equals( elem.getAttribute( "codetype" ) ) ||
                            elem.getAttribute( "classid" ).startsWith( "java:" ) ) );
                break;
            case ELEMENT:
                match = ( elem instanceof HTMLFormControl );
                break;
            case LINK:
                match = ( ( elem instanceof HTMLAnchorElement ||
                            elem instanceof HTMLAreaElement ) &&
                          elem.getAttribute( "href" ).length() > 0 );
                break;
            case AREA:
                match = ( elem instanceof HTMLAreaElement );
                break;
            case OPTION:
                match = ( elem instanceof HTMLOptionElement );
                break;
            case ROW:
                match = ( elem instanceof HTMLTableRowElement );
                break;
            case TBODY:
                match = ( elem instanceof HTMLTableSectionElement &&
                          elem.getTagName().equals( "TBODY" ) );
                break;
            case CELL:
                match = ( elem instanceof HTMLTableCellElement );
                break;
            }
            if ( match && name != null )
            {
                if ( elem instanceof HTMLAnchorElement &&
                     name.equals( elem.getAttribute( "name" ) ) )
                    return true;
                match = name.equals( elem.getAttribute( "id" ) );
            }
        }
        return match;
    }
}
class CollectionIndex
{
    int getIndex()
    {
        return _index;
    }
    void decrement()
    {
        -- _index;
    }
    boolean isZero()
    {
        return _index <= 0;
    }
    CollectionIndex( int index )
    {
        _index = index;
    }
    private int        _index;
}
