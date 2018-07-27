package org.apache.batik.anim.timing;
public abstract class TimingSpecifier {
    protected TimedElement owner;
    protected boolean isBegin;
    protected TimingSpecifier(TimedElement owner, boolean isBegin) {
        this.owner = owner;
        this.isBegin = isBegin;
    }
    public TimedElement getOwner() {
        return owner;
    }
    public boolean isBegin() {
        return isBegin;
    }
    public void initialize() {
    }
    public void deinitialize() {
    }
    public abstract boolean isEventCondition();
    float newInterval(Interval interval) {
        return Float.POSITIVE_INFINITY;
    }
    float removeInterval(Interval interval) {
        return Float.POSITIVE_INFINITY;
    }
    float handleTimebaseUpdate(InstanceTime instanceTime, float newTime) {
        return Float.POSITIVE_INFINITY;
    }
}
