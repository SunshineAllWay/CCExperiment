package org.apache.batik.svggen;
import java.util.List;
import org.apache.batik.ext.awt.g2d.GraphicContext;
public interface SVGConverter extends SVGSyntax{
    SVGDescriptor toSVG(GraphicContext gc);
    List getDefinitionSet();
}
