package org.apache.batik.svggen;
import java.awt.BasicStroke;
import org.apache.batik.ext.awt.g2d.GraphicContext;
public class SVGBasicStroke extends AbstractSVGConverter{
    public SVGBasicStroke(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc){
        if(gc.getStroke() instanceof BasicStroke)
            return toSVG((BasicStroke)gc.getStroke());
        else
            return null;
    }
    public final SVGStrokeDescriptor toSVG(BasicStroke stroke)
    {
        String strokeWidth = doubleString(stroke.getLineWidth());
        String capStyle = endCapToSVG(stroke.getEndCap());
        String joinStyle = joinToSVG(stroke.getLineJoin());
        String miterLimit = doubleString(stroke.getMiterLimit());
        float[] array = stroke.getDashArray();
        String dashArray = null;
        if(array != null)
            dashArray = dashArrayToSVG(array);
        else
            dashArray = SVG_NONE_VALUE;
        String dashOffset = doubleString(stroke.getDashPhase());
        return new SVGStrokeDescriptor(strokeWidth, capStyle,
                                       joinStyle, miterLimit,
                                       dashArray, dashOffset);
    }
    private final String dashArrayToSVG(float[] dashArray){
        StringBuffer dashArrayBuf = new StringBuffer( dashArray.length * 8 );
        if(dashArray.length > 0)
            dashArrayBuf.append(doubleString(dashArray[0]));
        for(int i=1; i<dashArray.length; i++){
            dashArrayBuf.append(COMMA);
            dashArrayBuf.append(doubleString(dashArray[i]));
        }
        return dashArrayBuf.toString();
    }
    private static String joinToSVG(int lineJoin){
        switch(lineJoin){
        case BasicStroke.JOIN_BEVEL:
            return SVG_BEVEL_VALUE;
        case BasicStroke.JOIN_ROUND:
            return SVG_ROUND_VALUE;
        case BasicStroke.JOIN_MITER:
        default:
            return SVG_MITER_VALUE;
        }
    }
    private static String endCapToSVG(int endCap){
        switch(endCap){
        case BasicStroke.CAP_BUTT:
            return SVG_BUTT_VALUE;
        case BasicStroke.CAP_ROUND:
            return SVG_ROUND_VALUE;
        default:
        case BasicStroke.CAP_SQUARE:
            return SVG_SQUARE_VALUE;
        }
    }
}
