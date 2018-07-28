package org.apache.batik.bridge.svg12;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.CursorManager;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.TextUtilities;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.SVGAElementBridge;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.ComputedValue;
import org.apache.batik.css.engine.value.svg12.SVG12ValueConstants;
import org.apache.batik.css.engine.value.svg12.LineHeightValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg12.SVGOMFlowRegionElement;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.flow.BlockInfo;
import org.apache.batik.gvt.flow.FlowTextNode;
import org.apache.batik.gvt.flow.RegionInfo;
import org.apache.batik.gvt.flow.TextLineBreaks;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.util.SVG12Constants;
import org.apache.batik.util.SVG12CSSConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
public class SVGFlowRootElementBridge extends SVG12TextElementBridge {
    public static final
        AttributedCharacterIterator.Attribute FLOW_PARAGRAPH =
        GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
    public static final
        AttributedCharacterIterator.Attribute FLOW_EMPTY_PARAGRAPH =
        GVTAttributedCharacterIterator.TextAttribute.FLOW_EMPTY_PARAGRAPH;
    public static final
        AttributedCharacterIterator.Attribute FLOW_LINE_BREAK =
        GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
    public static final
        AttributedCharacterIterator.Attribute FLOW_REGIONS =
        GVTAttributedCharacterIterator.TextAttribute.FLOW_REGIONS;
    public static final
        AttributedCharacterIterator.Attribute LINE_HEIGHT =
        GVTAttributedCharacterIterator.TextAttribute.LINE_HEIGHT;
    public static final
    GVTAttributedCharacterIterator.TextAttribute TEXTPATH =
        GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
    public static final
    GVTAttributedCharacterIterator.TextAttribute ANCHOR_TYPE =
        GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
    public static final
    GVTAttributedCharacterIterator.TextAttribute LETTER_SPACING =
        GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING;
    public static final
    GVTAttributedCharacterIterator.TextAttribute WORD_SPACING =
        GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING;
    public static final
    GVTAttributedCharacterIterator.TextAttribute KERNING =
        GVTAttributedCharacterIterator.TextAttribute.KERNING;
    protected Map flowRegionNodes;
    protected TextNode textNode;
    protected TextNode getTextNode() { return textNode; }
    protected RegionChangeListener regionChangeListener;
    public SVGFlowRootElementBridge() {}
    public String getNamespaceURI() {
        return SVG12Constants.SVG_NAMESPACE_URI;
    }
    public String getLocalName() {
        return SVG12Constants.SVG_FLOW_ROOT_TAG;
    }
    public Bridge getInstance() {
        return new SVGFlowRootElementBridge();
    }
    public boolean isComposite() {
        return false;
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
            return null;
        }
        CompositeGraphicsNode cgn = new CompositeGraphicsNode();
        String s = e.getAttributeNS(null, SVG_TRANSFORM_ATTRIBUTE);
        if (s.length() != 0) {
            cgn.setTransform
                (SVGUtilities.convertTransform(e, SVG_TRANSFORM_ATTRIBUTE, s,
                                               ctx));
        }
        cgn.setVisible(CSSUtilities.convertVisibility(e));
        RenderingHints hints = null;
        hints = CSSUtilities.convertColorRendering(e, hints);
        hints = CSSUtilities.convertTextRendering (e, hints);
        if (hints != null) {
            cgn.setRenderingHints(hints);
        }
        CompositeGraphicsNode cgn2 = new CompositeGraphicsNode();
        cgn.add(cgn2);
        FlowTextNode tn = (FlowTextNode)instantiateGraphicsNode();
        tn.setLocation(getLocation(ctx, e));
        if (ctx.getTextPainter() != null) {
            tn.setTextPainter(ctx.getTextPainter());
        }
        textNode = tn;
        cgn.add(tn);
        associateSVGContext(ctx, e, cgn);
        Node child = getFirstChild(e);
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addContextToChild(ctx,(Element)child);
            }
            child = getNextSibling(child);
        }
        return cgn;
    }
    protected GraphicsNode instantiateGraphicsNode() {
        return new FlowTextNode();
    }
    protected Point2D getLocation(BridgeContext ctx, Element e) {
        return new Point2D.Float(0,0);
    }
    protected boolean isTextElement(Element e) {
        if (!SVG_NAMESPACE_URI.equals(e.getNamespaceURI()))
            return false;
        String nodeName = e.getLocalName();
        return (nodeName.equals(SVG12Constants.SVG_FLOW_DIV_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_LINE_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_PARA_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_REGION_BREAK_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_SPAN_TAG));
    }
    protected boolean isTextChild(Element e) {
        if (!SVG_NAMESPACE_URI.equals(e.getNamespaceURI()))
            return false;
        String nodeName = e.getLocalName();
        return (nodeName.equals(SVG12Constants.SVG_A_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_LINE_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_PARA_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_REGION_BREAK_TAG) ||
                nodeName.equals(SVG12Constants.SVG_FLOW_SPAN_TAG));
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        CompositeGraphicsNode cgn = (CompositeGraphicsNode) node;
        boolean isStatic = !ctx.isDynamic();
        if (isStatic) {
            flowRegionNodes = new HashMap();
        } else {
            regionChangeListener = new RegionChangeListener();
        }
        CompositeGraphicsNode cgn2 = (CompositeGraphicsNode) cgn.get(0);
        GVTBuilder builder = ctx.getGVTBuilder();
        for (Node n = getFirstChild(e); n != null; n = getNextSibling(n)) {
            if (n instanceof SVGOMFlowRegionElement) {
                for (Node m = getFirstChild(n);
                        m != null;
                        m = getNextSibling(m)) {
                    if (m.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    GraphicsNode gn = builder.build(ctx, (Element) m);
                    if (gn != null) {
                        cgn2.add(gn);
                        if (isStatic) {
                            flowRegionNodes.put(m, gn);
                        }
                    }
                }
                if (!isStatic) {
                    AbstractNode an = (AbstractNode) n;
                    XBLEventSupport es =
                        (XBLEventSupport) an.initializeEventSupport();
                    es.addImplementationEventListenerNS
                        (SVG_NAMESPACE_URI, "shapechange", regionChangeListener,
                         false);
                }
            }
        }
        GraphicsNode tn = (GraphicsNode) cgn.get(1);
        super.buildGraphicsNode(ctx, e, tn);
        flowRegionNodes = null;
    }
    protected void computeLaidoutText(BridgeContext ctx,
                                       Element e,
                                       GraphicsNode node) {
        super.computeLaidoutText(ctx, getFlowDivElement(e), node);
    }
    protected void addContextToChild(BridgeContext ctx, Element e) {
        if (SVG_NAMESPACE_URI.equals(e.getNamespaceURI())) {
            String ln = e.getLocalName();
            if (ln.equals(SVG12Constants.SVG_FLOW_DIV_TAG)
                    || ln.equals(SVG12Constants.SVG_FLOW_LINE_TAG)
                    || ln.equals(SVG12Constants.SVG_FLOW_PARA_TAG)
                    || ln.equals(SVG12Constants.SVG_FLOW_SPAN_TAG)) {
                ((SVGOMElement) e).setSVGContext
                    (new FlowContentBridge(ctx, this, e));
            }
        }
        Node child = getFirstChild(e);
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addContextToChild(ctx,(Element)child);
            }
            child = getNextSibling(child);
        }
    }
    protected void removeContextFromChild(BridgeContext ctx, Element e) {
        if (SVG_NAMESPACE_URI.equals(e.getNamespaceURI())) {
            String ln = e.getLocalName();
            if (ln.equals(SVG12Constants.SVG_FLOW_DIV_TAG)
                    || ln.equals(SVG12Constants.SVG_FLOW_LINE_TAG)
                    || ln.equals(SVG12Constants.SVG_FLOW_PARA_TAG)
                    || ln.equals(SVG12Constants.SVG_FLOW_SPAN_TAG)) {
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
    protected AttributedString buildAttributedString(BridgeContext ctx,
                                                     Element element) {
        if (element == null) return null;
        List rgns = getRegions(ctx, element);
        AttributedString ret = getFlowDiv(ctx, element);
        if (ret == null) return ret;
        ret.addAttribute(FLOW_REGIONS, rgns, 0, 1);
        TextLineBreaks.findLineBrk(ret);
        return ret;
    }
    protected void dumpACIWord(AttributedString as) {
        if (as == null) return;
        StringBuffer chars = new StringBuffer();
        StringBuffer brkStr = new StringBuffer();
        AttributedCharacterIterator aci = as.getIterator();
        AttributedCharacterIterator.Attribute WORD_LIMIT =
            TextLineBreaks.WORD_LIMIT;
        for (char ch = aci.current();
             ch!=AttributedCharacterIterator.DONE;
             ch = aci.next()) {
                chars.append( ch ).append( ' ' ).append( ' ' );
                int w = ((Integer)aci.getAttribute(WORD_LIMIT)).intValue();
                brkStr.append( w ).append( ' ' );
                if (w < 10) {
                    brkStr.append( ' ' );
                }
        }
        System.out.println( chars.toString() );
        System.out.println( brkStr.toString() );
    }
    protected Element getFlowDivElement(Element elem) {
        String eNS = elem.getNamespaceURI();
        if (!eNS.equals(SVG_NAMESPACE_URI)) return null;
        String nodeName = elem.getLocalName();
        if (nodeName.equals(SVG12Constants.SVG_FLOW_DIV_TAG)) return elem;
        if (!nodeName.equals(SVG12Constants.SVG_FLOW_ROOT_TAG)) return null;
        for (Node n = getFirstChild(elem);
             n != null; n = getNextSibling(n)) {
            if (n.getNodeType()     != Node.ELEMENT_NODE) continue;
            String nNS = n.getNamespaceURI();
            if (!SVG_NAMESPACE_URI.equals(nNS)) continue;
            Element e = (Element)n;
            String ln = e.getLocalName();
            if (ln.equals(SVG12Constants.SVG_FLOW_DIV_TAG))
                return e;
        }
        return null;
    }
    protected AttributedString getFlowDiv(BridgeContext ctx, Element element) {
        Element flowDiv = getFlowDivElement(element);
        if (flowDiv == null) return null;
        return gatherFlowPara(ctx, flowDiv);
    }
    protected AttributedString gatherFlowPara
        (BridgeContext ctx, Element div) {
        TextPaintInfo divTPI = new TextPaintInfo();
        divTPI.visible   = true;
        divTPI.fillPaint = Color.black;
        elemTPI.put(div, divTPI);
        AttributedStringBuffer asb = new AttributedStringBuffer();
        List paraEnds  = new ArrayList();
        List paraElems = new ArrayList();
        List lnLocs    = new ArrayList();
        for (Node n = getFirstChild(div);
             n != null;
             n = getNextSibling(n)) {
            if (n.getNodeType() != Node.ELEMENT_NODE
                    || !getNamespaceURI().equals(n.getNamespaceURI())) {
                continue;
            }
            Element e = (Element)n;
            String ln = e.getLocalName();
            if (ln.equals(SVG12Constants.SVG_FLOW_PARA_TAG)) {
                fillAttributedStringBuffer
                    (ctx, e, true, null, null, asb, lnLocs);
                paraElems.add(e);
                paraEnds.add(new Integer(asb.length()));
            } else if (ln.equals(SVG12Constants.SVG_FLOW_REGION_BREAK_TAG)) {
                fillAttributedStringBuffer
                    (ctx, e, true, null, null, asb, lnLocs);
                paraElems.add(e);
                paraEnds.add(new Integer(asb.length()));
            }
        }
        divTPI.startChar = 0;
        divTPI.endChar   = asb.length()-1;
        AttributedString ret = asb.toAttributedString();
        if (ret == null)
            return null;
        int prevLN = 0;
        Iterator lnIter = lnLocs.iterator();
        while (lnIter.hasNext()) {
            int nextLN = ((Integer)lnIter.next()).intValue();
            if (nextLN == prevLN) continue;
            ret.addAttribute(FLOW_LINE_BREAK,
                             new Object(),
                             prevLN, nextLN);
            prevLN  = nextLN;
        }
        int start=0;
        int end;
        List emptyPara = null;
        for (int i=0; i<paraElems.size(); i++, start=end) {
            Element elem = (Element)paraElems.get(i);
            end  = ((Integer)paraEnds.get(i)).intValue();
            if (start == end) {
                if (emptyPara == null)
                    emptyPara = new LinkedList();
                emptyPara.add(makeBlockInfo(ctx, elem));
                continue;
            }
            ret.addAttribute(FLOW_PARAGRAPH, makeBlockInfo(ctx, elem),
                             start, end);
            if (emptyPara != null) {
                ret.addAttribute(FLOW_EMPTY_PARAGRAPH, emptyPara, start, end);
                emptyPara = null;
            }
        }
        return ret;
    }
    protected List getRegions(BridgeContext ctx, Element element)  {
        element = (Element)element.getParentNode();
        List ret = new LinkedList();
        for (Node n = getFirstChild(element);
             n != null; n = getNextSibling(n)) {
            if (n.getNodeType()     != Node.ELEMENT_NODE) continue;
            if (!SVG12Constants.SVG_NAMESPACE_URI.equals(n.getNamespaceURI()))
                continue;
            Element e = (Element)n;
            String ln = e.getLocalName();
            if (!SVG12Constants.SVG_FLOW_REGION_TAG.equals(ln))  continue;
            float verticalAlignment = 0.0f;
            gatherRegionInfo(ctx, e, verticalAlignment, ret);
        }
        return ret;
    }
    protected void gatherRegionInfo(BridgeContext ctx, Element rgn,
                                    float verticalAlign, List regions) {
        boolean isStatic = !ctx.isDynamic();
        for (Node n = getFirstChild(rgn); n != null; n = getNextSibling(n)) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            GraphicsNode gn = isStatic ? (GraphicsNode) flowRegionNodes.get(n)
                                       : ctx.getGraphicsNode(n);
            Shape s = gn.getOutline();
            if (s == null) {
                continue;
            }
            AffineTransform at = gn.getTransform();
            if (at != null) {
                s = at.createTransformedShape(s);
            }
            regions.add(new RegionInfo(s, verticalAlign));
        }
    }
    protected int startLen;
    protected void fillAttributedStringBuffer(BridgeContext ctx,
                                              Element element,
                                              boolean top,
                                              Integer bidiLevel,
                                              Map initialAttributes,
                                              AttributedStringBuffer asb,
                                              List lnLocs) {
        if ((!SVGUtilities.matchUserAgent(element, ctx.getUserAgent())) ||
            (!CSSUtilities.convertDisplay(element))) {
            return;
        }
        String  s        = XMLSupport.getXMLSpace(element);
        boolean preserve = s.equals(SVG_PRESERVE_VALUE);
        boolean prevEndsWithSpace;
        Element nodeElement = element;
        int elementStartChar = asb.length();
        if (top) {
            endLimit = startLen = asb.length();
        }
        if (preserve) {
            endLimit = startLen;
        }
        Map map = initialAttributes == null
                ? new HashMap()
                : new HashMap(initialAttributes);
        initialAttributes =
            getAttributeMap(ctx, element, null, bidiLevel, map);
        Object o = map.get(TextAttribute.BIDI_EMBEDDING);
        Integer subBidiLevel = bidiLevel;
        if (o != null) {
            subBidiLevel = (Integer) o;
        }
        int lineBreak = -1;
        if (lnLocs.size() != 0) {
            lineBreak = ((Integer)lnLocs.get(lnLocs.size()-1)).intValue();
        }
        for (Node n = getFirstChild(element);
             n != null;
             n = getNextSibling(n)) {
            if (preserve) {
                prevEndsWithSpace = false;
            } else {
                int len = asb.length();
                if (len == startLen) {
                    prevEndsWithSpace = true;
                } else {
                    prevEndsWithSpace = (asb.getLastChar() == ' ');
                    int idx = lnLocs.size()-1;
                    if (!prevEndsWithSpace && (idx >= 0)) {
                        Integer i = (Integer)lnLocs.get(idx);
                        if (i.intValue() == len) {
                            prevEndsWithSpace = true;
                        }
                    }
                }
            }
            switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                if (!SVG_NAMESPACE_URI.equals(n.getNamespaceURI()))
                    break;
                nodeElement = (Element)n;
                String ln = n.getLocalName();
                if (ln.equals(SVG12Constants.SVG_FLOW_LINE_TAG)) {
                    int before = asb.length();
                    fillAttributedStringBuffer(ctx, nodeElement, false,
                                               subBidiLevel, initialAttributes,
                                               asb, lnLocs);
                    lineBreak = asb.length();
                    lnLocs.add(new Integer(lineBreak));
                    if (before != lineBreak) {
                        initialAttributes = null;
                    }
                } else if (ln.equals(SVG12Constants.SVG_FLOW_SPAN_TAG) ||
                           ln.equals(SVG12Constants.SVG_ALT_GLYPH_TAG)) {
                    int before = asb.length();
                    fillAttributedStringBuffer(ctx, nodeElement, false,
                                               subBidiLevel, initialAttributes,
                                               asb, lnLocs);
                    if (asb.length() != before) {
                        initialAttributes = null;
                    }
                } else if (ln.equals(SVG_A_TAG)) {
                    if (ctx.isInteractive()) {
                        NodeEventTarget target = (NodeEventTarget)nodeElement;
                        UserAgent ua = ctx.getUserAgent();
                        SVGAElementBridge.CursorHolder ch;
                        ch = new SVGAElementBridge.CursorHolder
                            (CursorManager.DEFAULT_CURSOR);
                        target.addEventListenerNS
                            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                             SVG_EVENT_CLICK,
                             new SVGAElementBridge.AnchorListener(ua, ch),
                             false, null);
                        target.addEventListenerNS
                            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                             SVG_EVENT_MOUSEOVER,
                             new SVGAElementBridge.CursorMouseOverListener(ua,ch),
                             false, null);
                        target.addEventListenerNS
                            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                             SVG_EVENT_MOUSEOUT,
                             new SVGAElementBridge.CursorMouseOutListener(ua,ch),
                             false, null);
                    }
                    int before = asb.length();
                    fillAttributedStringBuffer(ctx, nodeElement, false,
                                               subBidiLevel, initialAttributes,
                                               asb, lnLocs);
                    if (asb.length() != before) {
                        initialAttributes = null;
                    }
                } else if (ln.equals(SVG_TREF_TAG)) {
                    String uriStr = XLinkSupport.getXLinkHref((Element)n);
                    Element ref = ctx.getReferencedElement((Element)n, uriStr);
                    s = TextUtilities.getElementContent(ref);
                    s = normalizeString(s, preserve, prevEndsWithSpace);
                    if (s.length() != 0) {
                        int trefStart = asb.length();
                        Map m = new HashMap();
                        getAttributeMap(ctx, nodeElement, null, bidiLevel, m);
                        asb.append(s, m);
                        int trefEnd = asb.length() - 1;
                        TextPaintInfo tpi;
                        tpi = (TextPaintInfo)elemTPI.get(nodeElement);
                        tpi.startChar = trefStart;
                        tpi.endChar   = trefEnd;
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
                int idx = lnLocs.size()-1;
                int len = asb.length();
                if (idx >= 0) {
                    Integer i = (Integer)lnLocs.get(idx);
                    if (i.intValue() >= len) {
                        i = new Integer(len-1);
                        lnLocs.set(idx, i);
                        idx--;
                        while (idx >= 0) {
                            i = (Integer)lnLocs.get(idx);
                            if (i.intValue() < len-1)
                                break;
                            lnLocs.remove(idx);
                            idx--;
                        }
                    }
                }
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
    protected Map getAttributeMap(BridgeContext ctx,
                                  Element element,
                                  TextPath textPath,
                                  Integer bidiLevel,
                                  Map result) {
        Map inheritingMap =
            super.getAttributeMap(ctx, element, textPath, bidiLevel, result);
        float fontSize   = TextUtilities.convertFontSize(element).floatValue();
        float lineHeight = getLineHeight(ctx, element, fontSize);
        result.put(LINE_HEIGHT, new Float(lineHeight));
        return inheritingMap;
    }
    protected void checkMap(Map attrs) {
        if (attrs.containsKey(TEXTPATH)) {
            return; 
        }
        if (attrs.containsKey(ANCHOR_TYPE)) {
            return; 
        }
        if (attrs.containsKey(LETTER_SPACING)) {
            return; 
        }
        if (attrs.containsKey(WORD_SPACING)) {
            return; 
        }
        if (attrs.containsKey(KERNING)) {
            return; 
        }
    }
    int marginTopIndex    = -1;
    int marginRightIndex  = -1;
    int marginBottomIndex = -1;
    int marginLeftIndex   = -1;
    int indentIndex       = -1;
    int textAlignIndex    = -1;
    int lineHeightIndex   = -1;
    protected void initCSSPropertyIndexes(Element e) {
        CSSEngine eng = CSSUtilities.getCSSEngine(e);
        marginTopIndex    = eng.getPropertyIndex(SVG12CSSConstants.CSS_MARGIN_TOP_PROPERTY);
        marginRightIndex  = eng.getPropertyIndex(SVG12CSSConstants.CSS_MARGIN_RIGHT_PROPERTY);
        marginBottomIndex = eng.getPropertyIndex(SVG12CSSConstants.CSS_MARGIN_BOTTOM_PROPERTY);
        marginLeftIndex   = eng.getPropertyIndex(SVG12CSSConstants.CSS_MARGIN_LEFT_PROPERTY);
        indentIndex       = eng.getPropertyIndex(SVG12CSSConstants.CSS_INDENT_PROPERTY);
        textAlignIndex    = eng.getPropertyIndex(SVG12CSSConstants.CSS_TEXT_ALIGN_PROPERTY);
        lineHeightIndex   = eng.getPropertyIndex(SVG12CSSConstants.CSS_LINE_HEIGHT_PROPERTY);
    }
    public BlockInfo makeBlockInfo(BridgeContext ctx, Element element) {
        if (marginTopIndex == -1) initCSSPropertyIndexes(element);
        Value v;
        v = CSSUtilities.getComputedStyle(element, marginTopIndex);
        float top = v.getFloatValue();
        v = CSSUtilities.getComputedStyle(element, marginRightIndex);
        float right = v.getFloatValue();
        v = CSSUtilities.getComputedStyle(element, marginBottomIndex);
        float bottom = v.getFloatValue();
        v = CSSUtilities.getComputedStyle(element, marginLeftIndex);
        float left = v.getFloatValue();
        v = CSSUtilities.getComputedStyle(element, indentIndex);
        float indent = v.getFloatValue();
        v = CSSUtilities.getComputedStyle(element, textAlignIndex);
        if (v == ValueConstants.INHERIT_VALUE) {
            v = CSSUtilities.getComputedStyle(element,
                                              SVGCSSEngine.DIRECTION_INDEX);
            if (v == ValueConstants.LTR_VALUE)
                v = SVG12ValueConstants.START_VALUE;
            else
                v = SVG12ValueConstants.END_VALUE;
        }
        int textAlign;
        if (v == SVG12ValueConstants.START_VALUE)
            textAlign = BlockInfo.ALIGN_START;
        else if (v == SVG12ValueConstants.MIDDLE_VALUE)
            textAlign = BlockInfo.ALIGN_MIDDLE;
        else if (v == SVG12ValueConstants.END_VALUE)
            textAlign = BlockInfo.ALIGN_END;
        else
            textAlign = BlockInfo.ALIGN_FULL;
        Map   fontAttrs      = new HashMap(20);
        List  fontList       = getFontList(ctx, element, fontAttrs);
        Float fs             = (Float)fontAttrs.get(TextAttribute.SIZE);
        float fontSize       = fs.floatValue();
        float lineHeight     = getLineHeight(ctx, element, fontSize);
        String ln = element.getLocalName();
        boolean rgnBr;
        rgnBr = ln.equals(SVG12Constants.SVG_FLOW_REGION_BREAK_TAG);
        return new BlockInfo(top, right, bottom, left, indent, textAlign,
                             lineHeight, fontList, fontAttrs, rgnBr);
    }
    protected float getLineHeight(BridgeContext ctx, Element element,
                                  float fontSize) {
        if (lineHeightIndex == -1) initCSSPropertyIndexes(element);
        Value v = CSSUtilities.getComputedStyle(element, lineHeightIndex);
        if ((v == ValueConstants.INHERIT_VALUE) ||
            (v == SVG12ValueConstants.NORMAL_VALUE)) {
            return fontSize*1.1f;
        }
        float lineHeight = v.getFloatValue();
        if (v instanceof ComputedValue)
            v = ((ComputedValue)v).getComputedValue();
        if ((v instanceof LineHeightValue) &&
            ((LineHeightValue)v).getFontSizeRelative())
            lineHeight *= fontSize;
        return lineHeight;
    }
    protected class FlowContentBridge extends AbstractTextChildTextContent {
        public FlowContentBridge(BridgeContext ctx,
                                 SVGTextElementBridge parent,
                                 Element e) {
            super(ctx, parent, e);
        }
    }
    protected class RegionChangeListener implements EventListener {
        public void handleEvent(Event evt) {
            laidoutText = null;
            computeLaidoutText(ctx, e, getTextNode());
        }
    }
}
