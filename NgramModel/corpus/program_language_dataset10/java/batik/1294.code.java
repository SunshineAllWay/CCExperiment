package org.apache.batik.swing.gvt;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.EventDispatcher.Dispatcher;
import org.apache.batik.util.HaltingThread;
public class GVTTreeRenderer extends HaltingThread {
    protected ImageRenderer renderer;
    protected Shape areaOfInterest;
    protected int width;
    protected int height;
    protected AffineTransform user2DeviceTransform;
    protected boolean doubleBuffering;
    protected List listeners = Collections.synchronizedList(new LinkedList());
    public GVTTreeRenderer(ImageRenderer r, AffineTransform usr2dev,
                           boolean dbuffer,
                           Shape aoi, int width, int height) {
        renderer = r;
        areaOfInterest = aoi;
        user2DeviceTransform = usr2dev;
        doubleBuffering = dbuffer;
        this.width = width;
        this.height = height;
    }
    public void run() {
        GVTTreeRendererEvent ev = new GVTTreeRendererEvent(this, null);
        try {
            fireEvent(prepareDispatcher, ev);
            renderer.setTransform(user2DeviceTransform);
            renderer.setDoubleBuffered(doubleBuffering);
            renderer.updateOffScreen(width, height);
            renderer.clearOffScreen();
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            ev = new GVTTreeRendererEvent(this, renderer.getOffScreen());
            fireEvent(startedDispatcher, ev);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            renderer.repaint(areaOfInterest);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            ev = new GVTTreeRendererEvent(this, renderer.getOffScreen());
            fireEvent(completedDispatcher, ev);
        } catch (NoClassDefFoundError e) {
        } catch (InterruptedBridgeException e) {
            fireEvent(cancelledDispatcher, ev);
        } catch (ThreadDeath td) {
            fireEvent(failedDispatcher, ev);
            throw td;
        } catch (Throwable t) {
            t.printStackTrace();
            fireEvent(failedDispatcher, ev);
        }
    }
    public void fireEvent(Dispatcher dispatcher, Object event) {
        EventDispatcher.fireEvent(dispatcher, listeners, event, true);
    }
    public void addGVTTreeRendererListener(GVTTreeRendererListener l) {
        listeners.add(l);
    }
    public void removeGVTTreeRendererListener(GVTTreeRendererListener l) {
        listeners.remove(l);
    }
    static Dispatcher prepareDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeRendererListener)listener).gvtRenderingPrepare
                    ((GVTTreeRendererEvent)event);
            }
        };
    static Dispatcher startedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeRendererListener)listener).gvtRenderingStarted
                    ((GVTTreeRendererEvent)event);
            }
        };
    static Dispatcher cancelledDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeRendererListener)listener).gvtRenderingCancelled
                    ((GVTTreeRendererEvent)event);
            }
        };
    static Dispatcher completedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeRendererListener)listener).gvtRenderingCompleted
                    ((GVTTreeRendererEvent)event);
            }
        };
    static Dispatcher failedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeRendererListener)listener).gvtRenderingFailed
                    ((GVTTreeRendererEvent)event);
            }
        };
}
