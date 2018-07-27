package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.MorphologyRable8Bit;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeMorphologyElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeMorphologyElementBridge() {}
    public String getLocalName() {
        return SVG_FE_MORPHOLOGY_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        float[] radii = convertRadius(filterElement, ctx);
        if (radii[0] == 0 || radii[1] == 0) {
            return null; 
        }
        boolean isDilate = convertOperator(filterElement, ctx);
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
        PadRable pad = new PadRable8Bit(in, primitiveRegion, PadMode.ZERO_PAD);
        Filter morphology
            = new MorphologyRable8Bit(pad, radii[0], radii[1], isDilate);
        handleColorInterpolationFilters(morphology, filterElement);
        PadRable filter = new PadRable8Bit
            (morphology, primitiveRegion, PadMode.ZERO_PAD);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
    protected static float[] convertRadius(Element filterElement,
                                           BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_RADIUS_ATTRIBUTE);
        if (s.length() == 0) {
            return new float[] {0, 0};
        }
        float [] radii = new float[2];
        StringTokenizer tokens = new StringTokenizer(s, " ,");
        try {
            radii[0] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
                radii[1] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
                radii[1] = radii[0];
            }
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_RADIUS_ATTRIBUTE, s, nfEx });
        }
        if (tokens.hasMoreTokens() || radii[0] < 0 || radii[1] < 0) {
            throw new BridgeException
                (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_RADIUS_ATTRIBUTE, s});
        }
        return radii;
    }
    protected static boolean convertOperator(Element filterElement,
                                             BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_OPERATOR_ATTRIBUTE);
        if (s.length() == 0) {
            return false;
        }
        if (SVG_ERODE_VALUE.equals(s)) {
            return false;
        }
        if (SVG_DILATE_VALUE.equals(s)) {
            return true;
        }
        throw new BridgeException
            (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] {SVG_OPERATOR_ATTRIBUTE, s});
    }
}
