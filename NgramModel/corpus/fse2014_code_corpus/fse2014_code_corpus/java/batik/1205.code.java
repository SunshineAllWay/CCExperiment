package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
public class SVGStrokeDescriptor implements SVGDescriptor, SVGSyntax{
    private String strokeWidth;
    private String capStyle;
    private String joinStyle;
    private String miterLimit;
    private String dashArray;
    private String dashOffset;
    public SVGStrokeDescriptor(String strokeWidth, String capStyle,
                               String joinStyle, String miterLimit,
                               String dashArray, String dashOffset){
        if(strokeWidth == null ||
           capStyle == null    ||
           joinStyle == null   ||
           miterLimit == null  ||
           dashArray == null   ||
           dashOffset == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_STROKE_NULL);
        this.strokeWidth = strokeWidth;
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;
        this.miterLimit = miterLimit;
        this.dashArray = dashArray;
        this.dashOffset = dashOffset;
    }
    String getStrokeWidth(){ return strokeWidth; }
    String getCapStyle(){ return capStyle; }
    String getJoinStyle(){ return joinStyle; }
    String getMiterLimit(){ return miterLimit; }
    String getDashArray(){ return dashArray; }
    String getDashOffset(){ return dashOffset; }
    public Map getAttributeMap(Map attrMap){
        if(attrMap == null)
            attrMap = new HashMap();
        attrMap.put(SVG_STROKE_WIDTH_ATTRIBUTE, strokeWidth);
        attrMap.put(SVG_STROKE_LINECAP_ATTRIBUTE, capStyle);
        attrMap.put(SVG_STROKE_LINEJOIN_ATTRIBUTE, joinStyle);
        attrMap.put(SVG_STROKE_MITERLIMIT_ATTRIBUTE, miterLimit);
        attrMap.put(SVG_STROKE_DASHARRAY_ATTRIBUTE, dashArray);
        attrMap.put(SVG_STROKE_DASHOFFSET_ATTRIBUTE, dashOffset);
        return attrMap;
    }
    public List getDefinitionSet(List defSet){
        if(defSet == null)
            defSet = new LinkedList();
        return defSet;
    }
}
