package org.apache.batik.transcoder.image;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.PaintKey;
import org.w3c.dom.Document;
public abstract class ImageTranscoder extends SVGAbstractTranscoder {
    protected ImageTranscoder() {
    }
    protected void transcode(Document document,
                             String uri,
                             TranscoderOutput output)
            throws TranscoderException {
        super.transcode(document, uri, output);
        int w = (int)(width+0.5);
        int h = (int)(height+0.5);
        ImageRenderer renderer = createRenderer();
        renderer.updateOffScreen(w, h);
        renderer.setTransform(curTxf);
        renderer.setTree(this.root);
        this.root = null; 
        try {
            Shape raoi = new Rectangle2D.Float(0, 0, width, height);
            renderer.repaint(curTxf.createInverse().
                             createTransformedShape(raoi));
            BufferedImage rend = renderer.getOffScreen();
            renderer = null; 
            BufferedImage dest = createImage(w, h);
            Graphics2D g2d = GraphicsUtil.createGraphics(dest);
            if (hints.containsKey(KEY_BACKGROUND_COLOR)) {
                Paint bgcolor = (Paint)hints.get(KEY_BACKGROUND_COLOR);
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setPaint(bgcolor);
                g2d.fillRect(0, 0, w, h);
            }
            if (rend != null) { 
                g2d.drawRenderedImage(rend, new AffineTransform());
            }
            g2d.dispose();
            rend = null; 
            writeImage(dest, output);
        } catch (Exception ex) {
            throw new TranscoderException(ex);
        }
    }
    protected ImageRenderer createRenderer() {
        ImageRendererFactory rendFactory = new ConcreteImageRendererFactory();
        return rendFactory.createStaticImageRenderer();
    }
    protected void forceTransparentWhite(BufferedImage img, SinglePixelPackedSampleModel sppsm) {
        int w = img.getWidth();
        int h = img.getHeight();
        DataBufferInt biDB=(DataBufferInt)img.getRaster().getDataBuffer();
        int scanStride = sppsm.getScanlineStride();
        int dbOffset = biDB.getOffset();
        int[] pixels = biDB.getBankData()[0];
        int p = dbOffset;
        int adjust = scanStride - w;
        int a=0, r=0, g=0, b=0, pel=0;
        for(int i=0; i<h; i++){
            for(int j=0; j<w; j++){
                pel = pixels[p];
                a = (pel >> 24) & 0xff;
                r = (pel >> 16) & 0xff;
                g = (pel >> 8 ) & 0xff;
                b =  pel        & 0xff;
                r = (255*(255 -a) + a*r)/255;
                g = (255*(255 -a) + a*g)/255;
                b = (255*(255 -a) + a*b)/255;
                pixels[p++] =
                    (a<<24 & 0xff000000) |
                    (r<<16 & 0xff0000) |
                    (g<<8  & 0xff00) |
                    (b     & 0xff);
            }
            p += adjust;
        }
    }
    public abstract BufferedImage createImage(int width, int height);
    public abstract void writeImage(BufferedImage img, TranscoderOutput output)
        throws TranscoderException;
    public static final TranscodingHints.Key KEY_BACKGROUND_COLOR
        = new PaintKey();
    public static final TranscodingHints.Key KEY_FORCE_TRANSPARENT_WHITE
        = new BooleanKey();
}
