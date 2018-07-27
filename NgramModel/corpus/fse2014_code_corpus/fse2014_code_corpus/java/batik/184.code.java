package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.ComponentTransferFunction;
import org.apache.batik.ext.awt.image.ConcreteComponentTransferFunction;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.ComponentTransferRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGFeComponentTransferElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeComponentTransferElementBridge() {}
    public String getLocalName() {
        return SVG_FE_COMPONENT_TRANSFER_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
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
        ComponentTransferFunction funcR = null;
        ComponentTransferFunction funcG = null;
        ComponentTransferFunction funcB = null;
        ComponentTransferFunction funcA = null;
        for (Node n = filterElement.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            Bridge bridge = ctx.getBridge(e);
            if (bridge == null || !(bridge instanceof SVGFeFuncElementBridge)) {
                continue;
            }
            SVGFeFuncElementBridge funcBridge
                = (SVGFeFuncElementBridge)bridge;
            ComponentTransferFunction func
                = funcBridge.createComponentTransferFunction(filterElement, e);
            if (funcBridge instanceof SVGFeFuncRElementBridge) {
                funcR = func;
            } else if (funcBridge instanceof SVGFeFuncGElementBridge) {
                funcG = func;
            } else if (funcBridge instanceof SVGFeFuncBElementBridge) {
                funcB = func;
            } else if (funcBridge instanceof SVGFeFuncAElementBridge) {
                funcA = func;
            }
        }
        Filter filter = new ComponentTransferRable8Bit
            (in, funcA, funcR, funcG, funcB);
        handleColorInterpolationFilters(filter, filterElement);
        filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
    public static class SVGFeFuncAElementBridge extends SVGFeFuncElementBridge {
        public SVGFeFuncAElementBridge() {}
        public String getLocalName() {
            return SVG_FE_FUNC_A_TAG;
        }
    }
    public static class SVGFeFuncRElementBridge extends SVGFeFuncElementBridge {
        public SVGFeFuncRElementBridge() {}
        public String getLocalName() {
            return SVG_FE_FUNC_R_TAG;
        }
    }
    public static class SVGFeFuncGElementBridge extends SVGFeFuncElementBridge {
        public SVGFeFuncGElementBridge() {}
        public String getLocalName() {
            return SVG_FE_FUNC_G_TAG;
        }
    }
    public static class SVGFeFuncBElementBridge extends SVGFeFuncElementBridge {
        public SVGFeFuncBElementBridge() {}
        public String getLocalName() {
            return SVG_FE_FUNC_B_TAG;
        }
    }
    protected abstract static class SVGFeFuncElementBridge
            extends AnimatableGenericSVGBridge {
        protected SVGFeFuncElementBridge() {}
        public ComponentTransferFunction createComponentTransferFunction
            (Element filterElement, Element funcElement) {
            int type = convertType(funcElement, ctx);
            switch (type) {
            case ComponentTransferFunction.DISCRETE: {
                float [] v = convertTableValues(funcElement, ctx);
                if (v == null) {
                    return ConcreteComponentTransferFunction.getIdentityTransfer();
                } else {
                    return ConcreteComponentTransferFunction.getDiscreteTransfer(v);
                }
            }
            case ComponentTransferFunction.IDENTITY: {
                return ConcreteComponentTransferFunction.getIdentityTransfer();
            }
            case ComponentTransferFunction.GAMMA: {
                float amplitude
                    = convertNumber(funcElement, SVG_AMPLITUDE_ATTRIBUTE, 1, ctx);
                float exponent
                    = convertNumber(funcElement, SVG_EXPONENT_ATTRIBUTE, 1, ctx);
                float offset
                    = convertNumber(funcElement, SVG_OFFSET_ATTRIBUTE, 0, ctx);
                return ConcreteComponentTransferFunction.getGammaTransfer
                    (amplitude, exponent, offset);
            }
            case ComponentTransferFunction.LINEAR: {
                float slope
                    = convertNumber(funcElement, SVG_SLOPE_ATTRIBUTE, 1, ctx);
                float intercept
                    = convertNumber(funcElement, SVG_INTERCEPT_ATTRIBUTE, 0, ctx);
                return ConcreteComponentTransferFunction.getLinearTransfer
                    (slope, intercept);
            }
            case ComponentTransferFunction.TABLE: {
                float [] v = convertTableValues(funcElement, ctx);
                if (v == null) {
                    return ConcreteComponentTransferFunction.getIdentityTransfer();
                } else {
                    return ConcreteComponentTransferFunction.getTableTransfer(v);
                }
            }
            default:
                throw new Error("invalid convertType:" + type ); 
            }
        }
        protected static float [] convertTableValues(Element e, BridgeContext ctx) {
            String s = e.getAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE);
            if (s.length() == 0) {
                return null;
            }
            StringTokenizer tokens = new StringTokenizer(s, " ,");
            float [] v = new float[tokens.countTokens()];
            try {
                for (int i = 0; tokens.hasMoreTokens(); ++i) {
                    v[i] = SVGUtilities.convertSVGNumber(tokens.nextToken());
                }
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, e, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_TABLE_VALUES_ATTRIBUTE, s});
        }
            return v;
        }
        protected static int convertType(Element e, BridgeContext ctx) {
            String s = e.getAttributeNS(null, SVG_TYPE_ATTRIBUTE);
            if (s.length() == 0) {
                throw new BridgeException(ctx, e, ERR_ATTRIBUTE_MISSING,
                                          new Object[] {SVG_TYPE_ATTRIBUTE});
            }
            if (SVG_DISCRETE_VALUE.equals(s)) {
                return ComponentTransferFunction.DISCRETE;
            }
            if (SVG_IDENTITY_VALUE.equals(s)) {
                return ComponentTransferFunction.IDENTITY;
            }
            if (SVG_GAMMA_VALUE.equals(s)) {
                return ComponentTransferFunction.GAMMA;
            }
            if (SVG_LINEAR_VALUE.equals(s)) {
                return ComponentTransferFunction.LINEAR;
            }
            if (SVG_TABLE_VALUE.equals(s)) {
                return ComponentTransferFunction.TABLE;
            }
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                      new Object[] {SVG_TYPE_ATTRIBUTE, s});
        }
    }
}
