package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
public class StrokeShapePainter implements ShapePainter {
    protected Shape shape;
    protected Shape strokedShape;
    protected Stroke stroke;
    protected Paint paint;
    public StrokeShapePainter(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        this.shape = shape;
    }
    public void setStroke(Stroke newStroke) {
        this.stroke       = newStroke;
        this.strokedShape = null;
    }
    public Stroke getStroke() {
        return stroke;
    }
    public void setPaint(Paint newPaint) {
        this.paint = newPaint;
    }
    public Paint getPaint() {
        return paint;
    }
    public void paint(Graphics2D g2d) {
        if (stroke != null && paint != null) {
            g2d.setPaint(paint);
            g2d.setStroke(stroke);
            g2d.draw(shape);
        }
    }
    public Shape getPaintedArea(){
        if ((paint == null) || (stroke == null))
            return null;
        if (strokedShape == null)
            strokedShape = stroke.createStrokedShape(shape);
        return strokedShape;
    }
    public Rectangle2D getPaintedBounds2D() {
        Shape painted = getPaintedArea();
        if (painted == null)
            return null;
        return painted.getBounds2D();
    }
    public boolean inPaintedArea(Point2D pt){
        Shape painted = getPaintedArea();
        if (painted == null)
            return false;
        return painted.contains(pt);
    }
    public Shape getSensitiveArea(){
        if (stroke == null)
            return null;
        if (strokedShape == null)
            strokedShape = stroke.createStrokedShape(shape);
        return strokedShape;
    }
    public Rectangle2D getSensitiveBounds2D() {
        Shape sensitive = getSensitiveArea();
        if (sensitive == null)
            return null;
        return sensitive.getBounds2D();
    }
    public boolean inSensitiveArea(Point2D pt){
        Shape sensitive = getSensitiveArea();
        if (sensitive == null)
            return false;
        return sensitive.contains(pt);
    }
    public void setShape(Shape shape){
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        this.shape = shape;
        this.strokedShape = null;
    }
    public Shape getShape(){
        return shape;
    }
}
