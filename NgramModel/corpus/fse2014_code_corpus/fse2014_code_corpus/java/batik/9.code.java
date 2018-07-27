package org.apache.batik.anim;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimatableElement;
public abstract class AbstractAnimation {
    public static final short CALC_MODE_DISCRETE = 0;
    public static final short CALC_MODE_LINEAR   = 1;
    public static final short CALC_MODE_PACED    = 2;
    public static final short CALC_MODE_SPLINE   = 3;
    protected TimedElement timedElement;
    protected AnimatableElement animatableElement;
    protected AbstractAnimation lowerAnimation;
    protected AbstractAnimation higherAnimation;
    protected boolean isDirty;
    protected boolean isActive;
    protected boolean isFrozen;
    protected float beginTime;
    protected AnimatableValue value;
    protected AnimatableValue composedValue;
    protected boolean usesUnderlyingValue;
    protected boolean toAnimation;
    protected AbstractAnimation(TimedElement timedElement,
                                AnimatableElement animatableElement) {
        this.timedElement = timedElement;
        this.animatableElement = animatableElement;
    }
    public TimedElement getTimedElement() {
        return timedElement;
    }
    public AnimatableValue getValue() {
        if (!isActive && !isFrozen) {
            return null;
        }
        return value;
    }
    public AnimatableValue getComposedValue() {
        if (!isActive && !isFrozen) {
            return null;
        }
        if (isDirty) {
            AnimatableValue lowerValue = null;
            if (!willReplace()) {
                if (lowerAnimation == null) {
                    lowerValue = animatableElement.getUnderlyingValue();
                    usesUnderlyingValue = true;
                } else {
                    lowerValue = lowerAnimation.getComposedValue();
                    usesUnderlyingValue = false;
                }
            }
            composedValue =
                value.interpolate(composedValue, null, 0f, lowerValue, 1);
            isDirty = false;
        }
        return composedValue;
    }
    public String toString() {
        return timedElement.toString();
    }
    public boolean usesUnderlyingValue() {
        return usesUnderlyingValue || toAnimation;
    }
    protected boolean willReplace() {
        return true;
    }
    protected void markDirty() {
        isDirty = true;
        if (higherAnimation != null
                && !higherAnimation.willReplace()
                && !higherAnimation.isDirty) {
            higherAnimation.markDirty();
        }
    }
    protected void sampledLastValue(int repeatIteration) {
    }
    protected abstract void sampledAt(float simpleTime, float simpleDur,
                                      int repeatIteration);
}
