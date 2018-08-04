package org.apache.batik.gvt;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Map;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.filter.Mask;
public interface GraphicsNode {
    int VISIBLE_PAINTED = 0;
    int VISIBLE_FILL = 1;
    int VISIBLE_STROKE = 2;
    int VISIBLE = 3;
    int PAINTED = 4;
    int FILL = 5;
    int STROKE = 6;
    int ALL = 7;
    int NONE = 8;
    AffineTransform IDENTITY = new AffineTransform();
    WeakReference getWeakReference();
    int getPointerEventType();
    void setPointerEventType(int pointerEventType);
    void setTransform(AffineTransform newTransform);
    AffineTransform getTransform();
    AffineTransform getInverseTransform();
    AffineTransform getGlobalTransform();
    void setComposite(Composite newComposite);
    Composite getComposite();
    void setVisible(boolean isVisible);
    boolean isVisible();
    void setClip(ClipRable newClipper);
    ClipRable getClip();
    void setRenderingHint(RenderingHints.Key key, Object value);
    void setRenderingHints(Map hints);
    void setRenderingHints(RenderingHints newHints);
    RenderingHints getRenderingHints();
    void setMask(Mask newMask);
    Mask getMask();
    void setFilter(Filter newFilter);
    Filter getFilter();
    Filter getGraphicsNodeRable(boolean createIfNeeded);
    Filter getEnableBackgroundGraphicsNodeRable(boolean createIfNeeded);
    void paint(Graphics2D g2d);
    void primitivePaint(Graphics2D g2d);
    CompositeGraphicsNode getParent();
    RootGraphicsNode getRoot();
    Rectangle2D getBounds();
    Rectangle2D getTransformedBounds(AffineTransform txf);
    Rectangle2D getPrimitiveBounds();
    Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf);
    Rectangle2D getGeometryBounds();
    Rectangle2D getTransformedGeometryBounds(AffineTransform txf);
    Rectangle2D getSensitiveBounds();
    Rectangle2D getTransformedSensitiveBounds(AffineTransform txf);
    boolean contains(Point2D p);
    boolean intersects(Rectangle2D r);
    GraphicsNode nodeHitAt(Point2D p);
    Shape getOutline();
}