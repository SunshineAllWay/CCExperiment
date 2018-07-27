package org.apache.html.dom;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableCaptionElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTableRowElement;
import org.w3c.dom.html.HTMLTableSectionElement;
public class HTMLTableElementImpl
    extends HTMLElementImpl
    implements HTMLTableElement
{
    private static final long serialVersionUID = -1824053099870917532L;
    public synchronized HTMLTableCaptionElement getCaption()
    {
        Node    child;
        child = getFirstChild();
        while ( child != null )
        {
            if ( child instanceof HTMLTableCaptionElement &&
                 child.getNodeName().equals( "CAPTION" ) )
                return (HTMLTableCaptionElement) child;
            child = child.getNextSibling();
        }
        return null;
    }
    public synchronized void setCaption( HTMLTableCaptionElement caption )
    {
        if ( caption != null && ! caption.getTagName().equals( "CAPTION" ) )
            throw new IllegalArgumentException( "HTM016 Argument 'caption' is not an element of type <CAPTION>." );
        deleteCaption();
        if ( caption != null )
            appendChild( caption );
    }
    public synchronized HTMLElement createCaption()
    {
        HTMLElement    section;
        section = getCaption();
        if ( section != null )
            return section;
        section = new HTMLTableCaptionElementImpl( (HTMLDocumentImpl) getOwnerDocument(), "CAPTION" );
        appendChild( section );
        return section;
    }
    public synchronized void deleteCaption()
    {
        Node    old;
        old = getCaption();
        if ( old != null )
            removeChild ( old );
    }
    public synchronized HTMLTableSectionElement getTHead()
    {
        Node    child;
        child = getFirstChild();
        while ( child != null )
        {
            if ( child instanceof HTMLTableSectionElement &&
                 child.getNodeName().equals( "THEAD" ) )
                return (HTMLTableSectionElement) child;
            child = child.getNextSibling();
        }
        return null;
    }
    public synchronized void setTHead( HTMLTableSectionElement tHead )
    {
        if ( tHead != null && ! tHead.getTagName().equals( "THEAD" ) )
            throw new IllegalArgumentException( "HTM017 Argument 'tHead' is not an element of type <THEAD>." );
        deleteTHead();
        if ( tHead != null )
            appendChild( tHead );
    }
    public synchronized HTMLElement createTHead()
    {
        HTMLElement    section;
        section = getTHead();
        if ( section != null )
            return section;
        section = new HTMLTableSectionElementImpl( (HTMLDocumentImpl) getOwnerDocument(), "THEAD" );
        appendChild( section );
        return section;
    }
    public synchronized void deleteTHead()
    {
        Node    old;
        old = getTHead();
        if ( old != null )
            removeChild ( old );
    }
    public synchronized HTMLTableSectionElement getTFoot()
    {
        Node    child;
        child = getFirstChild();
        while ( child != null )
        {
            if ( child instanceof HTMLTableSectionElement &&
                 child.getNodeName().equals( "TFOOT" ) )
                return (HTMLTableSectionElement) child;
            child = child.getNextSibling();
        }
        return null;
    }
    public synchronized void setTFoot( HTMLTableSectionElement tFoot )
    {
        if ( tFoot != null && ! tFoot.getTagName().equals( "TFOOT" ) )
            throw new IllegalArgumentException( "HTM018 Argument 'tFoot' is not an element of type <TFOOT>." );
        deleteTFoot();
        if ( tFoot != null )
            appendChild( tFoot );
    }
    public synchronized HTMLElement createTFoot()
    {
        HTMLElement    section;
        section = getTFoot();
        if ( section != null )
            return section;
        section = new HTMLTableSectionElementImpl( (HTMLDocumentImpl) getOwnerDocument(), "TFOOT" );
        appendChild( section );
        return section;
    }
    public synchronized void deleteTFoot()
    {
        Node    old;
        old = getTFoot();
        if ( old != null )
            removeChild ( old );
    }
    public HTMLCollection getRows()
    {
        if ( _rows == null )
            _rows = new HTMLCollectionImpl( this, HTMLCollectionImpl.ROW );
        return _rows;
    }
    public HTMLCollection getTBodies()
    {
        if ( _bodies == null )
            _bodies = new HTMLCollectionImpl( this, HTMLCollectionImpl.TBODY );
        return _bodies;
    }
    public String getAlign()
    {
        return capitalize( getAttribute( "align" ) );
    }
    public void setAlign( String align )
    {
        setAttribute( "align", align );
    }
    public String getBgColor()
    {
        return getAttribute( "bgcolor" );
    }
    public void setBgColor( String bgColor )
    {
        setAttribute( "bgcolor", bgColor );
    }
    public String getBorder()
    {
        return getAttribute( "border" );
    }
    public void setBorder( String border )
    {
        setAttribute( "border", border );
    }
    public String getCellPadding()
    {
        return getAttribute( "cellpadding" );
    }
    public void setCellPadding( String cellPadding )
    {
        setAttribute( "cellpadding", cellPadding );
    }
    public String getCellSpacing()
    {
        return getAttribute( "cellspacing" );
    }
    public void setCellSpacing( String cellSpacing )
    {
        setAttribute( "cellspacing", cellSpacing );
    }
    public String getFrame()
    {
        return capitalize( getAttribute( "frame" ) );
    }
    public void setFrame( String frame )
    {
        setAttribute( "frame", frame );
    }
    public String getRules()
    {
        return capitalize( getAttribute( "rules" ) );
    }
    public void setRules( String rules )
    {
        setAttribute( "rules", rules );
    }
    public String getSummary()
    {
        return getAttribute( "summary" );
    }
    public void setSummary( String summary )
    {
        setAttribute( "summary", summary );
    }
      public String getWidth()
    {
        return getAttribute( "width" );
    }
    public void setWidth( String width )
    {
        setAttribute( "width", width );
    }
    public HTMLElement insertRow( int index )
    {
        HTMLTableRowElementImpl    newRow;
        newRow = new HTMLTableRowElementImpl( (HTMLDocumentImpl) getOwnerDocument(), "TR" );
        insertRowX( index, newRow );
        return newRow;
    }
    void insertRowX( int index, HTMLTableRowElementImpl newRow )
    {
        Node    child;
        Node    lastSection = null;
        child = getFirstChild();
        while ( child != null )
        {
            if ( child instanceof HTMLTableRowElement )
            {
                if ( index == 0 )
                {
                    insertBefore( newRow, child );
                    return;
                }
            }
            else
            if ( child instanceof HTMLTableSectionElementImpl )
            {
                lastSection = child;
                index = ( (HTMLTableSectionElementImpl) child ).insertRowX( index, newRow );
                if ( index < 0 )
                    return;
            }
            child = child.getNextSibling();
        }
        if ( lastSection != null )
            lastSection.appendChild( newRow );
        else
            appendChild( newRow );
    }
    public synchronized void deleteRow( int index )
    {
        Node    child;
        child = getFirstChild();
        while ( child != null )
        {
            if ( child instanceof HTMLTableRowElement )
            {
                if ( index == 0 )
                {
                    removeChild ( child );
                    return;
                }
                --index;
            }
            else
            if ( child instanceof HTMLTableSectionElementImpl )
            {
                index = ( (HTMLTableSectionElementImpl) child ).deleteRowX( index );
                if ( index < 0 )
                    return;
            }
            child = child.getNextSibling();
        }
    }
    public Node cloneNode( boolean deep ) {
        HTMLTableElementImpl clonedNode = (HTMLTableElementImpl)super.cloneNode( deep );
        clonedNode._rows = null;
        clonedNode._bodies = null;
        return clonedNode;
    }
    public HTMLTableElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
    private HTMLCollectionImpl    _rows;
    private HTMLCollectionImpl    _bodies;
}
