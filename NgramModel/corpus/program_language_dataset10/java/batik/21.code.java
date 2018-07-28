package org.apache.batik.anim.timing;
public class IndefiniteTimingSpecifier extends TimingSpecifier {
    public IndefiniteTimingSpecifier(TimedElement owner, boolean isBegin) {
        super(owner, isBegin);
    }
    public String toString() {
        return "indefinite";
    }
    public void initialize() {
        if (!isBegin) {
            InstanceTime instance =
                new InstanceTime(this, TimedElement.INDEFINITE, false);
            owner.addInstanceTime(instance, isBegin);
        }
    }
    public boolean isEventCondition() {
        return false;
    }
}
