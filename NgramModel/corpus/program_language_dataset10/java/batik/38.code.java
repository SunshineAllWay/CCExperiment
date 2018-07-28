package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.svg.SVGAngle;
public class AnimatableAngleValue extends AnimatableNumberValue {
    protected static final String[] UNITS = {
        "", "", "deg", "rad", "grad"
    };
    protected short unit;
    public AnimatableAngleValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableAngleValue(AnimationTarget target, float v, short unit) {
        super(target, v);
        this.unit = unit;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableAngleValue res;
        if (result == null) {
            res = new AnimatableAngleValue(target);
        } else {
            res = (AnimatableAngleValue) result;
        }
        float v = value;
        short u = unit;
        if (to != null) {
            AnimatableAngleValue toAngle = (AnimatableAngleValue) to;
            if (toAngle.unit != u) {
                v = rad(v, u);
                v += interpolation * (rad(toAngle.value, toAngle.unit) - v);
                u = SVGAngle.SVG_ANGLETYPE_RAD;
            } else {
                v += interpolation * (toAngle.value - v);
            }
        }
        if (accumulation != null) {
            AnimatableAngleValue accAngle = (AnimatableAngleValue) accumulation;
            if (accAngle.unit != u) {
                v += multiplier * rad(accAngle.value, accAngle.unit);
                u = SVGAngle.SVG_ANGLETYPE_RAD;
            } else {
                v += multiplier * accAngle.value;
            }
        }
        if (res.value != v || res.unit != u) {
            res.value = v;
            res.unit = u;
            res.hasChanged = true;
        }
        return res;
    }
    public short getUnit() {
        return unit;
    }
    public float distanceTo(AnimatableValue other) {
        AnimatableAngleValue o = (AnimatableAngleValue) other;
        return Math.abs(rad(value, unit) - rad(o.value, o.unit));
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableAngleValue
            (target, 0, SVGAngle.SVG_ANGLETYPE_UNSPECIFIED);
    }
    public String getCssText() {
        return super.getCssText() + UNITS[unit];
    }
    public static float rad(float v, short unit) {
        switch (unit) {
            case SVGAngle.SVG_ANGLETYPE_RAD:
                return v;
            case SVGAngle.SVG_ANGLETYPE_GRAD:
                return (float) Math.PI * v / 200;
            default:
                return (float) Math.PI * v / 180;
        }
    }
}
