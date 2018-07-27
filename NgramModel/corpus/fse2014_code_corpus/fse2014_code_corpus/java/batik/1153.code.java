package org.apache.batik.svggen;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import org.w3c.dom.Element;
public interface GenericImageHandler {
    void setDOMTreeManager(DOMTreeManager domTreeManager);
    Element createElement(SVGGeneratorContext generatorContext);
    AffineTransform handleImage(Image image, Element imageElement,
                                       int x, int y,
                                       int width, int height,
                                       SVGGeneratorContext generatorContext);
    AffineTransform handleImage(RenderedImage image, Element imageElement,
                                       int x, int y,
                                       int width, int height,
                                       SVGGeneratorContext generatorContext);
    AffineTransform handleImage(RenderableImage image, Element imageElement,
                                       double x, double y,
                                       double width, double height,
                                       SVGGeneratorContext generatorContext);
}
