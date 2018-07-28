package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatablePercentageValue extends AnimatableNumberValue {
    protected AnimatablePercentageValue(AnimationTarget target) {
        super(target);
    }
    public AnimatablePercentageValue(AnimationTarget target, float v) {
        super(target, v);
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        if (result == null) {
            result = new AnimatablePercentageValue(target);
        }
        return super.interpolate
            (result, to, interpolation, accumulation, multiplier);
    }
    public AnimatableValue getZeroValue() {
        return new AnimatablePercentageValue(target, 0);
    }
    public String getCssText() {
        return super.getCssText() + "%";
    }
}
