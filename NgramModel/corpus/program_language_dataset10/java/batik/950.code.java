package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
public class CompositeShapePainter implements ShapePainter {
    protected Shape shape;
    protected ShapePainter [] painters;
    protected int count;
    public CompositeShapePainter(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        this.shape = shape;
    }
    public void addShapePainter(ShapePainter shapePainter) {
        if (shapePainter == null) {
            return;
        }
        if (shape != shapePainter.getShape()) {
            shapePainter.setShape(shape);
        }
        if (painters == null) {
            painters = new ShapePainter[2];
        }
        if (count == painters.length) {
            ShapePainter [] newPainters = new ShapePainter[ count + count/2 + 1];
            System.arraycopy(painters, 0, newPainters, 0, count);
            painters = newPainters;
        }
        painters[count++] = shapePainter;
    }
    public ShapePainter getShapePainter(int index) {
        return painters[index];
    }
    public int getShapePainterCount() {
        return count;
    }
    public void paint(Graphics2D g2d) {
        if (painters != null) {
            for (int i=0; i < count; ++i) {
                painters[i].paint(g2d);
            }
        }
    }
    public Shape getPaintedArea(){
        if (painters == null)
            return null;
        Area paintedArea = new Area();
        for (int i=0; i < count; ++i) {
            Shape s = painters[i].getPaintedArea();
            if (s != null) {
                paintedArea.add(new Area(s));
            }
        }
        return paintedArea;
    }
    public Rectangle2D getPaintedBounds2D(){
        if (painters == null)
            return null;
        Rectangle2D bounds = null;
        for (int i=0; i < count; ++i) {
            Rectangle2D pb = painters[i].getPaintedBounds2D();
            if (pb == null) continue;
            if (bounds == null) bounds = (Rectangle2D)pb.clone();
            else                bounds.add(pb);
        }
        return bounds;
    }
    public boolean inPaintedArea(Point2D pt){
        if (painters == null)
            return false;
        for (int i=0; i < count; ++i) {
            if (painters[i].inPaintedArea(pt))
                return true;
        }
        return false;
    }
    public Shape getSensitiveArea() {
        if (painters == null)
            return null;
        Area paintedArea = new Area();
        for (int i=0; i < count; ++i) {
            Shape s = painters[i].getSensitiveArea();
            if (s != null) {
                paintedArea.add(new Area(s));
            }
        }
        return paintedArea;
    }
    public Rectangle2D getSensitiveBounds2D() {
        if (painters == null)
            return null;
        Rectangle2D bounds = null;
        for (int i=0; i < count; ++i) {
            Rectangle2D pb = painters[i].getSensitiveBounds2D();
            if (pb == null) continue;
            if (bounds == null) bounds = (Rectangle2D)pb.clone();
            else                bounds.add(pb);
        }
        return bounds;
    }
    public boolean inSensitiveArea(Point2D pt){
        if (painters == null)
            return false;
        for (int i=0; i < count; ++i) {
            if (painters[i].inSensitiveArea(pt))
                return true;
        }
        return false;
    }
    public void setShape(Shape shape){
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        if (painters != null) {
            for (int i=0; i < count; ++i) {
                painters[i].setShape(shape);
            }
        }
        this.shape = shape;
    }
    public Shape getShape(){
        return shape;
    }
}
