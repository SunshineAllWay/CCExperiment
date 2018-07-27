package org.apache.batik.svggen;
import java.util.HashSet;
import java.util.Set;
public class SVGStylingAttributes implements SVGSyntax{
    static Set attrSet = new HashSet();
    static {
        attrSet.add(SVG_CLIP_PATH_ATTRIBUTE);
        attrSet.add(SVG_COLOR_INTERPOLATION_ATTRIBUTE);
        attrSet.add(SVG_COLOR_RENDERING_ATTRIBUTE);
        attrSet.add(SVG_ENABLE_BACKGROUND_ATTRIBUTE);
        attrSet.add(SVG_FILL_ATTRIBUTE);
        attrSet.add(SVG_FILL_OPACITY_ATTRIBUTE);
        attrSet.add(SVG_FILL_RULE_ATTRIBUTE);
        attrSet.add(SVG_FILTER_ATTRIBUTE);
        attrSet.add(SVG_FLOOD_COLOR_ATTRIBUTE);
        attrSet.add(SVG_FLOOD_OPACITY_ATTRIBUTE);
        attrSet.add(SVG_FONT_FAMILY_ATTRIBUTE);
        attrSet.add(SVG_FONT_SIZE_ATTRIBUTE);
        attrSet.add(SVG_FONT_WEIGHT_ATTRIBUTE);
        attrSet.add(SVG_FONT_STYLE_ATTRIBUTE);
        attrSet.add(SVG_IMAGE_RENDERING_ATTRIBUTE);
        attrSet.add(SVG_MASK_ATTRIBUTE);
        attrSet.add(SVG_OPACITY_ATTRIBUTE);
        attrSet.add(SVG_SHAPE_RENDERING_ATTRIBUTE);
        attrSet.add(SVG_STOP_COLOR_ATTRIBUTE);
        attrSet.add(SVG_STOP_OPACITY_ATTRIBUTE);
        attrSet.add(SVG_STROKE_ATTRIBUTE);
        attrSet.add(SVG_STROKE_OPACITY_ATTRIBUTE);
        attrSet.add(SVG_STROKE_DASHARRAY_ATTRIBUTE);
        attrSet.add(SVG_STROKE_DASHOFFSET_ATTRIBUTE);
        attrSet.add(SVG_STROKE_LINECAP_ATTRIBUTE);
        attrSet.add(SVG_STROKE_LINEJOIN_ATTRIBUTE);
        attrSet.add(SVG_STROKE_MITERLIMIT_ATTRIBUTE);
        attrSet.add(SVG_STROKE_WIDTH_ATTRIBUTE);
        attrSet.add(SVG_TEXT_RENDERING_ATTRIBUTE);
    }
    public static final Set set = attrSet;
}
