package org.apache.batik.svggen;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import org.w3c.dom.Element;
public class SVGEllipse extends SVGGraphicObjectConverter {
    private SVGLine svgLine;
    public SVGEllipse(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public Element toSVG(Ellipse2D ellipse) {
        if(ellipse.getWidth() < 0 || ellipse.getHeight() < 0){
            return null;
        }
        if(ellipse.getWidth() == ellipse.getHeight())
            return toSVGCircle(ellipse);
        else
            return toSVGEllipse(ellipse);
    }
    private Element toSVGCircle(Ellipse2D ellipse){
        Element svgCircle =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_CIRCLE_TAG);
        svgCircle.setAttributeNS(null, SVG_CX_ATTRIBUTE,
                                 doubleString(ellipse.getX() +
                                              ellipse.getWidth()/2));
        svgCircle.setAttributeNS(null, SVG_CY_ATTRIBUTE,
                                 doubleString(ellipse.getY() +
                                              ellipse.getHeight()/2));
        svgCircle.setAttributeNS(null, SVG_R_ATTRIBUTE,
                                 doubleString(ellipse.getWidth()/2));
        return svgCircle;
    }
    private Element toSVGEllipse(Ellipse2D ellipse){
        if(ellipse.getWidth() > 0 && ellipse.getHeight() > 0){
            Element svgCircle =
                generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                            SVG_ELLIPSE_TAG);
            svgCircle.setAttributeNS(null, SVG_CX_ATTRIBUTE,
                                     doubleString(ellipse.getX() +
                                                  ellipse.getWidth()/2));
            svgCircle.setAttributeNS(null, SVG_CY_ATTRIBUTE,
                                     doubleString(ellipse.getY() +
                                                  ellipse.getHeight()/2));
            svgCircle.setAttributeNS(null, SVG_RX_ATTRIBUTE,
                                     doubleString(ellipse.getWidth()/2));
            svgCircle.setAttributeNS(null, SVG_RY_ATTRIBUTE,
                                     doubleString(ellipse.getHeight()/2));
            return svgCircle;
        }
        else if(ellipse.getWidth() == 0 && ellipse.getHeight() > 0){
            Line2D line = new Line2D.Double(ellipse.getX(), ellipse.getY(), ellipse.getX(), 
                                            ellipse.getY() + ellipse.getHeight());
            if (svgLine == null)
                svgLine = new SVGLine(generatorContext);
            return svgLine.toSVG(line);
        }
        else if(ellipse.getWidth() > 0 && ellipse.getHeight() == 0){
            Line2D line = new Line2D.Double(ellipse.getX(), ellipse.getY(),
                                            ellipse.getX() + ellipse.getWidth(),
                                            ellipse.getY());
            if (svgLine == null)
                svgLine = new SVGLine(generatorContext);
            return svgLine.toSVG(line);
        }
        return null;
    }
}
