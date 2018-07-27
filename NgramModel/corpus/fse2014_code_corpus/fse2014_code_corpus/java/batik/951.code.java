package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
public class FillShapePainter implements ShapePainter {
    protected Shape shape;
    protected Paint paint;
    public FillShapePainter(Shape shape) {
        if (shape == null)
            throw new IllegalArgumentException("Shape can not be null!");
        this.shape = shape;
    }
    public void setPaint(Paint newPaint) {
        this.paint = newPaint;
    }
    public Paint getPaint() {
        return paint;
    }
    public void paint(Graphics2D g2d) {
        if (paint != null) {
            g2d.setPaint(paint);
            g2d.fill(shape);
        }
    }
    public Shape getPaintedArea(){
        if (paint == null)
            return null;
        return shape;
    }
    public Rectangle2D getPaintedBounds2D(){
        if ((paint == null) || (shape == null))
            return  null;
        return shape.getBounds2D();
    }
    public boolean inPaintedArea(Point2D pt){
        if ((paint == null) || (shape == null))
            return  false;
        return shape.contains(pt);
    }
    public Shape getSensitiveArea(){
        return shape;
    }
    public Rectangle2D getSensitiveBounds2D() {
        if (shape == null)
            return  null;
        return shape.getBounds2D();
    }
    public boolean inSensitiveArea(Point2D pt){
        if (shape == null)
            return  false;
        return shape.contains(pt);
    }
    public void setShape(Shape shape){
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        this.shape = shape;
    }
    public Shape getShape(){
        return shape;
    }
}
