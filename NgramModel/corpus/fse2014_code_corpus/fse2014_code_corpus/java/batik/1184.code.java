package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
public class SVGFontDescriptor implements SVGDescriptor, SVGSyntax {
    private Element def;
    private String fontSize;
    private String fontWeight;
    private String fontStyle;
    private String fontFamily;
    public SVGFontDescriptor(String fontSize,
                             String fontWeight,
                             String fontStyle,
                             String fontFamily,
                             Element def){
        if (fontSize == null ||
            fontWeight == null ||
            fontStyle == null ||
            fontFamily == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_FONT_NULL);
        this.fontSize = fontSize;
        this.fontWeight = fontWeight;
        this.fontStyle = fontStyle;
        this.fontFamily = fontFamily;
        this.def = def;
    }
    public Map getAttributeMap(Map attrMap){
        if(attrMap == null)
            attrMap = new HashMap();
        attrMap.put(SVG_FONT_SIZE_ATTRIBUTE, fontSize);
        attrMap.put(SVG_FONT_WEIGHT_ATTRIBUTE, fontWeight);
        attrMap.put(SVG_FONT_STYLE_ATTRIBUTE, fontStyle);
        attrMap.put(SVG_FONT_FAMILY_ATTRIBUTE, fontFamily);
        return attrMap;
    }
    public Element getDef(){
        return def;
    }
    public List getDefinitionSet(List defSet){
        if (defSet == null)
            defSet = new LinkedList();
        if(def != null && !defSet.contains(def))
            defSet.add(def);
        return defSet;
    }
}
