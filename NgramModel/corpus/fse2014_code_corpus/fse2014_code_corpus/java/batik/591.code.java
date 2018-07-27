package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFETileElement;
public class SVGOMFETileElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFETileElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedString in;
    protected SVGOMFETileElement() {
    }
    public SVGOMFETileElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        in = createLiveAnimatedString(null, SVG_IN_ATTRIBUTE);
    }
    public String getLocalName() {
        return SVG_FE_TILE_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    protected Node newNode() {
        return new SVGOMFETileElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
