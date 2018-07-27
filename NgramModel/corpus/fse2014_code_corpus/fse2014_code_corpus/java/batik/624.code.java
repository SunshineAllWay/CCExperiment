package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGRectElement;
public class SVGOMRectElement
    extends    SVGGraphicsElement
    implements SVGRectElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_RX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_RY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_WIDTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_HEIGHT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedLength x;
    protected SVGOMAnimatedLength y;
    protected AbstractSVGAnimatedLength rx;
    protected AbstractSVGAnimatedLength ry;
    protected SVGOMAnimatedLength width;
    protected SVGOMAnimatedLength height;
    protected SVGOMRectElement() {
    }
    public SVGOMRectElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        x = createLiveAnimatedLength
            (null, SVG_X_ATTRIBUTE, SVG_RECT_X_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        y = createLiveAnimatedLength
            (null, SVG_Y_ATTRIBUTE, SVG_RECT_Y_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        width =
            createLiveAnimatedLength
                (null, SVG_WIDTH_ATTRIBUTE, null,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
        height =
            createLiveAnimatedLength
                (null, SVG_HEIGHT_ATTRIBUTE, null,
                 SVGOMAnimatedLength.VERTICAL_LENGTH, true);
        rx = new AbstractSVGAnimatedLength
            (this, null, SVG_RX_ATTRIBUTE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, true) {
                protected String getDefaultValue() {
                    Attr attr = getAttributeNodeNS(null, SVG_RY_ATTRIBUTE);
                    if (attr == null) {
                        return "0";
                    }
                    return attr.getValue();
                }
                protected void attrChanged() {
                    super.attrChanged();
                    AbstractSVGAnimatedLength ry =
                        (AbstractSVGAnimatedLength) getRy();
                    if (isSpecified() && !ry.isSpecified()) {
                        ry.attrChanged();
                    }
                }
            };
        ry = new AbstractSVGAnimatedLength
            (this, null, SVG_RY_ATTRIBUTE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, true) {
                protected String getDefaultValue() {
                    Attr attr = getAttributeNodeNS(null, SVG_RX_ATTRIBUTE);
                    if (attr == null) {
                        return "0";
                    }
                    return attr.getValue();
                }
                protected void attrChanged() {
                    super.attrChanged();
                    AbstractSVGAnimatedLength rx =
                        (AbstractSVGAnimatedLength) getRx();
                    if (isSpecified() && !rx.isSpecified()) {
                        rx.attrChanged();
                    }
                }
            };
        liveAttributeValues.put(null, SVG_RX_ATTRIBUTE, rx);
        liveAttributeValues.put(null, SVG_RY_ATTRIBUTE, ry);
        AnimatedAttributeListener l =
            ((SVGOMDocument) ownerDocument).getAnimatedAttributeListener();
        rx.addAnimatedAttributeListener(l);
        ry.addAnimatedAttributeListener(l);
    }
    public String getLocalName() {
        return SVG_RECT_TAG;
    }
    public SVGAnimatedLength getX() {
        return x;
    }
    public SVGAnimatedLength getY() {
        return y;
    }
    public SVGAnimatedLength getWidth() {
        return width;
    }
    public SVGAnimatedLength getHeight() {
        return height;
    }
    public SVGAnimatedLength getRx() {
        return rx;
    }
    public SVGAnimatedLength getRy() {
        return ry;
    }
    protected Node newNode() {
        return new SVGOMRectElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
    public void updateAttributeValue(String ns, String ln,
                                     AnimatableValue val) {
        if (ns == null) {
            if (ln.equals(SVG_RX_ATTRIBUTE)) {
                super.updateAttributeValue(ns, ln, val);
                AbstractSVGAnimatedLength ry =
                    (AbstractSVGAnimatedLength) getRy();
                if (!ry.isSpecified()) {
                    super.updateAttributeValue(ns, SVG_RY_ATTRIBUTE, val);
                }
                return;
            } else if (ln.equals(SVG_RY_ATTRIBUTE)) {
                super.updateAttributeValue(ns, ln, val);
                AbstractSVGAnimatedLength rx =
                    (AbstractSVGAnimatedLength) getRx();
                if (!rx.isSpecified()) {
                    super.updateAttributeValue(ns, SVG_RX_ATTRIBUTE, val);
                }
                return;
            }
        }
        super.updateAttributeValue(ns, ln, val);
    }
}
