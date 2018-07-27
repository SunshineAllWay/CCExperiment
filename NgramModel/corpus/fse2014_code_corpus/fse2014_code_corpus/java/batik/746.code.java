package org.apache.batik.ext.awt.geom;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
public class ShapeExtender implements ExtendedShape {
    Shape shape;
    public ShapeExtender(Shape shape) {
        this.shape = shape;
    }
    public boolean contains(double x, double y) {
        return shape.contains(x, y);
    }
    public boolean contains(double x, double y, double w, double h) {
        return shape.contains(x, y, w, h);
    }
    public boolean contains(Point2D p) {
        return shape.contains(p);
    }
    public boolean contains(Rectangle2D r) {
        return shape.contains(r);
    }
    public Rectangle getBounds() {
        return shape.getBounds();
    }
    public Rectangle2D getBounds2D() {
        return shape.getBounds2D();
    }
    public PathIterator getPathIterator(AffineTransform at) {
        return shape.getPathIterator(at);
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return shape.getPathIterator(at, flatness);
    }
    public ExtendedPathIterator getExtendedPathIterator() {
        return new EPIWrap(shape.getPathIterator(null));
    }
    public boolean intersects(double x, double y, double w, double h) {
        return shape.intersects(x, y, w, h);
    }
    public boolean intersects(Rectangle2D r) {
        return shape.intersects(r);
    }
    public static class EPIWrap implements ExtendedPathIterator {
        PathIterator pi = null;
        public EPIWrap(PathIterator pi) {
            this.pi = pi;
        }
        public int currentSegment() {
            float[] coords = new float[6];
            return pi.currentSegment(coords);
        }
        public int currentSegment(double[] coords) { 
            return pi.currentSegment(coords); }
        public int currentSegment(float[] coords) {
            return pi.currentSegment(coords); }
        public int getWindingRule() {
            return pi.getWindingRule();
        }
        public boolean isDone() {
            return pi.isDone(); }
        public void next() {
            pi.next();
        }
    }
}
