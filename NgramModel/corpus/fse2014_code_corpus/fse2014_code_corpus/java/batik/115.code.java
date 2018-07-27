package org.apache.batik.bridge;
import java.awt.Color;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.DistantLight;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.PointLight;
import org.apache.batik.ext.awt.image.SpotLight;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public abstract class AbstractSVGLightingElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    protected AbstractSVGLightingElementBridge() {}
    protected static
        Light extractLight(Element filterElement, BridgeContext ctx) {
        Color color = CSSUtilities.convertLightingColor(filterElement, ctx);
        for (Node n = filterElement.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            Bridge bridge = ctx.getBridge(e);
            if (bridge == null ||
                !(bridge instanceof AbstractSVGLightElementBridge)) {
                continue;
            }
            return ((AbstractSVGLightElementBridge)bridge).createLight
                (ctx, filterElement, e, color);
        }
        return null;
    }
    protected static double[] convertKernelUnitLength(Element filterElement,
                                                      BridgeContext ctx) {
        String s = filterElement.getAttributeNS
            (null, SVG_KERNEL_UNIT_LENGTH_ATTRIBUTE);
        if (s.length() == 0) {
            return null;
        }
        double [] units = new double[2];
        StringTokenizer tokens = new StringTokenizer(s, " ,");
        try {
            units[0] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
                units[1] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
                units[1] = units[0];
            }
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_KERNEL_UNIT_LENGTH_ATTRIBUTE, s});
        }
        if (tokens.hasMoreTokens() || units[0] <= 0 || units[1] <= 0) {
            throw new BridgeException
                (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_KERNEL_UNIT_LENGTH_ATTRIBUTE, s});
        }
        return units;
    }
    protected abstract static class AbstractSVGLightElementBridge
        extends AnimatableGenericSVGBridge {
        public abstract Light createLight(BridgeContext ctx,
                                          Element filterElement,
                                          Element lightElement,
                                          Color color);
    }
    public static class SVGFeSpotLightElementBridge
        extends AbstractSVGLightElementBridge {
        public SVGFeSpotLightElementBridge() {}
        public String getLocalName() {
            return SVG_FE_SPOT_LIGHT_TAG;
        }
        public Light createLight(BridgeContext ctx,
                                 Element filterElement,
                                 Element lightElement,
                                 Color color) {
            double x = convertNumber(lightElement, SVG_X_ATTRIBUTE, 0, ctx);
            double y = convertNumber(lightElement, SVG_Y_ATTRIBUTE, 0, ctx);
            double z = convertNumber(lightElement, SVG_Z_ATTRIBUTE, 0, ctx);
            double px = convertNumber(lightElement, SVG_POINTS_AT_X_ATTRIBUTE,
                                      0, ctx);
            double py = convertNumber(lightElement, SVG_POINTS_AT_Y_ATTRIBUTE,
                                      0, ctx);
            double pz = convertNumber(lightElement, SVG_POINTS_AT_Z_ATTRIBUTE,
                                      0, ctx);
            double specularExponent = convertNumber
                (lightElement, SVG_SPECULAR_EXPONENT_ATTRIBUTE, 1, ctx);
            double limitingConeAngle = convertNumber
                (lightElement, SVG_LIMITING_CONE_ANGLE_ATTRIBUTE, 90, ctx);
            return new SpotLight(x, y, z,
                                 px, py, pz,
                                 specularExponent,
                                 limitingConeAngle,
                                 color);
        }
    }
    public static class SVGFeDistantLightElementBridge
        extends AbstractSVGLightElementBridge {
        public SVGFeDistantLightElementBridge() {}
        public String getLocalName() {
            return SVG_FE_DISTANT_LIGHT_TAG;
        }
        public Light createLight(BridgeContext ctx,
                                 Element filterElement,
                                 Element lightElement,
                                 Color color) {
            double azimuth
                = convertNumber(lightElement, SVG_AZIMUTH_ATTRIBUTE, 0, ctx);
            double elevation
                = convertNumber(lightElement, SVG_ELEVATION_ATTRIBUTE, 0, ctx);
            return new DistantLight(azimuth, elevation, color);
        }
    }
    public static class SVGFePointLightElementBridge
        extends AbstractSVGLightElementBridge {
        public SVGFePointLightElementBridge() {}
        public String getLocalName() {
            return SVG_FE_POINT_LIGHT_TAG;
        }
        public Light createLight(BridgeContext ctx,
                                 Element filterElement,
                                 Element lightElement,
                                 Color color) {
            double x = convertNumber(lightElement, SVG_X_ATTRIBUTE, 0, ctx);
            double y = convertNumber(lightElement, SVG_Y_ATTRIBUTE, 0, ctx);
            double z = convertNumber(lightElement, SVG_Z_ATTRIBUTE, 0, ctx);
            return new PointLight(x, y, z, color);
        }
    }
}
