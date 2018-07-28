package org.apache.batik.anim.timing;
import org.w3c.dom.events.Event;
import org.w3c.dom.smil.TimeEvent;
public class RepeatTimingSpecifier extends EventbaseTimingSpecifier {
    protected int repeatIteration;
    protected boolean repeatIterationSpecified;
    public RepeatTimingSpecifier(TimedElement owner, boolean isBegin,
                                 float offset, String syncbaseID) {
        super(owner, isBegin, offset, syncbaseID,
              owner.getRoot().getRepeatEventName());
    }
    public RepeatTimingSpecifier(TimedElement owner, boolean isBegin,
                                 float offset, String syncbaseID,
                                 int repeatIteration) {
        super(owner, isBegin, offset, syncbaseID,
              owner.getRoot().getRepeatEventName());
        this.repeatIteration = repeatIteration;
        this.repeatIterationSpecified = true;
    }
    public String toString() {
        return (eventbaseID == null ? "" : eventbaseID + ".") + "repeat"
            + (repeatIterationSpecified ? "(" + repeatIteration + ")" : "")
            + (offset != 0 ? super.toString() : "");
    }
    public void handleEvent(Event e) {
        TimeEvent evt = (TimeEvent) e;
        if (!repeatIterationSpecified || evt.getDetail() == repeatIteration) {
            super.handleEvent(e);
        }
    }
}
