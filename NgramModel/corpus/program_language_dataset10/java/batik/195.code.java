package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.SpecularLightingRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeSpecularLightingElementBridge
    extends AbstractSVGLightingElementBridge {
    public SVGFeSpecularLightingElementBridge() {}
    public String getLocalName() {
        return SVG_FE_SPECULAR_LIGHTING_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        float surfaceScale = convertNumber(filterElement,
                                           SVG_SURFACE_SCALE_ATTRIBUTE, 1, ctx);
        float specularConstant = convertNumber
            (filterElement, SVG_SPECULAR_CONSTANT_ATTRIBUTE, 1, ctx);
        float specularExponent = convertSpecularExponent(filterElement, ctx);
        Light light = extractLight(filterElement, ctx);
        double[] kernelUnitLength = convertKernelUnitLength(filterElement, ctx);
        Filter in = getIn(filterElement,
                          filteredElement,
                          filteredNode,
                          inputFilter,
                          filterMap,
                          ctx);
        if (in == null) {
            return null; 
        }
        Rectangle2D defaultRegion = in.getBounds2D();
        Rectangle2D primitiveRegion
            = SVGUtilities.convertFilterPrimitiveRegion(filterElement,
                                                        filteredElement,
                                                        filteredNode,
                                                        defaultRegion,
                                                        filterRegion,
                                                        ctx);
        Filter filter = new  SpecularLightingRable8Bit(in,
                                                       primitiveRegion,
                                                       light,
                                                       specularConstant,
                                                       specularExponent,
                                                       surfaceScale,
                                                       kernelUnitLength);
        handleColorInterpolationFilters(filter, filterElement);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
    protected static float convertSpecularExponent(Element filterElement,
                                                   BridgeContext ctx) {
        String s = filterElement.getAttributeNS
            (null, SVG_SPECULAR_EXPONENT_ATTRIBUTE);
        if (s.length() == 0) {
            return 1; 
        } else {
            try {
                float v = SVGUtilities.convertSVGNumber(s);
                if (v < 1 || v > 128) {
                    throw new BridgeException
                        (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                         new Object[] {SVG_SPECULAR_CONSTANT_ATTRIBUTE, s});
                }
                return v;
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_SPECULAR_CONSTANT_ATTRIBUTE, s, nfEx });
            }
        }
    }
}
