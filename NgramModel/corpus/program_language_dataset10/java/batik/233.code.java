package org.apache.batik.bridge;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.batik.bridge.svg12.DefaultXBLManager;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12ScriptingEnvironment;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.gvt.UpdateTracker;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.util.EventDispatcher.Dispatcher;
import org.apache.batik.util.RunnableQueue;
import org.w3c.dom.Document;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;
public class UpdateManager  {
    static final int MIN_REPAINT_TIME;
    static {
        int value = 20;
        try {
            String s = System.getProperty
            ("org.apache.batik.min_repaint_time", "20");
            value = Integer.parseInt(s);
        } catch (SecurityException se) {
        } catch (NumberFormatException nfe){
        } finally {
            MIN_REPAINT_TIME = value;
        }
    }
    protected BridgeContext bridgeContext;
    protected Document document;
    protected RunnableQueue updateRunnableQueue;
    protected RunnableQueue.RunHandler runHandler;
    protected volatile boolean running;
    protected volatile boolean suspendCalled;
    protected List listeners = Collections.synchronizedList(new LinkedList());
    protected ScriptingEnvironment scriptingEnvironment;
    protected RepaintManager repaintManager;
    protected UpdateTracker updateTracker;
    protected GraphicsNode graphicsNode;
    protected boolean started;
    protected BridgeContext[] secondaryBridgeContexts;
    protected ScriptingEnvironment[] secondaryScriptingEnvironments;
    protected int minRepaintTime;
    public UpdateManager(BridgeContext ctx,
                         GraphicsNode gn,
                         Document doc) {
        bridgeContext = ctx;
        bridgeContext.setUpdateManager(this);
        document = doc;
        updateRunnableQueue = RunnableQueue.createRunnableQueue();
        runHandler = createRunHandler();
        updateRunnableQueue.setRunHandler(runHandler);
        graphicsNode = gn;
        scriptingEnvironment = initializeScriptingEnvironment(bridgeContext);
        secondaryBridgeContexts =
            (BridgeContext[]) ctx.getChildContexts().clone();
        secondaryScriptingEnvironments =
            new ScriptingEnvironment[secondaryBridgeContexts.length];
        for (int i = 0; i < secondaryBridgeContexts.length; i++) {
            BridgeContext resCtx = secondaryBridgeContexts[i];
            if (!((SVGOMDocument) resCtx.getDocument()).isSVG12()) {
                continue;
            }
            resCtx.setUpdateManager(this);
            ScriptingEnvironment se = initializeScriptingEnvironment(resCtx);
            secondaryScriptingEnvironments[i] = se;
        }
        minRepaintTime = MIN_REPAINT_TIME;
    }
    public int getMinRepaintTime() {
        return minRepaintTime;
    }
    public void setMinRepaintTime(int minRepaintTime) {
        this.minRepaintTime = minRepaintTime;
    }
    protected ScriptingEnvironment initializeScriptingEnvironment
            (BridgeContext ctx) {
        SVGOMDocument d = (SVGOMDocument) ctx.getDocument();
        ScriptingEnvironment se;
        if (d.isSVG12()) {
            se = new SVG12ScriptingEnvironment(ctx);
            ctx.xblManager = new DefaultXBLManager(d, ctx);
            d.setXBLManager(ctx.xblManager);
        } else {
            se = new ScriptingEnvironment(ctx);
        }
        return se;
    }
    public synchronized void dispatchSVGLoadEvent()
            throws InterruptedException {
        dispatchSVGLoadEvent(bridgeContext, scriptingEnvironment);
        for (int i = 0; i < secondaryScriptingEnvironments.length; i++) {
            BridgeContext ctx = secondaryBridgeContexts[i];
            if (!((SVGOMDocument) ctx.getDocument()).isSVG12()) {
                continue;
            }
            ScriptingEnvironment se = secondaryScriptingEnvironments[i];
            dispatchSVGLoadEvent(ctx, se);
        }
        secondaryBridgeContexts = null;
        secondaryScriptingEnvironments = null;
    }
    protected void dispatchSVGLoadEvent(BridgeContext ctx,
                                        ScriptingEnvironment se) {
        se.loadScripts();
        se.dispatchSVGLoadEvent();
        if (ctx.isSVG12() && ctx.xblManager != null) {
            SVG12BridgeContext ctx12 = (SVG12BridgeContext) ctx;
            ctx12.addBindingListener();
            ctx12.xblManager.startProcessing();
        }
    }
    public void dispatchSVGZoomEvent()
        throws InterruptedException {
        scriptingEnvironment.dispatchSVGZoomEvent();
    }
    public void dispatchSVGScrollEvent()
        throws InterruptedException {
        scriptingEnvironment.dispatchSVGScrollEvent();
    }
    public void dispatchSVGResizeEvent()
        throws InterruptedException {
        scriptingEnvironment.dispatchSVGResizeEvent();
    }
    public void manageUpdates(final ImageRenderer r) {
        updateRunnableQueue.preemptLater(new Runnable() {
                public void run() {
                    synchronized (UpdateManager.this) {
                        running = true;
                        updateTracker = new UpdateTracker();
                        RootGraphicsNode root = graphicsNode.getRoot();
                        if (root != null){
                            root.addTreeGraphicsNodeChangeListener
                                (updateTracker);
                        }
                        repaintManager = new RepaintManager(r);
                        UpdateManagerEvent ev = new UpdateManagerEvent
                            (UpdateManager.this, null, null);
                        fireEvent(startedDispatcher, ev);
                        started = true;
                    }
                }
            });
        resume();
    }
    public BridgeContext getBridgeContext() {
        return bridgeContext;
    }
    public RunnableQueue getUpdateRunnableQueue() {
        return updateRunnableQueue;
    }
    public RepaintManager getRepaintManager() {
        return repaintManager;
    }
    public UpdateTracker getUpdateTracker() {
        return updateTracker;
    }
    public Document getDocument() {
        return document;
    }
    public ScriptingEnvironment getScriptingEnvironment() {
        return scriptingEnvironment;
    }
    public synchronized boolean isRunning() {
        return running;
    }
    public synchronized void suspend() {
        if (updateRunnableQueue.getQueueState() == RunnableQueue.RUNNING) {
            updateRunnableQueue.suspendExecution(false);
        }
        suspendCalled = true;
    }
    public synchronized void resume() {
        if (updateRunnableQueue.getQueueState() != RunnableQueue.RUNNING) {
            updateRunnableQueue.resumeExecution();
        }
    }
    public void interrupt() {
        Runnable r = new Runnable() {
                public void run() {
                    synchronized (UpdateManager.this) {
                        if (started) {
                            dispatchSVGUnLoadEvent();
                        } else {
                            running = false;
                            scriptingEnvironment.interrupt();
                            updateRunnableQueue.getThread().halt();
                        }
                    }
                }
            };
        try {
            updateRunnableQueue.preemptLater(r);
            updateRunnableQueue.resumeExecution(); 
        } catch (IllegalStateException ise) {
        }
    }
    public void dispatchSVGUnLoadEvent() {
        if (!started) {
            throw new IllegalStateException("UpdateManager not started.");
        }
        updateRunnableQueue.preemptLater(new Runnable() {
                public void run() {
                    synchronized (UpdateManager.this) {
                        AbstractEvent evt = (AbstractEvent)
                            ((DocumentEvent)document).createEvent("SVGEvents");
                        String type;
                        if (bridgeContext.isSVG12()) {
                            type = "unload";
                        } else {
                            type = "SVGUnload";
                        }
                        evt.initEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                        type,
                                        false,    
                                        false);   
                        ((EventTarget)(document.getDocumentElement())).
                            dispatchEvent(evt);
                        running = false;
                        scriptingEnvironment.interrupt();
                        updateRunnableQueue.getThread().halt();
                        bridgeContext.dispose();
                        UpdateManagerEvent ev = new UpdateManagerEvent
                            (UpdateManager.this, null, null);
                        fireEvent(stoppedDispatcher, ev);
                    }
                }
            });
        resume();
    }
    public void updateRendering(AffineTransform u2d,
                                boolean dbr,
                                Shape aoi,
                                int width,
                                int height) {
        repaintManager.setupRenderer(u2d,dbr,aoi,width,height);
        List l = new ArrayList(1);
        l.add(aoi);
        updateRendering(l, false);
    }
    public void updateRendering(AffineTransform u2d,
                                boolean dbr,
                                boolean cpt,
                                Shape aoi,
                                int width,
                                int height) {
        repaintManager.setupRenderer(u2d,dbr,aoi,width,height);
        List l = new ArrayList(1);
        l.add(aoi);
        updateRendering(l, cpt);
    }
    protected void updateRendering(List areas,
                                   boolean clearPaintingTransform) {
        try {
            UpdateManagerEvent ev = new UpdateManagerEvent
                (this, repaintManager.getOffScreen(), null);
            fireEvent(updateStartedDispatcher, ev);
            Collection c = repaintManager.updateRendering(areas);
            List l = new ArrayList(c);
            ev = new UpdateManagerEvent
                (this, repaintManager.getOffScreen(),
                 l, clearPaintingTransform);
            fireEvent(updateCompletedDispatcher, ev);
        } catch (ThreadDeath td) {
            UpdateManagerEvent ev = new UpdateManagerEvent
                (this, null, null);
            fireEvent(updateFailedDispatcher, ev);
            throw td;
        } catch (Throwable t) {
            UpdateManagerEvent ev = new UpdateManagerEvent
                (this, null, null);
            fireEvent(updateFailedDispatcher, ev);
        }
    }
    long outOfDateTime=0;
    protected void repaint() {
        if (!updateTracker.hasChanged()) {
            outOfDateTime = 0;
            return;
        }
        long ctime = System.currentTimeMillis();
        if (ctime < allResumeTime) {
            createRepaintTimer();
            return;
        }
        if (allResumeTime > 0) {
            releaseAllRedrawSuspension();
        }
        if (ctime-outOfDateTime < minRepaintTime) {
            synchronized (updateRunnableQueue.getIteratorLock()) {
                Iterator i = updateRunnableQueue.iterator();
                while (i.hasNext())
                    if (!(i.next() instanceof NoRepaintRunnable))
                        return;
            }
        }
        List dirtyAreas = updateTracker.getDirtyAreas();
        updateTracker.clear();
        if (dirtyAreas != null) {
            updateRendering(dirtyAreas, false);
        }
        outOfDateTime = 0;
    }
    public void forceRepaint() {
        if (!updateTracker.hasChanged()) {
            outOfDateTime = 0;
            return;
        }
        List dirtyAreas = updateTracker.getDirtyAreas();
        updateTracker.clear();
        if (dirtyAreas != null) {
            updateRendering(dirtyAreas, false);
        }
        outOfDateTime = 0;
    }
    protected class SuspensionInfo {
        int index;
        long resumeMilli;
        public SuspensionInfo(int index, long resumeMilli) {
            this.index = index;
            this.resumeMilli = resumeMilli;
        }
        public int getIndex() { return index; }
        public long getResumeMilli() { return resumeMilli; }
    }
    protected class RepaintTimerTask extends TimerTask {
        UpdateManager um;
        RepaintTimerTask(UpdateManager um) {
            this.um = um;
        }
        public void run() {
            RunnableQueue rq = um.getUpdateRunnableQueue();
            if (rq == null) return;
            rq.invokeLater(new Runnable() {
                    public void run() { }
                });
        }
    }
    List suspensionList = new ArrayList();
    int nextSuspensionIndex = 1;
    long allResumeTime = -1;
    Timer repaintTriggerTimer = null;
    TimerTask repaintTimerTask = null;
    void createRepaintTimer() {
        if (repaintTimerTask != null) return;
        if (allResumeTime < 0)        return;
        if (repaintTriggerTimer == null)
            repaintTriggerTimer = new Timer(true);
        long delay = allResumeTime - System.currentTimeMillis();
        if (delay < 0) delay = 0;
        repaintTimerTask = new RepaintTimerTask(this);
        repaintTriggerTimer.schedule(repaintTimerTask, delay);
    }
    void resetRepaintTimer() {
        if (repaintTimerTask == null) return;
        if (allResumeTime < 0)        return;
        if (repaintTriggerTimer == null)
            repaintTriggerTimer = new Timer(true);
        long delay = allResumeTime - System.currentTimeMillis();
        if (delay < 0) delay = 0;
        repaintTimerTask = new RepaintTimerTask(this);
        repaintTriggerTimer.schedule(repaintTimerTask, delay);
    }
    int addRedrawSuspension(int max_wait_milliseconds) {
        long resumeTime = System.currentTimeMillis() + max_wait_milliseconds;
        SuspensionInfo si = new SuspensionInfo(nextSuspensionIndex++,
                                               resumeTime);
        if (resumeTime > allResumeTime) {
            allResumeTime = resumeTime;
            resetRepaintTimer();
        }
        suspensionList.add(si);
        return si.getIndex();
    }
    void releaseAllRedrawSuspension() {
        suspensionList.clear();
        allResumeTime = -1;
        resetRepaintTimer();
    }
    boolean releaseRedrawSuspension(int index) {
        if (index > nextSuspensionIndex) return false;
        if (suspensionList.size() == 0) return true;
        int lo = 0, hi=suspensionList.size()-1;
        while (lo < hi) {
            int mid = (lo+hi)>>1;
            SuspensionInfo si = (SuspensionInfo)suspensionList.get(mid);
            int idx = si.getIndex();
            if      (idx == index) { lo = hi = mid; }
            else if (idx <  index) { lo = mid+1; }
            else                   { hi = mid-1; }
        }
        SuspensionInfo si = (SuspensionInfo)suspensionList.get(lo);
        int idx = si.getIndex();
        if (idx != index)
            return true;  
        suspensionList.remove(lo);
        if (suspensionList.size() == 0) {
            allResumeTime = -1;
            resetRepaintTimer();
        } else {
            long resumeTime = si.getResumeMilli();
            if (resumeTime == allResumeTime) {
                allResumeTime = findNewAllResumeTime();
                resetRepaintTimer();
            }
        }
        return true;
    }
    long findNewAllResumeTime() {
        long ret = -1;
        Iterator i = suspensionList.iterator();
        while (i.hasNext()) {
            SuspensionInfo si = (SuspensionInfo)i.next();
            long t = si.getResumeMilli();
            if (t > ret) ret = t;
        }
        return ret;
    }
    public void addUpdateManagerListener(UpdateManagerListener l) {
        listeners.add(l);
    }
    public void removeUpdateManagerListener(UpdateManagerListener l) {
        listeners.remove(l);
    }
    protected void fireEvent(Dispatcher dispatcher, Object event) {
        EventDispatcher.fireEvent(dispatcher, listeners, event, false);
    }
    static Dispatcher startedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).managerStarted
                    ((UpdateManagerEvent)event);
            }
        };
    static Dispatcher stoppedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).managerStopped
                    ((UpdateManagerEvent)event);
            }
        };
    static Dispatcher suspendedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).managerSuspended
                    ((UpdateManagerEvent)event);
            }
        };
    static Dispatcher resumedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).managerResumed
                    ((UpdateManagerEvent)event);
            }
        };
    static Dispatcher updateStartedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).updateStarted
                    ((UpdateManagerEvent)event);
            }
        };
    static Dispatcher updateCompletedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).updateCompleted
                    ((UpdateManagerEvent)event);
            }
        };
    static Dispatcher updateFailedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((UpdateManagerListener)listener).updateFailed
                    ((UpdateManagerEvent)event);
            }
        };
    protected RunnableQueue.RunHandler createRunHandler() {
        return new UpdateManagerRunHander();
    }
    protected class UpdateManagerRunHander
        extends RunnableQueue.RunHandlerAdapter {
        public void runnableStart(RunnableQueue rq, Runnable r) {
            if (running && !(r instanceof NoRepaintRunnable)) {
                if (outOfDateTime == 0)
                    outOfDateTime = System.currentTimeMillis();
            }
        }
        public void runnableInvoked(RunnableQueue rq, Runnable r) {
            if (running && !(r instanceof NoRepaintRunnable)) {
                repaint();
            }
        }
        public void executionSuspended(RunnableQueue rq) {
            synchronized (UpdateManager.this) {
                if (suspendCalled) {
                    running = false;
                    UpdateManagerEvent ev = new UpdateManagerEvent
                        (this, null, null);
                    fireEvent(suspendedDispatcher, ev);
                }
            }
        }
        public void executionResumed(RunnableQueue rq) {
            synchronized (UpdateManager.this) {
                if (suspendCalled && !running) {
                    running = true;
                    suspendCalled = false;
                    UpdateManagerEvent ev = new UpdateManagerEvent
                        (this, null, null);
                    fireEvent(resumedDispatcher, ev);
                }
            }
        }
    }
}
