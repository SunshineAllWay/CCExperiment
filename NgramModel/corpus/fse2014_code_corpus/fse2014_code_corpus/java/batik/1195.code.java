package org.apache.batik.svggen;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGLinearGradient extends AbstractSVGConverter {
    public SVGLinearGradient(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        Paint paint = gc.getPaint();
        return toSVG((GradientPaint)paint);
    }
    public SVGPaintDescriptor toSVG(GradientPaint gradient) {
        SVGPaintDescriptor gradientDesc =
            (SVGPaintDescriptor)descMap.get(gradient);
        Document domFactory = generatorContext.domFactory;
        if (gradientDesc == null) {
            Element gradientDef =
                domFactory.createElementNS(SVG_NAMESPACE_URI,
                                           SVG_LINEAR_GRADIENT_TAG);
            gradientDef.setAttributeNS(null, SVG_GRADIENT_UNITS_ATTRIBUTE,
                                       SVG_USER_SPACE_ON_USE_VALUE);
            Point2D p1 = gradient.getPoint1();
            Point2D p2 = gradient.getPoint2();
            gradientDef.setAttributeNS(null, SVG_X1_ATTRIBUTE,
                                       doubleString(p1.getX()));
            gradientDef.setAttributeNS(null, SVG_Y1_ATTRIBUTE,
                                       doubleString(p1.getY()));
            gradientDef.setAttributeNS(null, SVG_X2_ATTRIBUTE,
                                       doubleString(p2.getX()));
            gradientDef.setAttributeNS(null, SVG_Y2_ATTRIBUTE,
                                       doubleString(p2.getY()));
            String spreadMethod = SVG_PAD_VALUE;
            if(gradient.isCyclic())
                spreadMethod = SVG_REFLECT_VALUE;
            gradientDef.setAttributeNS
                (null, SVG_SPREAD_METHOD_ATTRIBUTE, spreadMethod);
            Element gradientStop =
                domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
            gradientStop.setAttributeNS(null, SVG_OFFSET_ATTRIBUTE,
                                      SVG_ZERO_PERCENT_VALUE);
            SVGPaintDescriptor colorDesc = SVGColor.toSVG(gradient.getColor1(), generatorContext);
            gradientStop.setAttributeNS(null, SVG_STOP_COLOR_ATTRIBUTE,
                                      colorDesc.getPaintValue());
            gradientStop.setAttributeNS(null, SVG_STOP_OPACITY_ATTRIBUTE,
                                      colorDesc.getOpacityValue());
            gradientDef.appendChild(gradientStop);
            gradientStop =
                domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
            gradientStop.setAttributeNS(null, SVG_OFFSET_ATTRIBUTE,
                                      SVG_HUNDRED_PERCENT_VALUE);
            colorDesc = SVGColor.toSVG(gradient.getColor2(), generatorContext);
            gradientStop.setAttributeNS(null, SVG_STOP_COLOR_ATTRIBUTE,
                                        colorDesc.getPaintValue());
            gradientStop.setAttributeNS(null, SVG_STOP_OPACITY_ATTRIBUTE,
                                        colorDesc.getOpacityValue());
            gradientDef.appendChild(gradientStop);
            gradientDef.
                setAttributeNS(null, SVG_ID_ATTRIBUTE,
                               generatorContext.idGenerator.
                               generateID(ID_PREFIX_LINEAR_GRADIENT));
            StringBuffer paintAttrBuf = new StringBuffer(URL_PREFIX);
            paintAttrBuf.append(SIGN_POUND);
            paintAttrBuf.append(gradientDef.getAttributeNS(null, SVG_ID_ATTRIBUTE));
            paintAttrBuf.append(URL_SUFFIX);
            gradientDesc = new SVGPaintDescriptor(paintAttrBuf.toString(),
                                                  SVG_OPAQUE_VALUE,
                                                  gradientDef);
            descMap.put(gradient, gradientDesc);
            defSet.add(gradientDef);
        }
        return gradientDesc;
    }
}
