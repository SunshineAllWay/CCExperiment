package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGURIReference;
public abstract class SVGURIReferenceTextPositioningElement
    extends    SVGOMTextPositioningElement
    implements SVGURIReference {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMTextPositioningElement.xmlTraitInformation);
        t.put(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_URI));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedString href;
    protected SVGURIReferenceTextPositioningElement() {
    }
    protected SVGURIReferenceTextPositioningElement(String prefix,
                                                    AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        href =
            createLiveAnimatedString(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE);
    }
    public SVGAnimatedString getHref() {
        return href;
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
