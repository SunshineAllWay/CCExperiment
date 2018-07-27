package org.apache.batik.svggen;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import org.w3c.dom.Element;
public class SimpleImageHandler implements GenericImageHandler, SVGSyntax, ErrorConstants {
    static final String XLINK_NAMESPACE_URI =
        "http://www.w3.org/1999/xlink";
    protected ImageHandler imageHandler;
    public SimpleImageHandler(ImageHandler imageHandler){
        if (imageHandler == null){
            throw new IllegalArgumentException();
        }
        this.imageHandler = imageHandler;
    }
    public void setDOMTreeManager(DOMTreeManager domTreeManager){
    }
    public Element createElement(SVGGeneratorContext generatorContext) {
        Element imageElement =
            generatorContext.getDOMFactory().createElementNS
            (SVG_NAMESPACE_URI, SVG_IMAGE_TAG);
        return imageElement;
    }
    public AffineTransform handleImage(Image image,
                                       Element imageElement,
                                       int x, int y,
                                       int width, int height,
                                       SVGGeneratorContext generatorContext) {
        int imageWidth      = image.getWidth(null);
        int imageHeight     = image.getHeight(null);
        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {
            handleEmptyImage(imageElement);
        } else {
            imageHandler.handleImage(image, imageElement, generatorContext);
            setImageAttributes(imageElement, x, y, width, height,
                               generatorContext);
        }
        return null;
    }
    public AffineTransform handleImage(RenderedImage image,
                                       Element imageElement,
                                       int x, int y,
                                       int width, int height,
                                       SVGGeneratorContext generatorContext) {
        int imageWidth      = image.getWidth();
        int imageHeight     = image.getHeight();
        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {
            handleEmptyImage(imageElement);
        } else {
            imageHandler.handleImage(image, imageElement, generatorContext);
            setImageAttributes(imageElement, x, y, width, height, 
                               generatorContext);
        }
        return null;
    }
    public AffineTransform handleImage(RenderableImage image,
                                       Element imageElement,
                                       double x, double y,
                                       double width, double height,
                                       SVGGeneratorContext generatorContext) {
        double imageWidth   = image.getWidth();
        double imageHeight  = image.getHeight();
        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {
            handleEmptyImage(imageElement);
        } else {
            imageHandler.handleImage(image, imageElement, generatorContext);
            setImageAttributes(imageElement, x, y, width, height, generatorContext);
        }
        return null;
    }
    protected void setImageAttributes(Element imageElement,
                                      double x, 
                                      double y,
                                      double width,
                                      double height,
                                      SVGGeneratorContext generatorContext) {
        imageElement.setAttributeNS(null,
                                    SVG_X_ATTRIBUTE,
                                    generatorContext.doubleString(x));
        imageElement.setAttributeNS(null,
                                    SVG_Y_ATTRIBUTE,
                                    generatorContext.doubleString(y));
        imageElement.setAttributeNS(null,
                                    SVG_WIDTH_ATTRIBUTE,
                                    generatorContext.doubleString(width));
        imageElement.setAttributeNS(null,
                                    SVG_HEIGHT_ATTRIBUTE,
                                    generatorContext.doubleString(height));
        imageElement.setAttributeNS(null,
                                    SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                    SVG_NONE_VALUE);
    }
    protected void handleEmptyImage(Element imageElement) {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, "");
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, "0");
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, "0");
    }
}
