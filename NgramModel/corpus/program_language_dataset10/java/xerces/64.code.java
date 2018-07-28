package org.apache.html.dom;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Locale;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFrameSetElement;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLHtmlElement;
import org.w3c.dom.html.HTMLTitleElement;
public class HTMLDocumentImpl
    extends DocumentImpl
    implements HTMLDocument
{
    private static final long serialVersionUID = 4285791750126227180L;
    private HTMLCollectionImpl    _anchors;
    private HTMLCollectionImpl    _forms;
    private HTMLCollectionImpl    _images;
    private HTMLCollectionImpl    _links;
    private HTMLCollectionImpl    _applets;
    private StringWriter        _writer;
    private static Hashtable        _elementTypesHTML;
    private static final Class[]    _elemClassSigHTML =
                new Class[] { HTMLDocumentImpl.class, String.class };
    public HTMLDocumentImpl()
    {
        super();
        populateElementTypes();
    }
    public synchronized Element getDocumentElement()
    {
        Node    html;
        Node    child;
        Node    next;
        html = getFirstChild();
        while ( html != null )
        {
            if ( html instanceof HTMLHtmlElement )
            {
                return (HTMLElement) html;
            }
            html = html.getNextSibling();
        }
        html = new HTMLHtmlElementImpl( this, "HTML" );
        child = getFirstChild();
        while ( child != null )
        {
            next = child.getNextSibling();
            html.appendChild( child );
            child = next;
        }
        appendChild( html );
        return (HTMLElement) html;
    }
    public synchronized HTMLElement getHead()
    {
        Node    head;
        Node    html;
        Node    child;
        Node    next;
        html = getDocumentElement();
        synchronized ( html )
        {
            head = html.getFirstChild();
            while ( head != null && ! ( head instanceof HTMLHeadElement ) )
                head = head.getNextSibling();
            if ( head != null )
            {
                synchronized ( head )
                {
                    child = html.getFirstChild();
                    while ( child != null && child != head )
                    {
                        next = child.getNextSibling();
                        head.insertBefore( child, head.getFirstChild() );
                        child = next;
                    }
                }
                return (HTMLElement) head;
            }
            head = new HTMLHeadElementImpl( this, "HEAD" );
            html.insertBefore( head, html.getFirstChild() );
        }
        return (HTMLElement) head;
    }
    public synchronized String getTitle()
    {
        HTMLElement head;
        NodeList    list;
        Node        title;
        head = getHead();
        list = head.getElementsByTagName( "TITLE" );
        if ( list.getLength() > 0 ) {
            title = list.item( 0 );
            return ( (HTMLTitleElement) title ).getText();
        }
        return "";
    }
    public synchronized void setTitle( String newTitle )
    {
        HTMLElement head;
        NodeList    list;
        Node        title;
        head = getHead();
        list = head.getElementsByTagName( "TITLE" );
        if ( list.getLength() > 0 ) {
            title = list.item( 0 );
            if ( title.getParentNode() != head )
                head.appendChild( title );
            ( (HTMLTitleElement) title ).setText( newTitle );
        }
        else
        {
            title = new HTMLTitleElementImpl( this, "TITLE" );
            ( (HTMLTitleElement) title ).setText( newTitle );
            head.appendChild( title );
        }
    }
    public synchronized HTMLElement getBody()
    {
        Node    html;
        Node    head;
        Node    body;
        Node    child;
        Node    next;
        html = getDocumentElement();
        head = getHead();
        synchronized ( html )
        {
            body = head.getNextSibling();
            while ( body != null && ! ( body instanceof HTMLBodyElement )
                    && ! ( body instanceof HTMLFrameSetElement ) )
                body = body.getNextSibling();
            if ( body != null )
            {
                synchronized ( body )
                {
                    child = head.getNextSibling();
                    while ( child != null && child != body )
                    {
                        next = child.getNextSibling();
                        body.insertBefore( child, body.getFirstChild() );
                        child = next;
                    }
                }
                return (HTMLElement) body;
            }
            body = new HTMLBodyElementImpl( this, "BODY" );
            html.appendChild( body );
        }
        return (HTMLElement) body;
    }
    public synchronized void setBody( HTMLElement newBody )
    {
        Node    html;
        Node    body;
        Node    head;
        Node    child;
        NodeList list;
        synchronized ( newBody )
        {
            html = getDocumentElement();
            head = getHead();
            synchronized ( html )
            {
                list = this.getElementsByTagName( "BODY" );
                if ( list.getLength() > 0 ) {
                    body = list.item( 0 );
                    synchronized ( body )
                    {
                        child = head;
                        while ( child != null )
                        {
                            if ( child instanceof Element )
                            {
                                if ( child != body )
                                    html.insertBefore( newBody, child );
                                else
                                    html.replaceChild( newBody, body );
                                return;
                            }
                            child = child.getNextSibling();
                        }
                        html.appendChild( newBody );
                    }
                    return;
                }
                html.appendChild( newBody );
            }
        }
    }
    public synchronized Element getElementById( String elementId )
    {
        Element idElement = super.getElementById(elementId);
        if (idElement != null) {
            return idElement;
        }
        return getElementById( elementId, this );
    }
    public NodeList getElementsByName( String elementName )
    {
        return new NameNodeListImpl( this, elementName );
    }
    public final NodeList getElementsByTagName( String tagName )
    {
        return super.getElementsByTagName( tagName.toUpperCase(Locale.ENGLISH) );
    }
    public final NodeList getElementsByTagNameNS( String namespaceURI,
                                                  String localName )
    {
        if ( namespaceURI != null && namespaceURI.length() > 0 ) {
            return super.getElementsByTagNameNS( namespaceURI, localName.toUpperCase(Locale.ENGLISH) );
        }
        return super.getElementsByTagName( localName.toUpperCase(Locale.ENGLISH) );
    }
    public Element createElementNS(String namespaceURI, String qualifiedName,
                                   String localpart)
        throws DOMException
    {
        return createElementNS(namespaceURI, qualifiedName);
    }
    public Element createElementNS( String namespaceURI, String qualifiedName )
    {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            return createElement( qualifiedName );
        }
        return super.createElementNS( namespaceURI, qualifiedName );
    }
    public Element createElement( String tagName )
        throws DOMException
    {
        Class        elemClass;
        Constructor    cnst;
        tagName = tagName.toUpperCase(Locale.ENGLISH);
        elemClass = (Class) _elementTypesHTML.get( tagName );
        if ( elemClass != null )
        {
            try
            {
                cnst = elemClass.getConstructor( _elemClassSigHTML );
                return (Element) cnst.newInstance( new Object[] { this, tagName } );
            }
            catch ( Exception except )
            {
                throw new IllegalStateException( "HTM15 Tag '" + tagName + "' associated with an Element class that failed to construct.\n" + tagName);
            }
        }
        return new HTMLElementImpl( this, tagName );
    }
    public Attr createAttribute( String name )
        throws DOMException
    {
        return super.createAttribute( name.toLowerCase(Locale.ENGLISH) );
    }
    public String getReferrer()
    {
        return null;
    }
    public String getDomain()
    {
        return null;
    }
    public String getURL()
    {
        return null;
    }
    public String getCookie()
    {
        return null;
    }
    public void setCookie( String cookie )
    {
    }
    public HTMLCollection getImages()
    {
        if ( _images == null )
            _images = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.IMAGE );
        return _images;
    }
    public HTMLCollection getApplets()
    {
        if ( _applets == null )
            _applets = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.APPLET );
        return _applets;
    }
    public HTMLCollection getLinks()
    {
        if ( _links == null )
            _links = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.LINK );
        return _links;
    }
    public HTMLCollection getForms()
    {
        if ( _forms == null )
            _forms = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.FORM );
        return _forms;
    }
    public HTMLCollection getAnchors()
    {
        if ( _anchors == null )
            _anchors = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.ANCHOR );
        return _anchors;
    }
    public void open()
    {
        if ( _writer == null )
            _writer = new StringWriter();
    }
    public void close()
    {
        if ( _writer != null )
        {
            _writer = null;
        }
    }
    public void write( String text )
    {
        if ( _writer != null )
            _writer.write( text );
    }
    public void writeln( String text )
    {
        if ( _writer != null )
            _writer.write( text + "\n" );
    }
    public Node cloneNode( boolean deep )
    {
        HTMLDocumentImpl newdoc = new HTMLDocumentImpl();
        callUserDataHandlers(this, newdoc, UserDataHandler.NODE_CLONED);
        cloneNode(newdoc, deep);
        return newdoc;
    }
    protected boolean canRenameElements(String newNamespaceURI, String newNodeName, ElementImpl el) {
        if (el.getNamespaceURI() != null) {
            return newNamespaceURI != null;
        }
        Class newClass = (Class) _elementTypesHTML.get(newNodeName.toUpperCase(Locale.ENGLISH));
        Class oldClass = (Class) _elementTypesHTML.get(el.getTagName());
        return newClass == oldClass;
    }
    private Element getElementById( String elementId, Node node )
    {
        Node    child;
        Element    result;
        child = node.getFirstChild();
        while ( child != null )
        {
            if ( child instanceof Element )
            {
                if ( elementId.equals( ( (Element) child ).getAttribute( "id" ) ) )
                    return (Element) child;
                result = getElementById( elementId, child );
                if ( result != null )
                    return result;
            }
            child = child.getNextSibling();
        }
        return null;
    }
    private synchronized static void populateElementTypes()
    {
        if ( _elementTypesHTML != null )
            return;
        _elementTypesHTML = new Hashtable( 63 );
        populateElementType( "A", "HTMLAnchorElementImpl" );
        populateElementType( "APPLET", "HTMLAppletElementImpl" );
        populateElementType( "AREA", "HTMLAreaElementImpl" );
        populateElementType( "BASE",  "HTMLBaseElementImpl" );
        populateElementType( "BASEFONT", "HTMLBaseFontElementImpl" );
        populateElementType( "BLOCKQUOTE", "HTMLQuoteElementImpl" );
        populateElementType( "BODY", "HTMLBodyElementImpl" );
        populateElementType( "BR", "HTMLBRElementImpl" );
        populateElementType( "BUTTON", "HTMLButtonElementImpl" );
        populateElementType( "DEL", "HTMLModElementImpl" );
        populateElementType( "DIR", "HTMLDirectoryElementImpl" );
        populateElementType( "DIV",  "HTMLDivElementImpl" );
        populateElementType( "DL", "HTMLDListElementImpl" );
        populateElementType( "FIELDSET", "HTMLFieldSetElementImpl" );
        populateElementType( "FONT", "HTMLFontElementImpl" );
        populateElementType( "FORM", "HTMLFormElementImpl" );
        populateElementType( "FRAME","HTMLFrameElementImpl" );
        populateElementType( "FRAMESET", "HTMLFrameSetElementImpl" );
        populateElementType( "HEAD", "HTMLHeadElementImpl" );
        populateElementType( "H1", "HTMLHeadingElementImpl" );
        populateElementType( "H2", "HTMLHeadingElementImpl" );
        populateElementType( "H3", "HTMLHeadingElementImpl" );
        populateElementType( "H4", "HTMLHeadingElementImpl" );
        populateElementType( "H5", "HTMLHeadingElementImpl" );
        populateElementType( "H6", "HTMLHeadingElementImpl" );
        populateElementType( "HR", "HTMLHRElementImpl" );
        populateElementType( "HTML", "HTMLHtmlElementImpl" );
        populateElementType( "IFRAME", "HTMLIFrameElementImpl" );
        populateElementType( "IMG", "HTMLImageElementImpl" );
        populateElementType( "INPUT", "HTMLInputElementImpl" );
        populateElementType( "INS", "HTMLModElementImpl" );
        populateElementType( "ISINDEX", "HTMLIsIndexElementImpl" );
        populateElementType( "LABEL", "HTMLLabelElementImpl" );
        populateElementType( "LEGEND", "HTMLLegendElementImpl" );
        populateElementType( "LI", "HTMLLIElementImpl" );
        populateElementType( "LINK", "HTMLLinkElementImpl" );
        populateElementType( "MAP", "HTMLMapElementImpl" );
        populateElementType( "MENU", "HTMLMenuElementImpl" );
        populateElementType( "META", "HTMLMetaElementImpl" );
        populateElementType( "OBJECT", "HTMLObjectElementImpl" );
        populateElementType( "OL", "HTMLOListElementImpl" );
        populateElementType( "OPTGROUP", "HTMLOptGroupElementImpl" );
        populateElementType( "OPTION", "HTMLOptionElementImpl" );
        populateElementType( "P", "HTMLParagraphElementImpl" );
        populateElementType( "PARAM", "HTMLParamElementImpl" );
        populateElementType( "PRE", "HTMLPreElementImpl" );
        populateElementType( "Q", "HTMLQuoteElementImpl" );
        populateElementType( "SCRIPT", "HTMLScriptElementImpl" );
        populateElementType( "SELECT", "HTMLSelectElementImpl" );
        populateElementType( "STYLE", "HTMLStyleElementImpl" );
        populateElementType( "TABLE", "HTMLTableElementImpl" );
        populateElementType( "CAPTION", "HTMLTableCaptionElementImpl" );
        populateElementType( "TD", "HTMLTableCellElementImpl" );
        populateElementType( "TH", "HTMLTableCellElementImpl" );
        populateElementType( "COL", "HTMLTableColElementImpl" );
        populateElementType( "COLGROUP", "HTMLTableColElementImpl" );
        populateElementType( "TR", "HTMLTableRowElementImpl" );
        populateElementType( "TBODY", "HTMLTableSectionElementImpl" );
        populateElementType( "THEAD", "HTMLTableSectionElementImpl" );
        populateElementType( "TFOOT", "HTMLTableSectionElementImpl" );
        populateElementType( "TEXTAREA", "HTMLTextAreaElementImpl" );
        populateElementType( "TITLE", "HTMLTitleElementImpl" );
        populateElementType( "UL", "HTMLUListElementImpl" );
    }
    private static void populateElementType( String tagName, String className )
    {
        try {
            _elementTypesHTML.put( tagName,
                ObjectFactory.findProviderClass("org.apache.html.dom." + className,
                    HTMLDocumentImpl.class.getClassLoader(), true) );
        } catch ( Exception except ) {
            throw new RuntimeException( "HTM019 OpenXML Error: Could not find or execute class " + className + " implementing HTML element " + tagName
                                  + "\n" + className + "\t" + tagName);
        }
    }
}
