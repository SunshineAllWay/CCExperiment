package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.ColorMatrixRable;
import org.apache.batik.ext.awt.image.renderable.ColorMatrixRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeColorMatrixElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeColorMatrixElementBridge() {}
    public String getLocalName() {
        return SVG_FE_COLOR_MATRIX_TAG;
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
        int type = convertType(filterElement, ctx);
        ColorMatrixRable colorMatrix;
        switch (type) {
        case ColorMatrixRable.TYPE_HUE_ROTATE:
            float a = convertValuesToHueRotate(filterElement, ctx);
            colorMatrix = ColorMatrixRable8Bit.buildHueRotate(a);
            break;
        case ColorMatrixRable.TYPE_LUMINANCE_TO_ALPHA:
            colorMatrix = ColorMatrixRable8Bit.buildLuminanceToAlpha();
            break;
        case ColorMatrixRable.TYPE_MATRIX:
            float [][] matrix = convertValuesToMatrix(filterElement, ctx);
            colorMatrix = ColorMatrixRable8Bit.buildMatrix(matrix);
            break;
        case ColorMatrixRable.TYPE_SATURATE:
            float s = convertValuesToSaturate(filterElement, ctx);
            colorMatrix = ColorMatrixRable8Bit.buildSaturate(s);
            break;
        default:
            throw new Error("invalid convertType:" + type ); 
        }
        colorMatrix.setSource(in);
        handleColorInterpolationFilters(colorMatrix, filterElement);
        Filter filter
            = new PadRable8Bit(colorMatrix, primitiveRegion, PadMode.ZERO_PAD);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
    protected static float[][] convertValuesToMatrix(Element filterElement,
                                                     BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_VALUES_ATTRIBUTE);
        float [][] matrix = new float[4][5];
        if (s.length() == 0) {
            matrix[0][0] = 1;
            matrix[1][1] = 1;
            matrix[2][2] = 1;
            matrix[3][3] = 1;
            return matrix;
        }
        StringTokenizer tokens = new StringTokenizer(s, " ,");
        int n = 0;
        try {
            while (n < 20 && tokens.hasMoreTokens()) {
                matrix[n/5][n%5]
                    = SVGUtilities.convertSVGNumber(tokens.nextToken());
                n++;
            }
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_VALUES_ATTRIBUTE, s, nfEx });
        }
        if (n != 20 || tokens.hasMoreTokens()) {
            throw new BridgeException
                (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_VALUES_ATTRIBUTE, s});
        }
        for (int i = 0; i < 4; ++i) {
            matrix[i][4] *= 255;
        }
        return matrix;
    }
    protected static float convertValuesToSaturate(Element filterElement,
                                                   BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_VALUES_ATTRIBUTE);
        if (s.length() == 0)
            return 1; 
        try {
            return SVGUtilities.convertSVGNumber(s);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_VALUES_ATTRIBUTE, s});
        }
    }
    protected static float convertValuesToHueRotate(Element filterElement,
                                                    BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_VALUES_ATTRIBUTE);
        if (s.length() == 0)
            return 0; 
        try {
            return (float) Math.toRadians( SVGUtilities.convertSVGNumber(s) );
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object [] {SVG_VALUES_ATTRIBUTE, s});
        }
    }
    protected static int convertType(Element filterElement, BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_TYPE_ATTRIBUTE);
        if (s.length() == 0) {
            return ColorMatrixRable.TYPE_MATRIX;
        }
        if (SVG_HUE_ROTATE_VALUE.equals(s)) {
            return ColorMatrixRable.TYPE_HUE_ROTATE;
        }
        if (SVG_LUMINANCE_TO_ALPHA_VALUE.equals(s)) {
            return ColorMatrixRable.TYPE_LUMINANCE_TO_ALPHA;
        }
        if (SVG_MATRIX_VALUE.equals(s)) {
            return ColorMatrixRable.TYPE_MATRIX;
        }
        if (SVG_SATURATE_VALUE.equals(s)) {
            return ColorMatrixRable.TYPE_SATURATE;
        }
        throw new BridgeException
            (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] {SVG_TYPE_ATTRIBUTE, s});
    }
}
