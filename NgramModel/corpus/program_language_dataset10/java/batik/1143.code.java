package org.apache.batik.svggen;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import org.w3c.dom.Element;
public abstract class DefaultCachedImageHandler
    implements CachedImageHandler,
               SVGSyntax,
               ErrorConstants {
    static final String XLINK_NAMESPACE_URI =
        "http://www.w3.org/1999/xlink";
    static final AffineTransform IDENTITY = new AffineTransform();
    private static Method createGraphics = null;
    private static boolean initDone = false;
    private static final Class[] paramc = new Class[] {BufferedImage.class};
    private static Object[] paramo = null;
    protected ImageCacher imageCacher;
    public ImageCacher getImageCacher() {
        return imageCacher;
    }
    void setImageCacher(ImageCacher imageCacher) {
        if (imageCacher == null){
            throw new IllegalArgumentException();
        }
        DOMTreeManager dtm = null;
        if (this.imageCacher != null){
            dtm = this.imageCacher.getDOMTreeManager();
        }
        this.imageCacher = imageCacher;
        if (dtm != null){
            this.imageCacher.setDOMTreeManager(dtm);
        }
    }
    public void setDOMTreeManager(DOMTreeManager domTreeManager){
        imageCacher.setDOMTreeManager(domTreeManager);
    }
    private static Graphics2D createGraphics(BufferedImage buf) {
        if (!initDone) {
            try {
                Class clazz = Class.forName("org.apache.batik.ext.awt.image.GraphicsUtil");
                createGraphics = clazz.getMethod("createGraphics", paramc);
                paramo = new Object[1];
            } catch (Throwable t) {
            } finally {
                initDone = true;
            }
        }
        if (createGraphics == null)
            return buf.createGraphics();
        else {
            paramo[0] = buf;
            Graphics2D g2d = null;
            try {
                g2d = (Graphics2D)createGraphics.invoke(null, paramo);
            } catch (Exception e) {
            }
            return g2d;
        }
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
        AffineTransform af  = null;
        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {
            handleEmptyImage(imageElement);
        } else {
            try {
                handleHREF(image, imageElement, generatorContext);
            } catch (SVGGraphics2DIOException e) {
                try {
                    generatorContext.errorHandler.handleError(e);
                } catch (SVGGraphics2DIOException io) {
                    throw new SVGGraphics2DRuntimeException(io);
                }
            }
            af = handleTransform(imageElement, x, y, imageWidth, imageHeight,
                                 width, height, generatorContext);
        }
        return af;
    }
    public AffineTransform handleImage(RenderedImage image,
                                       Element imageElement,
                                       int x, int y,
                                       int width, int height,
                                       SVGGeneratorContext generatorContext) {
        int imageWidth      = image.getWidth();
        int imageHeight     = image.getHeight();
        AffineTransform af  = null;
        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {
            handleEmptyImage(imageElement);
        } else {
            try {
                handleHREF(image, imageElement, generatorContext);
            } catch (SVGGraphics2DIOException e) {
                try {
                    generatorContext.errorHandler.handleError(e);
                } catch (SVGGraphics2DIOException io) {
                    throw new SVGGraphics2DRuntimeException(io);
                }
            }
            af = handleTransform(imageElement, x, y, imageWidth, imageHeight,
                                 width, height, generatorContext);
        }
        return af;
    }
    public AffineTransform handleImage(RenderableImage image,
                                       Element imageElement,
                                       double x, double y,
                                       double width, double height,
                                       SVGGeneratorContext generatorContext) {
        double imageWidth   = image.getWidth();
        double imageHeight  = image.getHeight();
        AffineTransform af  = null;
        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {
            handleEmptyImage(imageElement);
        } else {
            try {
                handleHREF(image, imageElement, generatorContext);
            } catch (SVGGraphics2DIOException e) {
                try {
                    generatorContext.errorHandler.handleError(e);
                } catch (SVGGraphics2DIOException io) {
                    throw new SVGGraphics2DRuntimeException(io);
                }
            }
            af = handleTransform(imageElement, x,y,
                                 imageWidth, imageHeight,
                                 width, height, generatorContext);
        }
        return af;
    }
    protected AffineTransform handleTransform(Element imageElement,
                                              double x, double y,
                                              double srcWidth,
                                              double srcHeight,
                                              double dstWidth,
                                              double dstHeight,
                                              SVGGeneratorContext generatorContext) {
        imageElement.setAttributeNS(null,
                                    SVG_X_ATTRIBUTE,
                                    generatorContext.doubleString(x));
        imageElement.setAttributeNS(null,
                                    SVG_Y_ATTRIBUTE,
                                    generatorContext.doubleString(y));
        imageElement.setAttributeNS(null,
                                    SVG_WIDTH_ATTRIBUTE,
                                    generatorContext.doubleString(dstWidth));
        imageElement.setAttributeNS(null,
                                    SVG_HEIGHT_ATTRIBUTE,
                                    generatorContext.doubleString(dstHeight));
        return null;
    }
    protected void handleEmptyImage(Element imageElement) {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, "");
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, "0");
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, "0");
    }
    public void handleHREF(Image image, Element imageElement,
                           SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        if (image == null)
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_NULL);
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width==0 || height==0) {
            handleEmptyImage(imageElement);
        } else {
            if (image instanceof RenderedImage) {
                handleHREF((RenderedImage)image, imageElement,
                           generatorContext);
            } else {
                BufferedImage buf = buildBufferedImage(new Dimension(width, height));
                Graphics2D g = createGraphics(buf);
                g.drawImage(image, 0, 0, null);
                g.dispose();
                handleHREF((RenderedImage)buf, imageElement,
                           generatorContext);
            }
        }
    }
    public BufferedImage buildBufferedImage(Dimension size){
        return new BufferedImage(size.width, size.height, getBufferedImageType());
    }
    protected void handleHREF(RenderedImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        BufferedImage buf = null;
        if (image instanceof BufferedImage
            &&
            ((BufferedImage)image).getType() == getBufferedImageType()){
            buf = (BufferedImage)image;
        } else {
            Dimension size = new Dimension(image.getWidth(), image.getHeight());
            buf = buildBufferedImage(size);
            Graphics2D g = createGraphics(buf);
            g.drawRenderedImage(image, IDENTITY);
            g.dispose();
        }
        cacheBufferedImage(imageElement, buf, generatorContext);
    }
    protected void handleHREF(RenderableImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        Dimension size = new Dimension((int)Math.ceil(image.getWidth()),
                                       (int)Math.ceil(image.getHeight()));
        BufferedImage buf = buildBufferedImage(size);
        Graphics2D g = createGraphics(buf);
        g.drawRenderableImage(image, IDENTITY);
        g.dispose();
        handleHREF((RenderedImage)buf, imageElement, generatorContext);
    }
    protected void cacheBufferedImage(Element imageElement,
                                      BufferedImage buf,
                                      SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        ByteArrayOutputStream os;
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_CONTEXT_NULL);
        try {
            os = new ByteArrayOutputStream();
            encodeImage(buf, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new SVGGraphics2DIOException(ERR_UNEXPECTED, e);
        }
        String ref = imageCacher.lookup(os,
                                                  buf.getWidth(),
                                                  buf.getHeight(),
                                                  generatorContext);
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME,
                                    getRefPrefix() + ref);
    }
    public abstract String getRefPrefix();
    public abstract void encodeImage(BufferedImage buf, OutputStream os)
        throws IOException;
    public abstract int getBufferedImageType();
}
