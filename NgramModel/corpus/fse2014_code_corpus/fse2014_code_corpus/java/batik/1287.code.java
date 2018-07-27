package org.apache.batik.swing;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.event.ComponentEvent;
import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.gvt.JGVTComponentListener;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
public class JSVGScrollPane extends JPanel
{
    protected JSVGCanvas canvas;
    protected JPanel horizontalPanel;
    protected JScrollBar vertical;
    protected JScrollBar horizontal;
    protected Component cornerBox;
    protected boolean scrollbarsAlwaysVisible = false;
    protected SBListener hsbListener;
    protected SBListener vsbListener;
    protected Rectangle2D viewBox = null; 
    protected boolean ignoreScrollChange = false;
    public JSVGScrollPane(JSVGCanvas canvas) {
        super();
        this.canvas = canvas;
        canvas.setRecenterOnResize(false);
        vertical   = new JScrollBar(JScrollBar.VERTICAL,   0, 0, 0, 0);
        horizontal = new JScrollBar(JScrollBar.HORIZONTAL, 0, 0, 0, 0);
        horizontalPanel = new JPanel(new BorderLayout());
        horizontalPanel.add(horizontal, BorderLayout.CENTER);
        cornerBox = Box.createRigidArea
            (new Dimension(vertical.getPreferredSize().width,
                           horizontal.getPreferredSize().height));
        horizontalPanel.add(cornerBox, BorderLayout.EAST);
        hsbListener = createScrollBarListener(false);
        horizontal.getModel().addChangeListener(hsbListener);
        vsbListener = createScrollBarListener(true);
        vertical.getModel().addChangeListener(vsbListener);
        updateScrollbarState(false, false);
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(vertical, BorderLayout.EAST);
        add(horizontalPanel, BorderLayout.SOUTH);
        canvas.addSVGDocumentLoaderListener(createLoadListener());
        ScrollListener xlistener = createScrollListener();
        addComponentListener(xlistener);
        canvas.addGVTTreeRendererListener(xlistener);
        canvas.addJGVTComponentListener  (xlistener);
        canvas.addGVTTreeBuilderListener (xlistener);
        canvas.addUpdateManagerListener  (xlistener);
    }
    public boolean getScrollbarsAlwaysVisible() {
        return scrollbarsAlwaysVisible;
    }
    public void setScrollbarsAlwaysVisible(boolean vis) {
        scrollbarsAlwaysVisible = vis;
        resizeScrollBars();
    }
    protected SBListener createScrollBarListener(boolean isVertical) {
        return new SBListener(isVertical);
    }
    protected ScrollListener createScrollListener() {
        return new ScrollListener();
    }
    protected SVGDocumentLoaderListener createLoadListener() {
        return new SVGScrollDocumentLoaderListener();
    }
    public JSVGCanvas getCanvas() {
        return canvas;
    }
    class SVGScrollDocumentLoaderListener extends SVGDocumentLoaderAdapter {
        public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
            NodeEventTarget root
                = (NodeEventTarget) e.getSVGDocument().getRootElement();
            root.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                 SVGConstants.SVG_SVGZOOM_EVENT_TYPE,
                 new EventListener() {
                     public void handleEvent(Event evt) {
                         if (!(evt.getTarget() instanceof SVGSVGElement))
                             return;
                         SVGSVGElement svg = (SVGSVGElement) evt.getTarget();
                         scaleChange(svg.getCurrentScale());
                     } 
                 }, false, null);
        }
    }
    public void reset() {
        viewBox = null;
        updateScrollbarState(false, false);
        revalidate();
    }
    protected void setScrollPosition() {
        checkAndSetViewBoxRect();
        if (viewBox == null) return;
        AffineTransform crt = canvas.getRenderingTransform();
        AffineTransform vbt = canvas.getViewBoxTransform();
        if (crt == null) crt = new AffineTransform();
        if (vbt == null) vbt = new AffineTransform();
        Rectangle r2d = vbt.createTransformedShape(viewBox).getBounds();
        int tx = 0, ty = 0;
        if (r2d.x < 0) tx -= r2d.x;
        if (r2d.y < 0) ty -= r2d.y;
        int deltaX = horizontal.getValue()-tx;
        int deltaY = vertical.getValue()  -ty;
        crt.preConcatenate
            (AffineTransform.getTranslateInstance(-deltaX, -deltaY));
        canvas.setRenderingTransform(crt);
    }
    protected class WheelListener implements MouseWheelListener
    {
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            final JScrollBar sb = (vertical.isVisible()) ?
                vertical : horizontal;        
            if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                final int amt = e.getUnitsToScroll() * sb.getUnitIncrement();
                sb.setValue(sb.getValue() + amt);
            } else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL){
                final int amt = e.getWheelRotation() * sb.getBlockIncrement();
                sb.setValue(sb.getValue() + amt);
            }
        }
    }
    protected class SBListener implements ChangeListener
    {
        protected boolean inDrag = false;
        protected int startValue;
        protected boolean isVertical;
        public SBListener(boolean vertical)
        {
            isVertical = vertical;
        }
        public synchronized void stateChanged(ChangeEvent e)
        {
            if(ignoreScrollChange) return;
            Object src = e.getSource();
            if (!(src instanceof BoundedRangeModel))
                return;
            int val = ((isVertical)?vertical.getValue():
                       horizontal.getValue());
            BoundedRangeModel brm = (BoundedRangeModel)src;
            if (brm.getValueIsAdjusting()) {
                if (!inDrag) {
                    inDrag = true;
                    startValue = val;
                } else {
                    AffineTransform at;
                    if (isVertical) {
                        at = AffineTransform.getTranslateInstance
                            (0, startValue-val);
                    } else {
                        at = AffineTransform.getTranslateInstance
                            (startValue-val, 0);
                    }
                    canvas.setPaintingTransform(at);
                }
            } else {
                if (inDrag) {
                    inDrag = false;
                    if (val == startValue) {
                        canvas.setPaintingTransform(new AffineTransform());
                        return;
                    }
                }
                setScrollPosition();
            }
        }
    }
    protected class ScrollListener extends ComponentAdapter
        implements JGVTComponentListener, GVTTreeBuilderListener,
                   GVTTreeRendererListener, UpdateManagerListener
    {
        protected boolean isReady = false;
        public void componentTransformChanged(ComponentEvent evt)
        {
            if(isReady)
                resizeScrollBars();
        }
        public void componentResized(ComponentEvent evt)
        {
            if(isReady)
                resizeScrollBars();
        }
        public void gvtBuildStarted  (GVTTreeBuilderEvent e) {
            isReady = false;
            updateScrollbarState(false, false);
        }
        public void gvtBuildCompleted(GVTTreeBuilderEvent e)
        {
            isReady = true;
            viewBox = null;   
        }
        public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
            if (viewBox == null) {
                resizeScrollBars();
                return;
            }
            Rectangle2D newview = getViewBoxRect();
            if (newview == null) return;
            if ((newview.getX() != viewBox.getX()) ||
                (newview.getY() != viewBox.getY()) ||
                (newview.getWidth() != viewBox.getWidth()) ||
                (newview.getHeight() != viewBox.getHeight())) {
                viewBox = newview;
                resizeScrollBars();
            }
        }
        public void updateCompleted(UpdateManagerEvent e) {
            if (viewBox == null) {
                resizeScrollBars();
                return;
            }
            Rectangle2D newview = getViewBoxRect();
            if (newview == null) return;
            if ((newview.getX() != viewBox.getX()) ||
                (newview.getY() != viewBox.getY()) ||
                (newview.getWidth() != viewBox.getWidth()) ||
                (newview.getHeight() != viewBox.getHeight())) {
                viewBox = newview;
                resizeScrollBars();
            }
        }
        public void gvtBuildCancelled(GVTTreeBuilderEvent e) { }
        public void gvtBuildFailed   (GVTTreeBuilderEvent e) { }
        public void gvtRenderingPrepare  (GVTTreeRendererEvent e) { }
        public void gvtRenderingStarted  (GVTTreeRendererEvent e) { }
        public void gvtRenderingCancelled(GVTTreeRendererEvent e) { }
        public void gvtRenderingFailed   (GVTTreeRendererEvent e) { }
        public void managerStarted  (UpdateManagerEvent e) { }
        public void managerSuspended(UpdateManagerEvent e) { }
        public void managerResumed  (UpdateManagerEvent e) { }
        public void managerStopped  (UpdateManagerEvent e) { }
        public void updateStarted   (UpdateManagerEvent e) { }
        public void updateFailed    (UpdateManagerEvent e) { }
    }
    protected void resizeScrollBars()
    {
        ignoreScrollChange = true;
        checkAndSetViewBoxRect();
        if (viewBox == null) return;
        AffineTransform vbt = canvas.getViewBoxTransform();
        if (vbt == null) vbt = new AffineTransform();
        Rectangle r2d = vbt.createTransformedShape(viewBox).getBounds();
        int maxW = r2d.width;
        int maxH = r2d.height;
        int tx = 0, ty = 0;
        if (r2d.x > 0) maxW += r2d.x;
        else           tx   -= r2d.x;
        if (r2d.y > 0) maxH += r2d.y;
        else           ty   -= r2d.y;
        Dimension vpSize = updateScrollbarVisibility(tx, ty, maxW, maxH);
        vertical.  setValues(ty, vpSize.height, 0, maxH);
        horizontal.setValues(tx, vpSize.width,  0, maxW);
        vertical.  setBlockIncrement( (int) (0.9f * vpSize.height) );
        horizontal.setBlockIncrement( (int) (0.9f * vpSize.width) );
        vertical.  setUnitIncrement( (int) (0.2f * vpSize.height) );
        horizontal.setUnitIncrement( (int) (0.2f * vpSize.width) );
        doLayout();
        horizontalPanel.doLayout();
        horizontal.doLayout();
        vertical.doLayout();
        ignoreScrollChange = false;
    }
    protected Dimension updateScrollbarVisibility(int tx, int ty,
                                                  int maxW, int maxH) {
        Dimension vpSize = canvas.getSize();
        int maxVPW = vpSize.width;  int minVPW = vpSize.width;
        int maxVPH = vpSize.height; int minVPH = vpSize.height;
        if (vertical.isVisible()) {
            maxVPW += vertical.getPreferredSize().width;
        } else {
            minVPW -= vertical.getPreferredSize().width;
        }
        if (horizontalPanel.isVisible()) {
            maxVPH += horizontal.getPreferredSize().height;
        } else {
            minVPH -= horizontal.getPreferredSize().height;
        }
        boolean hNeeded, vNeeded;
        Dimension ret = new Dimension();
        if (scrollbarsAlwaysVisible) {
            hNeeded = (maxW > minVPW);
            vNeeded = (maxH > minVPH);
            ret.width  = minVPW;
            ret.height = minVPH;
        } else {
            hNeeded = (maxW > maxVPW) || (tx != 0);
            vNeeded = (maxH > maxVPH) || (ty != 0);
            if      (vNeeded && !hNeeded) hNeeded = (maxW > minVPW);
            else if (hNeeded && !vNeeded) vNeeded = (maxH > minVPH);
            ret.width  = (hNeeded)?minVPW:maxVPW;
            ret.height = (vNeeded)?minVPH:maxVPH;
        }
        updateScrollbarState(hNeeded, vNeeded);
        return ret;
    }
    protected void updateScrollbarState(boolean hNeeded, boolean vNeeded) {
        horizontal.setEnabled(hNeeded);
        vertical  .setEnabled(vNeeded);
        if (scrollbarsAlwaysVisible) {
            horizontalPanel.setVisible(true);
            vertical       .setVisible(true);
            cornerBox      .setVisible(true);
        } else {
            horizontalPanel.setVisible(hNeeded);
            vertical       .setVisible(vNeeded);
            cornerBox      .setVisible(hNeeded&&vNeeded);
        }
    }
    protected void checkAndSetViewBoxRect() {
        if (viewBox != null) return;
        Rectangle2D newview = getViewBoxRect();
        if (newview == null) return;
        viewBox = newview;
    }
    protected Rectangle2D getViewBoxRect() {
        SVGDocument doc = canvas.getSVGDocument();
        if (doc == null) return null;
        SVGSVGElement el = doc.getRootElement();
        if (el == null) return null;
        String viewBoxStr = el.getAttributeNS
            (null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
        if (viewBoxStr.length() != 0) {
            float[] rect = ViewBox.parseViewBoxAttribute(el, viewBoxStr, null);
            return new Rectangle2D.Float(rect[0], rect[1],
                                         rect[2], rect[3]);
        }
        GraphicsNode gn = canvas.getGraphicsNode();
        if (gn == null) return null;
        Rectangle2D bounds = gn.getBounds();
        if (bounds == null) return null;
        return (Rectangle2D) bounds.clone();
    }
    public void scaleChange(float scale) {
    }
}
