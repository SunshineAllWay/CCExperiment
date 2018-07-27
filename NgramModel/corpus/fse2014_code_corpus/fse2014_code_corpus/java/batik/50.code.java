package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableNumberValue extends AnimatableValue {
    protected float value;
    protected AnimatableNumberValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableNumberValue(AnimationTarget target, float v) {
        super(target);
        value = v;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableNumberValue res;
        if (result == null) {
            res = new AnimatableNumberValue(target);
        } else {
            res = (AnimatableNumberValue) result;
        }
        float v = value;
        if (to != null) {
            AnimatableNumberValue toNumber = (AnimatableNumberValue) to;
            v += interpolation * (toNumber.value - value);
        }
        if (accumulation != null) {
            AnimatableNumberValue accNumber = (AnimatableNumberValue) accumulation;
            v += multiplier * accNumber.value;
        }
        if (res.value != v) {
            res.value = v;
            res.hasChanged = true;
        }
        return res;
    }
    public float getValue() {
        return value;
    }
    public boolean canPace() {
        return true;
    }
    public float distanceTo(AnimatableValue other) {
        AnimatableNumberValue o = (AnimatableNumberValue) other;
        return Math.abs(value - o.value);
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableNumberValue(target, 0);
    }
    public String getCssText() {
        return formatNumber(value);
    }
}
