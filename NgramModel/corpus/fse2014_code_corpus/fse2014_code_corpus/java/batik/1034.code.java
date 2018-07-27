package org.apache.batik.gvt.renderer;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;
public interface ImageRenderer extends Renderer{
    void dispose();
    void updateOffScreen(int width, int height);
    void setTransform(AffineTransform usr2dev);
    public AffineTransform getTransform();
    void setRenderingHints(RenderingHints rh);
    RenderingHints getRenderingHints();
    BufferedImage getOffScreen();
    void clearOffScreen();
    void flush();
    void flush(Rectangle r);
    void flush(Collection areas);
}
