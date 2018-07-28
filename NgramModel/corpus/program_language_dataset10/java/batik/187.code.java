package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.renderable.DiffuseLightingRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeDiffuseLightingElementBridge
    extends AbstractSVGLightingElementBridge {
    public SVGFeDiffuseLightingElementBridge() {}
    public String getLocalName() {
        return SVG_FE_DIFFUSE_LIGHTING_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        float surfaceScale
            = convertNumber(filterElement, SVG_SURFACE_SCALE_ATTRIBUTE, 1, ctx);
        float diffuseConstant
            = convertNumber(filterElement, SVG_DIFFUSE_CONSTANT_ATTRIBUTE, 1,
                            ctx);
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
        Filter filter = new DiffuseLightingRable8Bit(in,
                                                     primitiveRegion,
                                                     light,
                                                     diffuseConstant,
                                                     surfaceScale,
                                                     kernelUnitLength);
        handleColorInterpolationFilters(filter, filterElement);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
}
