package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableNumberOrIdentValue extends AnimatableNumberValue {
    protected boolean isIdent;
    protected String ident;
    protected boolean numericIdent;
    protected AnimatableNumberOrIdentValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableNumberOrIdentValue(AnimationTarget target, float v,
                                        boolean numericIdent) {
        super(target, v);
        this.numericIdent = numericIdent;
    }
    public AnimatableNumberOrIdentValue(AnimationTarget target, String ident) {
        super(target);
        this.ident = ident;
        this.isIdent = true;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableNumberOrIdentValue(target, 0f, numericIdent);
    }
    public String getCssText() {
        if (isIdent) {
            return ident;
        }
        if (numericIdent) {
            return Integer.toString((int) value);
        }
        return super.getCssText();
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to, float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableNumberOrIdentValue res;
        if (result == null) {
            res = new AnimatableNumberOrIdentValue(target);
        } else {
            res = (AnimatableNumberOrIdentValue) result;
        }
        if (to == null) {
            if (isIdent) {
                res.hasChanged = !res.isIdent || !res.ident.equals(ident);
                res.ident = ident;
                res.isIdent = true;
            } else if (numericIdent) {
                res.hasChanged = res.value != value || res.isIdent;
                res.value = value;
                res.isIdent = false;
                res.hasChanged = true;
                res.numericIdent = true;
            } else {
                float oldValue = res.value;
                super.interpolate(res, to, interpolation, accumulation,
                                  multiplier);
                res.numericIdent = false;
                if (res.value != oldValue) {
                    res.hasChanged = true;
                }
            }
        } else {
            AnimatableNumberOrIdentValue toValue
                = (AnimatableNumberOrIdentValue) to;
            if (isIdent || toValue.isIdent || numericIdent) {
                if (interpolation >= 0.5) {
                    if (res.isIdent != toValue.isIdent
                            || res.value != toValue.value
                            || res.isIdent && toValue.isIdent
                                && !toValue.ident.equals(ident)) {
                        res.isIdent = toValue.isIdent;
                        res.ident = toValue.ident;
                        res.value = toValue.value;
                        res.numericIdent = toValue.numericIdent;
                        res.hasChanged = true;
                    }
                } else {
                    if (res.isIdent != isIdent
                            || res.value != value
                            || res.isIdent && isIdent
                                && !res.ident.equals(ident)) {
                        res.isIdent = isIdent;
                        res.ident = ident;
                        res.value = value;
                        res.numericIdent = numericIdent;
                        res.hasChanged = true;
                    }
                }
            } else {
                super.interpolate(res, to, interpolation, accumulation,
                                  multiplier);
                res.numericIdent = false;
            }
        }
        return res;
    }
}
