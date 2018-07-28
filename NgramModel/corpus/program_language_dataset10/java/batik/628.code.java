package org.apache.batik.dom.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.stylesheets.LinkStyle;
import org.w3c.dom.svg.SVGStyleElement;
public class SVGOMStyleElement
    extends    SVGOMElement
    implements CSSStyleSheetNode,
               SVGStyleElement,
               LinkStyle {
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(1);
        attributeInitializer.addAttribute(XMLSupport.XML_NAMESPACE_URI,
                                          "xml", "space", "preserve");
    }
    protected transient org.w3c.dom.stylesheets.StyleSheet sheet;
    protected transient StyleSheet styleSheet;
    protected transient EventListener domCharacterDataModifiedListener =
        new DOMCharacterDataModifiedListener();
    protected SVGOMStyleElement() {
    }
    public SVGOMStyleElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_STYLE_TAG;
    }
    public StyleSheet getCSSStyleSheet() {
        if (styleSheet == null) {
            if (getType().equals("text/css")) {
                SVGOMDocument doc = (SVGOMDocument)getOwnerDocument();
                CSSEngine e = doc.getCSSEngine();
                String text = "";
                Node n = getFirstChild();
                if (n != null) {
                    StringBuffer sb = new StringBuffer();
                    while (n != null) {
                        if (n.getNodeType() == Node.CDATA_SECTION_NODE
                            || n.getNodeType() == Node.TEXT_NODE)
                            sb.append(n.getNodeValue());
                        n = n.getNextSibling();
                    }
                    text = sb.toString();
                }
                ParsedURL burl = null;
                String bu = getBaseURI();
                if (bu != null) {
                    burl = new ParsedURL(bu);
                }
                String media = getAttributeNS(null, SVG_MEDIA_ATTRIBUTE);
                styleSheet = e.parseStyleSheet(text, burl, media);
                addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                   "DOMCharacterDataModified",
                                   domCharacterDataModifiedListener,
                                   false,
                                   null);
            }
        }
        return styleSheet;
    }
    public org.w3c.dom.stylesheets.StyleSheet getSheet() {
        throw new UnsupportedOperationException
            ("LinkStyle.getSheet() is not implemented"); 
    }
    public String getXMLspace() {
        return XMLSupport.getXMLSpace(this);
    }
    public void setXMLspace(String space) throws DOMException {
        setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, space);
    }
    public String getType() {
        return getAttributeNS(null, SVG_TYPE_ATTRIBUTE);
    }
    public void setType(String type) throws DOMException {
        setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
    }
    public String getMedia() {
        return getAttribute(SVG_MEDIA_ATTRIBUTE);
    }
    public void setMedia(String media) throws DOMException {
        setAttribute(SVG_MEDIA_ATTRIBUTE, media);
    }
    public String getTitle() {
        return getAttribute(SVG_TITLE_ATTRIBUTE);
    }
    public void setTitle(String title) throws DOMException {
        setAttribute(SVG_TITLE_ATTRIBUTE, title);
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMStyleElement();
    }
    protected class DOMCharacterDataModifiedListener
        implements EventListener {
        public void handleEvent(Event evt) {
            styleSheet = null;
        }
    }
}
