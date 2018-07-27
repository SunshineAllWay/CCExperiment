package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
public class SVGHintsDescriptor implements SVGDescriptor, SVGSyntax {
    private String colorInterpolation;
    private String colorRendering;
    private String textRendering;
    private String shapeRendering;
    private String imageRendering;
    public SVGHintsDescriptor(String colorInterpolation,
                              String colorRendering,
                              String textRendering,
                              String shapeRendering,
                              String imageRendering){
        if(colorInterpolation == null ||
           colorRendering == null ||
           textRendering == null ||
           shapeRendering == null ||
           imageRendering == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_HINT_NULL);
        this.colorInterpolation = colorInterpolation;
        this.colorRendering = colorRendering;
        this.textRendering = textRendering;
        this.shapeRendering = shapeRendering;
        this.imageRendering = imageRendering;
    }
    public Map getAttributeMap(Map attrMap) {
        if (attrMap == null)
            attrMap = new HashMap();
        attrMap.put(SVG_COLOR_INTERPOLATION_ATTRIBUTE, colorInterpolation);
        attrMap.put(SVG_COLOR_RENDERING_ATTRIBUTE, colorRendering);
        attrMap.put(SVG_TEXT_RENDERING_ATTRIBUTE, textRendering);
        attrMap.put(SVG_SHAPE_RENDERING_ATTRIBUTE, shapeRendering);
        attrMap.put(SVG_IMAGE_RENDERING_ATTRIBUTE, imageRendering);
        return attrMap;
    }
    public List getDefinitionSet(List defSet) {
        if (defSet == null)
            defSet = new LinkedList();
        return defSet;
    }
}
