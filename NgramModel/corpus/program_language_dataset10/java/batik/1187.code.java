package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
public class SVGGraphicContextConverter {
    private static final int GRAPHIC_CONTEXT_CONVERTER_COUNT = 6;
    private SVGTransform transformConverter;
    private SVGPaint paintConverter;
    private SVGBasicStroke strokeConverter;
    private SVGComposite compositeConverter;
    private SVGClip clipConverter;
    private SVGRenderingHints hintsConverter;
    private SVGFont fontConverter;
    private SVGConverter[] converters =
        new SVGConverter[GRAPHIC_CONTEXT_CONVERTER_COUNT];
    public SVGTransform getTransformConverter() { return transformConverter; }
    public SVGPaint getPaintConverter(){ return paintConverter; }
    public SVGBasicStroke getStrokeConverter(){ return strokeConverter; }
    public SVGComposite getCompositeConverter(){ return compositeConverter; }
    public SVGClip getClipConverter(){ return clipConverter; }
    public SVGRenderingHints getHintsConverter(){ return hintsConverter; }
    public SVGFont getFontConverter(){ return fontConverter; }
    public SVGGraphicContextConverter(SVGGeneratorContext generatorContext) {
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_CONTEXT_NULL);
        transformConverter = new SVGTransform(generatorContext);
        paintConverter = new SVGPaint(generatorContext);
        strokeConverter = new SVGBasicStroke(generatorContext);
        compositeConverter = new SVGComposite(generatorContext);
        clipConverter = new SVGClip(generatorContext);
        hintsConverter = new SVGRenderingHints(generatorContext);
        fontConverter = new SVGFont(generatorContext);
        int i=0;
        converters[i++] = paintConverter;
        converters[i++] = strokeConverter;
        converters[i++] = compositeConverter;
        converters[i++] = clipConverter;
        converters[i++] = hintsConverter;
        converters[i++] = fontConverter;
    }
    public String toSVG(TransformStackElement[] transformStack) {
        return transformConverter.toSVGTransform(transformStack);
    }
    public SVGGraphicContext toSVG(GraphicContext gc) {
        Map groupAttrMap = new HashMap();
        for (int i=0; i<converters.length; i++) {
            SVGDescriptor desc = converters[i].toSVG(gc);
            if (desc != null)
                desc.getAttributeMap(groupAttrMap);
        }
        return new SVGGraphicContext(groupAttrMap,
                                     gc.getTransformStack());
    }
    public List getDefinitionSet() {
        List defSet = new LinkedList();
        for(int i=0; i<converters.length; i++)
            defSet.addAll(converters[i].getDefinitionSet());
        return defSet;
    }
}
