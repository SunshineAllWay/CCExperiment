package org.apache.batik.svggen;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import org.w3c.dom.Element;
public class SVGArc extends SVGGraphicObjectConverter {
    private SVGLine svgLine;
    private SVGEllipse svgEllipse;
    public SVGArc(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public Element toSVG(Arc2D arc) {
        double ext    = arc.getAngleExtent();
        double width  = arc.getWidth();
        double height = arc.getHeight();
        if (width == 0 || height == 0) {
            Line2D line = new Line2D.Double
                (arc.getX(), arc.getY(),
                 arc.getX() + width,
                 arc.getY() + height);
            if (svgLine == null) {
                svgLine = new SVGLine(generatorContext);
            }
            return svgLine.toSVG(line);
        }
        if (ext >= 360 || ext <= -360) {
            Ellipse2D ellipse = new Ellipse2D.Double
                (arc.getX(), arc.getY(), width, height);
            if (svgEllipse == null) {
                svgEllipse = new SVGEllipse(generatorContext);
            }
            return svgEllipse.toSVG(ellipse);
        }
        Element svgPath = generatorContext.domFactory.createElementNS
            (SVG_NAMESPACE_URI, SVG_PATH_TAG);
        StringBuffer d = new StringBuffer( 64 );
        Point2D startPt = arc.getStartPoint();
        Point2D endPt   = arc.getEndPoint();
        int     type    = arc.getArcType();
        d.append(PATH_MOVE);
        d.append(doubleString(startPt.getX()));
        d.append(SPACE);
        d.append(doubleString(startPt.getY()));
        d.append(SPACE);
        d.append(PATH_ARC);
        d.append(doubleString(width / 2));
        d.append(SPACE);
        d.append(doubleString(height / 2));
        d.append(SPACE);
        d.append( '0' );  
        d.append(SPACE);
        if (ext > 0) {
            if (ext > 180)  d.append( '1' );  
            else            d.append( '0' );  
            d.append(SPACE);
            d.append( '0' );  
        } else {
            if (ext < -180)  d.append( '1' );  
            else             d.append( '0' );  
            d.append(SPACE);
            d.append( '1' );  
        }
        d.append(SPACE);
        d.append(doubleString(endPt.getX()));
        d.append(SPACE);
        d.append(doubleString(endPt.getY()));
        if (type == Arc2D.CHORD) {
            d.append(PATH_CLOSE);
        } else if (type == Arc2D.PIE) {
            double cx = arc.getX() + width / 2;
            double cy = arc.getY() + height / 2;
            d.append(PATH_LINE_TO);
            d.append(SPACE);
            d.append(doubleString(cx));
            d.append(SPACE);
            d.append(doubleString(cy));
            d.append(SPACE);
            d.append(PATH_CLOSE);
        }
        svgPath.setAttributeNS(null, SVG_D_ATTRIBUTE, d.toString());
        return svgPath;
    }
}
