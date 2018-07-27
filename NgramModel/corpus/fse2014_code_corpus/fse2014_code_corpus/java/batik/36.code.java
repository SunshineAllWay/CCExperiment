package org.apache.batik.anim.timing;
import java.util.Calendar;
public class WallclockTimingSpecifier extends TimingSpecifier {
    protected Calendar time;
    protected InstanceTime instance;
    public WallclockTimingSpecifier(TimedElement owner, boolean isBegin,
                                    Calendar time) {
        super(owner, isBegin);
        this.time = time;
    }
    public String toString() {
        return "wallclock(" + time.toString() + ")";
    }
    public void initialize() {
        float t = owner.getRoot().convertWallclockTime(time);
        instance = new InstanceTime(this, t, false);
        owner.addInstanceTime(instance, isBegin);
    }
    public boolean isEventCondition() {
        return false;
    }
}
