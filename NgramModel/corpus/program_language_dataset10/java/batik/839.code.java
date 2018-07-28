package org.apache.batik.ext.awt.image.renderable;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FloodRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
public class FloodRable8Bit extends AbstractRable
    implements FloodRable {
    Paint floodPaint;
    Rectangle2D floodRegion;
    public FloodRable8Bit(Rectangle2D floodRegion,
                              Paint floodPaint) {
        setFloodPaint(floodPaint);
        setFloodRegion(floodRegion);
    }
    public void setFloodPaint(Paint paint) {
        touch();
        if (paint == null) {
            floodPaint = new Color(0, 0, 0, 0);
        } else {
            floodPaint = paint;
        }
    }
    public Paint getFloodPaint() {
        return floodPaint;
    }
    public Rectangle2D getBounds2D() {
        return (Rectangle2D)floodRegion.clone();
    }
    public Rectangle2D getFloodRegion(){
        return (Rectangle2D)floodRegion.clone();
    }
    public void setFloodRegion(Rectangle2D floodRegion){
        if(floodRegion == null){
            throw new IllegalArgumentException();
        }
        touch();
        this.floodRegion = floodRegion;
    }
    public RenderedImage createRendering(RenderContext rc) {
        AffineTransform usr2dev = rc.getTransform();
        if (usr2dev == null) {
            usr2dev = new AffineTransform();
        }
        Rectangle2D imageRect = getBounds2D();
        Rectangle2D userAOI;
        Shape aoi = rc.getAreaOfInterest();
        if (aoi == null) {
            aoi     = imageRect;
            userAOI = imageRect;
        } else {
            userAOI = aoi.getBounds2D();
            if ( ! imageRect.intersects(userAOI) )
                return null;
            Rectangle2D.intersect(imageRect, userAOI, userAOI);
        }
        final Rectangle renderedArea
            = usr2dev.createTransformedShape(userAOI).getBounds();
        if ((renderedArea.width <= 0) || (renderedArea.height <= 0)) {
            return null;
        }
        CachableRed cr;
        cr = new FloodRed(renderedArea, getFloodPaint());
        cr = new PadRed(cr, renderedArea, PadMode.ZERO_PAD, null);
        return cr;
    }
}
