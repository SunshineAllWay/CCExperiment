package org.apache.batik.anim;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.dom.anim.AnimatableElement;
public class SetAnimation extends AbstractAnimation {
    protected AnimatableValue to;
    public SetAnimation(TimedElement timedElement,
                        AnimatableElement animatableElement,
                        AnimatableValue to) {
        super(timedElement, animatableElement);
        this.to = to;
    }
    protected void sampledAt(float simpleTime, float simpleDur,
                             int repeatIteration) {
        if (value == null) {
            value = to;
            markDirty();
        }
    }
    protected void sampledLastValue(int repeatIteration) {
        if (value == null) {
            value = to;
            markDirty();
        }
    }
}
