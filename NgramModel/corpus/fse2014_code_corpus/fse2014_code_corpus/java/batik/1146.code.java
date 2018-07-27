package org.apache.batik.svggen;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
public class DefaultImageHandler
    implements ImageHandler, ErrorConstants, XMLConstants {
    public DefaultImageHandler() {
    }
    public void handleImage(Image image, Element imageElement,
                            SVGGeneratorContext generatorContext) {
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,  String.valueOf( image.getWidth( null ) ) );
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, String.valueOf( image.getHeight( null ) ) );
        try {
            handleHREF(image, imageElement, generatorContext);
        } catch (SVGGraphics2DIOException e) {
            try {
                generatorContext.errorHandler.handleError(e);
            } catch (SVGGraphics2DIOException io) {
                throw new SVGGraphics2DRuntimeException(io);
            }
        }
    }
    public void handleImage(RenderedImage image, Element imageElement,
                            SVGGeneratorContext generatorContext) {
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,  String.valueOf( image.getWidth() ) );
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, String.valueOf( image.getHeight() ) );
        try {
            handleHREF(image, imageElement, generatorContext);
        } catch (SVGGraphics2DIOException e) {
            try {
                generatorContext.errorHandler.handleError(e);
            } catch (SVGGraphics2DIOException io) {
                throw new SVGGraphics2DRuntimeException(io);
            }
        }
    }
    public void handleImage(RenderableImage image, Element imageElement,
                            SVGGeneratorContext generatorContext) {
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,  String.valueOf( image.getWidth() ) );
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, String.valueOf( image.getHeight() ) );
        try {
            handleHREF(image, imageElement, generatorContext);
        } catch (SVGGraphics2DIOException e) {
            try {
                generatorContext.errorHandler.handleError(e);
            } catch (SVGGraphics2DIOException io) {
                throw new SVGGraphics2DRuntimeException(io);
            }
        }
    }
    protected void handleHREF(Image image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, image.toString());
    }
    protected void handleHREF(RenderedImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, image.toString());
    }
    protected void handleHREF(RenderableImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, image.toString());
    }
}
