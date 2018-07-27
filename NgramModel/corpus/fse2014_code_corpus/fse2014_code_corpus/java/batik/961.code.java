package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
public class RasterImageNode extends AbstractGraphicsNode {
    protected Filter image;
    public RasterImageNode() {}
    public void setImage(Filter newImage) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.image = newImage;
        fireGraphicsNodeChangeCompleted();
    }
    public Filter getImage() {
        return image;
    }
    public Rectangle2D getImageBounds() {
        if (image == null)
            return null;
        return (Rectangle2D) image.getBounds2D().clone();
    }
    public Filter getGraphicsNodeRable() {
        return image;
    }
    public void primitivePaint(Graphics2D g2d) {
        if (image == null) return;
        GraphicsUtil.drawImage(g2d, image);
    }
    public Rectangle2D getPrimitiveBounds() {
        if (image == null)
            return null;
        return image.getBounds2D();
    }
    public Rectangle2D getGeometryBounds() {
        if (image == null)
            return null;
        return image.getBounds2D();
    }
    public Rectangle2D getSensitiveBounds() {
        if (image == null)
            return null;
        return image.getBounds2D();
    }
    public Shape getOutline() {
        if (image == null)
            return null;
        return image.getBounds2D();
    }
}
