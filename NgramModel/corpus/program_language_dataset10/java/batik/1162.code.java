package org.apache.batik.svggen;
import java.awt.AlphaComposite;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;
public class SVGAlphaComposite extends AbstractSVGConverter {
    private Map compositeDefsMap = new HashMap();
    private boolean backgroundAccessRequired = false;
    public SVGAlphaComposite(SVGGeneratorContext generatorContext) {
        super(generatorContext);
        compositeDefsMap.put(AlphaComposite.Src,
                             compositeToSVG(AlphaComposite.Src));
        compositeDefsMap.put(AlphaComposite.SrcIn,
                             compositeToSVG(AlphaComposite.SrcIn));
        compositeDefsMap.put(AlphaComposite.SrcOut,
                             compositeToSVG(AlphaComposite.SrcOut));
        compositeDefsMap.put(AlphaComposite.DstIn,
                             compositeToSVG(AlphaComposite.DstIn));
        compositeDefsMap.put(AlphaComposite.DstOut,
                             compositeToSVG(AlphaComposite.DstOut));
        compositeDefsMap.put(AlphaComposite.DstOver,
                             compositeToSVG(AlphaComposite.DstOver));
        compositeDefsMap.put(AlphaComposite.Clear,
                             compositeToSVG(AlphaComposite.Clear));
    }
    public List getAlphaCompositeFilterSet() {
        return new LinkedList(compositeDefsMap.values());
    }
    public boolean requiresBackgroundAccess() {
        return backgroundAccessRequired;
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        return toSVG((AlphaComposite)gc.getComposite());
    }
    public SVGCompositeDescriptor toSVG(AlphaComposite composite) {
        SVGCompositeDescriptor compositeDesc =
            (SVGCompositeDescriptor)descMap.get(composite);
        if(compositeDesc == null){
            String opacityValue = doubleString(composite.getAlpha());
            String filterValue = null;
            Element filterDef = null;
            if(composite.getRule() != AlphaComposite.SRC_OVER){
                AlphaComposite majorComposite =
                    AlphaComposite.getInstance(composite.getRule());
                filterDef = (Element)compositeDefsMap.get(majorComposite);
                defSet.add(filterDef);
                StringBuffer filterAttrBuf = new StringBuffer(URL_PREFIX);
                filterAttrBuf.append(SIGN_POUND);
                filterAttrBuf.append(filterDef.getAttributeNS(null, SVG_ID_ATTRIBUTE));
                filterAttrBuf.append(URL_SUFFIX);
                filterValue = filterAttrBuf.toString();
            } else
                filterValue = SVG_NONE_VALUE;
            compositeDesc = new SVGCompositeDescriptor(opacityValue, filterValue,
                                                       filterDef);
            descMap.put(composite, compositeDesc);
        }
        if (composite.getRule() != AlphaComposite.SRC_OVER)
            backgroundAccessRequired = true;
        return compositeDesc;
    }
    private Element compositeToSVG(AlphaComposite composite) {
        String operator = null;
        String input1 = null;
        String input2 = null;
        String k2 = "0";
        String id = null;
        switch(composite.getRule()){
        case AlphaComposite.CLEAR:
            operator = SVG_ARITHMETIC_VALUE;
            input1 = SVG_SOURCE_GRAPHIC_VALUE;
            input2 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_CLEAR;
            break;
        case AlphaComposite.SRC:
            operator = SVG_ARITHMETIC_VALUE;
            input1 = SVG_SOURCE_GRAPHIC_VALUE;
            input2 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_SRC;
            k2 = SVG_DIGIT_ONE_VALUE;
            break;
        case AlphaComposite.SRC_IN:
            operator = SVG_IN_VALUE;
            input1 = SVG_SOURCE_GRAPHIC_VALUE;
            input2 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_SRC_IN;
            break;
        case AlphaComposite.SRC_OUT:
            operator = SVG_OUT_VALUE;
            input1 = SVG_SOURCE_GRAPHIC_VALUE;
            input2 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_SRC_OUT;
            break;
        case AlphaComposite.DST_IN:
            operator = SVG_IN_VALUE;
            input2 = SVG_SOURCE_GRAPHIC_VALUE;
            input1 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_DST_IN;
            break;
        case AlphaComposite.DST_OUT:
            operator = SVG_OUT_VALUE;
            input2 = SVG_SOURCE_GRAPHIC_VALUE;
            input1 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_DST_OUT;
            break;
        case AlphaComposite.DST_OVER:
            operator = SVG_OVER_VALUE;
            input2 = SVG_SOURCE_GRAPHIC_VALUE;
            input1 = SVG_BACKGROUND_IMAGE_VALUE;
            id = ID_PREFIX_ALPHA_COMPOSITE_DST_OVER;
            break;
        default:
            throw new Error("invalid rule:" + composite.getRule() );
        }
        Element compositeFilter =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_FILTER_TAG);
        compositeFilter.setAttributeNS(null, SVG_ID_ATTRIBUTE, id);
        compositeFilter.setAttributeNS(null, SVG_FILTER_UNITS_ATTRIBUTE,
                                     SVG_OBJECT_BOUNDING_BOX_VALUE);
        compositeFilter.setAttributeNS(null, SVG_X_ATTRIBUTE, SVG_ZERO_PERCENT_VALUE);
        compositeFilter.setAttributeNS(null, SVG_Y_ATTRIBUTE, SVG_ZERO_PERCENT_VALUE);
        compositeFilter.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,
                                       SVG_HUNDRED_PERCENT_VALUE);
        compositeFilter.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE,
                                       SVG_HUNDRED_PERCENT_VALUE);
        Element feComposite =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_FE_COMPOSITE_TAG);
        feComposite.setAttributeNS(null, SVG_OPERATOR_ATTRIBUTE, operator);
        feComposite.setAttributeNS(null, SVG_IN_ATTRIBUTE, input1);
        feComposite.setAttributeNS(null, SVG_IN2_ATTRIBUTE, input2);
        feComposite.setAttributeNS(null, SVG_K2_ATTRIBUTE, k2);
        feComposite.setAttributeNS(null, SVG_RESULT_ATTRIBUTE, SVG_COMPOSITE_VALUE);
        Element feFlood =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_FE_FLOOD_TAG);
        feFlood.setAttributeNS(null, SVG_FLOOD_COLOR_ATTRIBUTE, "white");
        feFlood.setAttributeNS(null, SVG_FLOOD_OPACITY_ATTRIBUTE, "1");
        feFlood.setAttributeNS(null, SVG_RESULT_ATTRIBUTE, SVG_FLOOD_VALUE);
        Element feMerge =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_FE_MERGE_TAG);
        Element feMergeNodeFlood =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_FE_MERGE_NODE_TAG);
        feMergeNodeFlood.setAttributeNS(null, SVG_IN_ATTRIBUTE, SVG_FLOOD_VALUE);
        Element feMergeNodeComposite =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_FE_MERGE_NODE_TAG);
        feMergeNodeComposite.setAttributeNS(null, SVG_IN_ATTRIBUTE,
                                            SVG_COMPOSITE_VALUE);
        feMerge.appendChild(feMergeNodeFlood);
        feMerge.appendChild(feMergeNodeComposite);
        compositeFilter.appendChild(feFlood);
        compositeFilter.appendChild(feComposite);
        compositeFilter.appendChild(feMerge);
        return compositeFilter;
    }
}
