package org.apache.batik.svggen;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
class NullOp implements BufferedImageOp {
    public BufferedImage filter(BufferedImage src, BufferedImage dest){
        java.awt.Graphics2D g = dest.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dest;
    }
    public Rectangle2D getBounds2D(BufferedImage src){
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM){
        BufferedImage dest = null;
        if(destCM==null)
            destCM = src.getColorModel();
        dest = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
                                 destCM.isAlphaPremultiplied(), null);
        return dest;
    }
    public Point2D getPoint2D (Point2D srcPt, Point2D destPt){
        if(destPt==null)
            destPt = new Point2D.Double();
        destPt.setLocation(srcPt.getX(), srcPt.getY());
        return destPt;
    }
    public RenderingHints getRenderingHints(){
        return null;
    }
}