package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGClipPathElement;
public class SVGOMClipPathElement
    extends    SVGGraphicsElement
    implements SVGClipPathElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_CLIP_PATH_UNITS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        xmlTraitInformation = t;
    }
    protected static final String[] CLIP_PATH_UNITS_VALUES = {
        "",
        SVG_USER_SPACE_ON_USE_VALUE,
        SVG_OBJECT_BOUNDING_BOX_VALUE
    };
    protected SVGOMAnimatedEnumeration clipPathUnits;
    protected SVGOMClipPathElement() {
    }
    public SVGOMClipPathElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        clipPathUnits =
            createLiveAnimatedEnumeration
                (null, SVG_CLIP_PATH_UNITS_ATTRIBUTE, CLIP_PATH_UNITS_VALUES,
                 (short) 1);
    }
    public String getLocalName() {
        return SVG_CLIP_PATH_TAG;
    }
    public SVGAnimatedEnumeration getClipPathUnits() {
        return clipPathUnits;
    }
    protected Node newNode() {
        return new SVGOMClipPathElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
