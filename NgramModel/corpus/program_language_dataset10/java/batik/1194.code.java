package org.apache.batik.svggen;
import java.awt.geom.Line2D;
import org.w3c.dom.Element;
public class SVGLine extends SVGGraphicObjectConverter {
    public SVGLine(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public Element toSVG(Line2D line) {
        Element svgLine =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_LINE_TAG);
        svgLine.setAttributeNS
            (null, SVG_X1_ATTRIBUTE, doubleString(line.getX1()));
        svgLine.setAttributeNS
            (null, SVG_Y1_ATTRIBUTE, doubleString(line.getY1()));
        svgLine.setAttributeNS
            (null, SVG_X2_ATTRIBUTE, doubleString(line.getX2()));
        svgLine.setAttributeNS
            (null, SVG_Y2_ATTRIBUTE, doubleString(line.getY2()));
        return svgLine;
    }
}
