package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatablePointListValue extends AnimatableNumberListValue {
    protected AnimatablePointListValue(AnimationTarget target) {
        super(target);
    }
    public AnimatablePointListValue(AnimationTarget target, float[] numbers) {
        super(target, numbers);
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        if (result == null) {
            result = new AnimatablePointListValue(target);
        }
        return super.interpolate
            (result, to, interpolation, accumulation, multiplier);
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        float[] ns = new float[numbers.length];
        return new AnimatablePointListValue(target, ns);
    }
}
