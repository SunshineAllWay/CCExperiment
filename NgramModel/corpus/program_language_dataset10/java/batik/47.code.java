package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableNumberOptionalNumberValue extends AnimatableValue {
    protected float number;
    protected boolean hasOptionalNumber;
    protected float optionalNumber;
    protected AnimatableNumberOptionalNumberValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableNumberOptionalNumberValue(AnimationTarget target,
                                               float n) {
        super(target);
        number = n;
    }
    public AnimatableNumberOptionalNumberValue(AnimationTarget target, float n,
                                               float on) {
        super(target);
        number = n;
        optionalNumber = on;
        hasOptionalNumber = true;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableNumberOptionalNumberValue res;
        if (result == null) {
            res = new AnimatableNumberOptionalNumberValue(target);
        } else {
            res = (AnimatableNumberOptionalNumberValue) result;
        }
        float newNumber, newOptionalNumber;
        boolean newHasOptionalNumber;
        if (to != null && interpolation >= 0.5) {
            AnimatableNumberOptionalNumberValue toValue
                = (AnimatableNumberOptionalNumberValue) to;
            newNumber = toValue.number;
            newOptionalNumber = toValue.optionalNumber;
            newHasOptionalNumber = toValue.hasOptionalNumber;
        } else {
            newNumber = number;
            newOptionalNumber = optionalNumber;
            newHasOptionalNumber = hasOptionalNumber;
        }
        if (res.number != newNumber
                || res.hasOptionalNumber != newHasOptionalNumber
                || res.optionalNumber != newOptionalNumber) {
            res.number = number;
            res.optionalNumber = optionalNumber;
            res.hasOptionalNumber = hasOptionalNumber;
            res.hasChanged = true;
        }
        return res;
    }
    public float getNumber() {
        return number;
    }
    public boolean hasOptionalNumber() {
        return hasOptionalNumber;
    }
    public float getOptionalNumber() {
        return optionalNumber;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        if (hasOptionalNumber) {
            return new AnimatableNumberOptionalNumberValue(target, 0f, 0f);
        }
        return new AnimatableNumberOptionalNumberValue(target, 0f);
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer();
        sb.append(formatNumber(number));
        if (hasOptionalNumber) {
            sb.append(' ');
            sb.append(formatNumber(optionalNumber));
        }
        return sb.toString();
    }
}
