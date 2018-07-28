package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEOffsetElement;
public class SVGOMFEOffsetElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEOffsetElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_DX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_DY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedNumber dx;
    protected SVGOMAnimatedNumber dy;
    protected SVGOMFEOffsetElement() {
    }
    public SVGOMFEOffsetElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        in = createLiveAnimatedString(null, SVG_IN_ATTRIBUTE);
        dx = createLiveAnimatedNumber(null, SVG_DX_ATTRIBUTE, 0f);
        dy = createLiveAnimatedNumber(null, SVG_DY_ATTRIBUTE, 0f);
    }
    public String getLocalName() {
        return SVG_FE_OFFSET_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedNumber getDx() {
        return dx;
    } 
    public SVGAnimatedNumber getDy() {
        return dy;
    }
    protected Node newNode() {
        return new SVGOMFEOffsetElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
