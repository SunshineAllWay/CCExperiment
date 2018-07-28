package org.apache.batik.svggen;
import java.awt.RenderingHints;
import org.apache.batik.ext.awt.g2d.GraphicContext;
public class SVGRenderingHints extends AbstractSVGConverter{
    public SVGRenderingHints(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc){
        return toSVG(gc.getRenderingHints());
    }
    public static SVGHintsDescriptor toSVG(RenderingHints hints){
        String colorInterpolation = SVG_AUTO_VALUE;
        String colorRendering = SVG_AUTO_VALUE;
        String textRendering = SVG_AUTO_VALUE;
        String shapeRendering = SVG_AUTO_VALUE;
        String imageRendering = SVG_AUTO_VALUE;
        if(hints != null){
            Object rendering = hints.get(RenderingHints.KEY_RENDERING);
            if(rendering == RenderingHints.VALUE_RENDER_DEFAULT){
                colorInterpolation = SVG_AUTO_VALUE;
                colorRendering = SVG_AUTO_VALUE;
                textRendering = SVG_AUTO_VALUE;
                shapeRendering = SVG_AUTO_VALUE;
                imageRendering = SVG_AUTO_VALUE;
            }
            else if(rendering == RenderingHints.VALUE_RENDER_SPEED){
                colorInterpolation = SVG_SRGB_VALUE;
                colorRendering = SVG_OPTIMIZE_SPEED_VALUE;
                textRendering = SVG_OPTIMIZE_SPEED_VALUE;
                shapeRendering = SVG_GEOMETRIC_PRECISION_VALUE;
                imageRendering = SVG_OPTIMIZE_SPEED_VALUE;
            }
            else if(rendering == RenderingHints.VALUE_RENDER_QUALITY){
                colorInterpolation = SVG_LINEAR_RGB_VALUE;
                colorRendering = SVG_OPTIMIZE_QUALITY_VALUE;
                textRendering = SVG_OPTIMIZE_QUALITY_VALUE;
                shapeRendering = SVG_GEOMETRIC_PRECISION_VALUE;
                imageRendering = SVG_OPTIMIZE_QUALITY_VALUE;
            }
            Object fractionalMetrics = hints.get(RenderingHints.KEY_FRACTIONALMETRICS);
            if(fractionalMetrics == RenderingHints.VALUE_FRACTIONALMETRICS_ON){
                textRendering = SVG_OPTIMIZE_QUALITY_VALUE;
                shapeRendering = SVG_GEOMETRIC_PRECISION_VALUE;
            }
            else if(fractionalMetrics == RenderingHints.VALUE_FRACTIONALMETRICS_OFF){
                textRendering = SVG_OPTIMIZE_SPEED_VALUE;
                shapeRendering = SVG_OPTIMIZE_SPEED_VALUE;
            }
            else if(fractionalMetrics == RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT){
                textRendering = SVG_AUTO_VALUE;
                shapeRendering = SVG_AUTO_VALUE;
            }
            Object antialiasing = hints.get(RenderingHints.KEY_ANTIALIASING);
            if(antialiasing == RenderingHints.VALUE_ANTIALIAS_ON){
                textRendering = SVG_OPTIMIZE_LEGIBILITY_VALUE;
                shapeRendering = SVG_AUTO_VALUE;
            }
            else if(antialiasing == RenderingHints.VALUE_ANTIALIAS_OFF){
                textRendering = SVG_GEOMETRIC_PRECISION_VALUE;
                shapeRendering = SVG_CRISP_EDGES_VALUE;
            }
            else if(antialiasing == RenderingHints.VALUE_ANTIALIAS_DEFAULT){
                textRendering = SVG_AUTO_VALUE;
                shapeRendering = SVG_AUTO_VALUE;
            }
            Object textAntialiasing = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if(textAntialiasing == RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                textRendering = SVG_GEOMETRIC_PRECISION_VALUE;
            else if(textAntialiasing == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
                textRendering = SVG_OPTIMIZE_SPEED_VALUE;
            else if(textAntialiasing == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT)
                textRendering = SVG_AUTO_VALUE;
            Object colorRenderingHint = hints.get(RenderingHints.KEY_COLOR_RENDERING);
            if(colorRenderingHint == RenderingHints.VALUE_COLOR_RENDER_DEFAULT)
                colorRendering = SVG_AUTO_VALUE;
            else if(colorRenderingHint == RenderingHints.VALUE_COLOR_RENDER_QUALITY)
                colorRendering = SVG_OPTIMIZE_QUALITY_VALUE;
            else if(colorRenderingHint == RenderingHints.VALUE_COLOR_RENDER_SPEED)
                colorRendering = SVG_OPTIMIZE_SPEED_VALUE;
            Object interpolation = hints.get(RenderingHints.KEY_INTERPOLATION);
            if(interpolation == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
                imageRendering = SVG_OPTIMIZE_SPEED_VALUE;
            else if(interpolation == RenderingHints.VALUE_INTERPOLATION_BICUBIC
                    ||
                    interpolation == RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                imageRendering = SVG_OPTIMIZE_QUALITY_VALUE;
        } 
        return new SVGHintsDescriptor(colorInterpolation,
                                      colorRendering,
                                      textRendering,
                                      shapeRendering,
                                      imageRendering);
    }
}
