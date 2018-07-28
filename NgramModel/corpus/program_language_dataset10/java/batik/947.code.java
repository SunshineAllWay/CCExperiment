package org.apache.batik.gvt;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.event.GraphicsNodeChangeEvent;
import org.apache.batik.gvt.event.GraphicsNodeChangeListener;
import org.apache.batik.gvt.filter.GraphicsNodeRable;
import org.apache.batik.gvt.filter.GraphicsNodeRable8Bit;
import org.apache.batik.gvt.filter.Mask;
import org.apache.batik.util.HaltingThread;
public abstract class AbstractGraphicsNode implements GraphicsNode {
    protected EventListenerList listeners;
    protected AffineTransform transform;
    protected AffineTransform inverseTransform;
    protected Composite composite;
    protected boolean isVisible = true;
    protected ClipRable clip;
    protected RenderingHints hints;
    protected CompositeGraphicsNode parent;
    protected RootGraphicsNode root;
    protected Mask mask;
    protected Filter filter;
    protected int pointerEventType = VISIBLE_PAINTED;
    protected WeakReference graphicsNodeRable;
    protected WeakReference enableBackgroundGraphicsNodeRable;
    protected WeakReference weakRef;
    private Rectangle2D bounds;
    protected GraphicsNodeChangeEvent changeStartedEvent   = null;
    protected GraphicsNodeChangeEvent changeCompletedEvent = null;
    protected AbstractGraphicsNode() {}
    public WeakReference getWeakReference() {
        if (weakRef == null)
            weakRef =  new WeakReference(this);
        return weakRef;
    }
    public int getPointerEventType() {
        return pointerEventType;
    }
    public void setPointerEventType(int pointerEventType) {
        this.pointerEventType = pointerEventType;
    }
    public void setTransform(AffineTransform newTransform) {
        fireGraphicsNodeChangeStarted();
        this.transform = newTransform;
        if(transform.getDeterminant() != 0){
            try{
                inverseTransform = transform.createInverse();
            }catch(NoninvertibleTransformException e){
                throw new Error( e.getMessage() );
            }
        } else {
            inverseTransform = transform;
        }
        if (parent != null)
            parent.invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
    }
    public AffineTransform getTransform() {
        return transform;
    }
    public AffineTransform getInverseTransform(){
        return inverseTransform;
    }
    public AffineTransform getGlobalTransform(){
        AffineTransform ctm = new AffineTransform();
        GraphicsNode node = this;
        while (node != null) {
            if(node.getTransform() != null){
                ctm.preConcatenate(node.getTransform());
            }
            node = node.getParent();
        }
        return ctm;
    }
    public void setComposite(Composite newComposite) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.composite = newComposite;
        fireGraphicsNodeChangeCompleted();
    }
    public Composite getComposite() {
        return composite;
    }
    public void setVisible(boolean isVisible) {
        fireGraphicsNodeChangeStarted();
        this.isVisible = isVisible;
        invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
    }
    public boolean isVisible() {
        return isVisible;
    }
    public void setClip(ClipRable newClipper) {
        if ((newClipper == null) && (this.clip == null))
            return; 
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.clip = newClipper;
        fireGraphicsNodeChangeCompleted();
    }
    public ClipRable getClip() {
        return clip;
    }
    public void setRenderingHint(RenderingHints.Key key, Object value) {
        fireGraphicsNodeChangeStarted();
        if (this.hints == null) {
            this.hints = new RenderingHints(key, value);
        } else {
            hints.put(key, value);
        }
        fireGraphicsNodeChangeCompleted();
    }
    public void setRenderingHints(Map hints) {
        fireGraphicsNodeChangeStarted();
        if (this.hints == null) {
            this.hints = new RenderingHints(hints);
        } else {
            this.hints.putAll(hints);
        }
        fireGraphicsNodeChangeCompleted();
    }
    public void setRenderingHints(RenderingHints newHints) {
        fireGraphicsNodeChangeStarted();
        hints = newHints;
        fireGraphicsNodeChangeCompleted();
    }
    public RenderingHints getRenderingHints() {
        return hints;
    }
    public void setMask(Mask newMask) {
        if ((newMask == null) && (mask == null))
            return; 
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        mask = newMask;
        fireGraphicsNodeChangeCompleted();
    }
    public Mask getMask() {
        return mask;
    }
    public void setFilter(Filter newFilter) {
        if ((newFilter == null) && (filter == null))
            return; 
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        filter = newFilter;
        fireGraphicsNodeChangeCompleted();
    }
    public Filter getFilter() {
        return filter;
    }
    public Filter getGraphicsNodeRable(boolean createIfNeeded) {
        GraphicsNodeRable ret = null;
        if (graphicsNodeRable != null) {
            ret = (GraphicsNodeRable)graphicsNodeRable.get();
            if (ret != null) return ret;
        }
        if (createIfNeeded) {
        ret = new GraphicsNodeRable8Bit(this);
        graphicsNodeRable = new WeakReference(ret);
        }
        return ret;
    }
    public Filter getEnableBackgroundGraphicsNodeRable
        (boolean createIfNeeded) {
        GraphicsNodeRable ret = null;
        if (enableBackgroundGraphicsNodeRable != null) {
            ret = (GraphicsNodeRable)enableBackgroundGraphicsNodeRable.get();
            if (ret != null) return ret;
        }
        if (createIfNeeded) {
            ret = new GraphicsNodeRable8Bit(this);
            ret.setUsePrimitivePaint(false);
            enableBackgroundGraphicsNodeRable = new WeakReference(ret);
        }
        return ret;
    }
    public void paint(Graphics2D g2d){
        if ((composite != null) &&
            (composite instanceof AlphaComposite)) {
            AlphaComposite ac = (AlphaComposite)composite;
            if (ac.getAlpha() < 0.001)
                return;         
        }
        Rectangle2D bounds = getBounds();
        if (bounds == null) return;
        Composite       defaultComposite = null;
        AffineTransform defaultTransform = null;
        RenderingHints  defaultHints     = null;
        Graphics2D      baseG2d          = null;
        if (clip != null)  {
            baseG2d = g2d;
            g2d = (Graphics2D)g2d.create();
            if (hints != null)
                g2d.addRenderingHints(hints);
            if (transform != null)
                g2d.transform(transform);
            if (composite != null)
                g2d.setComposite(composite);
            g2d.clip(clip.getClipPath());
        } else {
            if (hints != null) {
                defaultHints = g2d.getRenderingHints();
                g2d.addRenderingHints(hints);
            }
            if (transform != null) {
                defaultTransform = g2d.getTransform();
                g2d.transform(transform);
            }
            if (composite != null) {
                defaultComposite = g2d.getComposite();
                g2d.setComposite(composite);
            }
        }
        Shape curClip = g2d.getClip();
        g2d.setRenderingHint(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST,
                             curClip);
        boolean paintNeeded = true;
        Shape g2dClip = curClip; 
        if (g2dClip != null) {
            Rectangle2D cb = g2dClip.getBounds2D();
            if(!bounds.intersects(cb.getX(),     cb.getY(),
                                  cb.getWidth(), cb.getHeight()))
                paintNeeded = false;
        }
        if (paintNeeded){
            boolean antialiasedClip = false;
            if ((clip != null) && clip.getUseAntialiasedClip()) {
                antialiasedClip = isAntialiasedClip(g2d.getTransform(),
                                                    g2d.getRenderingHints(),
                                                    clip.getClipPath());
            }
            boolean useOffscreen = isOffscreenBufferNeeded();
            useOffscreen |= antialiasedClip;
            if (!useOffscreen) {
                primitivePaint(g2d);
            } else {
                Filter filteredImage = null;
                if(filter == null){
                    filteredImage = getGraphicsNodeRable(true);
                }
                else {
                    filteredImage = filter;
                }
                if (mask != null) {
                    if (mask.getSource() != filteredImage){
                        mask.setSource(filteredImage);
                    }
                    filteredImage = mask;
                }
                if (clip != null && antialiasedClip) {
                    if (clip.getSource() != filteredImage){
                        clip.setSource(filteredImage);
                    }
                    filteredImage = clip;
                }
                baseG2d = g2d;
                g2d = (Graphics2D)g2d.create();
                if(antialiasedClip){
                    g2d.setClip(null);
                }
                Rectangle2D filterBounds = filteredImage.getBounds2D();
                g2d.clip(filterBounds);
                org.apache.batik.ext.awt.image.GraphicsUtil.drawImage
                    (g2d, filteredImage);
                g2d.dispose();
                g2d = baseG2d;
                baseG2d = null;
            }
        }
        if (baseG2d != null) {
            g2d.dispose();
        } else {
            if (defaultHints != null)
                g2d.setRenderingHints(defaultHints);
            if (defaultTransform != null)
                g2d.setTransform(defaultTransform);
            if (defaultComposite != null) {
                g2d.setComposite(defaultComposite);
            }
        }
    }
    private void traceFilter(Filter filter, String prefix){
        System.out.println(prefix + filter.getClass().getName());
        System.out.println(prefix + filter.getBounds2D());
        List sources = filter.getSources();
        int nSources = sources != null ? sources.size() : 0;
        prefix += "\t";
        for(int i=0; i<nSources; i++){
            Filter source = (Filter)sources.get(i);
            traceFilter(source, prefix);
        }
        System.out.flush();
    }
    protected boolean isOffscreenBufferNeeded() {
        return ((filter != null) ||
                (mask != null) ||
                (composite != null &&
                 !AlphaComposite.SrcOver.equals(composite)));
    }
    protected boolean isAntialiasedClip(AffineTransform usr2dev,
                                        RenderingHints hints,
                                        Shape clip){
        if (clip == null) return false;
        Object val = hints.get(RenderingHintsKeyExt.KEY_TRANSCODING);
        if ((val == RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING) ||
            (val == RenderingHintsKeyExt.VALUE_TRANSCODING_VECTOR))
            return false;
        if(!(clip instanceof Rectangle2D &&
             usr2dev.getShearX() == 0 &&
             usr2dev.getShearY() == 0))
            return true;
        return false;
    }
    public void fireGraphicsNodeChangeStarted(GraphicsNode changeSrc) {
        if (changeStartedEvent == null)
            changeStartedEvent = new GraphicsNodeChangeEvent
                (this, GraphicsNodeChangeEvent.CHANGE_STARTED);
        changeStartedEvent.setChangeSrc(changeSrc);
        fireGraphicsNodeChangeStarted(changeStartedEvent);
        changeStartedEvent.setChangeSrc(null);
    }
    public void fireGraphicsNodeChangeStarted() {
        if (changeStartedEvent == null)
            changeStartedEvent = new GraphicsNodeChangeEvent
                (this, GraphicsNodeChangeEvent.CHANGE_STARTED);
        else {
            changeStartedEvent.setChangeSrc(null);
        }
        fireGraphicsNodeChangeStarted(changeStartedEvent);
    }
    public void fireGraphicsNodeChangeStarted
        (GraphicsNodeChangeEvent changeStartedEvent) {
        RootGraphicsNode rootGN = getRoot();
        if (rootGN == null) return;
        List l = rootGN.getTreeGraphicsNodeChangeListeners();
        if (l == null) return;
        Iterator i = l.iterator();
        GraphicsNodeChangeListener gncl;
        while (i.hasNext()) {
            gncl = (GraphicsNodeChangeListener)i.next();
            gncl.changeStarted(changeStartedEvent);
        }
    }
    public void fireGraphicsNodeChangeCompleted() {
        if (changeCompletedEvent == null) {
            changeCompletedEvent = new GraphicsNodeChangeEvent
                (this, GraphicsNodeChangeEvent.CHANGE_COMPLETED);
        }
        RootGraphicsNode rootGN = getRoot();
        if (rootGN == null) return;
        List l = rootGN.getTreeGraphicsNodeChangeListeners();
        if (l == null) return;
        Iterator i = l.iterator();
        GraphicsNodeChangeListener gncl;
        while (i.hasNext()) {
            gncl = (GraphicsNodeChangeListener)i.next();
            gncl.changeCompleted(changeCompletedEvent);
        }
    }
    public CompositeGraphicsNode getParent() {
        return parent;
    }
    public RootGraphicsNode getRoot() {
        return root;
    }
    protected void setRoot(RootGraphicsNode newRoot) {
        this.root = newRoot;
    }
    protected void setParent(CompositeGraphicsNode newParent) {
        this. parent = newParent;
    }
    protected void invalidateGeometryCache() {
        if (parent != null) {
            parent.invalidateGeometryCache();
        }
        bounds = null;
    }
    public Rectangle2D getBounds(){
        if (bounds == null) {
            if(filter == null){
                bounds = getPrimitiveBounds();
            } else {
                bounds = filter.getBounds2D();
            }
            if(bounds != null){
                if (clip != null) {
                    Rectangle2D clipR = clip.getClipPath().getBounds2D();
                    if (clipR.intersects(bounds))
                        Rectangle2D.intersect(bounds, clipR, bounds);
                }
                if (mask != null) {
                    Rectangle2D maskR = mask.getBounds2D();
                    if (maskR.intersects(bounds))
                        Rectangle2D.intersect(bounds, maskR, bounds);
                }
            }
            bounds = normalizeRectangle(bounds);
            if (HaltingThread.hasBeenHalted()) {
                invalidateGeometryCache();
            }
        }
        return bounds;
    }
    public Rectangle2D getTransformedBounds(AffineTransform txf){
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        Rectangle2D tBounds = null;
        if (filter == null) {
            tBounds = getTransformedPrimitiveBounds(txf);
        } else {
            tBounds = t.createTransformedShape
                (filter.getBounds2D()).getBounds2D();
        }
        if (tBounds != null) {
            if (clip != null) {
                Rectangle2D.intersect
                    (tBounds,
                     t.createTransformedShape(clip.getClipPath()).getBounds2D(),
                     tBounds);
            }
            if(mask != null) {
                Rectangle2D.intersect
                    (tBounds,
                     t.createTransformedShape(mask.getBounds2D()).getBounds2D(),
                     tBounds);
            }
        }
        return tBounds;
    }
    public Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf) {
        Rectangle2D tpBounds = getPrimitiveBounds();
        if (tpBounds == null) {
            return null;
        }
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        return t.createTransformedShape(tpBounds).getBounds2D();
    }
    public Rectangle2D getTransformedGeometryBounds(AffineTransform txf) {
        Rectangle2D tpBounds = getGeometryBounds();
        if (tpBounds == null) {
            return null;
        }
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        return t.createTransformedShape(tpBounds).getBounds2D();
    }
    public Rectangle2D getTransformedSensitiveBounds(AffineTransform txf) {
        Rectangle2D sBounds = getSensitiveBounds();
        if (sBounds == null) {
            return null;
        }
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        return t.createTransformedShape(sBounds).getBounds2D();
    }
    public boolean contains(Point2D p) {
        Rectangle2D b = getSensitiveBounds();
        if (b == null || !b.contains(p)) {
            return false;
        }
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case VISIBLE_FILL:
        case VISIBLE_STROKE:
        case VISIBLE:
            return isVisible;
        case PAINTED:
        case FILL:
        case STROKE:
        case ALL:
            return true;
        case NONE:
        default:
            return false;
        }
    }
    public boolean intersects(Rectangle2D r) {
        Rectangle2D b = getBounds();
        if (b == null) return false;
        return b.intersects(r);
    }
    public GraphicsNode nodeHitAt(Point2D p) {
        return (contains(p) ? this : null);
    }
    static double EPSILON = 1e-6;
    protected Rectangle2D normalizeRectangle(Rectangle2D bounds) {
        if (bounds == null) return null;
        if ((bounds.getWidth() < EPSILON)) {
            if (bounds.getHeight() < EPSILON) {
                AffineTransform gt = getGlobalTransform();
                double det = Math.sqrt(gt.getDeterminant());
                return new Rectangle2D.Double
                    (bounds.getX(), bounds.getY(), EPSILON/det, EPSILON/det);
            } else {
                double tmpW = bounds.getHeight()*EPSILON;
                if (tmpW < bounds.getWidth())
                    tmpW = bounds.getWidth();
                return new Rectangle2D.Double
                    (bounds.getX(), bounds.getY(),
                     tmpW, bounds.getHeight());
            }
        } else if (bounds.getHeight() < EPSILON) {
            double tmpH = bounds.getWidth()*EPSILON;
            if (tmpH < bounds.getHeight())
                tmpH = bounds.getHeight();
            return new Rectangle2D.Double
                (bounds.getX(), bounds.getY(),
                 bounds.getWidth(), tmpH);
        }
        return bounds;
    }
}
