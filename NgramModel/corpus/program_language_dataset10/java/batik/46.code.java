package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableNumberListValue extends AnimatableValue {
    protected float[] numbers;
    protected AnimatableNumberListValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableNumberListValue(AnimationTarget target, float[] numbers) {
        super(target);
        this.numbers = numbers;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableNumberListValue toNumList = (AnimatableNumberListValue) to;
        AnimatableNumberListValue accNumList =
            (AnimatableNumberListValue) accumulation;
        boolean hasTo = to != null;
        boolean hasAcc = accumulation != null;
        boolean canInterpolate =
            !(hasTo && toNumList.numbers.length != numbers.length)
                && !(hasAcc && accNumList.numbers.length != numbers.length);
        float[] baseValues;
        if (!canInterpolate && hasTo && interpolation >= 0.5) {
            baseValues = toNumList.numbers;
        } else {
            baseValues = numbers;
        }
        int len = baseValues.length;
        AnimatableNumberListValue res;
        if (result == null) {
            res = new AnimatableNumberListValue(target);
            res.numbers = new float[len];
        } else {
            res = (AnimatableNumberListValue) result;
            if (res.numbers == null || res.numbers.length != len) {
                res.numbers = new float[len];
            }
        }
        for (int i = 0; i < len; i++) {
            float newValue = baseValues[i];
            if (canInterpolate) {
                if (hasTo) {
                    newValue += interpolation * (toNumList.numbers[i] - newValue);
                }
                if (hasAcc) {
                    newValue += multiplier * accNumList.numbers[i];
                }
            }
            if (res.numbers[i] != newValue) {
                res.numbers[i] = newValue;
                res.hasChanged = true;
            }
        }
        return res;
    }
    public float[] getNumbers() {
        return numbers;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        float[] ns = new float[numbers.length];
        return new AnimatableNumberListValue(target, ns);
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer();
        sb.append(numbers[0]);
        for (int i = 1; i < numbers.length; i++) {
            sb.append(' ');
            sb.append(numbers[i]);
        }
        return sb.toString();
    }
}
