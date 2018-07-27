package org.apache.batik.svggen;
import java.util.Map;
import org.w3c.dom.Element;
public interface StyleHandler {
    void setStyle(Element element, Map styleMap,
                         SVGGeneratorContext generatorContext);
}
