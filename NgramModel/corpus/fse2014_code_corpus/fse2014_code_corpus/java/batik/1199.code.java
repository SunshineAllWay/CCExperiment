package org.apache.batik.svggen;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import org.w3c.dom.Element;
public class SVGPath extends SVGGraphicObjectConverter {
    public SVGPath(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public Element toSVG(Shape path) {
        String dAttr = toSVGPathData(path, generatorContext);
        if (dAttr==null || dAttr.length() == 0){
            return null;
        }
        Element svgPath = generatorContext.domFactory.createElementNS
            (SVG_NAMESPACE_URI, SVG_PATH_TAG);
        svgPath.setAttributeNS(null, SVG_D_ATTRIBUTE, dAttr);
        if (path.getPathIterator(null).getWindingRule() == GeneralPath.WIND_EVEN_ODD)
            svgPath.setAttributeNS(null, SVG_FILL_RULE_ATTRIBUTE, SVG_EVEN_ODD_VALUE);
        return svgPath;
    }
     public static String toSVGPathData(Shape path, SVGGeneratorContext gc) {
        StringBuffer d = new StringBuffer( 40 );
        PathIterator pi = path.getPathIterator(null);
        float[] seg = new float[6];
        int segType = 0;
        while (!pi.isDone()) {
            segType = pi.currentSegment(seg);
            switch(segType) {
            case PathIterator.SEG_MOVETO:
                d.append(PATH_MOVE);
                appendPoint(d, seg[0], seg[1], gc);
                break;
            case PathIterator.SEG_LINETO:
                d.append(PATH_LINE_TO);
                appendPoint(d, seg[0], seg[1], gc);
                break;
            case PathIterator.SEG_CLOSE:
                d.append(PATH_CLOSE);
                break;
            case PathIterator.SEG_QUADTO:
                d.append(PATH_QUAD_TO);
                appendPoint(d, seg[0], seg[1], gc);
                appendPoint(d, seg[2], seg[3], gc);
                break;
            case PathIterator.SEG_CUBICTO:
                d.append(PATH_CUBIC_TO);
                appendPoint(d, seg[0], seg[1], gc);
                appendPoint(d, seg[2], seg[3], gc);
                appendPoint(d, seg[4], seg[5], gc);
                break;
            default:
                throw new Error("invalid segmentType:" + segType );
            }
            pi.next();
        } 
        if (d.length() > 0)
            return d.toString().trim();
        else {
            return "";
        }
    }
    private static void appendPoint(StringBuffer d, float x, float y, SVGGeneratorContext gc) {
        d.append(gc.doubleString(x));
        d.append(SPACE);
        d.append(gc.doubleString(y));
        d.append(SPACE);
    }
}
