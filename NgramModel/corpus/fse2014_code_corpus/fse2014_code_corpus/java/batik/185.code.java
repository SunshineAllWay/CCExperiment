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
public class SVGFeCompositeElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeCompositeElementBridge() {}
    public String getLocalName() {
        return SVG_FE_COMPOSITE_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        CompositeRule rule = convertOperator(filterElement, ctx);
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
    protected static CompositeRule convertOperator(Element filterElement,
                                                   BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_OPERATOR_ATTRIBUTE);
        if (s.length() == 0) {
            return CompositeRule.OVER; 
        }
        if (SVG_ATOP_VALUE.equals(s)) {
            return CompositeRule.ATOP;
        }
        if (SVG_IN_VALUE.equals(s)) {
            return CompositeRule.IN;
        }
        if (SVG_OVER_VALUE.equals(s)) {
            return CompositeRule.OVER;
        }
        if (SVG_OUT_VALUE.equals(s)) {
            return CompositeRule.OUT;
        }
        if (SVG_XOR_VALUE.equals(s)) {
            return CompositeRule.XOR;
        }
        if (SVG_ARITHMETIC_VALUE.equals(s)) {
            float k1 = convertNumber(filterElement, SVG_K1_ATTRIBUTE, 0, ctx);
            float k2 = convertNumber(filterElement, SVG_K2_ATTRIBUTE, 0, ctx);
            float k3 = convertNumber(filterElement, SVG_K3_ATTRIBUTE, 0, ctx);
            float k4 = convertNumber(filterElement, SVG_K4_ATTRIBUTE, 0, ctx);
            return CompositeRule.ARITHMETIC(k1, k2, k3, k4);
        }
        throw new BridgeException
            (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] {SVG_OPERATOR_ATTRIBUTE, s});
    }
}
