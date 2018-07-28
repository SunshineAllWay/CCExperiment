package org.apache.batik.anim.timing;
import java.lang.ref.WeakReference;
import java.util.HashMap;
public class SyncbaseTimingSpecifier extends OffsetTimingSpecifier {
    protected String syncbaseID;
    protected TimedElement syncbaseElement;
    protected boolean syncBegin;
    protected HashMap instances = new HashMap();
    public SyncbaseTimingSpecifier(TimedElement owner, boolean isBegin,
                                   float offset, String syncbaseID,
                                   boolean syncBegin) {
        super(owner, isBegin, offset);
        this.syncbaseID = syncbaseID;
        this.syncBegin = syncBegin;
        this.syncbaseElement = owner.getTimedElementById(syncbaseID);
        syncbaseElement.addDependent(this, syncBegin);
    }
    public String toString() {
        return syncbaseID + "." + (syncBegin ? "begin" : "end")
            + (offset != 0 ? super.toString() : "");
    }
    public void initialize() {
    }
    public boolean isEventCondition() {
        return false;
    }
    float newInterval(Interval interval) {
        if (owner.hasPropagated) {
            return Float.POSITIVE_INFINITY;
        }
        InstanceTime instance =
            new InstanceTime(this, (syncBegin ? interval.getBegin()
                                              : interval.getEnd()) + offset,
                             true);
        instances.put(interval, instance);
        interval.addDependent(instance, syncBegin);
        return owner.addInstanceTime(instance, isBegin);
    }
    float removeInterval(Interval interval) {
        if (owner.hasPropagated) {
            return Float.POSITIVE_INFINITY;
        }
        InstanceTime instance = (InstanceTime) instances.get(interval);
        interval.removeDependent(instance, syncBegin);
        return owner.removeInstanceTime(instance, isBegin);
    }
    float handleTimebaseUpdate(InstanceTime instanceTime, float newTime) {
        if (owner.hasPropagated) {
            return Float.POSITIVE_INFINITY;
        }
        return owner.instanceTimeChanged(instanceTime, isBegin);
    }
}
