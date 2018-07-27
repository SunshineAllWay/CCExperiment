package org.apache.batik.anim.timing;
public class OffsetTimingSpecifier extends TimingSpecifier {
    protected float offset;
    public OffsetTimingSpecifier(TimedElement owner, boolean isBegin,
                                 float offset) {
        super(owner, isBegin);
        this.offset = offset;
    }
    public String toString() {
        return (offset >= 0 ? "+" : "") + offset;
    }
    public void initialize() {
        InstanceTime instance = new InstanceTime(this, offset, false);
        owner.addInstanceTime(instance, isBegin);
    }
    public boolean isEventCondition() {
        return false;
    }
}
