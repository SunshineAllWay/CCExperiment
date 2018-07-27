package org.apache.batik.bridge;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMAnimatedEnumeration;
import org.apache.batik.dom.svg.SVGOMAnimatedLengthList;
import org.apache.batik.dom.svg.SVGOMAnimatedNumberList;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMTextPositioningElement;
import org.apache.batik.dom.svg.SVGTextContent;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.font.FontFamilyResolver;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.UnresolvedFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.text.TextHit;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.gvt.text.TextSpanLayout;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGLengthList;
import org.w3c.dom.svg.SVGNumberList;
import org.w3c.dom.svg.SVGTextContentElement;
import org.w3c.dom.svg.SVGTextPositioningElement;
public class SVGTextElementBridge extends AbstractGraphicsNodeBridge
    implements SVGTextContent {
    protected static final Integer ZERO = new Integer(0);
    public static final
        AttributedCharacterIterator.Attribute TEXT_COMPOUND_DELIMITER =
        GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_DELIMITER;
    public static final
        AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID =
        GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
    public static final AttributedCharacterIterator.Attribute PAINT_INFO =
         GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
    public static final
        AttributedCharacterIterator.Attribute ALT_GLYPH_HANDLER =
        GVTAttributedCharacterIterator.TextAttribute.ALT_GLYPH_HANDLER;
    public static final
        AttributedCharacterIterator.Attribute TEXTPATH
        = GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
    public static final
        AttributedCharacterIterator.Attribute ANCHOR_TYPE
        = GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
    public static final
        AttributedCharacterIterator.Attribute GVT_FONT_FAMILIES
        = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES;
    public static final
        AttributedCharacterIterator.Attribute GVT_FONTS
        = GVTAttributedCharacterIterator.TextAttribute.GVT_FONTS;
    public static final
        AttributedCharacterIterator.Attribute BASELINE_SHIFT
        = GVTAttributedCharacterIterator.TextAttribute.BASELINE_SHIFT;
    protected AttributedString laidoutText;
    protected WeakHashMap elemTPI = new WeakHashMap();
    protected boolean usingComplexSVGFont = false;
    public SVGTextElementBridge() {}
    public String getLocalName() {
        return SVG_TEXT_TAG;
    }
    public Bridge getInstance() {
        return new SVGTextElementBridge();
    }
    protected TextNode getTextNode() {
        return (TextNode)node;
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        TextNode node = (TextNode)super.createGraphicsNode(ctx, e);
        if (node == null)
            return null;
        associateSVGContext(ctx, e, node);
        Node child = getFirstChild(e);
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addContextToChild(ctx,(Element)child);
            }
            child = getNextSibling(child);
        }
        if (ctx.getTextPainter() != null)
            node.setTextPainter(ctx.getTextPainter());
        RenderingHints hints = null;
        hints = CSSUtilities.convertColorRendering(e, hints);
        hints = CSSUtilities.convertTextRendering (e, hints);
        if (hints != null)
            node.setRenderingHints(hints);
        node.setLocation(getLocation(ctx, e));
        return node;
    }
    protected GraphicsNode instantiateGraphicsNode() {
        return new TextNode();
    }
    protected Point2D getLocation(BridgeContext ctx, Element e) {
        try {
            SVGOMTextPositioningElement te = (SVGOMTextPositioningElement) e;
            SVGOMAnimatedLengthList _x = (SVGOMAnimatedLengthList) te.getX();
            _x.check();
            SVGLengthList xs = _x.getAnimVal();
            float x = 0;
            if (xs.getNumberOfItems() > 0) {
                x = xs.getItem(0).getValue();
            }
            SVGOMAnimatedLengthList _y = (SVGOMAnimatedLengthList) te.getY();
            _y.check();
            SVGLengthList ys = _y.getAnimVal();
            float y = 0;
            if (ys.getNumberOfItems() > 0) {
                y = ys.getItem(0).getValue();
            }
            return new Point2D.Float(x, y);
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    protected boolean isTextElement(Element e) {
        if (!SVG_NAMESPACE_URI.equals(e.getNamespaceURI()))
            return false;
        String nodeName = e.getLocalName();
        return (nodeName.equals(SVG_TEXT_TAG) ||
                nodeName.equals(SVG_TSPAN_TAG) ||
                nodeName.equals(SVG_ALT_GLYPH_TAG) ||
                nodeName.equals(SVG_A_TAG) ||
                nodeName.equals(SVG_TEXT_PATH_TAG) ||
                nodeName.equals(SVG_TREF_TAG));
    }
    protected boolean isTextChild(Element e) {
        if (!SVG_NAMESPACE_URI.equals(e.getNamespaceURI()))
            return false;
        String nodeName = e.getLocalName();
        return (nodeName.equals(SVG_TSPAN_TAG) ||
                nodeName.equals(SVG_ALT_GLYPH_TAG) ||
                nodeName.equals(SVG_A_TAG) ||
                nodeName.equals(SVG_TEXT_PATH_TAG) ||
                nodeName.equals(SVG_TREF_TAG));
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        e.normalize();
        computeLaidoutText(ctx, e, node);
        node.setComposite(CSSUtilities.convertOpacity(e));
        node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
        node.setMask(CSSUtilities.convertMask(e, node, ctx));
        node.setClip(CSSUtilities.convertClipPath(e, node, ctx));
        node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
        initializeDynamicSupport(ctx, e, node);
        if (!ctx.isDynamic()) {
            elemTPI.clear();
        }
    }
    public boolean isComposite() {
        return false;
    }
    protected Node getFirstChild(Node n) {
        return n.getFirstChild();
    }
    protected Node getNextSibling(Node n) {
        return n.getNextSibling();
    }
    protected Node getParentNode(Node n) {
        return n.getParentNode();
    }
    protected DOMChildNodeRemovedEventListener childNodeRemovedEventListener;
    protected class DOMChildNodeRemovedEventListener implements EventListener {
        public void handleEvent(Event evt) {
            handleDOMChildNodeRemovedEvent((MutationEvent)evt);
        }
    }
    protected DOMSubtreeModifiedEventListener subtreeModifiedEventListener;
    protected class DOMSubtreeModifiedEventListener implements EventListener {
        public void handleEvent(Event evt) {
            handleDOMSubtreeModifiedEvent((MutationEvent)evt);
        }
    }
    protected void initializeDynamicSupport(BridgeContext ctx,
                                            Element e,
                                            GraphicsNode node) {
        super.initializeDynamicSupport(ctx, e, node);
        if (ctx.isDynamic()) {
            addTextEventListeners(ctx, (NodeEventTarget) e);
        }
    }
    protected void addTextEventListeners(BridgeContext ctx, NodeEventTarget e) {
        if (childNodeRemovedEventListener == null) {
            childNodeRemovedEventListener =
                new DOMChildNodeRemovedEventListener();
        }
        if (subtreeModifiedEventListener == null) {
            subtreeModifiedEventListener =
                new DOMSubtreeModifiedEventListener();
        }
        e.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             childNodeRemovedEventListener, true, null);
        ctx.storeEventListenerNS
            (e, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             childNodeRemovedEventListener, true);
        e.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             subtreeModifiedEventListener, false, null);
        ctx.storeEventListenerNS
            (e, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             subtreeModifiedEventListener, false);
    }
    protected void removeTextEventListeners(BridgeContext ctx,
                                            NodeEventTarget e) {
        e.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             childNodeRemovedEventListener, true);
        e.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             subtreeModifiedEventListener, false);
    }
    public void dispose() {
        removeTextEventListeners(ctx, (NodeEventTarget) e);
        super.dispose();
    }
    protected void addContextToChild(BridgeContext ctx, Element e) {
        if (SVG_NAMESPACE_URI.equals(e.getNamespaceURI())) {
            if (e.getLocalName().equals(SVG_TSPAN_TAG)) {
                ((SVGOMElement)e).setSVGContext
                    (new TspanBridge(ctx, this, e));
            } else if (e.getLocalName().equals(SVG_TEXT_PATH_TAG)) {
                ((SVGOMElement)e).setSVGContext
                    (new TextPathBridge(ctx, this, e));
            } else if (e.getLocalName().equals(SVG_TREF_TAG)) {
                ((SVGOMElement)e).setSVGContext
                    (new TRefBridge(ctx, this, e));
            }
        }
        Node child = getFirstChild(e);
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addContextToChild(ctx, (Element)child);
            }
            child = getNextSibling(child);
        }
    }
    protected void removeContextFromChild(BridgeContext ctx, Element e) {
        if (SVG_NAMESPACE_URI.equals(e.getNamespaceURI())) {
            if (e.getLocalName().equals(SVG_TSPAN_TAG)) {
                ((AbstractTextChildBridgeUpdateHandler)
                    ((SVGOMElement) e).getSVGContext()).dispose();
            } else if (e.getLocalName().equals(SVG_TEXT_PATH_TAG)) {
                ((AbstractTextChildBridgeUpdateHandler)
                    ((SVGOMElement) e).getSVGContext()).dispose();
            } else if (e.getLocalName().equals(SVG_TREF_TAG)) {
                ((AbstractTextChildBridgeUpdateHandler)
                    ((SVGOMElement) e).getSVGContext()).dispose();
            }
        }
        Node child = getFirstChild(e);
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeContextFromChild(ctx, (Element)child);
            }
            child = getNextSibling(child);
        }
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
        Node childNode = (Node)evt.getTarget();
        switch(childNode.getNodeType()) {
            case Node.TEXT_NODE:        
            case Node.CDATA_SECTION_NODE:
                laidoutText = null;
                break;
            case Node.ELEMENT_NODE: {
                Element childElement = (Element)childNode;
                if (isTextChild(childElement)) {
                    addContextToChild(ctx, childElement);
                    laidoutText = null;
                }
                break;
            }
        }
        if (laidoutText == null) {
            computeLaidoutText(ctx, e, getTextNode());
        }
    }
    public void handleDOMChildNodeRemovedEvent(MutationEvent evt) {
        Node childNode = (Node)evt.getTarget();
        switch (childNode.getNodeType()) {
            case Node.TEXT_NODE:           
            case Node.CDATA_SECTION_NODE:
                if (isParentDisplayed(childNode)) {
                    laidoutText = null;
                }
                break;
            case Node.ELEMENT_NODE: {
                Element childElt = (Element) childNode;
                if (isTextChild(childElt)) {
                    laidoutText = null;
                    removeContextFromChild(ctx, childElt);
                }
                break;
            }
            default:
        }
    }
    public void handleDOMSubtreeModifiedEvent(MutationEvent evt) {
        if (laidoutText == null) {
            computeLaidoutText(ctx, e, getTextNode());
        }
    }
    public void handleDOMCharacterDataModified(MutationEvent evt){
        Node childNode = (Node)evt.getTarget();
        if (isParentDisplayed(childNode)) {
            laidoutText = null;
        }
    }
    protected boolean isParentDisplayed(Node childNode) {
        Node parentNode = getParentNode(childNode);
        return isTextElement((Element)parentNode);
    }
    protected void computeLaidoutText(BridgeContext ctx,
                                      Element e,
                                      GraphicsNode node) {
        TextNode tn = (TextNode)node;
        elemTPI.clear();
        AttributedString as = buildAttributedString(ctx, e);
        if (as == null) {
            tn.setAttributedCharacterIterator(null);
            return;
        }
        addGlyphPositionAttributes(as, e, ctx);
        if (ctx.isDynamic()) {
            laidoutText = new AttributedString(as.getIterator());
        }
        tn.setAttributedCharacterIterator(as.getIterator());
        TextPaintInfo pi = new TextPaintInfo();
        setBaseTextPaintInfo(pi, e, node, ctx);
        setDecorationTextPaintInfo(pi, e);
        addPaintAttributes(as, e, tn, pi, ctx);
        if (usingComplexSVGFont) {
            tn.setAttributedCharacterIterator(as.getIterator());
        }
        if (ctx.isDynamic()) {
            checkBBoxChange();
        }
    }
    private boolean hasNewACI;
    private Element cssProceedElement;
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (ln.equals(SVG_X_ATTRIBUTE)
                    || ln.equals(SVG_Y_ATTRIBUTE)
                    || ln.equals(SVG_DX_ATTRIBUTE)
                    || ln.equals(SVG_DY_ATTRIBUTE)
                    || ln.equals(SVG_ROTATE_ATTRIBUTE)
                    || ln.equals(SVG_TEXT_LENGTH_ATTRIBUTE)
                    || ln.equals(SVG_LENGTH_ADJUST_ATTRIBUTE)) {
                char c = ln.charAt(0);
                if (c == 'x' || c == 'y') {
                    getTextNode().setLocation(getLocation(ctx, e));
                }
                computeLaidoutText(ctx, e, getTextNode());
                return;
            }
        }
        super.handleAnimatedAttributeChanged(alav);
    }
    public void handleCSSEngineEvent(CSSEngineEvent evt) {
        hasNewACI = false;
        int [] properties = evt.getProperties();
        for (int i=0; i < properties.length; ++i) {
            switch(properties[i]) {         
            case SVGCSSEngine.BASELINE_SHIFT_INDEX:
            case SVGCSSEngine.DIRECTION_INDEX:
            case SVGCSSEngine.DISPLAY_INDEX:
            case SVGCSSEngine.FONT_FAMILY_INDEX:
            case SVGCSSEngine.FONT_SIZE_INDEX:
            case SVGCSSEngine.FONT_STRETCH_INDEX:
            case SVGCSSEngine.FONT_STYLE_INDEX:
            case SVGCSSEngine.FONT_WEIGHT_INDEX:
            case SVGCSSEngine.GLYPH_ORIENTATION_HORIZONTAL_INDEX:
            case SVGCSSEngine.GLYPH_ORIENTATION_VERTICAL_INDEX:
            case SVGCSSEngine.KERNING_INDEX:
            case SVGCSSEngine.LETTER_SPACING_INDEX:
            case SVGCSSEngine.TEXT_ANCHOR_INDEX:
            case SVGCSSEngine.UNICODE_BIDI_INDEX:
            case SVGCSSEngine.WORD_SPACING_INDEX:
            case SVGCSSEngine.WRITING_MODE_INDEX: {
                if (!hasNewACI) {
                    hasNewACI = true;
                    computeLaidoutText(ctx, e, getTextNode());
                }
                break;
            }
            }
        }
        cssProceedElement = evt.getElement();
        super.handleCSSEngineEvent(evt);
        cssProceedElement = null;
    }
    protected void handleCSSPropertyChanged(int property) {
        switch(property) {                  
        case SVGCSSEngine.FILL_INDEX:
        case SVGCSSEngine.FILL_OPACITY_INDEX:
        case SVGCSSEngine.STROKE_INDEX:
        case SVGCSSEngine.STROKE_OPACITY_INDEX:
        case SVGCSSEngine.STROKE_WIDTH_INDEX:
        case SVGCSSEngine.STROKE_LINECAP_INDEX:
        case SVGCSSEngine.STROKE_LINEJOIN_INDEX:
        case SVGCSSEngine.STROKE_MITERLIMIT_INDEX:
        case SVGCSSEngine.STROKE_DASHARRAY_INDEX:
        case SVGCSSEngine.STROKE_DASHOFFSET_INDEX:
        case SVGCSSEngine.TEXT_DECORATION_INDEX:
            rebuildACI();
            break;
        case SVGCSSEngine.VISIBILITY_INDEX:
            rebuildACI();
            super.handleCSSPropertyChanged(property);
            break;
        case SVGCSSEngine.TEXT_RENDERING_INDEX: {
            RenderingHints hints = node.getRenderingHints();
            hints = CSSUtilities.convertTextRendering(e, hints);
            if (hints != null) {
                node.setRenderingHints(hints);
            }
            break;
        }
        case SVGCSSEngine.COLOR_RENDERING_INDEX: {
            RenderingHints hints = node.getRenderingHints();
            hints = CSSUtilities.convertColorRendering(e, hints);
            if (hints != null) {
                node.setRenderingHints(hints);
            }
            break;
        }
        default:
            super.handleCSSPropertyChanged(property);
        }
    }
    protected void rebuildACI() {
        if (hasNewACI)
            return;
        TextNode textNode = getTextNode();
        if (textNode.getAttributedCharacterIterator() == null)
            return;
        TextPaintInfo pi, oldPI;
        if ( cssProceedElement == e ){
            pi = new TextPaintInfo();
            setBaseTextPaintInfo(pi, e, node, ctx);
            setDecorationTextPaintInfo(pi, e);
            oldPI = (TextPaintInfo)elemTPI.get(e);
        } else {
            TextPaintInfo parentPI;
            parentPI = getParentTextPaintInfo(cssProceedElement);
            pi = getTextPaintInfo(cssProceedElement, textNode, parentPI, ctx);
            oldPI = (TextPaintInfo)elemTPI.get(cssProceedElement);
        }
        if (oldPI == null) return;
        textNode.swapTextPaintInfo(pi, oldPI);
        if (usingComplexSVGFont)
            textNode.setAttributedCharacterIterator
                (textNode.getAttributedCharacterIterator());
    }
    int getElementStartIndex(Element element) {
        TextPaintInfo tpi = (TextPaintInfo)elemTPI.get(element);
        if (tpi == null) return -1;
        return tpi.startChar;
    }
    int getElementEndIndex(Element element) {
        TextPaintInfo tpi = (TextPaintInfo)elemTPI.get(element);
        if (tpi == null) return -1;
        return tpi.endChar;
    }
    protected AttributedString buildAttributedString(BridgeContext ctx,
                                                     Element element) {
        AttributedStringBuffer asb = new AttributedStringBuffer();
        fillAttributedStringBuffer(ctx, element, true, null, null, null, asb);
        return asb.toAttributedString();
    }
    protected int endLimit;
    protected void fillAttributedStringBuffer(BridgeContext ctx,
                                              Element element,
                                              boolean top,
                                              TextPath textPath,
                                              Integer bidiLevel,
                                              Map initialAttributes,
                                              AttributedStringBuffer asb) {
        if ((!SVGUtilities.matchUserAgent(element, ctx.getUserAgent())) ||
            (!CSSUtilities.convertDisplay(element))) {
            return;
        }
        String s = XMLSupport.getXMLSpace(element);
        boolean preserve = s.equals(SVG_PRESERVE_VALUE);
        boolean prevEndsWithSpace;
        Element nodeElement = element;
        int elementStartChar = asb.length();
        if (top) {
            endLimit = 0;
        }
        if (preserve) {
            endLimit = asb.length();
        }
        Map map = initialAttributes == null
                ? new HashMap()
                : new HashMap(initialAttributes);
        initialAttributes =
            getAttributeMap(ctx, element, textPath, bidiLevel, map);
        Object o = map.get(TextAttribute.BIDI_EMBEDDING);
        Integer subBidiLevel = bidiLevel;
        if (o != null) {
            subBidiLevel = (Integer) o;
        }
        for (Node n = getFirstChild(element);
             n != null;
             n = getNextSibling(n)) {
            if (preserve) {
                prevEndsWithSpace = false;
            } else {
                if (asb.length() == 0) {
                    prevEndsWithSpace = true;
                } else {
                    prevEndsWithSpace = (asb.getLastChar() == ' ');
                }
            }
            switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                if (!SVG_NAMESPACE_URI.equals(n.getNamespaceURI()))
                    break;
                nodeElement = (Element)n;
                String ln = n.getLocalName();
                if (ln.equals(SVG_TSPAN_TAG) ||
                    ln.equals(SVG_ALT_GLYPH_TAG)) {
                    int before = asb.count;
                    fillAttributedStringBuffer(ctx,
                                               nodeElement,
                                               false,
                                               textPath,
                                               subBidiLevel,
                                               initialAttributes,
                                               asb);
                    if (asb.count != before) {
                        initialAttributes = null;
                    }
                } else if (ln.equals(SVG_TEXT_PATH_TAG)) {
                    SVGTextPathElementBridge textPathBridge
                        = (SVGTextPathElementBridge)ctx.getBridge(nodeElement);
                    TextPath newTextPath
                        = textPathBridge.createTextPath(ctx, nodeElement);
                    if (newTextPath != null) {
                        int before = asb.count;
                        fillAttributedStringBuffer(ctx,
                                                   nodeElement,
                                                   false,
                                                   newTextPath,
                                                   subBidiLevel,
                                                   initialAttributes,
                                                   asb);
                        if (asb.count != before) {
                            initialAttributes = null;
                        }
                    }
                } else if (ln.equals(SVG_TREF_TAG)) {
                    String uriStr = XLinkSupport.getXLinkHref((Element)n);
                    Element ref = ctx.getReferencedElement((Element)n, uriStr);
                    s = TextUtilities.getElementContent(ref);
                    s = normalizeString(s, preserve, prevEndsWithSpace);
                    if (s.length() != 0) {
                        int trefStart = asb.length();
                        Map m = initialAttributes == null
                                ? new HashMap()
                                : new HashMap(initialAttributes);
                        getAttributeMap
                            (ctx, nodeElement, textPath, bidiLevel, m);
                        asb.append(s, m);
                        int trefEnd = asb.length() - 1;
                        TextPaintInfo tpi;
                        tpi = (TextPaintInfo)elemTPI.get(nodeElement);
                        tpi.startChar = trefStart;
                        tpi.endChar   = trefEnd;
                        initialAttributes = null;
                    }
                } else if (ln.equals(SVG_A_TAG)) {
                    NodeEventTarget target = (NodeEventTarget)nodeElement;
                    UserAgent ua = ctx.getUserAgent();
                    SVGAElementBridge.CursorHolder ch;
                    ch = new SVGAElementBridge.CursorHolder
                        (CursorManager.DEFAULT_CURSOR);
                    EventListener l;
                    l = new SVGAElementBridge.AnchorListener(ua, ch);
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                         SVG_EVENT_CLICK, l, false, null);
                    ctx.storeEventListenerNS
                        (target, XMLConstants.XML_EVENTS_NAMESPACE_URI,
                         SVG_EVENT_CLICK, l, false);
                    int before = asb.count;
                    fillAttributedStringBuffer(ctx,
                                               nodeElement,
                                               false,
                                               textPath,
                                               subBidiLevel,
                                               initialAttributes,
                                               asb);
                    if (asb.count != before) {
                        initialAttributes = null;
                    }
                }
                break;
            case Node.TEXT_NODE:                     
            case Node.CDATA_SECTION_NODE:
                s = n.getNodeValue();
                s = normalizeString(s, preserve, prevEndsWithSpace);
                if (s.length() != 0) {
                    asb.append(s, map);
                    if (preserve) {
                        endLimit = asb.length();
                    }
                    initialAttributes = null;
                }
            }
        }
        if (top) {
            boolean strippedSome = false;
            while ((endLimit < asb.length()) && (asb.getLastChar() == ' ')) {
                asb.stripLast();
                strippedSome = true;
            }
            if (strippedSome) {
                Iterator iter = elemTPI.values().iterator();
                while (iter.hasNext()) {
                    TextPaintInfo tpi = (TextPaintInfo)iter.next();
                    if (tpi.endChar >= asb.length()) {
                        tpi.endChar = asb.length()-1;
                        if (tpi.startChar > tpi.endChar)
                            tpi.startChar = tpi.endChar;
                    }
                }
            }
        }
        int elementEndChar = asb.length()-1;
        TextPaintInfo tpi = (TextPaintInfo)elemTPI.get(element);
        tpi.startChar = elementStartChar;
        tpi.endChar   = elementEndChar;
    }
    protected String normalizeString(String s,
                                     boolean preserve,
                                     boolean stripfirst) {
        StringBuffer sb = new StringBuffer(s.length());
        if (preserve) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {                
                case 10:
                case 13:
                case '\t':
                    sb.append(' ');
                    break;
                default:
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        int idx = 0;
        if (stripfirst) {
            loop: while (idx < s.length()) {
                switch (s.charAt(idx)) {
                default:
                    break loop;
                case 10:                   
                case 13:
                case ' ':
                case '\t':
                    idx++;
                }
            }
        }
        boolean space = false;
        for (int i = idx; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case 10:                      
            case 13:
                break;
            case ' ':                     
            case '\t':
                if (!space) {
                    sb.append(' ');
                    space = true;
                }
                break;
            default:
                sb.append(c);
                space = false;
            }
        }
        return sb.toString();
    }
    protected static class AttributedStringBuffer {
        protected List strings;
        protected List attributes;
        protected int count;
        protected int length;
        public AttributedStringBuffer() {
            strings    = new ArrayList();
            attributes = new ArrayList();
            count      = 0;
            length     = 0;
        }
        public boolean isEmpty() {
            return count == 0;
        }
        public int length() {
            return length;
        }
        public void append(String s, Map m) {
            if (s.length() == 0) return;
            strings.add(s);
            attributes.add(m);
            count++;
            length += s.length();
        }
        public int getLastChar() {
            if (count == 0) {
                return -1;
            }
            String s = (String)strings.get(count - 1);
            return s.charAt(s.length() - 1);
        }
        public void stripFirst() {
            String s = (String)strings.get(0);
            if (s.charAt(s.length() - 1) != ' ')
                return;
            length--;
            if (s.length() == 1) {
                attributes.remove(0);
                strings.remove(0);
                count--;
                return;
            }
            strings.set(0, s.substring(1));
        }
        public void stripLast() {
            String s = (String)strings.get(count - 1);
            if (s.charAt(s.length() - 1) != ' ')
                return;
            length--;
            if (s.length() == 1) {
                attributes.remove(--count);
                strings.remove(count);
                return;
            }
            strings.set(count-1, s.substring(0, s.length() - 1));
        }
        public AttributedString toAttributedString() {
            switch (count) {
            case 0:
                return null;
            case 1:
                return new AttributedString((String)strings.get(0),
                                            (Map)attributes.get(0));
            }
            StringBuffer sb = new StringBuffer( strings.size() * 5 );
            Iterator it = strings.iterator();
            while (it.hasNext()) {
                sb.append((String)it.next());
            }
            AttributedString result = new AttributedString(sb.toString());
            Iterator sit = strings.iterator();
            Iterator ait = attributes.iterator();
            int idx = 0;
            while (sit.hasNext()) {
                String s = (String)sit.next();
                int nidx = idx + s.length();
                Map m = (Map)ait.next();
                Iterator kit = m.keySet().iterator();
                Iterator vit = m.values().iterator();
                while (kit.hasNext()) {
                    Attribute attr = (Attribute)kit.next();
                    Object val = vit.next();
                    result.addAttribute(attr, val, idx, nidx);
                }
                idx = nidx;
            }
            return result;
        }
        public String toString() {
            switch (count) {
            case 0:
                return "";
            case 1:
                return (String)strings.get(0);
            }
            StringBuffer sb = new StringBuffer( strings.size() * 5 );
            Iterator it = strings.iterator();
            while (it.hasNext()) {
                sb.append((String)it.next());
            }
            return sb.toString();
        }
    }
    protected boolean nodeAncestorOf(Node node1, Node node2) {
        if (node2 == null || node1 == null) {
            return false;
        }
        Node parent = getParentNode(node2);
        while (parent != null && parent != node1) {
            parent = getParentNode(parent);
        }
        return (parent == node1);
    }
    protected void addGlyphPositionAttributes(AttributedString as,
                                              Element element,
                                              BridgeContext ctx) {
        if ((!SVGUtilities.matchUserAgent(element, ctx.getUserAgent())) ||
            (!CSSUtilities.convertDisplay(element))) {
            return;
        }
        if (element.getLocalName().equals(SVG_TEXT_PATH_TAG)) {
            addChildGlyphPositionAttributes(as, element, ctx);
            return;
        }
        int firstChar = getElementStartIndex(element);
        if (firstChar == -1) return;
        int lastChar = getElementEndIndex(element);
        if (!(element instanceof SVGTextPositioningElement)) {
            addChildGlyphPositionAttributes(as, element, ctx);
            return;
        }
        SVGTextPositioningElement te = (SVGTextPositioningElement) element;
        try {
            SVGOMAnimatedLengthList _x =
                (SVGOMAnimatedLengthList) te.getX();
            _x.check();
            SVGOMAnimatedLengthList _y =
                (SVGOMAnimatedLengthList) te.getY();
            _y.check();
            SVGOMAnimatedLengthList _dx =
                (SVGOMAnimatedLengthList) te.getDx();
            _dx.check();
            SVGOMAnimatedLengthList _dy =
                (SVGOMAnimatedLengthList) te.getDy();
            _dy.check();
            SVGOMAnimatedNumberList _rotate =
                (SVGOMAnimatedNumberList) te.getRotate();
            _rotate.check();
            SVGLengthList xs  = _x.getAnimVal();
            SVGLengthList ys  = _y.getAnimVal();
            SVGLengthList dxs = _dx.getAnimVal();
            SVGLengthList dys = _dy.getAnimVal();
            SVGNumberList rs  = _rotate.getAnimVal();
            int len;
            len = xs.getNumberOfItems();
            for (int i = 0; i < len && firstChar + i <= lastChar; i++) {
                as.addAttribute
                    (GVTAttributedCharacterIterator.TextAttribute.X,
                     new Float(xs.getItem(i).getValue()), firstChar + i,
                               firstChar + i + 1);
            }
            len = ys.getNumberOfItems();
            for (int i = 0; i < len && firstChar + i <= lastChar; i++) {
                as.addAttribute
                    (GVTAttributedCharacterIterator.TextAttribute.Y,
                     new Float(ys.getItem(i).getValue()), firstChar + i,
                               firstChar + i + 1);
            }
            len = dxs.getNumberOfItems();
            for (int i = 0; i < len && firstChar + i <= lastChar; i++) {
                as.addAttribute
                    (GVTAttributedCharacterIterator.TextAttribute.DX,
                     new Float(dxs.getItem(i).getValue()), firstChar + i,
                               firstChar + i + 1);
            }
            len = dys.getNumberOfItems();
            for (int i = 0; i < len && firstChar + i <= lastChar; i++) {
                as.addAttribute
                    (GVTAttributedCharacterIterator.TextAttribute.DY,
                     new Float(dys.getItem(i).getValue()), firstChar + i,
                               firstChar + i + 1);
            }
            len = rs.getNumberOfItems();
            if (len == 1) {  
                Float rad = new Float(Math.toRadians(rs.getItem(0).getValue()));
                as.addAttribute
                    (GVTAttributedCharacterIterator.TextAttribute.ROTATION,
                     rad, firstChar, lastChar + 1);
            } else if (len > 1) {  
                for (int i = 0; i < len && firstChar + i <= lastChar; i++) {
                    Float rad = new Float(Math.toRadians(rs.getItem(i).getValue()));
                    as.addAttribute
                        (GVTAttributedCharacterIterator.TextAttribute.ROTATION,
                         rad, firstChar + i, firstChar + i + 1);
                }
            }
            addChildGlyphPositionAttributes(as, element, ctx);
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    protected void addChildGlyphPositionAttributes(AttributedString as,
                                                   Element element,
                                                   BridgeContext ctx) {
        for (Node child = getFirstChild(element);
             child != null;
             child = getNextSibling(child)) {
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;
            Element childElement = (Element)child;
            if (isTextChild(childElement)) {
                addGlyphPositionAttributes(as, childElement, ctx);
            }
        }
    }
    protected void addPaintAttributes(AttributedString as,
                                      Element element,
                                      TextNode node,
                                      TextPaintInfo pi,
                                      BridgeContext ctx) {
        if ((!SVGUtilities.matchUserAgent(element, ctx.getUserAgent())) ||
            (!CSSUtilities.convertDisplay(element))) {
            return;
        }
        Object o = elemTPI.get(element);
        if (o != null) {
            node.swapTextPaintInfo(pi, (TextPaintInfo)o);
        }
        addChildPaintAttributes(as, element, node, pi, ctx);
    }
    protected void addChildPaintAttributes(AttributedString as,
                                           Element element,
                                           TextNode node,
                                           TextPaintInfo parentPI,
                                           BridgeContext ctx) {
        for (Node child = getFirstChild(element);
             child != null;
             child = getNextSibling(child)) {
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element childElement = (Element)child;
            if (isTextChild(childElement)) {
                TextPaintInfo pi = getTextPaintInfo(childElement, node,
                                                        parentPI, ctx);
                addPaintAttributes(as, childElement, node, pi, ctx);
            }
        }
    }
    protected List getFontList(BridgeContext ctx,
                               Element       element,
                               Map           result) {
        result.put(TEXT_COMPOUND_ID, new SoftReference(element));
        Float fsFloat = TextUtilities.convertFontSize(element);
        float fontSize = fsFloat.floatValue();
        result.put(TextAttribute.SIZE, fsFloat);
        result.put(TextAttribute.WIDTH,
                TextUtilities.convertFontStretch(element));
        result.put(TextAttribute.POSTURE,
                TextUtilities.convertFontStyle(element));
        result.put(TextAttribute.WEIGHT,
                TextUtilities.convertFontWeight(element));
        Value v = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.FONT_WEIGHT_INDEX);
        String fontWeightString = v.getCssText();
        String fontStyleString = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.FONT_STYLE_INDEX).getStringValue();
        result.put(TEXT_COMPOUND_DELIMITER, element);
        Value val = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.FONT_FAMILY_INDEX);
        List fontFamilyList = new ArrayList();
        List fontList = new ArrayList();
        int len = val.getLength();
        for (int i = 0; i < len; i++) {
            Value it = val.item(i);
            String fontFamilyName = it.getStringValue();
            GVTFontFamily fontFamily;
            fontFamily = SVGFontUtilities.getFontFamily
                (element, ctx, fontFamilyName,
                 fontWeightString, fontStyleString);
            if (fontFamily == null) continue;
            if (fontFamily instanceof UnresolvedFontFamily) {
                fontFamily = FontFamilyResolver.resolve
                    ((UnresolvedFontFamily)fontFamily);
                if (fontFamily == null) continue;
            }
            fontFamilyList.add(fontFamily);
            if (fontFamily instanceof SVGFontFamily) {
                SVGFontFamily svgFF = (SVGFontFamily)fontFamily;
                if (svgFF.isComplex()) {
                    usingComplexSVGFont = true;
                }
            }
            GVTFont ft = fontFamily.deriveFont(fontSize, result);
            fontList.add(ft);
        }
        result.put(GVT_FONT_FAMILIES, fontFamilyList);
        if (!ctx.isDynamic()) {
            result.remove(TEXT_COMPOUND_DELIMITER);
        }
        return fontList;
    }
    protected Map getAttributeMap(BridgeContext ctx,
                                  Element element,
                                  TextPath textPath,
                                  Integer bidiLevel,
                                  Map result) {
        SVGTextContentElement tce = null;
        if (element instanceof SVGTextContentElement) {
            tce = (SVGTextContentElement) element;
        }
        Map inheritMap = null;
        String s;
        if (SVG_NAMESPACE_URI.equals(element.getNamespaceURI()) &&
            element.getLocalName().equals(SVG_ALT_GLYPH_TAG)) {
            result.put(ALT_GLYPH_HANDLER,
                       new SVGAltGlyphHandler(ctx, element));
        }
        TextPaintInfo pi = new TextPaintInfo();
        pi.visible   = true;
        pi.fillPaint = Color.black;
        result.put(PAINT_INFO, pi);
        elemTPI.put(element, pi);
        if (textPath != null) {
            result.put(TEXTPATH, textPath);
        }
        TextNode.Anchor a = TextUtilities.convertTextAnchor(element);
        result.put(ANCHOR_TYPE, a);
        List fontList = getFontList(ctx, element, result);
        result.put(GVT_FONTS, fontList);
        Object bs = TextUtilities.convertBaselineShift(element);
        if (bs != null) {
            result.put(BASELINE_SHIFT, bs);
        }
        Value val =  CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.UNICODE_BIDI_INDEX);
        s = val.getStringValue();
        if (s.charAt(0) == 'n') {
            if (bidiLevel != null)
                result.put(TextAttribute.BIDI_EMBEDDING, bidiLevel);
        } else {
            val = CSSUtilities.getComputedStyle
                (element, SVGCSSEngine.DIRECTION_INDEX);
            String rs = val.getStringValue();
            int cbidi = 0;
            if (bidiLevel != null) cbidi = bidiLevel.intValue();
            if (cbidi < 0) cbidi = -cbidi;
            switch (rs.charAt(0)) {
            case 'l':
                result.put(TextAttribute.RUN_DIRECTION,
                           TextAttribute.RUN_DIRECTION_LTR);
                if ((cbidi & 0x1) == 1) cbidi++; 
                else                    cbidi+=2; 
                break;
            case 'r':
                result.put(TextAttribute.RUN_DIRECTION,
                           TextAttribute.RUN_DIRECTION_RTL);
                if ((cbidi & 0x1) == 1) cbidi+=2; 
                else                    cbidi++; 
                break;
            }
            switch (s.charAt(0)) {
            case 'b': 
                cbidi = -cbidi; 
                break;
            }
            result.put(TextAttribute.BIDI_EMBEDDING, new Integer(cbidi));
        }
        val = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.WRITING_MODE_INDEX);
        s = val.getStringValue();
        switch (s.charAt(0)) {
        case 'l':
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.WRITING_MODE,
                       GVTAttributedCharacterIterator.
                       TextAttribute.WRITING_MODE_LTR);
            break;
        case 'r':
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.WRITING_MODE,
                       GVTAttributedCharacterIterator.
                       TextAttribute.WRITING_MODE_RTL);
            break;
        case 't':
                result.put(GVTAttributedCharacterIterator.
                       TextAttribute.WRITING_MODE,
                       GVTAttributedCharacterIterator.
                       TextAttribute.WRITING_MODE_TTB);
            break;
        }
        val = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.GLYPH_ORIENTATION_VERTICAL_INDEX);
        int primitiveType = val.getPrimitiveType();
        switch ( primitiveType ) {
        case CSSPrimitiveValue.CSS_IDENT: 
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION,
                       GVTAttributedCharacterIterator.
                       TextAttribute.ORIENTATION_AUTO);
            break;
        case CSSPrimitiveValue.CSS_DEG:
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION,
                       GVTAttributedCharacterIterator.
                       TextAttribute.ORIENTATION_ANGLE);
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION_ANGLE,
                       new Float(val.getFloatValue()));
            break;
        case CSSPrimitiveValue.CSS_RAD:
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION,
                       GVTAttributedCharacterIterator.
                       TextAttribute.ORIENTATION_ANGLE);
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION_ANGLE,
                       new Float( Math.toDegrees( val.getFloatValue() ) ));
            break;
        case CSSPrimitiveValue.CSS_GRAD:
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION,
                       GVTAttributedCharacterIterator.
                       TextAttribute.ORIENTATION_ANGLE);
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.VERTICAL_ORIENTATION_ANGLE,
                       new Float(val.getFloatValue() * 9 / 5));
            break;
        default:
            throw new IllegalStateException("unexpected primitiveType (V):" + primitiveType );
        }
        val = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.GLYPH_ORIENTATION_HORIZONTAL_INDEX);
        primitiveType = val.getPrimitiveType();
        switch ( primitiveType ) {
        case CSSPrimitiveValue.CSS_DEG:
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.HORIZONTAL_ORIENTATION_ANGLE,
                       new Float(val.getFloatValue()));
            break;
        case CSSPrimitiveValue.CSS_RAD:
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.HORIZONTAL_ORIENTATION_ANGLE,
                       new Float( Math.toDegrees( val.getFloatValue() ) ));
            break;
        case CSSPrimitiveValue.CSS_GRAD:
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.HORIZONTAL_ORIENTATION_ANGLE,
                       new Float(val.getFloatValue() * 9 / 5));
            break;
        default:
            throw new IllegalStateException("unexpected primitiveType (H):" + primitiveType );
        }
        Float sp = TextUtilities.convertLetterSpacing(element);
        if (sp != null) {
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.LETTER_SPACING,
                       sp);
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.CUSTOM_SPACING,
                       Boolean.TRUE);
        }
        sp = TextUtilities.convertWordSpacing(element);
        if (sp != null) {
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.WORD_SPACING,
                       sp);
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.CUSTOM_SPACING,
                       Boolean.TRUE);
        }
        sp = TextUtilities.convertKerning(element);
        if (sp != null) {
            result.put(GVTAttributedCharacterIterator.TextAttribute.KERNING,
                       sp);
            result.put(GVTAttributedCharacterIterator.
                       TextAttribute.CUSTOM_SPACING,
                       Boolean.TRUE);
        }
        if (tce == null) {
            return inheritMap;
        }
        try {
            AbstractSVGAnimatedLength textLength =
                (AbstractSVGAnimatedLength) tce.getTextLength();
            if (textLength.isSpecified()) {
                if (inheritMap == null) {
                    inheritMap = new HashMap();
                }
                Object value = new Float(textLength.getCheckedValue());
                result.put
                    (GVTAttributedCharacterIterator.TextAttribute.BBOX_WIDTH,
                     value);
                inheritMap.put
                    (GVTAttributedCharacterIterator.TextAttribute.BBOX_WIDTH,
                     value);
                SVGOMAnimatedEnumeration _lengthAdjust =
                    (SVGOMAnimatedEnumeration) tce.getLengthAdjust();
                if (_lengthAdjust.getCheckedVal() ==
                        SVGTextContentElement.LENGTHADJUST_SPACINGANDGLYPHS) {
                    result.put(GVTAttributedCharacterIterator.
                               TextAttribute.LENGTH_ADJUST,
                               GVTAttributedCharacterIterator.
                               TextAttribute.ADJUST_ALL);
                    inheritMap.put(GVTAttributedCharacterIterator.
                                   TextAttribute.LENGTH_ADJUST,
                                   GVTAttributedCharacterIterator.
                                   TextAttribute.ADJUST_ALL);
                } else {
                    result.put(GVTAttributedCharacterIterator.
                               TextAttribute.LENGTH_ADJUST,
                               GVTAttributedCharacterIterator.
                               TextAttribute.ADJUST_SPACING);
                    inheritMap.put(GVTAttributedCharacterIterator.
                                   TextAttribute.LENGTH_ADJUST,
                                   GVTAttributedCharacterIterator.
                                   TextAttribute.ADJUST_SPACING);
                    result.put(GVTAttributedCharacterIterator.
                               TextAttribute.CUSTOM_SPACING,
                               Boolean.TRUE);
                    inheritMap.put(GVTAttributedCharacterIterator.
                                   TextAttribute.CUSTOM_SPACING,
                                   Boolean.TRUE);
                }
            }
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
        return inheritMap;
    }
    protected TextPaintInfo getParentTextPaintInfo(Element child) {
        Node parent = getParentNode(child);
        while (parent != null) {
            TextPaintInfo tpi = (TextPaintInfo)elemTPI.get(parent);
            if (tpi != null) return tpi;
            parent = getParentNode(parent);
        }
        return null;
    }
    protected TextPaintInfo getTextPaintInfo(Element element,
                                             GraphicsNode node,
                                             TextPaintInfo parentTPI,
                                             BridgeContext ctx) {
        CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.TEXT_DECORATION_INDEX);
        TextPaintInfo pi = new TextPaintInfo(parentTPI);
        StyleMap sm = ((CSSStylableElement)element).getComputedStyleMap(null);
        if ((sm.isNullCascaded(SVGCSSEngine.TEXT_DECORATION_INDEX)) &&
            (sm.isNullCascaded(SVGCSSEngine.FILL_INDEX)) &&
            (sm.isNullCascaded(SVGCSSEngine.STROKE_INDEX)) &&
            (sm.isNullCascaded(SVGCSSEngine.STROKE_WIDTH_INDEX)) &&
            (sm.isNullCascaded(SVGCSSEngine.OPACITY_INDEX))) {
            return pi;
        }
        setBaseTextPaintInfo(pi, element, node, ctx);
        if (!sm.isNullCascaded(SVGCSSEngine.TEXT_DECORATION_INDEX))
            setDecorationTextPaintInfo(pi, element);
        return pi;
    }
    public void setBaseTextPaintInfo(TextPaintInfo pi, Element element,
                                     GraphicsNode node, BridgeContext ctx) {
        if (!element.getLocalName().equals(SVG_TEXT_TAG))
            pi.composite    = CSSUtilities.convertOpacity   (element);
        else
            pi.composite    = AlphaComposite.SrcOver;
        pi.visible      = CSSUtilities.convertVisibility(element);
        pi.fillPaint    = PaintServer.convertFillPaint  (element, node, ctx);
        pi.strokePaint  = PaintServer.convertStrokePaint(element, node, ctx);
        pi.strokeStroke = PaintServer.convertStroke     (element);
    }
    public void setDecorationTextPaintInfo(TextPaintInfo pi, Element element) {
        Value val = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.TEXT_DECORATION_INDEX);
        switch (val.getCssValueType()) {
        case CSSValue.CSS_VALUE_LIST:
            ListValue lst = (ListValue)val;
            int len = lst.getLength();
            for (int i = 0; i < len; i++) {
                Value v = lst.item(i);
                String s = v.getStringValue();
                switch (s.charAt(0)) {
                case 'u':
                    if (pi.fillPaint != null) {
                        pi.underlinePaint = pi.fillPaint;
                    }
                    if (pi.strokePaint != null) {
                        pi.underlineStrokePaint = pi.strokePaint;
                    }
                    if (pi.strokeStroke != null) {
                        pi.underlineStroke = pi.strokeStroke;
                    }
                    break;
                case 'o':
                    if (pi.fillPaint != null) {
                        pi.overlinePaint = pi.fillPaint;
                    }
                    if (pi.strokePaint != null) {
                        pi.overlineStrokePaint = pi.strokePaint;
                    }
                    if (pi.strokeStroke != null) {
                        pi.overlineStroke = pi.strokeStroke;
                    }
                    break;
                case 'l':
                    if (pi.fillPaint != null) {
                        pi.strikethroughPaint = pi.fillPaint;
                    }
                    if (pi.strokePaint != null) {
                        pi.strikethroughStrokePaint = pi.strokePaint;
                    }
                    if (pi.strokeStroke != null) {
                        pi.strikethroughStroke = pi.strokeStroke;
                    }
                    break;
                }
            }
            break;
        default: 
            pi.underlinePaint = null;
            pi.underlineStrokePaint = null;
            pi.underlineStroke = null;
            pi.overlinePaint = null;
            pi.overlineStrokePaint = null;
            pi.overlineStroke = null;
            pi.strikethroughPaint = null;
            pi.strikethroughStrokePaint = null;
            pi.strikethroughStroke = null;
            break;
        }
    }
    public abstract class AbstractTextChildSVGContext
            extends AnimatableSVGBridge {
        protected SVGTextElementBridge textBridge;
        public AbstractTextChildSVGContext(BridgeContext ctx,
                                           SVGTextElementBridge parent,
                                           Element e) {
            this.ctx = ctx;
            this.textBridge = parent;
            this.e = e;
        }
        public String getNamespaceURI() {
            return null;
        }
        public String getLocalName() {
            return null;
        }
        public Bridge getInstance() {
            return null;
        }
        public SVGTextElementBridge getTextBridge() { return textBridge; }
        public float getPixelUnitToMillimeter() {
            return ctx.getUserAgent().getPixelUnitToMillimeter();
        }
        public float getPixelToMM() {
            return getPixelUnitToMillimeter();
        }
        public Rectangle2D getBBox() {
            return null;
        }
        public AffineTransform getCTM() {
            return null;
        }
        public AffineTransform getGlobalTransform() {
            return null;
        }
        public AffineTransform getScreenTransform() {
            return null;
        }
        public void setScreenTransform(AffineTransform at) {
            return;
        }
        public float getViewportWidth() {
            return ctx.getBlockWidth(e);
        }
        public float getViewportHeight() {
            return ctx.getBlockHeight(e);
        }
        public float getFontSize() {
            return CSSUtilities.getComputedStyle
                (e, SVGCSSEngine.FONT_SIZE_INDEX).getFloatValue();
        }
    }
    protected abstract class AbstractTextChildBridgeUpdateHandler
        extends AbstractTextChildSVGContext implements BridgeUpdateHandler {
        protected AbstractTextChildBridgeUpdateHandler
            (BridgeContext ctx,
             SVGTextElementBridge parent,
             Element e) {
            super(ctx,parent,e);
        }
        public void handleDOMAttrModifiedEvent(MutationEvent evt) {
        }
        public void handleDOMNodeInsertedEvent(MutationEvent evt) {
            textBridge.handleDOMNodeInsertedEvent(evt);
        }
        public void handleDOMNodeRemovedEvent(MutationEvent evt) {
        }
        public void handleDOMCharacterDataModified(MutationEvent evt) {
            textBridge.handleDOMCharacterDataModified(evt);
        }
        public void handleCSSEngineEvent(CSSEngineEvent evt) {
            textBridge.handleCSSEngineEvent(evt);
        }
        public void handleAnimatedAttributeChanged
                (AnimatedLiveAttributeValue alav) {
        }
        public void handleOtherAnimationChanged(String type) {
        }
        public void dispose(){
            ((SVGOMElement)e).setSVGContext(null);
            elemTPI.remove(e);
        }
    }
    protected class AbstractTextChildTextContent
        extends AbstractTextChildBridgeUpdateHandler
        implements SVGTextContent {
        protected AbstractTextChildTextContent
            (BridgeContext ctx,
             SVGTextElementBridge parent,
             Element e) {
            super(ctx,parent,e);
        }
        public int getNumberOfChars(){
            return textBridge.getNumberOfChars(e);
        }
        public Rectangle2D getExtentOfChar(int charnum ){
            return textBridge.getExtentOfChar(e,charnum);
        }
        public Point2D getStartPositionOfChar(int charnum){
            return textBridge.getStartPositionOfChar(e,charnum);
        }
        public Point2D getEndPositionOfChar(int charnum){
            return textBridge.getEndPositionOfChar(e,charnum);
        }
        public void selectSubString(int charnum, int nchars){
            textBridge.selectSubString(e,charnum,nchars);
        }
        public float getRotationOfChar(int charnum){
            return textBridge.getRotationOfChar(e,charnum);
        }
        public float getComputedTextLength(){
            return textBridge.getComputedTextLength(e);
        }
        public float getSubStringLength(int charnum, int nchars){
            return textBridge.getSubStringLength(e,charnum,nchars);
        }
        public int getCharNumAtPosition(float x , float y){
            return textBridge.getCharNumAtPosition(e,x,y);
        }
    }
    protected class TRefBridge
        extends AbstractTextChildTextContent {
        protected TRefBridge(BridgeContext ctx,
                          SVGTextElementBridge parent,
                          Element e) {
            super(ctx,parent,e);
        }
        public void handleAnimatedAttributeChanged
                (AnimatedLiveAttributeValue alav) {
            if (alav.getNamespaceURI() == null) {
                String ln = alav.getLocalName();
                if (ln.equals(SVG_X_ATTRIBUTE)
                        || ln.equals(SVG_Y_ATTRIBUTE)
                        || ln.equals(SVG_DX_ATTRIBUTE)
                        || ln.equals(SVG_DY_ATTRIBUTE)
                        || ln.equals(SVG_ROTATE_ATTRIBUTE)
                        || ln.equals(SVG_TEXT_LENGTH_ATTRIBUTE)
                        || ln.equals(SVG_LENGTH_ADJUST_ATTRIBUTE)) {
                    textBridge.computeLaidoutText(ctx, textBridge.e,
                                                  textBridge.getTextNode());
                    return;
                }
            }
            super.handleAnimatedAttributeChanged(alav);
        }
    }
    protected class TextPathBridge
        extends AbstractTextChildTextContent{
        protected TextPathBridge(BridgeContext ctx,
                              SVGTextElementBridge parent,
                              Element e){
            super(ctx,parent,e);
        }
    }
    protected class TspanBridge
        extends AbstractTextChildTextContent {
        protected TspanBridge(BridgeContext ctx,
                           SVGTextElementBridge parent,
                           Element e){
            super(ctx,parent,e);
        }
        public void handleAnimatedAttributeChanged
                (AnimatedLiveAttributeValue alav) {
            if (alav.getNamespaceURI() == null) {
                String ln = alav.getLocalName();
                if (ln.equals(SVG_X_ATTRIBUTE)
                        || ln.equals(SVG_Y_ATTRIBUTE)
                        || ln.equals(SVG_DX_ATTRIBUTE)
                        || ln.equals(SVG_DY_ATTRIBUTE)
                        || ln.equals(SVG_ROTATE_ATTRIBUTE)
                        || ln.equals(SVG_TEXT_LENGTH_ATTRIBUTE)
                        || ln.equals(SVG_LENGTH_ADJUST_ATTRIBUTE)) {
                    textBridge.computeLaidoutText(ctx, textBridge.e,
                                                  textBridge.getTextNode());
                    return;
                }
            }
            super.handleAnimatedAttributeChanged(alav);
        }
    }
    public int getNumberOfChars(){
        return getNumberOfChars(e);
    }
    public Rectangle2D getExtentOfChar(int charnum ){
        return getExtentOfChar(e,charnum);
    }
    public Point2D getStartPositionOfChar(int charnum){
        return getStartPositionOfChar(e,charnum);
    }
    public Point2D getEndPositionOfChar(int charnum){
        return getEndPositionOfChar(e,charnum);
    }
    public void selectSubString(int charnum, int nchars){
        selectSubString(e,charnum,nchars);
    }
    public float getRotationOfChar(int charnum){
        return getRotationOfChar(e,charnum);
    }
    public float getComputedTextLength(){
        return getComputedTextLength(e);
    }
    public float getSubStringLength(int charnum, int nchars){
        return getSubStringLength(e,charnum,nchars);
    }
    public int getCharNumAtPosition(float x , float y){
        return getCharNumAtPosition(e,x,y);
    }
    protected int getNumberOfChars(Element element){
        AttributedCharacterIterator aci;
        aci = getTextNode().getAttributedCharacterIterator();
        if (aci == null)
            return 0;
        int firstChar = getElementStartIndex(element);
        if (firstChar == -1)
            return 0; 
        int lastChar = getElementEndIndex(element);
        return( lastChar - firstChar + 1 );
    }
    protected Rectangle2D getExtentOfChar(Element element,int charnum ){
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null) return null;
        int firstChar = getElementStartIndex(element);
        if ( firstChar == -1 )
            return null;
        List list = getTextRuns(textNode);
        CharacterInformation info;
        info = getCharacterInformation(list, firstChar,charnum, aci);
        if ( info == null )
            return null;
        GVTGlyphVector it = info.layout.getGlyphVector();
        Shape b = null;
        if (info.glyphIndexStart == info.glyphIndexEnd) {
            if (it.isGlyphVisible(info.glyphIndexStart)) {
                b = it.getGlyphCellBounds(info.glyphIndexStart);
            }
        } else {
            GeneralPath path = null;
            for (int k = info.glyphIndexStart; k <= info.glyphIndexEnd; k++) {
                if (it.isGlyphVisible(k)) {
                    Rectangle2D gb = it.getGlyphCellBounds(k);
                    if (path == null) {
                        path = new GeneralPath(gb);
                    } else {
                        path.append(gb, false);
                    }
                }
            }
            b = path;
        }
        if (b == null) {
            return null;
        }
        return b.getBounds2D();
    }
    protected Point2D getStartPositionOfChar(Element element,int charnum){
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        int firstChar = getElementStartIndex(element);
        if ( firstChar == -1 )
            return null;
        List list = getTextRuns(textNode);
        CharacterInformation info;
        info = getCharacterInformation(list, firstChar,charnum, aci);
        if ( info == null )
            return null;
        return getStartPoint( info );
    }
    protected Point2D getStartPoint(CharacterInformation info){
        GVTGlyphVector it = info.layout.getGlyphVector();
        if (!it.isGlyphVisible(info.glyphIndexStart))
            return null;
        Point2D b = it.getGlyphPosition(info.glyphIndexStart);
        AffineTransform glyphTransform;
        glyphTransform = it.getGlyphTransform(info.glyphIndexStart);
        Point2D.Float result = new Point2D.Float(0, 0);
        if ( glyphTransform != null )
            glyphTransform.transform(result,result);
        result.x += b.getX();
        result.y += b.getY();
        return result;
    }
    protected Point2D getEndPositionOfChar(Element element,int charnum ){
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        int firstChar = getElementStartIndex(element);
        if ( firstChar == -1 )
            return null;
        List list = getTextRuns(textNode);
        CharacterInformation info;
        info = getCharacterInformation(list, firstChar,charnum, aci);
        if ( info == null )
            return null;
        return getEndPoint(info);
    }
    protected Point2D getEndPoint(CharacterInformation info){
        GVTGlyphVector it = info.layout.getGlyphVector();
        if (!it.isGlyphVisible(info.glyphIndexEnd))
            return null;
        Point2D b = it.getGlyphPosition(info.glyphIndexEnd);
        AffineTransform glyphTransform;
        glyphTransform = it.getGlyphTransform(info.glyphIndexEnd);
        GVTGlyphMetrics metrics = it.getGlyphMetrics(info.glyphIndexEnd);
        Point2D.Float result = new Point2D.Float
            (metrics.getHorizontalAdvance(), 0);
        if ( glyphTransform != null )
            glyphTransform.transform(result,result);
        result.x += b.getX();
        result.y += b.getY();
        return result;
    }
    protected float getRotationOfChar(Element element, int charnum){
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return 0;
        int firstChar = getElementStartIndex(element);
        if ( firstChar == -1 )
            return 0;
        List list = getTextRuns(textNode);
        CharacterInformation info;
        info = getCharacterInformation(list, firstChar,charnum, aci);
        double angle = 0.0;
        int nbGlyphs = 0;
        if ( info != null ){
            GVTGlyphVector it = info.layout.getGlyphVector();
            for( int k = info.glyphIndexStart ;
                 k <= info.glyphIndexEnd ;
                 k++ ){
                if (!it.isGlyphVisible(k)) continue;
                nbGlyphs++;
                AffineTransform glyphTransform = it.getGlyphTransform(k);
                if ( glyphTransform == null ) continue;
                double glyphAngle = 0.0;
                double cosTheta = glyphTransform.getScaleX();
                double sinTheta = glyphTransform.getShearX();
                if ( cosTheta == 0.0 ){
                    if ( sinTheta > 0 ) glyphAngle = Math.PI;
                    else                glyphAngle = -Math.PI;
                } else {
                    glyphAngle = Math.atan(sinTheta/cosTheta);    
                    if ( cosTheta < 0 )
                        glyphAngle += Math.PI;
                }
                glyphAngle = (Math.toDegrees( - glyphAngle ) ) % 360.0;
                angle += glyphAngle - info.getComputedOrientationAngle();
            }
        }
        if (nbGlyphs == 0) return 0;
        return (float)(angle / nbGlyphs );
    }
    protected float getComputedTextLength(Element e) {
        return getSubStringLength(e,0,getNumberOfChars(e));
    }
    protected float getSubStringLength(Element element,
                                       int charnum,
                                       int nchars){
        if (nchars == 0) {
            return 0;
        }
        float length = 0;
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return -1;
        int firstChar = getElementStartIndex(element);
        if ( firstChar == -1 )
            return -1;
        List list = getTextRuns(textNode);
        CharacterInformation currentInfo;
        currentInfo = getCharacterInformation(list, firstChar,charnum,aci);
        CharacterInformation lastCharacterInRunInfo = null;
        int chIndex = currentInfo.characterIndex+1;
        GVTGlyphVector vector = currentInfo.layout.getGlyphVector();
        float [] advs = currentInfo.layout.getGlyphAdvances();
        boolean [] glyphTrack = new boolean[advs.length];
        for( int k = charnum +1; k < charnum +nchars ; k++) {
            if (currentInfo.layout.isOnATextPath() ){
                for (int gi = currentInfo.glyphIndexStart;
                     gi <= currentInfo.glyphIndexEnd; gi++) {
                    if ((vector.isGlyphVisible(gi)) && !glyphTrack[gi])
                        length += advs[gi+1]-advs[gi];
                    glyphTrack[gi] = true;
                }
                CharacterInformation newInfo;
                newInfo = getCharacterInformation(list, firstChar, k, aci);
                if (newInfo.layout != currentInfo.layout) {
                    vector = newInfo.layout.getGlyphVector();
                    advs = newInfo.layout.getGlyphAdvances();
                    glyphTrack = new boolean[advs.length];
                    chIndex = currentInfo.characterIndex+1;
                }
                currentInfo = newInfo;
            } else {
                if ( currentInfo.layout.hasCharacterIndex(chIndex) ){
                    chIndex++;
                    continue;
                }
                lastCharacterInRunInfo = getCharacterInformation
                    (list,firstChar,k-1,aci);
                length += distanceFirstLastCharacterInRun
                    (currentInfo,lastCharacterInRunInfo);
                currentInfo = getCharacterInformation(list,firstChar,k,aci);
                chIndex = currentInfo.characterIndex+1;
                vector  = currentInfo.layout.getGlyphVector();
                advs    = currentInfo.layout.getGlyphAdvances();
                glyphTrack = new boolean[advs.length];
                lastCharacterInRunInfo = null;
            }
        }
        if (currentInfo.layout.isOnATextPath() ){
            for (int gi = currentInfo.glyphIndexStart;
                 gi <= currentInfo.glyphIndexEnd; gi++) {
                if ((vector.isGlyphVisible(gi)) && !glyphTrack[gi])
                    length += advs[gi+1]-advs[gi];
                glyphTrack[gi] = true;
            }
        } else {
            if ( lastCharacterInRunInfo == null ){
                lastCharacterInRunInfo = getCharacterInformation
                    (list,firstChar,charnum+nchars-1,aci);
            }
            length += distanceFirstLastCharacterInRun
                (currentInfo,lastCharacterInRunInfo);
        }
        return length;
    }
    protected float distanceFirstLastCharacterInRun
        (CharacterInformation first, CharacterInformation last){
        float [] advs = first.layout.getGlyphAdvances();
        int firstStart = first.glyphIndexStart;
        int firstEnd   = first.glyphIndexEnd;
        int lastStart  = last.glyphIndexStart;
        int lastEnd    = last.glyphIndexEnd;
        int start = (firstStart<lastStart)?firstStart:lastStart;
        int end   = (firstEnd<lastEnd)?lastEnd:firstEnd;
        return advs[end+1] - advs[start];
    }
    protected float distanceBetweenRun
        (CharacterInformation last, CharacterInformation first){
        float distance;
        Point2D startPoint;
        Point2D endPoint;
        CharacterInformation info = new CharacterInformation();
        info.layout = last.layout;
        info.glyphIndexEnd = last.layout.getGlyphCount()-1;
        startPoint = getEndPoint(info);
        info.layout = first.layout;
        info.glyphIndexStart = 0;
        endPoint = getStartPoint(info);
        if( first.isVertical() ){
            distance = (float)(endPoint.getY() - startPoint.getY());
        }
        else{
            distance = (float)(endPoint.getX() - startPoint.getX());
        }
        return distance;
    }
    protected void selectSubString(Element element, int charnum, int nchars) {
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return;
        int firstChar = getElementStartIndex(element);
        if ( firstChar == -1 )
            return;
        List list = getTextRuns(textNode);
        int lastChar = getElementEndIndex(element);
        CharacterInformation firstInfo, lastInfo;
        firstInfo = getCharacterInformation(list, firstChar,charnum,aci);
        lastInfo  = getCharacterInformation(list, firstChar,charnum+nchars-1,aci);
        Mark firstMark, lastMark;
        firstMark = textNode.getMarkerForChar(firstInfo.characterIndex,true);
        if ( lastInfo != null && lastInfo.characterIndex <= lastChar ){
            lastMark = textNode.getMarkerForChar(lastInfo.characterIndex,false);
        }
        else{
            lastMark = textNode.getMarkerForChar(lastChar,false);
        }
        ctx.getUserAgent().setTextSelection(firstMark,lastMark);
    }
    protected int getCharNumAtPosition(Element e, float x, float y){
        TextNode textNode = getTextNode();
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return -1;
        List list = getTextRuns(textNode);
        TextHit hit = null;
        for( int i = list.size()-1 ; i>= 0 && hit == null; i-- ){
            StrokingTextPainter.TextRun textRun;
            textRun = (StrokingTextPainter.TextRun)list.get(i);
            hit = textRun.getLayout().hitTestChar(x,y);
        }
        if ( hit == null )
            return -1;
        int first = getElementStartIndex( e );
        int last  = getElementEndIndex( e );
        int hitIndex = hit.getCharIndex();
        if ( hitIndex >= first && hitIndex <= last )
            return hitIndex - first;
        return -1;
    }
    protected List getTextRuns(TextNode node){
        if ( node.getTextRuns() == null ){
            node.getPrimitiveBounds();
        }
        return node.getTextRuns();
    }
    protected CharacterInformation getCharacterInformation
        (List list,int startIndex, int charnum,
         AttributedCharacterIterator aci)
    {
        CharacterInformation info = new CharacterInformation();
        info.characterIndex = startIndex+charnum;
        for (int i = 0; i < list.size(); i++) {
            StrokingTextPainter.TextRun run;
            run = (StrokingTextPainter.TextRun)list.get(i);
            if (!run.getLayout().hasCharacterIndex(info.characterIndex) )
                continue;
            info.layout = run.getLayout();
            aci.setIndex(info.characterIndex);
            if (aci.getAttribute(ALT_GLYPH_HANDLER) != null){
                info.glyphIndexStart = 0;
                info.glyphIndexEnd = info.layout.getGlyphCount()-1;
            } else {
                info.glyphIndexStart = info.layout.getGlyphIndex
                    (info.characterIndex);
                if ( info.glyphIndexStart == -1 ){
                    info.glyphIndexStart = 0;
                    info.glyphIndexEnd = info.layout.getGlyphCount()-1;
                }
                else{
                    info.glyphIndexEnd = info.glyphIndexStart;
                }
            }
            return info;
        }
        return null;
    }
    protected static class CharacterInformation{
        TextSpanLayout layout;
        int glyphIndexStart;
        int glyphIndexEnd;
        int characterIndex;
        public boolean isVertical(){
            return layout.isVertical();
        }
        public double getComputedOrientationAngle(){
            return layout.getComputedOrientationAngle(characterIndex);
        }
    }
    public Set getTextIntersectionSet(AffineTransform at,
                                       Rectangle2D rect) {
        Set elems = new HashSet();
        TextNode tn = getTextNode();
        List list = tn.getTextRuns();
        if (list == null)
            return elems;
        for (int i = 0 ; i < list.size(); i++) {
            StrokingTextPainter.TextRun run;
            run = (StrokingTextPainter.TextRun)list.get(i);
            TextSpanLayout layout = run.getLayout();
            AttributedCharacterIterator aci = run.getACI();
            aci.first();
            SoftReference sr;
            sr =(SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
            Element elem = (Element)sr.get();
            if (elem == null) continue;
            if (elems.contains(elem)) continue;
            if (!isTextSensitive(elem))   continue;
            Rectangle2D glBounds = layout.getBounds2D();
            if (glBounds != null) {
                glBounds = at.createTransformedShape(glBounds).getBounds2D();
                if (!rect.intersects(glBounds)) {
                    continue;
                }
            }
            GVTGlyphVector gv = layout.getGlyphVector();
            for (int g = 0; g < gv.getNumGlyphs(); g++) {
                Shape gBounds = gv.getGlyphLogicalBounds(g);
                if (gBounds != null) {
                    gBounds = at.createTransformedShape
                        (gBounds).getBounds2D();
                    if (gBounds.intersects(rect)) {
                        elems.add(elem);
                        break;
                    }
                }
            }
        }
        return elems;
    }
    public Set getTextEnclosureSet(AffineTransform at,
                                    Rectangle2D rect) {
        TextNode tn = getTextNode();
        Set elems = new HashSet();
        List list = tn.getTextRuns();
        if (list == null)
            return elems;
        Set reject = new HashSet();
        for (int i = 0 ; i < list.size(); i++) {
            StrokingTextPainter.TextRun run;
            run = (StrokingTextPainter.TextRun)list.get(i);
            TextSpanLayout layout = run.getLayout();
            AttributedCharacterIterator aci = run.getACI();
            aci.first();
            SoftReference sr;
            sr =(SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
            Element elem = (Element)sr.get();
            if (elem == null) continue;
            if (reject.contains(elem)) continue;
            if (!isTextSensitive(elem)) {
                reject.add(elem);
                continue;
            }
            Rectangle2D glBounds = layout.getBounds2D();
            if ( glBounds == null ){
                continue;
            }
            glBounds = at.createTransformedShape( glBounds ).getBounds2D();
            if (rect.contains(glBounds)) {
                elems.add(elem);
            } else {
                reject.add(elem);
                elems.remove(elem);
            }
        }
        return elems;
    }
    public static boolean getTextIntersection(BridgeContext ctx,
                                              Element elem,
                                              AffineTransform ati,
                                              Rectangle2D rect,
                                              boolean checkSensitivity) {
        SVGContext svgCtx = null;
        if (elem instanceof SVGOMElement)
            svgCtx  = ((SVGOMElement)elem).getSVGContext();
        if (svgCtx == null) return false;
        SVGTextElementBridge txtBridge = null;
        if (svgCtx instanceof SVGTextElementBridge)
            txtBridge = (SVGTextElementBridge)svgCtx;
        else if (svgCtx instanceof AbstractTextChildSVGContext) {
            AbstractTextChildSVGContext childCtx;
            childCtx = (AbstractTextChildSVGContext)svgCtx;
            txtBridge = childCtx.getTextBridge();
        }
        if (txtBridge == null) return false;
        TextNode tn = txtBridge.getTextNode();
        List list = tn.getTextRuns();
        if (list == null)
            return false;
        Element  txtElem = txtBridge.e;
        AffineTransform at = tn.getGlobalTransform();
        at.preConcatenate(ati);
        Rectangle2D tnRect;
        tnRect = tn.getBounds();
        tnRect = at.createTransformedShape(tnRect).getBounds2D();
        if (!rect.intersects(tnRect)) return false;
        for (int i = 0 ; i < list.size(); i++) {
            StrokingTextPainter.TextRun run;
            run = (StrokingTextPainter.TextRun)list.get(i);
            TextSpanLayout layout = run.getLayout();
            AttributedCharacterIterator aci = run.getACI();
            aci.first();
            SoftReference sr;
            sr =(SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
            Element runElem = (Element)sr.get();
            if (runElem == null) continue;
            if (checkSensitivity && !isTextSensitive(runElem)) continue;
            Element p = runElem;
            while ((p != null) && (p != txtElem) && (p != elem)) {
                p = (Element) txtBridge.getParentNode(p);
            }
            if (p != elem) continue;
            Rectangle2D glBounds = layout.getBounds2D();
            if (glBounds == null) continue;
            glBounds = at.createTransformedShape(glBounds).getBounds2D();
            if (!rect.intersects(glBounds)) continue;
            GVTGlyphVector gv = layout.getGlyphVector();
            for (int g = 0; g < gv.getNumGlyphs(); g++) {
                Shape gBounds = gv.getGlyphLogicalBounds(g);
                if (gBounds != null) {
                    gBounds = at.createTransformedShape
                        (gBounds).getBounds2D();
                    if (gBounds.intersects(rect)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static Rectangle2D getTextBounds(BridgeContext ctx, Element elem,
                                            boolean checkSensitivity) {
        SVGContext svgCtx = null;
        if (elem instanceof SVGOMElement)
            svgCtx  = ((SVGOMElement)elem).getSVGContext();
        if (svgCtx == null) return null;
        SVGTextElementBridge txtBridge = null;
        if (svgCtx instanceof SVGTextElementBridge)
            txtBridge = (SVGTextElementBridge)svgCtx;
        else if (svgCtx instanceof AbstractTextChildSVGContext) {
            AbstractTextChildSVGContext childCtx;
            childCtx = (AbstractTextChildSVGContext)svgCtx;
            txtBridge = childCtx.getTextBridge();
        }
        if (txtBridge == null) return null;
        TextNode tn = txtBridge.getTextNode();
        List list = tn.getTextRuns();
        if (list == null)
            return null;
        Element  txtElem = txtBridge.e;
        Rectangle2D ret = null;
        for (int i = 0 ; i < list.size(); i++) {
            StrokingTextPainter.TextRun run;
            run = (StrokingTextPainter.TextRun)list.get(i);
            TextSpanLayout layout = run.getLayout();
            AttributedCharacterIterator aci = run.getACI();
            aci.first();
            SoftReference sr;
            sr =(SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
            Element runElem = (Element)sr.get();
            if (runElem == null) continue;
            if (checkSensitivity && !isTextSensitive(runElem)) continue;
            Element p = runElem;
            while ((p != null) && (p != txtElem) && (p != elem)) {
                p = (Element) txtBridge.getParentNode(p);
            }
            if (p != elem) continue;
            Rectangle2D glBounds = layout.getBounds2D();
            if (glBounds != null) {
                if (ret == null) ret = (Rectangle2D)glBounds.clone();
                else             ret.add(glBounds);
            }
        }
        return ret;
    }
    public static boolean isTextSensitive(Element e) {
        int     ptrEvts = CSSUtilities.convertPointerEvents(e);
        switch (ptrEvts) {
        case GraphicsNode.VISIBLE_PAINTED:   
        case GraphicsNode.VISIBLE_FILL:
        case GraphicsNode.VISIBLE_STROKE:
        case GraphicsNode.VISIBLE:
            return CSSUtilities.convertVisibility(e);
        case GraphicsNode.PAINTED:
        case GraphicsNode.FILL:              
        case GraphicsNode.STROKE:
        case GraphicsNode.ALL:
            return true;
        case GraphicsNode.NONE:
        default:
            return false;
        }
    }
}
