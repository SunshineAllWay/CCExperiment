package org.apache.batik.anim.timing;
import java.util.*;
import org.apache.batik.anim.AnimationException;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.parser.ClockHandler;
import org.apache.batik.parser.ClockParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.util.SMILConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
public abstract class TimedElement implements SMILConstants {
    public static final int FILL_REMOVE = 0;
    public static final int FILL_FREEZE = 1;
    public static final int RESTART_ALWAYS          = 0;
    public static final int RESTART_WHEN_NOT_ACTIVE = 1;
    public static final int RESTART_NEVER           = 2;
    public static final float INDEFINITE = Float.POSITIVE_INFINITY;
    public static final float UNRESOLVED = Float.NaN;
    protected TimedDocumentRoot root;
    protected TimeContainer parent;
    protected TimingSpecifier[] beginTimes;
    protected TimingSpecifier[] endTimes;
    protected float simpleDur;
    protected boolean durMedia;
    protected float repeatCount;
    protected float repeatDur;
    protected int currentRepeatIteration;
    protected float lastRepeatTime;
    protected int fillMode;
    protected int restartMode;
    protected float min;
    protected boolean minMedia;
    protected float max;
    protected boolean maxMedia;
    protected boolean isActive;
    protected boolean isFrozen;
    protected float lastSampleTime;
    protected float repeatDuration;
    protected List beginInstanceTimes = new ArrayList();
    protected List endInstanceTimes = new ArrayList();
    protected Interval currentInterval;
    protected float lastIntervalEnd;
    protected Interval previousInterval;
    protected LinkedList beginDependents = new LinkedList();
    protected LinkedList endDependents = new LinkedList();
    protected boolean shouldUpdateCurrentInterval = true;
    protected boolean hasParsed;
    protected Map handledEvents = new HashMap();
    protected boolean isSampling;
    protected boolean hasPropagated;
    public TimedElement() {
        beginTimes = new TimingSpecifier[0];
        endTimes = beginTimes;
        simpleDur = UNRESOLVED;
        repeatCount = UNRESOLVED;
        repeatDur = UNRESOLVED;
        lastRepeatTime = UNRESOLVED;
        max = INDEFINITE;
        lastSampleTime = UNRESOLVED;
        lastIntervalEnd = Float.NEGATIVE_INFINITY;
    }
    public TimedDocumentRoot getRoot() {
        return root;
    }
    public float getActiveTime() {
        return lastSampleTime;
    }
    public float getSimpleTime() {
        return lastSampleTime - lastRepeatTime;
    }
    protected float addInstanceTime(InstanceTime time, boolean isBegin) {
        hasPropagated = true;
        List instanceTimes = isBegin ? beginInstanceTimes : endInstanceTimes;
        int index = Collections.binarySearch(instanceTimes, time);
        if (index < 0) {
            index = -(index + 1);
        }
        instanceTimes.add(index, time);
        shouldUpdateCurrentInterval = true;
        float ret;
        if (root.isSampling() && !isSampling) {
            ret = sampleAt(root.getCurrentTime(), root.isHyperlinking());
        } else {
            ret = Float.POSITIVE_INFINITY;
        }
        hasPropagated = false;
        root.currentIntervalWillUpdate();
        return ret;
    }
    protected float removeInstanceTime(InstanceTime time, boolean isBegin) {
        hasPropagated = true;
        List instanceTimes = isBegin ? beginInstanceTimes : endInstanceTimes;
        int index = Collections.binarySearch(instanceTimes, time);
        for (int i = index; i >= 0; i--) {
            InstanceTime it = (InstanceTime) instanceTimes.get(i);
            if (it == time) {
                instanceTimes.remove(i);
                break;
            }
            if (it.compareTo(time) != 0) {
                break;
            }
        }
        int len = instanceTimes.size();
        for (int i = index + 1; i < len; i++) {
            InstanceTime it = (InstanceTime) instanceTimes.get(i);
            if (it == time) {
                instanceTimes.remove(i);
                break;
            }
            if (it.compareTo(time) != 0) {
                break;
            }
        }
        shouldUpdateCurrentInterval = true;
        float ret;
        if (root.isSampling() && !isSampling) {
            ret = sampleAt(root.getCurrentTime(), root.isHyperlinking());
        } else {
            ret = Float.POSITIVE_INFINITY;
        }
        hasPropagated = false;
        root.currentIntervalWillUpdate();
        return ret;
    }
    protected float instanceTimeChanged(InstanceTime time, boolean isBegin) {
        hasPropagated = true;
        shouldUpdateCurrentInterval = true;
        float ret;
        if (root.isSampling() && !isSampling) {
            ret = sampleAt(root.getCurrentTime(), root.isHyperlinking());
        } else {
            ret = Float.POSITIVE_INFINITY;
        }
        hasPropagated = false;
        return ret;
    }
    protected void addDependent(TimingSpecifier dependent, boolean forBegin) {
        if (forBegin) {
            beginDependents.add(dependent);
        } else {
            endDependents.add(dependent);
        }
    }
    protected void removeDependent(TimingSpecifier dependent,
                                   boolean forBegin) {
        if (forBegin) {
            beginDependents.remove(dependent);
        } else {
            endDependents.remove(dependent);
        }
    }
    public float getSimpleDur() {
        if (durMedia) {
            return getImplicitDur();
        } else if (isUnresolved(simpleDur)) {
            if (isUnresolved(repeatCount) && isUnresolved(repeatDur)
                    && endTimes.length > 0) {
                return INDEFINITE;
            }
            return getImplicitDur();
        } else {
            return simpleDur;
        }
    }
    public static boolean isUnresolved(float t) {
        return Float.isNaN(t);
    }
    public float getActiveDur(float B, float end) {
        float d = getSimpleDur();
        float PAD;
        if (!isUnresolved(end) && d == INDEFINITE) {
            PAD = minusTime(end, B);
            repeatDuration = minTime(max, maxTime(min, PAD));
            return repeatDuration;
        }
        float IAD;
        if (d == 0) {
            IAD = 0;
        } else {
            if (isUnresolved(repeatDur) && isUnresolved(repeatCount)) {
                IAD = d;
            } else {
                float p1 = isUnresolved(repeatCount)
                                ? INDEFINITE
                                : multiplyTime(d, repeatCount);
                float p2 = isUnresolved(repeatDur)
                                ? INDEFINITE
                                : repeatDur;
                IAD = minTime(minTime(p1, p2), INDEFINITE);
            }
        }
        if (isUnresolved(end) || end == INDEFINITE) {
            PAD = IAD;
        } else {
            PAD = minTime(IAD, minusTime(end, B));
        }
        repeatDuration = IAD;
        return minTime(max, maxTime(min, PAD));
    }
    protected float minusTime(float t1, float t2) {
        if (isUnresolved(t1) || isUnresolved(t2)) {
            return UNRESOLVED;
        }
        if (t1 == INDEFINITE || t2 == INDEFINITE) {
            return INDEFINITE;
        }
        return t1 - t2;
    }
    protected float multiplyTime(float t, float n) {
        if (isUnresolved(t) || t == INDEFINITE) {
            return t;
        }
        return t * n;
    }
    protected float minTime(float t1, float t2) {
        if (t1 == 0.0f || t2 == 0.0f) {
            return 0.0f;
        }
        if ((t1 == INDEFINITE || isUnresolved(t1))
                && t2 != INDEFINITE && !isUnresolved(t2)) {
            return t2;
        }
        if ((t2 == INDEFINITE || isUnresolved(t2))
                && t1 != INDEFINITE && !isUnresolved(t1)) {
            return t1;
        }
        if (t1 == INDEFINITE && isUnresolved(t2)
                || isUnresolved(t1) && t2 == INDEFINITE) {
            return INDEFINITE;
        }
        if (t1 < t2) {
            return t1;
        }
        return t2;
    }
    protected float maxTime(float t1, float t2) {
        if ((t1 == INDEFINITE || isUnresolved(t1))
                && t2 != INDEFINITE && !isUnresolved(t2)) {
            return t1;
        }
        if ((t2 == INDEFINITE || isUnresolved(t2))
                && t1 != INDEFINITE && !isUnresolved(t1)) {
            return t2;
        }
        if (t1 == INDEFINITE && isUnresolved(t2)
                || isUnresolved(t1) && t2 == INDEFINITE) {
            return UNRESOLVED;
        }
        if (t1 > t2) {
            return t1;
        }
        return t2;
    }
    protected float getImplicitDur() {
        return UNRESOLVED;
    }
    protected float notifyNewInterval(Interval interval) {
        float dependentMinTime = Float.POSITIVE_INFINITY;
        Iterator i = beginDependents.iterator();
        while (i.hasNext()) {
            TimingSpecifier ts = (TimingSpecifier) i.next();
            float t = ts.newInterval(interval);
            if (t < dependentMinTime) {
                dependentMinTime = t;
            }
        }
        i = endDependents.iterator();
        while (i.hasNext()) {
            TimingSpecifier ts = (TimingSpecifier) i.next();
            float t = ts.newInterval(interval);
            if (t < dependentMinTime) {
                dependentMinTime = t;
            }
        }
        return dependentMinTime;
    }
    protected float notifyRemoveInterval(Interval interval) {
        float dependentMinTime = Float.POSITIVE_INFINITY;
        Iterator i = beginDependents.iterator();
        while (i.hasNext()) {
            TimingSpecifier ts = (TimingSpecifier) i.next();
            float t = ts.removeInterval(interval);
            if (t < dependentMinTime) {
                dependentMinTime = t;
            }
        }
        i = endDependents.iterator();
        while (i.hasNext()) {
            TimingSpecifier ts = (TimingSpecifier) i.next();
            float t = ts.removeInterval(interval);
            if (t < dependentMinTime) {
                dependentMinTime = t;
            }
        }
        return dependentMinTime;
    }
    protected float sampleAt(float parentSimpleTime, boolean hyperlinking) {
        isSampling = true;
        float time = parentSimpleTime; 
        Iterator i = handledEvents.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            Event evt = (Event) e.getKey();
            Set ts = (Set) e.getValue();
            Iterator j = ts.iterator();
            boolean hasBegin = false, hasEnd = false;
            while (j.hasNext() && !(hasBegin && hasEnd)) {
                EventLikeTimingSpecifier t =
                    (EventLikeTimingSpecifier) j.next();
                if (t.isBegin()) {
                    hasBegin = true;
                } else {
                    hasEnd = true;
                }
            }
            boolean useBegin, useEnd;
            if (hasBegin && hasEnd) {
                useBegin = !isActive || restartMode == RESTART_ALWAYS;
                useEnd = !useBegin;
            } else if (hasBegin && (!isActive ||
                        restartMode == RESTART_ALWAYS)) {
                useBegin = true;
                useEnd = false;
            } else if (hasEnd && isActive) {
                useBegin = false;
                useEnd = true;
            } else {
                continue;
            }
            j = ts.iterator();
            while (j.hasNext()) {
                EventLikeTimingSpecifier t =
                    (EventLikeTimingSpecifier) j.next();
                boolean isBegin = t.isBegin();
                if (isBegin && useBegin || !isBegin && useEnd) {
                    t.resolve(evt);
                    shouldUpdateCurrentInterval = true;
                }
            }
        }
        handledEvents.clear();
        if (currentInterval != null) {
            float begin = currentInterval.getBegin();
            if (lastSampleTime < begin && time >= begin) {
                if (!isActive) {
                    toActive(begin);
                }
                isActive = true;
                isFrozen = false;
                lastRepeatTime = begin;
                fireTimeEvent
                    (SMIL_BEGIN_EVENT_NAME, currentInterval.getBegin(), 0);
            }
        }
        boolean hasEnded = currentInterval != null
            && time >= currentInterval.getEnd();
        if (currentInterval != null) {
            float begin = currentInterval.getBegin();
            if (time >= begin) {
                float d = getSimpleDur();
                while (time - lastRepeatTime >= d
                        && lastRepeatTime + d < begin + repeatDuration) {
                    lastRepeatTime += d;
                    currentRepeatIteration++;
                    fireTimeEvent(root.getRepeatEventName(), lastRepeatTime,
                                  currentRepeatIteration);
                }
            }
        }
        float dependentMinTime = Float.POSITIVE_INFINITY;
        if (hyperlinking) {
            shouldUpdateCurrentInterval = true;
        }
        while (shouldUpdateCurrentInterval || hasEnded) {
            if (hasEnded) {
                previousInterval = currentInterval;
                isActive = false;
                isFrozen = fillMode == FILL_FREEZE;
                toInactive(false, isFrozen);
                fireTimeEvent(SMIL_END_EVENT_NAME, currentInterval.getEnd(), 0);
            }
            boolean first =
                currentInterval == null && previousInterval == null;
            if (currentInterval != null && hyperlinking) {
                isActive = false;
                isFrozen = false;
                toInactive(false, false);
                currentInterval = null;
            }
            if (currentInterval == null || hasEnded) {
                if (first || hyperlinking || restartMode != RESTART_NEVER) {
                    float beginAfter;
                    boolean incl = true;
                    if (first || hyperlinking) {
                        beginAfter = Float.NEGATIVE_INFINITY;
                    } else {
                        beginAfter = previousInterval.getEnd();
                        incl = beginAfter != previousInterval.getBegin();
                    }
                    Interval interval =
                        computeInterval(first, false, beginAfter, incl);
                    if (interval == null) {
                        currentInterval = null;
                    } else {
                        float dmt = selectNewInterval(time, interval);
                        if (dmt < dependentMinTime) {
                            dependentMinTime = dmt;
                        }
                    }
                } else {
                    currentInterval = null;
                }
            } else {
                float currentBegin = currentInterval.getBegin();
                if (currentBegin > time) {
                    float beginAfter;
                    boolean incl = true;
                    if (previousInterval == null) {
                        beginAfter = Float.NEGATIVE_INFINITY;
                    } else {
                        beginAfter = previousInterval.getEnd();
                        incl = beginAfter != previousInterval.getBegin();
                    }
                    Interval interval =
                        computeInterval(false, false, beginAfter, incl);
                    float dmt = notifyRemoveInterval(currentInterval);
                    if (dmt < dependentMinTime) {
                        dependentMinTime = dmt;
                    }
                    if (interval == null) {
                        currentInterval = null;
                    } else {
                        dmt = selectNewInterval(time, interval);
                        if (dmt < dependentMinTime) {
                            dependentMinTime = dmt;
                        }
                    }
                } else {
                    Interval interval =
                        computeInterval(false, true, currentBegin, true);
                    float newEnd = interval.getEnd();
                    if (currentInterval.getEnd() != newEnd) {
                        float dmt =
                            currentInterval.setEnd
                                (newEnd, interval.getEndInstanceTime());
                        if (dmt < dependentMinTime) {
                            dependentMinTime = dmt;
                        }
                    }
                }
            }
            shouldUpdateCurrentInterval = false;
            hyperlinking = false;
            hasEnded = currentInterval != null && time >= currentInterval.getEnd();
        }
        float d = getSimpleDur();
        if (isActive && !isFrozen) {
            if (time - currentInterval.getBegin() >= repeatDuration) {
                isFrozen = fillMode == FILL_FREEZE;
                toInactive(true, isFrozen);
            } else {
                sampledAt(time - lastRepeatTime, d, currentRepeatIteration);
            }
        }
        if (isFrozen) {
            float t;
            boolean atLast;
            if (isActive) {
                t = currentInterval.getBegin() + repeatDuration - lastRepeatTime;
                atLast = lastRepeatTime + d == currentInterval.getBegin() + repeatDuration;    
            } else {                                                                           
                t = previousInterval.getEnd() - lastRepeatTime;                                
                atLast = lastRepeatTime + d == previousInterval.getEnd();                      
            }
            if (atLast) {
                sampledLastValue(currentRepeatIteration);
            } else {
                sampledAt(t % d, d, currentRepeatIteration);
            }
        } else if (!isActive) {
        }
        isSampling = false;
        lastSampleTime = time;
        if (currentInterval != null) {
            float t = currentInterval.getBegin() - time;
            if (t <= 0) {
                t = isConstantAnimation() || isFrozen ? currentInterval.getEnd() - time : 0;
            }
            if (dependentMinTime < t) {
                return dependentMinTime;
            }
            return t;
        }
        return dependentMinTime;
    }
    protected boolean endHasEventConditions() {
        for (int i = 0; i < endTimes.length; i++) {
            if (endTimes[i].isEventCondition()) {
                return true;
            }
        }
        return false;
    }
    protected float selectNewInterval(float time, Interval interval) {
        currentInterval = interval;
        float dmt = notifyNewInterval(currentInterval);
        float beginEventTime = currentInterval.getBegin();
        if (time >= beginEventTime) {
            lastRepeatTime = beginEventTime;
            if (beginEventTime < 0) {
                beginEventTime = 0;
            }
            toActive(beginEventTime);
            isActive = true;
            isFrozen = false;
            fireTimeEvent(SMIL_BEGIN_EVENT_NAME, beginEventTime, 0);
            float d = getSimpleDur();
            float end = currentInterval.getEnd();
            while (time - lastRepeatTime >= d
                    && lastRepeatTime + d < end) {
                lastRepeatTime += d;
                currentRepeatIteration++;
                fireTimeEvent(root.getRepeatEventName(), lastRepeatTime,
                              currentRepeatIteration);
            }
        }
        return dmt;
    }
    protected Interval computeInterval(boolean first, boolean fixedBegin,
                                       float beginAfter, boolean incl) {
        Iterator beginIterator = beginInstanceTimes.iterator();
        Iterator endIterator = endInstanceTimes.iterator();
        float parentSimpleDur = parent.getSimpleDur();
        InstanceTime endInstanceTime = endIterator.hasNext()
            ? (InstanceTime) endIterator.next()
            : null;
        boolean firstEnd = true;
        InstanceTime beginInstanceTime = null;
        InstanceTime nextBeginInstanceTime = null;
        for (;;) {
            float tempBegin;
            if (fixedBegin) {
                tempBegin = beginAfter;
                while (beginIterator.hasNext()) {
                    nextBeginInstanceTime = (InstanceTime) beginIterator.next();
                    if (nextBeginInstanceTime.getTime() > tempBegin) {
                        break;
                    }
                }
            } else {
                for (;;) {
                    if (!beginIterator.hasNext()) {
                        return null;
                    }
                    beginInstanceTime = (InstanceTime) beginIterator.next();
                    tempBegin = beginInstanceTime.getTime();
                    if (incl && tempBegin >= beginAfter
                            || !incl && tempBegin > beginAfter) {
                        if (beginIterator.hasNext()) {
                            nextBeginInstanceTime =
                                (InstanceTime) beginIterator.next();
                            if (beginInstanceTime.getTime()
                                    == nextBeginInstanceTime.getTime()) {
                                nextBeginInstanceTime = null;
                                continue;
                            }
                        }
                        break;
                    }
                }
            }
            if (tempBegin >= parentSimpleDur) {
                return null;
            }
            float tempEnd;
            if (endTimes.length == 0) {
                tempEnd = tempBegin + getActiveDur(tempBegin, INDEFINITE);
            } else {
                if (endInstanceTimes.isEmpty()) {
                    tempEnd = UNRESOLVED;
                } else {
                    tempEnd = endInstanceTime.getTime();
                    if (first && !firstEnd && tempEnd == tempBegin
                            || !first && currentInterval != null
                                && tempEnd == currentInterval.getEnd()
                                && (incl && beginAfter >= tempEnd
                                        || !incl && beginAfter > tempEnd)) {
                        for (;;) {
                            if (!endIterator.hasNext()) {
                                if (endHasEventConditions()) {
                                    tempEnd = UNRESOLVED;
                                    break;
                                }
                                return null;
                            }
                            endInstanceTime = (InstanceTime) endIterator.next();
                            tempEnd = endInstanceTime.getTime();
                            if (tempEnd > tempBegin) {
                                break;
                            }
                        }
                    }
                    firstEnd = false;
                    for (;;) {
                        if (tempEnd >= tempBegin) {
                            break;
                        }
                        if (!endIterator.hasNext()) {
                            if (endHasEventConditions()) {
                                tempEnd = UNRESOLVED;
                                break;
                            }
                            return null;
                        }
                        endInstanceTime = (InstanceTime) endIterator.next();
                        tempEnd = endInstanceTime.getTime();
                    }
                }
                float ad = getActiveDur(tempBegin, tempEnd);
                tempEnd = tempBegin + ad;
            }
            if (!first || tempEnd > 0 || tempBegin == 0 && tempEnd == 0
                    || isUnresolved(tempEnd)) {
                if (restartMode == RESTART_ALWAYS
                        && nextBeginInstanceTime != null) {
                    float nextBegin = nextBeginInstanceTime.getTime();
                    if (nextBegin < tempEnd || isUnresolved(tempEnd)) {
                        tempEnd = nextBegin;
                        endInstanceTime = nextBeginInstanceTime;
                    }
                }
                Interval i = new Interval(tempBegin, tempEnd,
                                          beginInstanceTime, endInstanceTime);
                return i;
            }
            if (fixedBegin) {
                return null;
            }
            beginAfter = tempEnd;
        }
    }
    protected void reset(boolean clearCurrentBegin) {
        Iterator i = beginInstanceTimes.iterator();
        while (i.hasNext()) {
            InstanceTime it = (InstanceTime) i.next();
            if (it.getClearOnReset() &&
                    (clearCurrentBegin
                        || currentInterval == null
                        || currentInterval.getBeginInstanceTime() != it)) {
                i.remove();
            }
        }
        i = endInstanceTimes.iterator();
        while (i.hasNext()) {
            InstanceTime it = (InstanceTime) i.next();
            if (it.getClearOnReset()) {
                i.remove();
            }
        }
        if (isFrozen) {
            removeFill();
        }
        currentRepeatIteration = 0;
        lastRepeatTime = UNRESOLVED;
        isActive = false;
        isFrozen = false;
        lastSampleTime = UNRESOLVED;
    }
    public void parseAttributes(String begin, String dur, String end,
                                String min, String max, String repeatCount,
                                String repeatDur, String fill,
                                String restart) {
        if (!hasParsed) {
            parseBegin(begin);
            parseDur(dur);
            parseEnd(end);
            parseMin(min);
            parseMax(max);
            if (this.min > this.max) {
                this.min = 0f;
                this.max = INDEFINITE;
            }
            parseRepeatCount(repeatCount);
            parseRepeatDur(repeatDur);
            parseFill(fill);
            parseRestart(restart);
            hasParsed = true;
        }
    }
    protected void parseBegin(String begin) {
        try {
            if (begin.length() == 0) {
                begin = SMIL_BEGIN_DEFAULT_VALUE;
            }
            beginTimes = TimingSpecifierListProducer.parseTimingSpecifierList
                (TimedElement.this, true, begin,
                 root.useSVG11AccessKeys, root.useSVG12AccessKeys);
        } catch (ParseException ex) {
            throw createException
                ("attribute.malformed",
                 new Object[] { null, SMIL_BEGIN_ATTRIBUTE });
        }
    }
    protected void parseDur(String dur) {
        if (dur.equals(SMIL_MEDIA_VALUE)) {
            durMedia = true;
            simpleDur = UNRESOLVED;
        } else {
            durMedia = false;
            if (dur.length() == 0 || dur.equals(SMIL_INDEFINITE_VALUE)) {
                simpleDur = INDEFINITE;
            } else {
                try {
                    simpleDur = parseClockValue(dur, false);
                } catch (ParseException e) {
                    throw createException
                        ("attribute.malformed",
                         new Object[] { null, SMIL_DUR_ATTRIBUTE });
                }
                if (simpleDur < 0) {
                    simpleDur = INDEFINITE;
                }
            }
        }
    }
    protected float parseClockValue(String s, boolean parseOffset)
            throws ParseException {
        ClockParser p = new ClockParser(parseOffset);
        class Handler implements ClockHandler {
            protected float v = 0;
            public void clockValue(float newClockValue) {
                v = newClockValue;
            }
        }
        Handler h = new Handler();
        p.setClockHandler(h);
        p.parse(s);
        return h.v;
    }
    protected void parseEnd(String end) {
        try {
            endTimes = TimingSpecifierListProducer.parseTimingSpecifierList
                (TimedElement.this, false, end,
                 root.useSVG11AccessKeys, root.useSVG12AccessKeys);
        } catch (ParseException ex) {
            throw createException
                ("attribute.malformed",
                 new Object[] { null, SMIL_END_ATTRIBUTE });
        }
    }
    protected void parseMin(String min) {
        if (min.equals(SMIL_MEDIA_VALUE)) {
            this.min = 0;
            minMedia = true;
        } else {
            minMedia = false;
            if (min.length() == 0) {
                this.min = 0;
            } else {
                try {
                    this.min = parseClockValue(min, false);
                } catch (ParseException ex) {
                	this.min = 0;
                }
                if (this.min < 0) {
                    this.min = 0;
                }
            }
        }
    }
    protected void parseMax(String max) {
        if (max.equals(SMIL_MEDIA_VALUE)) {
            this.max = INDEFINITE;
            maxMedia = true;
        } else {
            maxMedia = false;
            if (max.length() == 0 || max.equals(SMIL_INDEFINITE_VALUE)) {
                this.max = INDEFINITE;
            } else {
                try {
                    this.max = parseClockValue(max, false);
                } catch (ParseException ex) {
                	this.max = INDEFINITE;
                }
                if (this.max < 0) {
                    this.max = 0;
                }
            }
        }
    }
    protected void parseRepeatCount(String repeatCount) {
        if (repeatCount.length() == 0) {
            this.repeatCount = UNRESOLVED;
        } else if (repeatCount.equals(SMIL_INDEFINITE_VALUE)) {
            this.repeatCount = INDEFINITE;
        } else {
            try {
                this.repeatCount = Float.parseFloat(repeatCount);
                if (this.repeatCount > 0) {
                    return;
                }
            } catch (NumberFormatException ex) {
                throw createException
                    ("attribute.malformed",
                     new Object[] { null, SMIL_REPEAT_COUNT_ATTRIBUTE });
            }
        }
    }
    protected void parseRepeatDur(String repeatDur) {
        try {
            if (repeatDur.length() == 0) {
                this.repeatDur = UNRESOLVED;
            } else if (repeatDur.equals(SMIL_INDEFINITE_VALUE)) {
                this.repeatDur = INDEFINITE;
            } else {
                this.repeatDur = parseClockValue(repeatDur, false);
            }
        } catch (ParseException ex) {
            throw createException
                ("attribute.malformed",
                 new Object[] { null, SMIL_REPEAT_DUR_ATTRIBUTE });
        }
    }
    protected void parseFill(String fill) {
        if (fill.length() == 0 || fill.equals(SMIL_REMOVE_VALUE)) {
            fillMode = FILL_REMOVE;
        } else if (fill.equals(SMIL_FREEZE_VALUE)) {
            fillMode = FILL_FREEZE;
        } else {
            throw createException
                ("attribute.malformed",
                 new Object[] { null, SMIL_FILL_ATTRIBUTE });
        }
    }
    protected void parseRestart(String restart) {
        if (restart.length() == 0 || restart.equals(SMIL_ALWAYS_VALUE)) {
            restartMode = RESTART_ALWAYS;
        } else if (restart.equals(SMIL_WHEN_NOT_ACTIVE_VALUE)) {
            restartMode = RESTART_WHEN_NOT_ACTIVE;
        } else if (restart.equals(SMIL_NEVER_VALUE)) {
            restartMode = RESTART_NEVER;
        } else {
            throw createException
                ("attribute.malformed",
                 new Object[] { null, SMIL_RESTART_ATTRIBUTE });
        }
    }
    public void initialize() {
        for (int i = 0; i < beginTimes.length; i++) {
            beginTimes[i].initialize();
        }
        for (int i = 0; i < endTimes.length; i++) {
            endTimes[i].initialize();
        }
    }
    public void deinitialize() {
        for (int i = 0; i < beginTimes.length; i++) {
            beginTimes[i].deinitialize();
        }
        for (int i = 0; i < endTimes.length; i++) {
            endTimes[i].deinitialize();
        }
    }
    public void beginElement() {
        beginElement(0);
    }
    public void beginElement(float offset) {
        float t = root.convertWallclockTime( Calendar.getInstance());
        InstanceTime it = new InstanceTime(null, t + offset, true);
        addInstanceTime(it, true);
    }
    public void endElement() {
        endElement(0);
    }
    public void endElement(float offset) {
        float t = root.convertWallclockTime(Calendar.getInstance());
        InstanceTime it = new InstanceTime(null, t + offset, true);
        addInstanceTime(it, false);
    }
    public float getLastSampleTime() {
        return lastSampleTime;
    }
    public float getCurrentBeginTime() {
        float begin;
        if (currentInterval == null
                || (begin = currentInterval.getBegin()) < lastSampleTime) {
            return Float.NaN;
        }
        return begin;
    }
    public boolean canBegin() {
        return currentInterval == null
            || isActive && restartMode != RESTART_NEVER;
    }
    public boolean canEnd() {
        return isActive;
    }
    public float getHyperlinkBeginTime() {
        if (isActive) {
            return currentInterval.getBegin();
        }
        if (!beginInstanceTimes.isEmpty()) {
            return ((InstanceTime) beginInstanceTimes.get(0)).getTime();
        }
        return Float.NaN;
    }
    public TimingSpecifier[] getBeginTimingSpecifiers() {
        return (TimingSpecifier[]) beginTimes.clone();
    }
    public TimingSpecifier[] getEndTimingSpecifiers() {
        return (TimingSpecifier[]) endTimes.clone();
    }
    protected void fireTimeEvent(String eventType, float time, int detail) {
        Calendar t = (Calendar) root.getDocumentBeginTime().clone();
        t.add(Calendar.MILLISECOND, (int) Math.round(time * 1e3));
        fireTimeEvent(eventType, t, detail);
    }
    void eventOccurred(TimingSpecifier t, Event e) {
        Set ts = (HashSet) handledEvents.get(e);
        if (ts == null) {
            ts = new HashSet();
            handledEvents.put(e, ts);
        }
        ts.add(t);
        root.currentIntervalWillUpdate();
    }
    protected abstract void fireTimeEvent(String eventType, Calendar time,
                                          int detail);
    protected abstract void toActive(float begin);
    protected abstract void toInactive(boolean stillActive, boolean isFrozen);
    protected abstract void removeFill();
    protected abstract void sampledAt(float simpleTime, float simpleDur,
                                      int repeatIteration);
    protected abstract void sampledLastValue(int repeatIteration);
    protected abstract TimedElement getTimedElementById(String id);
    protected abstract EventTarget getEventTargetById(String id);
    protected abstract EventTarget getRootEventTarget();
    public abstract Element getElement();
    protected abstract EventTarget getAnimationEventTarget();
    public abstract boolean isBefore(TimedElement other);
    protected abstract boolean isConstantAnimation();
    public AnimationException createException(String code, Object[] params) {
        Element e = getElement();
        if (e != null) {
            params[0] = e.getNodeName();
        }
        return new AnimationException(this, code, params);
    }
    protected static final String RESOURCES =
        "org.apache.batik.anim.resources.Messages";
    protected static LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES, TimedElement.class.getClassLoader());
    public static void setLocale(Locale l) {
        localizableSupport.setLocale(l);
    }
    public static Locale getLocale() {
        return localizableSupport.getLocale();
    }
    public static String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        return localizableSupport.formatMessage(key, args);
    }
    public static String toString(float time) {
        if (Float.isNaN(time)) {
            return "UNRESOLVED";
        } else if (time == Float.POSITIVE_INFINITY) {
            return "INDEFINITE";
        } else {
            return Float.toString(time);
        }
    }
}
