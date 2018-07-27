package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.svg.SVGAnimatedPoints;
import org.w3c.dom.svg.SVGPointList;
public abstract class SVGPointShapeElement
    extends    SVGGraphicsElement
    implements SVGAnimatedPoints {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_POINTS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_POINTS_VALUE));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedPoints points;
    protected SVGPointShapeElement() {
    }
    public SVGPointShapeElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        points = createLiveAnimatedPoints(null, SVG_POINTS_ATTRIBUTE, "");
    }
    public SVGOMAnimatedPoints getSVGOMAnimatedPoints() {
        return points;
    }
    public SVGPointList getPoints() {
        return points.getPoints();
    }
    public SVGPointList getAnimatedPoints() {
        return points.getAnimatedPoints();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
