package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
public interface ShapePainter {
    void paint(Graphics2D g2d);
    Shape getPaintedArea();
    Rectangle2D getPaintedBounds2D();
    boolean inPaintedArea(Point2D pt);
    Shape getSensitiveArea();
    Rectangle2D getSensitiveBounds2D();
    boolean inSensitiveArea(Point2D pt);
    void setShape(Shape shape);
    Shape getShape();
}
