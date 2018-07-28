package org.apache.batik.anim.timing;
public interface TimegraphListener {
    void elementAdded(TimedElement e);
    void elementRemoved(TimedElement e);
    void elementActivated(TimedElement e, float t);
    void elementFilled(TimedElement e, float t);
    void elementDeactivated(TimedElement e, float t);
    void intervalCreated(TimedElement e, Interval i);
    void intervalRemoved(TimedElement e, Interval i);
    void intervalChanged(TimedElement e, Interval i);
    void intervalBegan(TimedElement e, Interval i);
    void elementRepeated(TimedElement e, int i, float t);
    void elementInstanceTimesChanged(TimedElement e, float isBegin);
}
