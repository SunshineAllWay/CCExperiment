package org.apache.batik.svggen;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import org.w3c.dom.Element;
public interface ImageHandler extends SVGSyntax {
    void handleImage(Image image, Element imageElement,
                            SVGGeneratorContext generatorContext);
    void handleImage(RenderedImage image, Element imageElement,
                            SVGGeneratorContext generatorContext);
    void handleImage(RenderableImage image, Element imageElement,
                            SVGGeneratorContext generatorContext);
}
