package org.apache.batik.svggen;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.ext.awt.g2d.GraphicContext;
public class SVGComposite implements SVGConverter {
    private SVGAlphaComposite svgAlphaComposite;
    private SVGCustomComposite svgCustomComposite;
    public SVGComposite(SVGGeneratorContext generatorContext) {
        this.svgAlphaComposite =  new SVGAlphaComposite(generatorContext);
        this.svgCustomComposite = new SVGCustomComposite(generatorContext);
    }
    public List getDefinitionSet() {
        List compositeDefs = new LinkedList(svgAlphaComposite.getDefinitionSet());
        compositeDefs.addAll(svgCustomComposite.getDefinitionSet());
        return compositeDefs;
    }
    public SVGAlphaComposite getAlphaCompositeConverter() {
        return svgAlphaComposite;
    }
    public SVGCustomComposite getCustomCompositeConverter() {
        return svgCustomComposite;
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        return toSVG(gc.getComposite());
    }
    public SVGCompositeDescriptor toSVG(Composite composite) {
        if (composite instanceof AlphaComposite)
            return svgAlphaComposite.toSVG((AlphaComposite)composite);
        else
            return svgCustomComposite.toSVG(composite);
    }
}
