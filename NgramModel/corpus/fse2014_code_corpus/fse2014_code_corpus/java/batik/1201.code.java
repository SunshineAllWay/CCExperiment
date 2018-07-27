package org.apache.batik.svggen;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import org.w3c.dom.Element;
public class SVGRectangle extends SVGGraphicObjectConverter {
    private SVGLine svgLine;
    public SVGRectangle(SVGGeneratorContext generatorContext) {
        super(generatorContext);
        svgLine = new SVGLine(generatorContext);
    }
    public Element toSVG(Rectangle2D rect) {
        return toSVG((RectangularShape)rect);
    }
    public Element toSVG(RoundRectangle2D rect) {
        Element svgRect = toSVG((RectangularShape)rect);
        if(svgRect != null && svgRect.getTagName() == SVG_RECT_TAG){
            svgRect.setAttributeNS(null, SVG_RX_ATTRIBUTE,
                                   doubleString(Math.abs(rect.getArcWidth()/2)));
            svgRect.setAttributeNS(null, SVG_RY_ATTRIBUTE,
                                   doubleString(Math.abs(rect.getArcHeight()/2)));
        }
        return svgRect;
    }
    private Element toSVG(RectangularShape rect) {
        if(rect.getWidth() > 0 && rect.getHeight() > 0){
            Element svgRect =
                generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                            SVG_RECT_TAG);
            svgRect.setAttributeNS(null, SVG_X_ATTRIBUTE, doubleString(rect.getX()));
            svgRect.setAttributeNS(null, SVG_Y_ATTRIBUTE, doubleString(rect.getY()));
            svgRect.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,
                                   doubleString(rect.getWidth()));
            svgRect.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE,
                                   doubleString(rect.getHeight()));
            return svgRect;
        }
        else{
            if(rect.getWidth() == 0 && rect.getHeight() > 0){
                Line2D line = new Line2D.Double(rect.getX(), rect.getY(), rect.getX(), 
                                                rect.getY() + rect.getHeight());
                return svgLine.toSVG(line);
            }
            else if(rect.getWidth() > 0 && rect.getHeight() == 0){
                Line2D line = new Line2D.Double(rect.getX(), rect.getY(),
                                                rect.getX() + rect.getWidth(),
                                                rect.getY());
                return svgLine.toSVG(line);
            }
            return null;
        }
    }
}
