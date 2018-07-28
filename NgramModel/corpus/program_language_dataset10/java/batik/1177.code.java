package org.apache.batik.svggen;
import java.awt.Composite;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;
public class SVGCustomComposite extends AbstractSVGConverter {
    public SVGCustomComposite(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        return toSVG(gc.getComposite());
    }
    public SVGCompositeDescriptor toSVG(Composite composite) {
        if (composite == null)
            throw new NullPointerException();
        SVGCompositeDescriptor compositeDesc =
            (SVGCompositeDescriptor)descMap.get(composite);
        if (compositeDesc == null) {
            SVGCompositeDescriptor desc =
                generatorContext.
                extensionHandler.handleComposite(composite,
                                                 generatorContext);
            if (desc != null) {
                Element def = desc.getDef();
                if(def != null)
                    defSet.add(def);
                descMap.put(composite, desc);
            }
        }
        return compositeDesc;
    }
}
