package org.apache.batik.swing.svg;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.DynamicGVTBuilder;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.EventDispatcher.Dispatcher;
import org.apache.batik.util.HaltingThread;
import org.w3c.dom.svg.SVGDocument;
public class GVTTreeBuilder extends HaltingThread {
    protected SVGDocument svgDocument;
    protected BridgeContext bridgeContext;
    protected List listeners = Collections.synchronizedList(new LinkedList());
    protected Exception exception;
    public GVTTreeBuilder(SVGDocument   doc,
                          BridgeContext bc) {
        svgDocument = doc;
        bridgeContext = bc;
    }
    public void run() {
        GVTTreeBuilderEvent ev;
        ev = new GVTTreeBuilderEvent(this, null);
        try {
            fireEvent(startedDispatcher, ev);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            GVTBuilder builder = null;
            if (bridgeContext.isDynamic()) {
                builder = new DynamicGVTBuilder();
            } else {
                builder = new GVTBuilder();
            }
            GraphicsNode gvtRoot = builder.build(bridgeContext, svgDocument);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            ev = new GVTTreeBuilderEvent(this, gvtRoot);
            fireEvent(completedDispatcher, ev);
        } catch (InterruptedBridgeException e) {
            fireEvent(cancelledDispatcher, ev);
        } catch (BridgeException e) {
            exception = e;
            ev = new GVTTreeBuilderEvent(this, e.getGraphicsNode());
            fireEvent(failedDispatcher, ev);
        } catch (Exception e) {
            exception = e;
            fireEvent(failedDispatcher, ev);
        } catch (ThreadDeath td) {
            exception = new Exception(td.getMessage());
            fireEvent(failedDispatcher, ev);
            throw td;
        } catch (Throwable t) {
            t.printStackTrace();
            exception = new Exception(t.getMessage());
            fireEvent(failedDispatcher, ev);
        } finally {
        }
    }
    public Exception getException() {
        return exception;
    }
    public void addGVTTreeBuilderListener(GVTTreeBuilderListener l) {
        listeners.add(l);
    }
    public void removeGVTTreeBuilderListener(GVTTreeBuilderListener l) {
        listeners.remove(l);
    }
    public void fireEvent(Dispatcher dispatcher, Object event) {
        EventDispatcher.fireEvent(dispatcher, listeners, event, true);
    }
    static Dispatcher startedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeBuilderListener)listener).gvtBuildStarted
                 ((GVTTreeBuilderEvent)event);
            }
        };
    static Dispatcher completedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeBuilderListener)listener).gvtBuildCompleted
                 ((GVTTreeBuilderEvent)event);
            }
        };
    static Dispatcher cancelledDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeBuilderListener)listener).gvtBuildCancelled
                 ((GVTTreeBuilderEvent)event);
            }
        };
    static Dispatcher failedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((GVTTreeBuilderListener)listener).gvtBuildFailed
                 ((GVTTreeBuilderEvent)event);
            }
        };
}
