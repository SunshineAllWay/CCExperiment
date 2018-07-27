package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
public class ProxyGraphicsNode extends AbstractGraphicsNode {
    protected GraphicsNode source;
    public ProxyGraphicsNode() {}
    public void setSource(GraphicsNode source) {
        this.source = source;
    }
    public GraphicsNode getSource() {
        return source;
    }
    public void primitivePaint(Graphics2D g2d) {
        if (source != null) {
            source.paint(g2d);
        }
    }
    public Rectangle2D getPrimitiveBounds() {
        if (source == null) 
            return null;
        return source.getBounds();
    }
    public Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf) {
        if (source == null) 
            return null;
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        return source.getTransformedPrimitiveBounds(t);
    }
    public Rectangle2D getGeometryBounds() {
        if (source == null) 
            return null;
        return source.getGeometryBounds();
    }
    public Rectangle2D getTransformedGeometryBounds(AffineTransform txf) {
        if (source == null) 
            return null;
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        return source.getTransformedGeometryBounds(t);
    }
    public Rectangle2D getSensitiveBounds() {
        if (source == null) 
            return null;
        return source.getSensitiveBounds();
    }
    public Shape getOutline() {
        if (source == null) 
            return null;
        return source.getOutline();
    }
}
