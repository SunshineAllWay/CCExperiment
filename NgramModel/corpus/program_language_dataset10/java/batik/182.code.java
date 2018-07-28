package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.CompositeRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeBlendElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeBlendElementBridge() {}
    public String getLocalName() {
        return SVG_FE_BLEND_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        CompositeRule rule = convertMode(filterElement, ctx);
        Filter in = getIn(filterElement,
                          filteredElement,
                          filteredNode,
                          inputFilter,
                          filterMap,
                          ctx);
        if (in == null) {
            return null; 
        }
        Filter in2 = getIn2(filterElement,
                            filteredElement,
                            filteredNode,
                            inputFilter,
                            filterMap,
                            ctx);
        if (in2 == null) {
            return null; 
        }
        Rectangle2D defaultRegion;
        defaultRegion = (Rectangle2D)in.getBounds2D().clone();
        defaultRegion.add(in2.getBounds2D());
        Rectangle2D primitiveRegion
            = SVGUtilities.convertFilterPrimitiveRegion(filterElement,
                                                        filteredElement,
                                                        filteredNode,
                                                        defaultRegion,
                                                        filterRegion,
                                                        ctx);
        List srcs = new ArrayList(2);
        srcs.add(in2);
        srcs.add(in);
        Filter filter = new CompositeRable8Bit(srcs, rule, true);
        handleColorInterpolationFilters(filter, filterElement);
        filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
    protected static CompositeRule convertMode(Element filterElement,
                                               BridgeContext ctx) {
        String rule = filterElement.getAttributeNS(null, SVG_MODE_ATTRIBUTE);
        if (rule.length() == 0) {
            return CompositeRule.OVER;
        }
        if (SVG_NORMAL_VALUE.equals(rule)) {
            return CompositeRule.OVER;
        }
        if (SVG_MULTIPLY_VALUE.equals(rule)) {
            return CompositeRule.MULTIPLY;
        }
        if (SVG_SCREEN_VALUE.equals(rule)) {
            return CompositeRule.SCREEN;
        }
        if (SVG_DARKEN_VALUE.equals(rule)) {
            return CompositeRule.DARKEN;
        }
        if (SVG_LIGHTEN_VALUE.equals(rule)) {
            return CompositeRule.LIGHTEN;
        }
        throw new BridgeException
            (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] {SVG_MODE_ATTRIBUTE, rule});
    }
}
