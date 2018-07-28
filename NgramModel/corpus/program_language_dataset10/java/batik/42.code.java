package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.svg.SVGLength;
public class AnimatableLengthListValue extends AnimatableValue {
    protected short[] lengthTypes;
    protected float[] lengthValues;
    protected short percentageInterpretation;
    protected AnimatableLengthListValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableLengthListValue(AnimationTarget target, short[] types,
                                     float[] values, short pcInterp) {
        super(target);
        this.lengthTypes = types;
        this.lengthValues = values;
        this.percentageInterpretation = pcInterp;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableLengthListValue toLengthList = (AnimatableLengthListValue) to;
        AnimatableLengthListValue accLengthList
            = (AnimatableLengthListValue) accumulation;
        boolean hasTo = to != null;
        boolean hasAcc = accumulation != null;
        boolean canInterpolate =
            !(hasTo && toLengthList.lengthTypes.length != lengthTypes.length)
                && !(hasAcc && accLengthList.lengthTypes.length != lengthTypes.length);
        short[] baseLengthTypes;
        float[] baseLengthValues;
        if (!canInterpolate && hasTo && interpolation >= 0.5) {
            baseLengthTypes = toLengthList.lengthTypes;
            baseLengthValues = toLengthList.lengthValues;
        } else {
            baseLengthTypes = lengthTypes;
            baseLengthValues = lengthValues;
        }
        int len = baseLengthTypes.length;
        AnimatableLengthListValue res;
        if (result == null) {
            res = new AnimatableLengthListValue(target);
            res.lengthTypes = new short[len];
            res.lengthValues = new float[len];
        } else {
            res = (AnimatableLengthListValue) result;
            if (res.lengthTypes == null || res.lengthTypes.length != len) {
                res.lengthTypes = new short[len];
                res.lengthValues = new float[len];
            }
        }
        res.hasChanged =
            percentageInterpretation != res.percentageInterpretation;
        res.percentageInterpretation = percentageInterpretation;
        for (int i = 0; i < len; i++) {
            float toV = 0, accV = 0;
            short newLengthType = baseLengthTypes[i];
            float newLengthValue = baseLengthValues[i];
            if (canInterpolate) {
                if (hasTo && !AnimatableLengthValue.compatibleTypes
                        (newLengthType,
                         percentageInterpretation,
                         toLengthList.lengthTypes[i],
                         toLengthList.percentageInterpretation)
                    || hasAcc && !AnimatableLengthValue.compatibleTypes
                        (newLengthType,
                         percentageInterpretation,
                         accLengthList.lengthTypes[i],
                         accLengthList.percentageInterpretation)) {
                    newLengthValue = target.svgToUserSpace
                        (newLengthValue, newLengthType,
                         percentageInterpretation);
                    newLengthType = SVGLength.SVG_LENGTHTYPE_NUMBER;
                    if (hasTo) {
                        toV = to.target.svgToUserSpace
                            (toLengthList.lengthValues[i],
                             toLengthList.lengthTypes[i],
                             toLengthList.percentageInterpretation);
                    }
                    if (hasAcc) {
                        accV = accumulation.target.svgToUserSpace
                            (accLengthList.lengthValues[i],
                             accLengthList.lengthTypes[i],
                             accLengthList.percentageInterpretation);
                    }
                } else {
                    if (hasTo) {
                        toV = toLengthList.lengthValues[i];
                    }
                    if (hasAcc) {
                        accV = accLengthList.lengthValues[i];
                    }
                }
                newLengthValue +=
                    interpolation * (toV - newLengthValue)
                        + multiplier * accV;
            }
            if (!res.hasChanged) {
                res.hasChanged = newLengthType != res.lengthTypes[i]
                    || newLengthValue != res.lengthValues[i];
            }
            res.lengthTypes[i] = newLengthType;
            res.lengthValues[i] = newLengthValue;
        }
        return res;
    }
    public short[] getLengthTypes() {
        return lengthTypes;
    }
    public float[] getLengthValues() {
        return lengthValues;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        float[] vs = new float[lengthValues.length];
        return new AnimatableLengthListValue
            (target, lengthTypes, vs, percentageInterpretation);
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer();
        if (lengthValues.length > 0) {
            sb.append(formatNumber(lengthValues[0]));
            sb.append(AnimatableLengthValue.UNITS[lengthTypes[0] - 1]);
        }
        for (int i = 1; i < lengthValues.length; i++) {
            sb.append(',');
            sb.append(formatNumber(lengthValues[i]));
            sb.append(AnimatableLengthValue.UNITS[lengthTypes[i] - 1]);
        }
        return sb.toString();
    }
}
