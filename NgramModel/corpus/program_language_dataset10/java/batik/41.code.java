package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableIntegerValue extends AnimatableValue {
    protected int value;
    protected AnimatableIntegerValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableIntegerValue(AnimationTarget target, int v) {
        super(target);
        value = v;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableIntegerValue res;
        if (result == null) {
            res = new AnimatableIntegerValue(target);
        } else {
            res = (AnimatableIntegerValue) result;
        }
        int v = value;
        if (to != null) {
            AnimatableIntegerValue toInteger = (AnimatableIntegerValue) to;
            v += value + interpolation * (toInteger.getValue() - value);
        }
        if (accumulation != null) {
            AnimatableIntegerValue accInteger =
                (AnimatableIntegerValue) accumulation;
            v += multiplier * accInteger.getValue();
        }
        if (res.value != v) {
            res.value = v;
            res.hasChanged = true;
        }
        return res;
    }
    public int getValue() {
        return value;
    }
    public boolean canPace() {
        return true;
    }
    public float distanceTo(AnimatableValue other) {
        AnimatableIntegerValue o = (AnimatableIntegerValue) other;
        return Math.abs(value - o.value);
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableIntegerValue(target, 0);
    }
    public String getCssText() {
        return Integer.toString(value);
    }
}
