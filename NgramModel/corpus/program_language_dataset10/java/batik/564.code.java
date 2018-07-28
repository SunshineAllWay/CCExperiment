package org.apache.batik.dom.svg;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.css.engine.CSSNavigableDocument;
import org.apache.batik.css.engine.CSSNavigableDocumentListener;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.GenericAttr;
import org.apache.batik.dom.GenericAttrNS;
import org.apache.batik.dom.GenericCDATASection;
import org.apache.batik.dom.GenericComment;
import org.apache.batik.dom.GenericDocumentFragment;
import org.apache.batik.dom.GenericElement;
import org.apache.batik.dom.GenericEntityReference;
import org.apache.batik.dom.GenericProcessingInstruction;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.StyleSheetFactory;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.i18n.Localizable;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLangSpace;
import org.w3c.dom.svg.SVGSVGElement;
public class SVGOMDocument
    extends    AbstractStylableDocument
    implements SVGDocument,
               SVGConstants,
               CSSNavigableDocument,
               IdContainer {
    protected static final String RESOURCES =
        "org.apache.batik.dom.svg.resources.Messages";
    protected transient LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES, getClass().getClassLoader());
    protected String referrer = "";
    protected ParsedURL url;
    protected transient boolean readonly;
    protected boolean isSVG12;
    protected HashMap cssNavigableDocumentListeners = new HashMap();
    protected AnimatedAttributeListener mainAnimatedAttributeListener =
        new AnimAttrListener();
    protected LinkedList animatedAttributeListeners = new LinkedList();
    protected transient SVGContext svgContext;
    protected SVGOMDocument() {
    }
    public SVGOMDocument(DocumentType dt, DOMImplementation impl) {
        super(dt, impl);
    }
    public void setLocale(Locale l) {
        super.setLocale(l);
        localizableSupport.setLocale(l);
    }
    public String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        try {
            return super.formatMessage(key, args);
        } catch (MissingResourceException e) {
            return localizableSupport.formatMessage(key, args);
        }
    }
    public String getTitle() {
        StringBuffer sb = new StringBuffer();
        boolean preserve = false;
        for (Node n = getDocumentElement().getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            String ns = n.getNamespaceURI();
            if (ns != null && ns.equals(SVG_NAMESPACE_URI)) {
                if (n.getLocalName().equals(SVG_TITLE_TAG)) {
                    preserve = ((SVGLangSpace)n).getXMLspace().equals("preserve");
                    for (n = n.getFirstChild();
                         n != null;
                         n = n.getNextSibling()) {
                        if (n.getNodeType() == Node.TEXT_NODE) {
                            sb.append(n.getNodeValue());
                        }
                    }
                    break;
                }
            }
        }
        String s = sb.toString();
        return (preserve)
            ? XMLSupport.preserveXMLSpace(s)
            : XMLSupport.defaultXMLSpace(s);
    }
    public String getReferrer() {
        return referrer;
    }
    public void setReferrer(String s) {
        referrer = s;
    }
    public String getDomain() {
        return (url == null) ? null : url.getHost();
    }
    public SVGSVGElement getRootElement() {
        return (SVGSVGElement)getDocumentElement();
    }
    public String getURL() {
        return documentURI;
    }
    public URL getURLObject() {
        try {
            return new URL(documentURI);
        } catch (MalformedURLException e) {
            return null;
        }
    }
    public ParsedURL getParsedURL() {
        return url;
    }
    public void setURLObject(URL url) {
        setParsedURL(new ParsedURL(url));
    }
    public void setParsedURL(ParsedURL url) {
        this.url = url;
        documentURI = url == null ? null : url.toString();
    }
    public void setDocumentURI(String uri) {
        documentURI = uri;
        url = uri == null ? null : new ParsedURL(uri);
    }
    public Element createElement(String tagName) throws DOMException {
        return new GenericElement(tagName.intern(), this);
    }
    public DocumentFragment createDocumentFragment() {
        return new GenericDocumentFragment(this);
    }
    public Text createTextNode(String data) {
        return new GenericText(data, this);
    }
    public Comment createComment(String data) {
        return new GenericComment(data, this);
    }
    public CDATASection createCDATASection(String data) throws DOMException {
        return new GenericCDATASection(data, this);
    }
    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data)
        throws DOMException {
        if ("xml-stylesheet".equals(target)) {
            return new SVGStyleSheetProcessingInstruction
                (data, this, (StyleSheetFactory)getImplementation());
        }
        return new GenericProcessingInstruction(target, data, this);
    }
    public Attr createAttribute(String name) throws DOMException {
        return new GenericAttr(name.intern(), this);
    }
    public EntityReference createEntityReference(String name)
        throws DOMException {
        return new GenericEntityReference(name, this);
    }
    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
        throws DOMException {
        if (namespaceURI == null) {
            return new GenericAttr(qualifiedName.intern(), this);
        } else {
            return new GenericAttrNS(namespaceURI.intern(),
                                     qualifiedName.intern(),
                                     this);
        }
    }
    public Element createElementNS(String namespaceURI, String qualifiedName)
        throws DOMException {
        SVGDOMImplementation impl = (SVGDOMImplementation)implementation;
        return impl.createElementNS(this, namespaceURI, qualifiedName);
    }
    public boolean isSVG12() {
        return isSVG12;
    }
    public void setIsSVG12(boolean b) {
        isSVG12 = b;
    }
    public boolean isId(Attr node) {
        if (node.getNamespaceURI() == null) {
            return SVG_ID_ATTRIBUTE.equals(node.getNodeName());
        }
        return node.getNodeName().equals(XML_ID_QNAME);
    }
    public void setSVGContext(SVGContext ctx) {
        svgContext = ctx;
    }
    public SVGContext getSVGContext() {
        return svgContext;
    }
    public void addCSSNavigableDocumentListener
            (CSSNavigableDocumentListener l) {
        if (cssNavigableDocumentListeners.containsKey(l)) {
            return;
        }
        DOMNodeInsertedListenerWrapper nodeInserted
            = new DOMNodeInsertedListenerWrapper(l);
        DOMNodeRemovedListenerWrapper nodeRemoved
            = new DOMNodeRemovedListenerWrapper(l);
        DOMSubtreeModifiedListenerWrapper subtreeModified
            = new DOMSubtreeModifiedListenerWrapper(l);
        DOMCharacterDataModifiedListenerWrapper cdataModified
            = new DOMCharacterDataModifiedListenerWrapper(l);
        DOMAttrModifiedListenerWrapper attrModified
            = new DOMAttrModifiedListenerWrapper(l);
        cssNavigableDocumentListeners.put
            (l, new EventListener[] { nodeInserted,
                                      nodeRemoved,
                                      subtreeModified,
                                      cdataModified,
                                      attrModified });
        addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                           "DOMNodeInserted", nodeInserted, false, null);
        addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                           "DOMNodeRemoved", nodeRemoved, false, null);
        addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                           "DOMSubtreeModified", subtreeModified, false, null);
        addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                           "DOMCharacterDataModified", cdataModified, false,
                           null);
        addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                           "DOMAttrModified", attrModified, false, null);
    }
    public void removeCSSNavigableDocumentListener
            (CSSNavigableDocumentListener l) {
        EventListener[] listeners
            = (EventListener[]) cssNavigableDocumentListeners.get(l);
        if (listeners == null) {
            return;
        }
        removeEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                              "DOMNodeInserted", listeners[0], false);
        removeEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                              "DOMNodeRemoved", listeners[1], false);
        removeEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                              "DOMSubtreeModified", listeners[2], false);
        removeEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                              "DOMCharacterDataModified", listeners[3], false);
        removeEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                              "DOMAttrModified", listeners[4], false);
        cssNavigableDocumentListeners.remove(l);
    }
    protected AnimatedAttributeListener getAnimatedAttributeListener() {
        return mainAnimatedAttributeListener;
    }
    protected void overrideStyleTextChanged(CSSStylableElement e, String text) {
        Iterator i = cssNavigableDocumentListeners.keySet().iterator();
        while (i.hasNext()) {
            CSSNavigableDocumentListener l =
                (CSSNavigableDocumentListener) i.next();
            l.overrideStyleTextChanged(e, text);
        }
    }
    protected void overrideStylePropertyRemoved(CSSStylableElement e,
                                                String name) {
        Iterator i = cssNavigableDocumentListeners.keySet().iterator();
        while (i.hasNext()) {
            CSSNavigableDocumentListener l =
                (CSSNavigableDocumentListener) i.next();
            l.overrideStylePropertyRemoved(e, name);
        }
    }
    protected void overrideStylePropertyChanged
            (CSSStylableElement e, String name, String value, String prio) {
        Iterator i = cssNavigableDocumentListeners.keySet().iterator();
        while (i.hasNext()) {
            CSSNavigableDocumentListener l =
                (CSSNavigableDocumentListener) i.next();
            l.overrideStylePropertyChanged(e, name, value, prio);
        }
    }
    public void addAnimatedAttributeListener
            (AnimatedAttributeListener aal) {
        if (animatedAttributeListeners.contains(aal)) {
            return;
        }
        animatedAttributeListeners.add(aal);
    }
    public void removeAnimatedAttributeListener
            (AnimatedAttributeListener aal) {
        animatedAttributeListeners.remove(aal);
    }
    protected class DOMNodeInsertedListenerWrapper implements EventListener {
        protected CSSNavigableDocumentListener listener;
        public DOMNodeInsertedListenerWrapper(CSSNavigableDocumentListener l) {
            listener = l;
        }
        public void handleEvent(Event evt) {
            evt = EventSupport.getUltimateOriginalEvent(evt);
            listener.nodeInserted((Node) evt.getTarget());
        }
    }
    protected class DOMNodeRemovedListenerWrapper implements EventListener {
        protected CSSNavigableDocumentListener listener;
        public DOMNodeRemovedListenerWrapper(CSSNavigableDocumentListener l) {
            listener = l;
        }
        public void handleEvent(Event evt) {
            evt = EventSupport.getUltimateOriginalEvent(evt);
            listener.nodeToBeRemoved((Node) evt.getTarget());
        }
    }
    protected class DOMSubtreeModifiedListenerWrapper implements EventListener {
        protected CSSNavigableDocumentListener listener;
        public DOMSubtreeModifiedListenerWrapper
                (CSSNavigableDocumentListener l) {
            listener = l;
        }
        public void handleEvent(Event evt) {
            evt = EventSupport.getUltimateOriginalEvent(evt);
            listener.subtreeModified((Node) evt.getTarget());
        }
    }
    protected class DOMCharacterDataModifiedListenerWrapper
            implements EventListener {
        protected CSSNavigableDocumentListener listener;
        public DOMCharacterDataModifiedListenerWrapper
                (CSSNavigableDocumentListener l) {
            listener = l;
        }
        public void handleEvent(Event evt) {
            evt = EventSupport.getUltimateOriginalEvent(evt);
            listener.characterDataModified((Node) evt.getTarget());
        }
    }
    protected class DOMAttrModifiedListenerWrapper implements EventListener {
        protected CSSNavigableDocumentListener listener;
        public DOMAttrModifiedListenerWrapper(CSSNavigableDocumentListener l) {
            listener = l;
        }
        public void handleEvent(Event evt) {
            evt = EventSupport.getUltimateOriginalEvent(evt);
            MutationEvent mevt = (MutationEvent) evt;
            listener.attrModified((Element) evt.getTarget(),
                                  (Attr) mevt.getRelatedNode(),
                                  mevt.getAttrChange(),
                                  mevt.getPrevValue(),
                                  mevt.getNewValue());
        }
    }
    protected class AnimAttrListener implements AnimatedAttributeListener {
        public void animatedAttributeChanged(Element e,
                                             AnimatedLiveAttributeValue alav) {
            Iterator i = animatedAttributeListeners.iterator();
            while (i.hasNext()) {
                AnimatedAttributeListener aal =
                    (AnimatedAttributeListener) i.next();
                aal.animatedAttributeChanged(e, alav);
            }
        }
        public void otherAnimationChanged(Element e, String type) {
            Iterator i = animatedAttributeListeners.iterator();
            while (i.hasNext()) {
                AnimatedAttributeListener aal =
                    (AnimatedAttributeListener) i.next();
                aal.otherAnimationChanged(e, type);
            }
        }
    }
    public CSSStyleDeclaration getOverrideStyle(Element elt,
                                                String pseudoElt) {
        if (elt instanceof SVGStylableElement && pseudoElt == null) {
            return ((SVGStylableElement) elt).getOverrideStyle();
        }
        return null;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected Node newNode() {
        return new SVGOMDocument();
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        SVGOMDocument sd = (SVGOMDocument)n;
        sd.localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
        sd.referrer = referrer;
        sd.url = url;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        SVGOMDocument sd = (SVGOMDocument)n;
        sd.localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
        sd.referrer = referrer;
        sd.url = url;
        return n;
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
    }
}
