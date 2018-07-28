package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGStopElement;
public class SVGOMStopElement
    extends    SVGStylableElement
    implements SVGStopElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
        t.put(null, SVG_OFFSET_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_OR_PERCENTAGE));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedNumber offset;
    protected SVGOMStopElement() {
    }
    public SVGOMStopElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        offset = createLiveAnimatedNumber(null, SVG_OFFSET_ATTRIBUTE, 0f, true);
    }
    public String getLocalName() {
        return SVG_STOP_TAG;
    }
    public SVGAnimatedNumber getOffset() {
        return offset;
    }
    protected Node newNode() {
        return new SVGOMStopElement();
    }    
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
