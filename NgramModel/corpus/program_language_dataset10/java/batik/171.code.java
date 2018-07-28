package org.apache.batik.bridge;
import java.awt.Color;
import java.awt.Paint;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Set;
import org.apache.batik.anim.AnimationEngine;
import org.apache.batik.anim.AnimationException;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableAngleValue;
import org.apache.batik.anim.values.AnimatableAngleOrIdentValue;
import org.apache.batik.anim.values.AnimatableBooleanValue;
import org.apache.batik.anim.values.AnimatableIntegerValue;
import org.apache.batik.anim.values.AnimatableLengthValue;
import org.apache.batik.anim.values.AnimatableLengthListValue;
import org.apache.batik.anim.values.AnimatableLengthOrIdentValue;
import org.apache.batik.anim.values.AnimatableNumberValue;
import org.apache.batik.anim.values.AnimatableNumberListValue;
import org.apache.batik.anim.values.AnimatableNumberOrPercentageValue;
import org.apache.batik.anim.values.AnimatablePathDataValue;
import org.apache.batik.anim.values.AnimatablePointListValue;
import org.apache.batik.anim.values.AnimatablePreserveAspectRatioValue;
import org.apache.batik.anim.values.AnimatableNumberOrIdentValue;
import org.apache.batik.anim.values.AnimatableRectValue;
import org.apache.batik.anim.values.AnimatableStringValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.anim.values.AnimatableColorValue;
import org.apache.batik.anim.values.AnimatablePaintValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGStylableElement;
import org.apache.batik.parser.DefaultPreserveAspectRatioHandler;
import org.apache.batik.parser.FloatArrayProducer;
import org.apache.batik.parser.DefaultLengthHandler;
import org.apache.batik.parser.LengthArrayProducer;
import org.apache.batik.parser.LengthHandler;
import org.apache.batik.parser.LengthListParser;
import org.apache.batik.parser.LengthParser;
import org.apache.batik.parser.NumberListParser;
import org.apache.batik.parser.PathArrayProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.PointsParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PreserveAspectRatioHandler;
import org.apache.batik.parser.PreserveAspectRatioParser;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.SMILConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
public class SVGAnimationEngine extends AnimationEngine {
    protected BridgeContext ctx;
    protected CSSEngine cssEngine;
    protected boolean started;
    protected AnimationTickRunnable animationTickRunnable;
    protected float initialStartTime;
    protected UncomputedAnimatableStringValueFactory
        uncomputedAnimatableStringValueFactory =
            new UncomputedAnimatableStringValueFactory();
    protected AnimatableLengthOrIdentFactory
        animatableLengthOrIdentFactory = new AnimatableLengthOrIdentFactory();
    protected AnimatableNumberOrIdentFactory
        animatableNumberOrIdentFactory =
            new AnimatableNumberOrIdentFactory(false);
    protected Factory[] factories = {
        null, 
        new AnimatableIntegerValueFactory(), 
        new AnimatableNumberValueFactory(), 
        new AnimatableLengthValueFactory(), 
        null, 
        new AnimatableAngleValueFactory(), 
        new AnimatableColorValueFactory(), 
        new AnimatablePaintValueFactory(), 
        null, 
        null, 
        uncomputedAnimatableStringValueFactory, 
        null, 
        null, 
        new AnimatableNumberListValueFactory(), 
        new AnimatableLengthListValueFactory(), 
        uncomputedAnimatableStringValueFactory, 
        uncomputedAnimatableStringValueFactory, 
        animatableLengthOrIdentFactory, 
        uncomputedAnimatableStringValueFactory, 
        uncomputedAnimatableStringValueFactory, 
        uncomputedAnimatableStringValueFactory, 
        uncomputedAnimatableStringValueFactory, 
        new AnimatablePathDataFactory(), 
        uncomputedAnimatableStringValueFactory, 
        null, 
        animatableNumberOrIdentFactory, 
        uncomputedAnimatableStringValueFactory, 
        null, 
        new AnimatableNumberOrIdentFactory(true), 
        new AnimatableAngleOrIdentFactory(), 
        null, 
        new AnimatablePointListValueFactory(), 
        new AnimatablePreserveAspectRatioValueFactory(), 
        null, 
        uncomputedAnimatableStringValueFactory, 
        null, 
        null, 
        null, 
        null, 
        animatableLengthOrIdentFactory, 
        animatableLengthOrIdentFactory, 
        animatableLengthOrIdentFactory, 
        animatableLengthOrIdentFactory, 
        animatableLengthOrIdentFactory, 
        animatableNumberOrIdentFactory, 
        null, 
        null, 
        new AnimatableNumberOrPercentageValueFactory(), 
        null, 
        new AnimatableBooleanValueFactory(), 
        new AnimatableRectValueFactory() 
    };
    protected boolean isSVG12;
    protected LinkedList initialBridges = new LinkedList();
    protected StyleMap dummyStyleMap;
    protected AnimationThread animationThread;
    protected int animationLimitingMode;
    protected float animationLimitingAmount;
    protected static final Set animationEventNames11 = new HashSet();
    protected static final Set animationEventNames12 = new HashSet();
    static {
        String[] eventNamesCommon = {
            "click", "mousedown", "mouseup", "mouseover", "mousemove",
            "mouseout", "beginEvent", "endEvent"
        };
        String[] eventNamesSVG11 = {
            "DOMSubtreeModified", "DOMNodeInserted", "DOMNodeRemoved",
            "DOMNodeRemovedFromDocument", "DOMNodeInsertedIntoDocument",
            "DOMAttrModified", "DOMCharacterDataModified", "SVGLoad",
            "SVGUnload", "SVGAbort", "SVGError", "SVGResize", "SVGScroll",
            "repeatEvent"
        };
        String[] eventNamesSVG12 = {
            "load", "resize", "scroll", "zoom"
        };
        for (int i = 0; i < eventNamesCommon.length; i++) {
            animationEventNames11.add(eventNamesCommon[i]);
            animationEventNames12.add(eventNamesCommon[i]);
        }
        for (int i = 0; i < eventNamesSVG11.length; i++) {
            animationEventNames11.add(eventNamesSVG11[i]);
        }
        for (int i = 0; i < eventNamesSVG12.length; i++) {
            animationEventNames12.add(eventNamesSVG12[i]);
        }
    }
    public SVGAnimationEngine(Document doc, BridgeContext ctx) {
        super(doc);
        this.ctx = ctx;
        SVGOMDocument d = (SVGOMDocument) doc;
        cssEngine = d.getCSSEngine();
        dummyStyleMap = new StyleMap(cssEngine.getNumberOfProperties());
        isSVG12 = d.isSVG12();
    }
    public void dispose() {
        synchronized (this) {
            pause();
            super.dispose();
        }
    }
    public void addInitialBridge(SVGAnimationElementBridge b) {
        if (initialBridges != null) {
            initialBridges.add(b);
        }
    }
    public boolean hasStarted() {
        return started;
    }
    public AnimatableValue parseAnimatableValue(Element animElt,
                                                AnimationTarget target,
                                                String ns, String ln,
                                                boolean isCSS,
                                                String s) {
        SVGOMElement elt = (SVGOMElement) target.getElement();
        int type;
        if (isCSS) {
            type = elt.getPropertyType(ln);
        } else {
            type = elt.getAttributeType(ns, ln);
        }
        Factory factory = factories[type];
        if (factory == null) {
            String an = ns == null ? ln : '{' + ns + '}' + ln;
            throw new BridgeException
                (ctx, animElt, "attribute.not.animatable",
                 new Object[] { target.getElement().getNodeName(), an });
        }
        return factories[type].createValue(target, ns, ln, isCSS, s);
    }
    public AnimatableValue getUnderlyingCSSValue(Element animElt,
                                                 AnimationTarget target,
                                                 String pn) {
        ValueManager[] vms = cssEngine.getValueManagers();
        int idx = cssEngine.getPropertyIndex(pn);
        if (idx != -1) {
            int type = vms[idx].getPropertyType();
            Factory factory = factories[type];
            if (factory == null) {
                throw new BridgeException
                    (ctx, animElt, "attribute.not.animatable",
                     new Object[] { target.getElement().getNodeName(), pn });
            }
            SVGStylableElement e = (SVGStylableElement) target.getElement();
            CSSStyleDeclaration over = e.getOverrideStyle();
            String oldValue = over.getPropertyValue(pn);
            if (oldValue != null) {
                over.removeProperty(pn);
            }
            Value v = cssEngine.getComputedStyle(e, null, idx);
            if (oldValue != null && !oldValue.equals("")) {
                over.setProperty(pn, oldValue, null);
            }
            return factories[type].createValue(target, pn, v);
        }
        return null;
    }
    public void pause() {
        super.pause();
        UpdateManager um = ctx.getUpdateManager();
        if (um != null) {
            um.getUpdateRunnableQueue().setIdleRunnable(null);
        }
    }
    public void unpause() {
        super.unpause();
        UpdateManager um = ctx.getUpdateManager();
        if (um != null) {
            um.getUpdateRunnableQueue().setIdleRunnable(animationTickRunnable);
        }
    }
    public float getCurrentTime() {
        boolean p = pauseTime != 0;
        unpause();
        float t = timedDocumentRoot.getCurrentTime();
        if (p) {
            pause();
        }
        return Float.isNaN(t) ? 0 : t;
    }
    public float setCurrentTime(float t) {
        if (started) {
            float ret = super.setCurrentTime(t);
            if (animationTickRunnable != null) {
                animationTickRunnable.resume();
            }
            return ret;
        } else {
            initialStartTime = t;
            return 0;
        }
    }
    protected TimedDocumentRoot createDocumentRoot() {
        return new AnimationRoot();
    }
    public void start(long documentStartTime) {
        if (started) {
            return;
        }
        started = true;
        try {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(documentStartTime));
                timedDocumentRoot.resetDocument(cal);
                Object[] bridges = initialBridges.toArray();
                initialBridges = null;
                for (int i = 0; i < bridges.length; i++) {
                    SVGAnimationElementBridge bridge =
                        (SVGAnimationElementBridge) bridges[i];
                    bridge.initializeAnimation();
                }
                for (int i = 0; i < bridges.length; i++) {
                    SVGAnimationElementBridge bridge =
                        (SVGAnimationElementBridge) bridges[i];
                    bridge.initializeTimedElement();
                }
                UpdateManager um = ctx.getUpdateManager();
                if (um != null) {
                    RunnableQueue q = um.getUpdateRunnableQueue();
                    animationTickRunnable = new AnimationTickRunnable(q, this);
                    q.setIdleRunnable(animationTickRunnable);
                    if (initialStartTime != 0) {
                        setCurrentTime(initialStartTime);
                    }
                }
            } catch (AnimationException ex) {
                throw new BridgeException(ctx, ex.getElement().getElement(),
                                          ex.getMessage());
            }
        } catch (Exception ex) {
            if (ctx.getUserAgent() == null) {
                ex.printStackTrace();
            } else {
                ctx.getUserAgent().displayError(ex);
            }
        }
    }
    public void setAnimationLimitingNone() {
        animationLimitingMode = 0;
    }
    public void setAnimationLimitingCPU(float pc) {
        animationLimitingMode = 1;
        animationLimitingAmount = pc;
    }
    public void setAnimationLimitingFPS(float fps) {
        animationLimitingMode = 2;
        animationLimitingAmount = fps;
    }
    protected class AnimationRoot extends TimedDocumentRoot {
        public AnimationRoot() {
            super(!isSVG12, isSVG12);
        }
        protected String getEventNamespaceURI(String eventName) {
            if (!isSVG12) {
                return null;
            }
            if (eventName.equals("focusin")
                    || eventName.equals("focusout")
                    || eventName.equals("activate")
                    || animationEventNames12.contains(eventName)) {
                return XMLConstants.XML_EVENTS_NAMESPACE_URI;
            }
            return null;
        }
        protected String getEventType(String eventName) {
            if (eventName.equals("focusin")) {
                return "DOMFocusIn";
            } else if (eventName.equals("focusout")) {
                return "DOMFocusOut";
            } else if (eventName.equals("activate")) {
                return "DOMActivate";
            }
            if (isSVG12) {
                if (animationEventNames12.contains(eventName)) {
                    return eventName;
                }
            } else {
                if (animationEventNames11.contains(eventName)) {
                    return eventName;
                }
            }
            return null;
        }
        protected String getRepeatEventName() {
            return SMILConstants.SMIL_REPEAT_EVENT_NAME;
        }
        protected void fireTimeEvent(String eventType, Calendar time,
                                     int detail) {
            AnimationSupport.fireTimeEvent
                ((EventTarget) document, eventType, time, detail);
        }
        protected void toActive(float begin) {
        }
        protected void toInactive(boolean stillActive, boolean isFrozen) {
        }
        protected void removeFill() {
        }
        protected void sampledAt(float simpleTime, float simpleDur,
                                 int repeatIteration) {
        }
        protected void sampledLastValue(int repeatIteration) {
        }
        protected TimedElement getTimedElementById(String id) {
            return AnimationSupport.getTimedElementById(id, document);
        }
        protected EventTarget getEventTargetById(String id) {
            return AnimationSupport.getEventTargetById(id, document);
        }
        protected EventTarget getAnimationEventTarget() {
            return null;
        }
        protected EventTarget getRootEventTarget() {
            return (EventTarget) document;
        }
        public Element getElement() {
            return null;
        }
        public boolean isBefore(TimedElement other) {
            return false;
        }
        protected void currentIntervalWillUpdate() {
            if (animationTickRunnable != null) {
                animationTickRunnable.resume();
            }
        }
    }
    protected static class DebugAnimationTickRunnable extends AnimationTickRunnable {
        float t = 0f;
        public DebugAnimationTickRunnable(RunnableQueue q, SVGAnimationEngine eng) {
            super(q, eng);
            waitTime = Long.MAX_VALUE;
            new Thread() {
                public void run() {
                    java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
                    System.out.println("Enter times.");
                    for (;;) {
                        String s;
                        try {
                            s = r.readLine();
                        } catch (java.io.IOException e) {
                            s = null;
                        }
                        if (s == null) {
                            System.exit(0);
                        }
                        t = Float.parseFloat(s);
                        DebugAnimationTickRunnable.this.resume();
                    }
                }
            }.start();
        }
        public void resume() {
            waitTime = 0;
            Object lock = q.getIteratorLock();
            synchronized (lock) {
                lock.notify();
            }
        }
        public long getWaitTime() {
            long wt = waitTime;
            waitTime = Long.MAX_VALUE;
            return wt;
        }
        public void run() {
            SVGAnimationEngine eng = getAnimationEngine();
            synchronized (eng) {
                try {
                    try {
                        eng.tick(t, false);
                    } catch (AnimationException ex) {
                        throw new BridgeException
                            (eng.ctx, ex.getElement().getElement(),
                             ex.getMessage());
                    }
                } catch (Exception ex) {
                    if (eng.ctx.getUserAgent() == null) {
                        ex.printStackTrace();
                    } else {
                        eng.ctx.getUserAgent().displayError(ex);
                    }
                }
            }
        }
    }
    protected static class AnimationTickRunnable
            implements RunnableQueue.IdleRunnable {
        protected Calendar time = Calendar.getInstance();
        protected long waitTime;
        protected RunnableQueue q;
        private static final int NUM_TIMES = 8;
        protected long[] times = new long[NUM_TIMES];
        protected long sumTime;
        protected int timeIndex;
        protected WeakReference engRef;
        protected static final int MAX_EXCEPTION_COUNT = 10;
        protected int exceptionCount;
        public AnimationTickRunnable(RunnableQueue q, SVGAnimationEngine eng) {
            this.q = q;
            this.engRef = new WeakReference(eng);
            Arrays.fill(times, 100);
            sumTime = 100 * NUM_TIMES;
        }
        public void resume() {
            waitTime = 0;
            Object lock = q.getIteratorLock();
            synchronized (lock) {
                lock.notify();
            }
        }
        public long getWaitTime() {
            return waitTime;
        }
        public void run() {
            SVGAnimationEngine eng = getAnimationEngine();
            synchronized (eng) {
                int animationLimitingMode = eng.animationLimitingMode;
                float animationLimitingAmount = eng.animationLimitingAmount;
                try {
                    try {
                        long before = System.currentTimeMillis();
                        time.setTime(new Date(before));
                        float t = eng.timedDocumentRoot.convertWallclockTime(time);
                        float t2 = eng.tick(t, false);
                        long after = System.currentTimeMillis();
                        long dur = after - before;
                        if (dur == 0) {
                            dur = 1;
                        }
                        sumTime -= times[timeIndex];
                        sumTime += dur;
                        times[timeIndex] = dur;
                        timeIndex = (timeIndex + 1) % NUM_TIMES;
                        if (t2 == Float.POSITIVE_INFINITY) {
                            waitTime = Long.MAX_VALUE;
                        } else {
                            waitTime = before + (long) (t2 * 1000) - 1000;
                            if (waitTime < after) {
                                waitTime = after;
                            }
                            if (animationLimitingMode != 0) {
                                float ave = (float) sumTime / NUM_TIMES;
                                float delay;
                                if (animationLimitingMode == 1) {
                                    delay = ave / animationLimitingAmount - ave;
                                } else {
                                    delay = 1000f / animationLimitingAmount - ave;
                                }
                                long newWaitTime = after + (long) delay;
                                if (newWaitTime > waitTime) {
                                    waitTime = newWaitTime;
                                }
                            }
                        }
                    } catch (AnimationException ex) {
                        throw new BridgeException
                            (eng.ctx, ex.getElement().getElement(),
                             ex.getMessage());
                    }
                    exceptionCount = 0;
                } catch (Exception ex) {
                    if (++exceptionCount < MAX_EXCEPTION_COUNT) {
                        if (eng.ctx.getUserAgent() == null) {
                            ex.printStackTrace();
                        } else {
                            eng.ctx.getUserAgent().displayError(ex);
                        }
                    }
                }
                if (animationLimitingMode == 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        protected SVGAnimationEngine getAnimationEngine() {
            return (SVGAnimationEngine) engRef.get();
        }
    }
    protected class AnimationThread extends Thread {
        protected Calendar time = Calendar.getInstance();
        protected RunnableQueue runnableQueue =
            ctx.getUpdateManager().getUpdateRunnableQueue();
        protected Ticker ticker = new Ticker();
        public void run() {
            if (true) {
                for (;;) {
                    time.setTime(new Date());
                    ticker.t = timedDocumentRoot.convertWallclockTime(time);
                    try {
                        runnableQueue.invokeAndWait(ticker);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                ticker.t = 1;
                while (ticker.t < 10) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }
                    try {
                        runnableQueue.invokeAndWait(ticker);
                    } catch (InterruptedException e) {
                        return;
                    }
                    ticker.t++;
                }
            }
        }
        protected class Ticker implements Runnable {
            protected float t;
            public void run() {
                tick(t, false);
            }
        }
    }
    protected interface Factory {
        AnimatableValue createValue(AnimationTarget target, String ns,
                                    String ln, boolean isCSS, String s);
        AnimatableValue createValue(AnimationTarget target, String pn, Value v);
    }
    protected abstract class CSSValueFactory implements Factory {
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return createValue(target, ln, createCSSValue(target, ln, s));
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            CSSStylableElement elt = (CSSStylableElement) target.getElement();
            v = computeValue(elt, pn, v);
            return createAnimatableValue(target, pn, v);
        }
        protected abstract AnimatableValue createAnimatableValue
            (AnimationTarget target, String pn, Value v);
        protected Value createCSSValue(AnimationTarget t, String pn, String s) {
            CSSStylableElement elt = (CSSStylableElement) t.getElement();
            Value v = cssEngine.parsePropertyValue(elt, pn, s);
            return computeValue(elt, pn, v);
        }
        protected Value computeValue(CSSStylableElement elt, String pn,
                                     Value v) {
            ValueManager[] vms = cssEngine.getValueManagers();
            int idx = cssEngine.getPropertyIndex(pn);
            if (idx != -1) {
                if (v.getCssValueType() == CSSValue.CSS_INHERIT) {
                    elt = CSSEngine.getParentCSSStylableElement(elt);
                    if (elt != null) {
                        return cssEngine.getComputedStyle(elt, null, idx);
                    }
                    return vms[idx].getDefaultValue();
                }
                v = vms[idx].computeValue(elt, null, cssEngine, idx,
                                          dummyStyleMap, v);
            }
            return v;
        }
    }
    protected class AnimatableBooleanValueFactory implements Factory {
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableBooleanValue(target, "true".equals(s));
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableBooleanValue(target,
                                              "true".equals(v.getCssText()));
        }
    }
    protected class AnimatableIntegerValueFactory implements Factory {
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableIntegerValue(target, Integer.parseInt(s));
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableIntegerValue(target,
                                              Math.round(v.getFloatValue()));
        }
    }
    protected class AnimatableNumberValueFactory implements Factory {
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableNumberValue(target, Float.parseFloat(s));
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableNumberValue(target, v.getFloatValue());
        }
    }
    protected class AnimatableNumberOrPercentageValueFactory
            implements Factory {
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            float v;
            boolean pc;
            if (s.charAt(s.length() - 1) == '%') {
                v = Float.parseFloat(s.substring(0, s.length() - 1));
                pc = true;
            } else {
                v = Float.parseFloat(s);
                pc = false;
            }
            return new AnimatableNumberOrPercentageValue(target, v, pc);
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            switch (v.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_PERCENTAGE:
                    return new AnimatableNumberOrPercentageValue
                        (target, v.getFloatValue(), true);
                case CSSPrimitiveValue.CSS_NUMBER:
                    return new AnimatableNumberOrPercentageValue
                        (target, v.getFloatValue());
            }
            return null;
        }
    }
    protected class AnimatablePreserveAspectRatioValueFactory implements Factory {
        protected short align;
        protected short meetOrSlice;
        protected PreserveAspectRatioParser parser =
            new PreserveAspectRatioParser();
        protected DefaultPreserveAspectRatioHandler handler =
            new DefaultPreserveAspectRatioHandler() {
            public void startPreserveAspectRatio() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_UNKNOWN;
                meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_UNKNOWN;
            }
            public void none() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE;
            }
            public void xMaxYMax() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMAX;
            }
            public void xMaxYMid() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMID;
            }
            public void xMaxYMin() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMIN;
            }
            public void xMidYMax() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMAX;
            }
            public void xMidYMid() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
            }
            public void xMidYMin() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMIN;
            }
            public void xMinYMax() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMAX;
            }
            public void xMinYMid() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMID;
            }
            public void xMinYMin() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMIN;
            }
            public void meet() throws ParseException {
                meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
            }
            public void slice() throws ParseException {
                meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_SLICE;
            }
        };
        public AnimatablePreserveAspectRatioValueFactory() {
            parser.setPreserveAspectRatioHandler(handler);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatablePreserveAspectRatioValue(target, align,
                                                              meetOrSlice);
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }
    protected class AnimatableLengthValueFactory implements Factory {
        protected short type;
        protected float value;
        protected LengthParser parser = new LengthParser();
        protected LengthHandler handler = new DefaultLengthHandler() {
            public void startLength() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_NUMBER;
            }
            public void lengthValue(float v) throws ParseException {
                value = v;
            }
            public void em() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_EMS;
            }
            public void ex() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_EXS;
            }
            public void in() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_IN;
            }
            public void cm() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_CM;
            }
            public void mm() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_MM;
            }
            public void pc() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PC;
            }
            public void pt() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PT;
            }
            public void px() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PX;
            }
            public void percentage() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PERCENTAGE;
            }
            public void endLength() throws ParseException {
            }
        };
        public AnimatableLengthValueFactory() {
            parser.setLengthHandler(handler);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            short pcInterp = target.getPercentageInterpretation(ns, ln, isCSS);
            try {
                parser.parse(s);
                return new AnimatableLengthValue
                    (target, type, value, pcInterp);
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableIntegerValue(target,
                                              Math.round(v.getFloatValue()));
        }
    }
    protected class AnimatableLengthListValueFactory implements Factory {
        protected LengthListParser parser = new LengthListParser();
        protected LengthArrayProducer producer = new LengthArrayProducer();
        public AnimatableLengthListValueFactory() {
            parser.setLengthListHandler(producer);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                short pcInterp = target.getPercentageInterpretation
                    (ns, ln, isCSS);
                parser.parse(s);
                return new AnimatableLengthListValue
                    (target, producer.getLengthTypeArray(),
                     producer.getLengthValueArray(),
                     pcInterp);
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }
    protected class AnimatableNumberListValueFactory implements Factory {
        protected NumberListParser parser = new NumberListParser();
        protected FloatArrayProducer producer = new FloatArrayProducer();
        public AnimatableNumberListValueFactory() {
            parser.setNumberListHandler(producer);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatableNumberListValue(target,
                                                     producer.getFloatArray());
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }
    protected class AnimatableRectValueFactory implements Factory {
        protected NumberListParser parser = new NumberListParser();
        protected FloatArrayProducer producer = new FloatArrayProducer();
        public AnimatableRectValueFactory() {
            parser.setNumberListHandler(producer);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                float[] r = producer.getFloatArray();
                if (r.length != 4) {
                    return null;
                }
                return new AnimatableRectValue(target, r[0], r[1], r[2], r[3]);
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }
    protected class AnimatablePointListValueFactory implements Factory {
        protected PointsParser parser = new PointsParser();
        protected FloatArrayProducer producer = new FloatArrayProducer();
        public AnimatablePointListValueFactory() {
            parser.setPointsHandler(producer);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatablePointListValue(target,
                                                    producer.getFloatArray());
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }
    protected class AnimatablePathDataFactory implements Factory {
        protected PathParser parser = new PathParser();
        protected PathArrayProducer producer = new PathArrayProducer();
        public AnimatablePathDataFactory() {
            parser.setPathHandler(producer);
        }
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatablePathDataValue
                    (target, producer.getPathCommands(),
                     producer.getPathParameters());
            } catch (ParseException e) {
                return null;
            }
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }
    protected class UncomputedAnimatableStringValueFactory implements Factory {
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableStringValue(target, s);
        }
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableStringValue(target, v.getCssText());
        }
    }
    protected class AnimatableLengthOrIdentFactory extends CSSValueFactory {
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v instanceof StringValue) {
                return new AnimatableLengthOrIdentValue(target,
                                                        v.getStringValue());
            }
            short pcInterp = target.getPercentageInterpretation(null, pn, true);
            FloatValue fv = (FloatValue) v;
            return new AnimatableLengthOrIdentValue
                (target, fv.getPrimitiveType(), fv.getFloatValue(), pcInterp);
        }
    }
    protected class AnimatableNumberOrIdentFactory extends CSSValueFactory {
        protected boolean numericIdents;
        public AnimatableNumberOrIdentFactory(boolean numericIdents) {
            this.numericIdents = numericIdents;
        }
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v instanceof StringValue) {
                return new AnimatableNumberOrIdentValue(target,
                                                        v.getStringValue());
            }
            FloatValue fv = (FloatValue) v;
            return new AnimatableNumberOrIdentValue(target, fv.getFloatValue(),
                                                    numericIdents);
        }
    }
    protected class AnimatableAngleValueFactory extends CSSValueFactory {
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            FloatValue fv = (FloatValue) v;
            short unit;
            switch (fv.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_NUMBER:
                case CSSPrimitiveValue.CSS_DEG:
                    unit = SVGAngle.SVG_ANGLETYPE_DEG;
                    break;
                case CSSPrimitiveValue.CSS_RAD:
                    unit = SVGAngle.SVG_ANGLETYPE_RAD;
                    break;
                case CSSPrimitiveValue.CSS_GRAD:
                    unit = SVGAngle.SVG_ANGLETYPE_GRAD;
                    break;
                default:
                    return null;
            }
            return new AnimatableAngleValue(target, fv.getFloatValue(), unit);
        }
    }
    protected class AnimatableAngleOrIdentFactory extends CSSValueFactory {
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v instanceof StringValue) {
                return new AnimatableAngleOrIdentValue(target,
                                                       v.getStringValue());
            }
            FloatValue fv = (FloatValue) v;
            short unit;
            switch (fv.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_NUMBER:
                case CSSPrimitiveValue.CSS_DEG:
                    unit = SVGAngle.SVG_ANGLETYPE_DEG;
                    break;
                case CSSPrimitiveValue.CSS_RAD:
                    unit = SVGAngle.SVG_ANGLETYPE_RAD;
                    break;
                case CSSPrimitiveValue.CSS_GRAD:
                    unit = SVGAngle.SVG_ANGLETYPE_GRAD;
                    break;
                default:
                    return null;
            }
            return new AnimatableAngleOrIdentValue(target, fv.getFloatValue(),
                                                   unit);
        }
    }
    protected class AnimatableColorValueFactory extends CSSValueFactory {
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            Paint p = PaintServer.convertPaint
                (target.getElement(), null, v, 1.0f, ctx);
            if (p instanceof Color) {
                Color c = (Color) p;
                return new AnimatableColorValue(target,
                                                c.getRed() / 255f,
                                                c.getGreen() / 255f,
                                                c.getBlue() / 255f);
            }
            return null;
        }
    }
    protected class AnimatablePaintValueFactory extends CSSValueFactory {
        protected AnimatablePaintValue createColorPaintValue(AnimationTarget t,
                                                             Color c) {
            return AnimatablePaintValue.createColorPaintValue
                (t, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
        }
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                switch (v.getPrimitiveType()) {
                    case CSSPrimitiveValue.CSS_IDENT:
                        return AnimatablePaintValue.createNonePaintValue(target);
                    case CSSPrimitiveValue.CSS_RGBCOLOR: {
                        Paint p = PaintServer.convertPaint
                            (target.getElement(), null, v, 1.0f, ctx);
                        return createColorPaintValue(target, (Color) p);
                    }
                    case CSSPrimitiveValue.CSS_URI:
                        return AnimatablePaintValue.createURIPaintValue
                            (target, v.getStringValue());
                }
            } else {
                Value v1 = v.item(0);
                switch (v1.getPrimitiveType()) {
                    case CSSPrimitiveValue.CSS_RGBCOLOR: {
                        Paint p = PaintServer.convertPaint
                            (target.getElement(), null, v, 1.0f, ctx);
                        return createColorPaintValue(target, (Color) p);
                    }
                    case CSSPrimitiveValue.CSS_URI: {
                        Value v2 = v.item(1);
                        switch (v2.getPrimitiveType()) {
                            case CSSPrimitiveValue.CSS_IDENT:
                                return AnimatablePaintValue.createURINonePaintValue
                                    (target, v1.getStringValue());
                            case CSSPrimitiveValue.CSS_RGBCOLOR: {
                                Paint p = PaintServer.convertPaint
                                    (target.getElement(), null, v.item(1), 1.0f, ctx);
                                return createColorPaintValue(target, (Color) p);
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
    protected class AnimatableStringValueFactory extends CSSValueFactory {
        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            return new AnimatableStringValue(target, v.getCssText());
        }
    }
}
