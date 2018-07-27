package org.apache.batik.svggen;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import org.w3c.dom.Element;
public abstract class AbstractImageHandlerEncoder extends DefaultImageHandler {
    private static final AffineTransform IDENTITY = new AffineTransform();
    private String imageDir = "";
    private String urlRoot = "";
    private static Method createGraphics = null;
    private static boolean initDone = false;
    private static final Class[] paramc = new Class[] {BufferedImage.class};
    private static Object[] paramo = null;
    private static Graphics2D createGraphics(BufferedImage buf) {
        if (!initDone) {
            try {
                Class clazz = Class.forName("org.apache.batik.ext.awt.image.GraphicsUtil");
                createGraphics = clazz.getMethod("createGraphics", paramc);
                paramo = new Object[1];
            } catch (ThreadDeath td) {
                throw td;
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
    public AbstractImageHandlerEncoder(String imageDir, String urlRoot)
        throws SVGGraphics2DIOException {
        if (imageDir == null)
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_DIR_NULL);
        File imageDirFile = new File(imageDir);
        if (!imageDirFile.exists())
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_DIR_DOES_NOT_EXIST);
        this.imageDir = imageDir;
        if (urlRoot != null)
            this.urlRoot = urlRoot;
        else {
            try{
                this.urlRoot = imageDirFile.toURL().toString();
            } catch (MalformedURLException e) {
                throw new SVGGraphics2DIOException(ERR_CANNOT_USE_IMAGE_DIR+
                                                   e.getMessage(),
                                                   e);
            }
        }
    }
    protected void handleHREF(Image image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        Dimension size = new Dimension(image.getWidth(null),
                                       image.getHeight(null));
        BufferedImage buf = buildBufferedImage(size);
        Graphics2D g = createGraphics(buf);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        saveBufferedImageToFile(imageElement, buf, generatorContext);
    }
    protected void handleHREF(RenderedImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        Dimension size = new Dimension(image.getWidth(), image.getHeight());
        BufferedImage buf = buildBufferedImage(size);
        Graphics2D g = createGraphics(buf);
        g.drawRenderedImage(image, IDENTITY);
        g.dispose();
        saveBufferedImageToFile(imageElement, buf, generatorContext);
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
        saveBufferedImageToFile(imageElement, buf, generatorContext);
    }
    private void saveBufferedImageToFile(Element imageElement,
                                         BufferedImage buf,
                                         SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_CONTEXT_NULL);
        File imageFile = null;
        while (imageFile == null) {
            String fileId = generatorContext.idGenerator.generateID(getPrefix());
            imageFile = new File(imageDir, fileId + getSuffix());
            if (imageFile.exists())
                imageFile = null;
        }
        encodeImage(buf, imageFile);
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, urlRoot + "/" +
                                    imageFile.getName());
    }
    public abstract String getSuffix();
    public abstract String getPrefix();
    public abstract void encodeImage(BufferedImage buf, File imageFile)
        throws SVGGraphics2DIOException;
    public abstract BufferedImage buildBufferedImage(Dimension size);
}
