package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.svg.SVGAngle;
public class AnimatableAngleOrIdentValue extends AnimatableAngleValue {
    protected boolean isIdent;
    protected String ident;
    protected AnimatableAngleOrIdentValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableAngleOrIdentValue(AnimationTarget target, float v, short unit) {
        super(target, v, unit);
    }
    public AnimatableAngleOrIdentValue(AnimationTarget target, String ident) {
        super(target);
        this.ident = ident;
        this.isIdent = true;
    }
    public boolean isIdent() {
        return isIdent;
    }
    public String getIdent() {
        return ident;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableAngleOrIdentValue
            (target, 0, SVGAngle.SVG_ANGLETYPE_UNSPECIFIED);
    }
    public String getCssText() {
        if (isIdent) {
            return ident;
        }
        return super.getCssText();
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to, float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableAngleOrIdentValue res;
        if (result == null) {
            res = new AnimatableAngleOrIdentValue(target);
        } else {
            res = (AnimatableAngleOrIdentValue) result;
        }
        if (to == null) {
            if (isIdent) {
                res.hasChanged = !res.isIdent || !res.ident.equals(ident);
                res.ident = ident;
                res.isIdent = true;
            } else {
                short oldUnit = res.unit;
                float oldValue = res.value;
                super.interpolate(res, to, interpolation, accumulation,
                                  multiplier);
                if (res.unit != oldUnit || res.value != oldValue) {
                    res.hasChanged = true;
                }
            }
        } else {
            AnimatableAngleOrIdentValue toValue
                = (AnimatableAngleOrIdentValue) to;
            if (isIdent || toValue.isIdent) {
                if (interpolation >= 0.5) {
                    if (res.isIdent != toValue.isIdent
                            || res.unit != toValue.unit
                            || res.value != toValue.value
                            || res.isIdent && toValue.isIdent
                                && !toValue.ident.equals(ident)) {
                        res.isIdent = toValue.isIdent;
                        res.ident = toValue.ident;
                        res.unit = toValue.unit;
                        res.value = toValue.value;
                        res.hasChanged = true;
                    }
                } else {
                    if (res.isIdent != isIdent
                            || res.unit != unit
                            || res.value != value
                            || res.isIdent && isIdent
                                && !res.ident.equals(ident)) {
                        res.isIdent = isIdent;
                        res.ident = ident;
                        res.unit = unit;
                        res.value = value;
                        res.hasChanged = true;
                    }
                }
            } else {
                super.interpolate(res, to, interpolation, accumulation,
                                  multiplier);
            }
        }
        return res;
    }
}
