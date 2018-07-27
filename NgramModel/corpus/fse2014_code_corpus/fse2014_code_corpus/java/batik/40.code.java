package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableColorValue extends AnimatableValue {
    protected float red;
    protected float green;
    protected float blue;
    protected AnimatableColorValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableColorValue(AnimationTarget target,
                                float r, float g, float b) {
        super(target);
        red = r;
        green = g;
        blue = b;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableColorValue res;
        if (result == null) {
            res = new AnimatableColorValue(target);
        } else {
            res = (AnimatableColorValue) result;
        }
        float oldRed = res.red;
        float oldGreen = res.green;
        float oldBlue = res.blue;
        res.red = red;
        res.green = green;
        res.blue = blue;
        AnimatableColorValue toColor = (AnimatableColorValue) to;
        AnimatableColorValue accColor = (AnimatableColorValue) accumulation;
        if (to != null) {
            res.red += interpolation * (toColor.red - res.red);
            res.green += interpolation * (toColor.green - res.green);
            res.blue += interpolation * (toColor.blue - res.blue);
        }
        if (accumulation != null) {
            res.red += multiplier * accColor.red;
            res.green += multiplier * accColor.green;
            res.blue += multiplier * accColor.blue;
        }
        if (res.red != oldRed || res.green != oldGreen || res.blue != oldBlue) {
            res.hasChanged = true;
        }
        return res;
    }
    public boolean canPace() {
        return true;
    }
    public float distanceTo(AnimatableValue other) {
        AnimatableColorValue o = (AnimatableColorValue) other;
        float dr = red - o.red;
        float dg = green - o.green;
        float db = blue - o.blue;
        return (float) Math.sqrt(dr * dr + dg * dg + db * db);
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableColorValue(target, 0f, 0f, 0f);
    }
    public String getCssText() {
        return "rgb(" + Math.round(red * 255) + ','
                + Math.round(green * 255) + ','
                + Math.round(blue * 255) + ')';
    }
}
