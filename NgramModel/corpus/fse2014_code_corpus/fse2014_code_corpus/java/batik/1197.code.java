package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.ext.awt.g2d.GraphicContext;
public class SVGPaint implements SVGConverter {
    private SVGLinearGradient svgLinearGradient;
    private SVGTexturePaint svgTexturePaint;
    private SVGColor svgColor;
    private SVGCustomPaint svgCustomPaint;
    private SVGGeneratorContext generatorContext;
    public SVGPaint(SVGGeneratorContext generatorContext) {
        this.svgLinearGradient = new SVGLinearGradient(generatorContext);
        this.svgTexturePaint = new SVGTexturePaint(generatorContext);
        this.svgCustomPaint = new SVGCustomPaint(generatorContext);
        this.svgColor = new SVGColor(generatorContext);
        this.generatorContext = generatorContext;
    }
    public List getDefinitionSet(){
        List paintDefs = new LinkedList(svgLinearGradient.getDefinitionSet());
        paintDefs.addAll(svgTexturePaint.getDefinitionSet());
        paintDefs.addAll(svgCustomPaint.getDefinitionSet());
        paintDefs.addAll(svgColor.getDefinitionSet());
        return paintDefs;
    }
    public SVGTexturePaint getTexturePaintConverter(){
        return svgTexturePaint;
    }
    public SVGLinearGradient getGradientPaintConverter(){
        return svgLinearGradient;
    }
    public SVGCustomPaint getCustomPaintConverter(){
        return svgCustomPaint;
    }
    public SVGColor getColorConverter(){
        return svgColor;
    }
    public SVGDescriptor toSVG(GraphicContext gc){
        return toSVG(gc.getPaint());
    }
    public SVGPaintDescriptor toSVG(Paint paint){
        SVGPaintDescriptor paintDesc = svgCustomPaint.toSVG(paint);
        if (paintDesc == null) {
            if (paint instanceof Color)
                paintDesc = SVGColor.toSVG((Color)paint, generatorContext);
            else if (paint instanceof GradientPaint)
                paintDesc = svgLinearGradient.toSVG((GradientPaint)paint);
            else if (paint instanceof TexturePaint)
                paintDesc = svgTexturePaint.toSVG((TexturePaint)paint);
        }
        return paintDesc;
    }
}
