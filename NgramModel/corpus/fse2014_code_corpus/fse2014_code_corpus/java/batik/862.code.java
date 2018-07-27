package org.apache.batik.ext.awt.image.rendered;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class BufferedImageCachableRed extends AbstractRed {
    BufferedImage bi;
    public BufferedImageCachableRed(BufferedImage bi) {
        super((CachableRed)null,
              new Rectangle(bi.getMinX(),  bi.getMinY(),
                            bi.getWidth(), bi.getHeight()),
              bi.getColorModel(), bi.getSampleModel(),
              bi.getMinX(), bi.getMinY(), null);
        this.bi = bi;
    }
    public BufferedImageCachableRed(BufferedImage bi,
                                            int xloc, int yloc) {
        super((CachableRed)null, new Rectangle(xloc,  yloc,
                                               bi.getWidth(),
                                               bi.getHeight()),
              bi.getColorModel(), bi.getSampleModel(), xloc, yloc, null);
        this.bi = bi;
    }
    public Rectangle getBounds() {
        return new Rectangle(getMinX(),
                             getMinY(),
                             getWidth(),
                             getHeight());
    }
    public BufferedImage getBufferedImage() {
        return bi;
    }
    public Object getProperty(String name) {
        return bi.getProperty(name);
    }
    public String [] getPropertyNames() {
        return bi.getPropertyNames();
    }
    public Raster getTile(int tileX, int tileY) {
        return bi.getTile(tileX,tileY);
    }
    public Raster getData() {
        Raster r = bi.getData();
        return r.createTranslatedChild(getMinX(), getMinY());
    }
    public Raster getData(Rectangle rect) {
        Rectangle r = (Rectangle)rect.clone();
        if ( ! r.intersects(getBounds()) )
            return null;
        r = r.intersection(getBounds());
        r.translate(-getMinX(), - getMinY());
        Raster ret = bi.getData(r);
        return ret.createTranslatedChild(ret.getMinX()+getMinX(),
                                         ret.getMinY()+getMinY());
    }
    public WritableRaster copyData(WritableRaster wr) {
        WritableRaster wr2 = wr.createWritableTranslatedChild
            (wr.getMinX()-getMinX(),
             wr.getMinY()-getMinY());
        GraphicsUtil.copyData(bi.getRaster(), wr2);
        return wr;
    }
}
