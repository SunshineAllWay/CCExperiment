package org.apache.batik.dom.svg;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.dom.anim.AnimationTargetListener;
import org.apache.batik.anim.values.AnimatableNumberOptionalNumberValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSNavigableNode;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.parser.UnitProcessor;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedInteger;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGFitToViewBox;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;
public abstract class SVGOMElement
    extends    AbstractElement
    implements SVGElement,
               ExtendedTraitAccess,
               AnimationTarget {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t = new DoublyIndexedTable();
        t.put(null, SVG_ID_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_CDATA));
        t.put(XML_NAMESPACE_URI, XML_BASE_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_URI));
        t.put(XML_NAMESPACE_URI, XML_SPACE_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_IDENT));
        t.put(XML_NAMESPACE_URI, XML_ID_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_CDATA));
        t.put(XML_NAMESPACE_URI, XML_LANG_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_LANG));
        xmlTraitInformation = t;
    }
    protected transient boolean readonly;
    protected String prefix;
    protected transient SVGContext svgContext;
    protected DoublyIndexedTable targetListeners;
    protected UnitProcessor.Context unitContext;
    protected SVGOMElement() {
    }
    protected SVGOMElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    protected void initializeAllLiveAttributes() {
    }
    public String getId() {
        if (((SVGOMDocument) ownerDocument).isSVG12) {
            Attr a = getAttributeNodeNS(XML_NAMESPACE_URI, SVG_ID_ATTRIBUTE);
            if (a != null) {
                return a.getNodeValue();
            }
        }
        return getAttributeNS(null, SVG_ID_ATTRIBUTE);
    }
    public void setId(String id) {
        if (((SVGOMDocument) ownerDocument).isSVG12) {
            setAttributeNS(XML_NAMESPACE_URI, XML_ID_QNAME, id);
            Attr a = getAttributeNodeNS(null, SVG_ID_ATTRIBUTE);
            if (a != null) {
                a.setNodeValue(id);
            }
        } else {
            setAttributeNS(null, SVG_ID_ATTRIBUTE, id);
        }
    }
    public String getXMLbase() {
        return getAttributeNS(XML_NAMESPACE_URI, XML_BASE_ATTRIBUTE);
    }
    public void setXMLbase(String xmlbase) throws DOMException {
        setAttributeNS(XML_NAMESPACE_URI, XML_BASE_QNAME, xmlbase);
    }
    public SVGSVGElement getOwnerSVGElement() {
        for (Element e = CSSEngine.getParentCSSStylableElement(this);
             e != null;
             e = CSSEngine.getParentCSSStylableElement(e)) {
            if (e instanceof SVGSVGElement) {
                return (SVGSVGElement)e;
            }
        }
        return null;
    }
    public SVGElement getViewportElement() {
        for (Element e = CSSEngine.getParentCSSStylableElement(this);
             e != null;
             e = CSSEngine.getParentCSSStylableElement(e)) {
            if (e instanceof SVGFitToViewBox) {
                return (SVGElement)e;
            }
        }
        return null;
    }
    public String getNodeName() {
        if (prefix == null || prefix.equals("")) {
            return getLocalName();
        }
        return prefix + ':' + getLocalName();
    }
    public String getNamespaceURI() {
        return SVGDOMImplementation.SVG_NAMESPACE_URI;
    }
    public void setPrefix(String prefix) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        if (prefix != null &&
            !prefix.equals("") &&
            !DOMUtilities.isValidName(prefix)) {
            throw createDOMException(DOMException.INVALID_CHARACTER_ERR,
                                     "prefix",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName(),
                                                    prefix });
        }
        this.prefix = prefix;
    }
    protected String getCascadedXMLBase(Node node) {
        String base = null;
        Node n = node.getParentNode();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                base = getCascadedXMLBase(n);
                break;
            }
            if (n instanceof CSSNavigableNode) {
                n = ((CSSNavigableNode) n).getCSSParentNode();
            } else {
                n = n.getParentNode();
            }
        }
        if (base == null) {
            AbstractDocument doc;
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                doc = (AbstractDocument) node;
            } else {
                doc = (AbstractDocument) node.getOwnerDocument();
            }
            base = doc.getDocumentURI();
        }
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
            node = node.getParentNode();
        }
        if (node == null) {
            return base;
        }
        Element e = (Element) node;
        Attr attr = e.getAttributeNodeNS(XML_NAMESPACE_URI, XML_BASE_ATTRIBUTE);
        if (attr != null) {
            if (base == null) {
                base = attr.getNodeValue();
            } else {
                base = new ParsedURL(base, attr.getNodeValue()).toString();
            }
        }
        return base;
    }
    public void setSVGContext(SVGContext ctx) {
        svgContext = ctx;
    }
    public SVGContext getSVGContext() {
        return svgContext;
    }
    public SVGException createSVGException(short type,
                                           String key,
                                           Object [] args) {
        try {
            return new SVGOMException
                (type, getCurrentDocument().formatMessage(key, args));
        } catch (Exception e) {
            return new SVGOMException(type, key);
        }
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
    protected SVGOMAnimatedTransformList createLiveAnimatedTransformList
            (String ns, String ln, String def) {
        SVGOMAnimatedTransformList v =
            new SVGOMAnimatedTransformList(this, ns, ln, def);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedBoolean createLiveAnimatedBoolean
            (String ns, String ln, boolean def) {
        SVGOMAnimatedBoolean v =
            new SVGOMAnimatedBoolean(this, ns, ln, def);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedString createLiveAnimatedString
            (String ns, String ln) {
        SVGOMAnimatedString v =
            new SVGOMAnimatedString(this, ns, ln);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedPreserveAspectRatio
            createLiveAnimatedPreserveAspectRatio() {
        SVGOMAnimatedPreserveAspectRatio v =
            new SVGOMAnimatedPreserveAspectRatio(this);
        liveAttributeValues.put(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedMarkerOrientValue
            createLiveAnimatedMarkerOrientValue(String ns, String ln) {
        SVGOMAnimatedMarkerOrientValue v =
            new SVGOMAnimatedMarkerOrientValue(this, ns, ln);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedPathData
            createLiveAnimatedPathData(String ns, String ln, String def) {
        SVGOMAnimatedPathData v =
            new SVGOMAnimatedPathData(this, ns, ln, def);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedNumber createLiveAnimatedNumber
            (String ns, String ln, float def) {
        return createLiveAnimatedNumber(ns, ln, def, false);
    }
    protected SVGOMAnimatedNumber createLiveAnimatedNumber
            (String ns, String ln, float def, boolean allowPercentage) {
        SVGOMAnimatedNumber v =
            new SVGOMAnimatedNumber(this, ns, ln, def, allowPercentage);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedNumberList createLiveAnimatedNumberList
            (String ns, String ln, String def, boolean canEmpty) {
        SVGOMAnimatedNumberList v =
            new SVGOMAnimatedNumberList(this, ns, ln, def, canEmpty);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedPoints createLiveAnimatedPoints
            (String ns, String ln, String def) {
        SVGOMAnimatedPoints v =
            new SVGOMAnimatedPoints(this, ns, ln, def);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedLengthList createLiveAnimatedLengthList
            (String ns, String ln, String def, boolean emptyAllowed,
             short dir) {
        SVGOMAnimatedLengthList v =
            new SVGOMAnimatedLengthList(this, ns, ln, def, emptyAllowed, dir);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedInteger createLiveAnimatedInteger
            (String ns, String ln, int def) {
        SVGOMAnimatedInteger v =
            new SVGOMAnimatedInteger(this, ns, ln, def);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedEnumeration createLiveAnimatedEnumeration
            (String ns, String ln, String[] val, short def) {
        SVGOMAnimatedEnumeration v =
            new SVGOMAnimatedEnumeration(this, ns, ln, val, def);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedLength createLiveAnimatedLength
            (String ns, String ln, String val, short dir, boolean nonneg) {
        SVGOMAnimatedLength v =
            new SVGOMAnimatedLength(this, ns, ln, val, dir, nonneg);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    protected SVGOMAnimatedRect createLiveAnimatedRect
            (String ns, String ln, String value) {
        SVGOMAnimatedRect v = new SVGOMAnimatedRect(this, ns, ln, value);
        liveAttributeValues.put(ns, ln, v);
        v.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
        return v;
    }
    public boolean hasProperty(String pn) {
        AbstractStylableDocument doc = (AbstractStylableDocument) ownerDocument;
        CSSEngine eng = doc.getCSSEngine();
        return eng.getPropertyIndex(pn) != -1
            || eng.getShorthandIndex(pn) != -1;
    }
    public boolean hasTrait(String ns, String ln) {
        return false;
    }
    public boolean isPropertyAnimatable(String pn) {
        AbstractStylableDocument doc = (AbstractStylableDocument) ownerDocument;
        CSSEngine eng = doc.getCSSEngine();
        int idx = eng.getPropertyIndex(pn);
        if (idx != -1) {
            ValueManager[] vms = eng.getValueManagers();
            return vms[idx].isAnimatableProperty();
        }
        idx = eng.getShorthandIndex(pn);
        if (idx != -1) {
            ShorthandManager[] sms = eng.getShorthandManagers();
            return sms[idx].isAnimatableProperty();
        }
        return false;
    }
    public final boolean isAttributeAnimatable(String ns, String ln) {
        DoublyIndexedTable t = getTraitInformationTable();
        TraitInformation ti = (TraitInformation) t.get(ns, ln);
        if (ti != null) {
            return ti.isAnimatable();
        }
        return false;
    }
    public boolean isPropertyAdditive(String pn) {
        AbstractStylableDocument doc = (AbstractStylableDocument) ownerDocument;
        CSSEngine eng = doc.getCSSEngine();
        int idx = eng.getPropertyIndex(pn);
        if (idx != -1) {
            ValueManager[] vms = eng.getValueManagers();
            return vms[idx].isAdditiveProperty();
        }
        idx = eng.getShorthandIndex(pn);
        if (idx != -1) {
            ShorthandManager[] sms = eng.getShorthandManagers();
            return sms[idx].isAdditiveProperty();
        }
        return false;
    }
    public boolean isAttributeAdditive(String ns, String ln) {
        return true;
    }
    public boolean isTraitAnimatable(String ns, String tn) {
        return false;
    }
    public boolean isTraitAdditive(String ns, String tn) {
        return false;
    }
    public int getPropertyType(String pn) {
        AbstractStylableDocument doc =
            (AbstractStylableDocument) ownerDocument;
        CSSEngine eng = doc.getCSSEngine();
        int idx = eng.getPropertyIndex(pn);
        if (idx != -1) {
            ValueManager[] vms = eng.getValueManagers();
            return vms[idx].getPropertyType();
        }
        return SVGTypes.TYPE_UNKNOWN;
    }
    public final int getAttributeType(String ns, String ln) {
        DoublyIndexedTable t = getTraitInformationTable();
        TraitInformation ti = (TraitInformation) t.get(ns, ln);
        if (ti != null) {
            return ti.getType();
        }
        return SVGTypes.TYPE_UNKNOWN;
    }
    public Element getElement() {
        return this;
    }
    public void updatePropertyValue(String pn, AnimatableValue val) {
    }
    public void updateAttributeValue(String ns, String ln,
                                     AnimatableValue val) {
        LiveAttributeValue a = getLiveAttributeValue(ns, ln);
        ((AbstractSVGAnimatedValue) a).updateAnimatedValue(val);
    }
    public void updateOtherValue(String type, AnimatableValue val) {
    }
    public AnimatableValue getUnderlyingValue(String ns, String ln) {
        LiveAttributeValue a = getLiveAttributeValue(ns, ln);
        if (!(a instanceof AnimatedLiveAttributeValue)) {
            return null;
        }
        return ((AnimatedLiveAttributeValue) a).getUnderlyingValue(this);
    }
    protected AnimatableValue getBaseValue(SVGAnimatedInteger n,
                                           SVGAnimatedInteger on) {
        return new AnimatableNumberOptionalNumberValue
            (this, n.getBaseVal(), on.getBaseVal());
    }
    protected AnimatableValue getBaseValue(SVGAnimatedNumber n,
                                           SVGAnimatedNumber on) {
        return new AnimatableNumberOptionalNumberValue
            (this, n.getBaseVal(), on.getBaseVal());
    }
    public short getPercentageInterpretation(String ns, String an,
                                             boolean isCSS) {
        if (isCSS || ns == null) {
            if (an.equals(CSSConstants.CSS_BASELINE_SHIFT_PROPERTY)
                    || an.equals(CSSConstants.CSS_FONT_SIZE_PROPERTY)) {
                return PERCENTAGE_FONT_SIZE;
            }
        }
        if (!isCSS) {
            DoublyIndexedTable t = getTraitInformationTable();
            TraitInformation ti = (TraitInformation) t.get(ns, an);
            if (ti != null) {
                return ti.getPercentageInterpretation();
            }
            return PERCENTAGE_VIEWPORT_SIZE;
        }
        return PERCENTAGE_VIEWPORT_SIZE;
    }
    protected final short getAttributePercentageInterpretation(String ns, String ln) {
        return PERCENTAGE_VIEWPORT_SIZE;
    }
    public boolean useLinearRGBColorInterpolation() {
        return false;
    }
    public float svgToUserSpace(float v, short type, short pcInterp) {
        if (unitContext == null) {
            unitContext = new UnitContext();
        }
        if (pcInterp == PERCENTAGE_FONT_SIZE
                && type == SVGLength.SVG_LENGTHTYPE_PERCENTAGE) {
            return 0f;
        } else {
            return UnitProcessor.svgToUserSpace(v, type, (short) (3 - pcInterp),
                                                unitContext);
        }
    }
    public void addTargetListener(String ns, String an, boolean isCSS,
                                  AnimationTargetListener l) {
        if (!isCSS) {
            if (targetListeners == null) {
                targetListeners = new DoublyIndexedTable();
            }
            LinkedList ll = (LinkedList) targetListeners.get(ns, an);
            if (ll == null) {
                ll = new LinkedList();
                targetListeners.put(ns, an, ll);
            }
            ll.add(l);
        }
    }
    public void removeTargetListener(String ns, String an, boolean isCSS,
                                     AnimationTargetListener l) {
        if (!isCSS) {
            LinkedList ll = (LinkedList) targetListeners.get(ns, an);
            ll.remove(l);
        }
    }
    void fireBaseAttributeListeners(String ns, String ln) {
        if (targetListeners != null) {
            LinkedList ll = (LinkedList) targetListeners.get(ns, ln);
            Iterator it = ll.iterator();
            while (it.hasNext()) {
                AnimationTargetListener l = (AnimationTargetListener) it.next();
                l.baseValueChanged(this, ns, ln, false);
            }
        }
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        SVGOMElement e = (SVGOMElement)n;
        e.prefix = prefix;
        e.initializeAllLiveAttributes();
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        SVGOMElement e = (SVGOMElement)n;
        e.prefix = prefix;
        e.initializeAllLiveAttributes();
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        SVGOMElement e = (SVGOMElement)n;
        e.prefix = prefix;
        e.initializeAllLiveAttributes();
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        SVGOMElement e = (SVGOMElement)n;
        e.prefix = prefix;
        e.initializeAllLiveAttributes();
        return n;
    }
    protected class UnitContext implements UnitProcessor.Context {
        public Element getElement() {
            return SVGOMElement.this;
        }
        public float getPixelUnitToMillimeter() {
            return getSVGContext().getPixelUnitToMillimeter();
        }
        public float getPixelToMM() {
            return getPixelUnitToMillimeter();
        }
        public float getFontSize() {
            return getSVGContext().getFontSize();
        }
        public float getXHeight() {
            return 0.5f;
        }
        public float getViewportWidth() {
            return getSVGContext().getViewportWidth();
        }
        public float getViewportHeight() {
            return getSVGContext().getViewportHeight();
        }
    }
}
