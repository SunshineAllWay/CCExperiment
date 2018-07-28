package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.svg.SVGLength;
public class AnimatableLengthValue extends AnimatableValue {
    protected static final String[] UNITS = {
        "", "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc"
    };
    protected short lengthType;
    protected float lengthValue;
    protected short percentageInterpretation;
    protected AnimatableLengthValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableLengthValue(AnimationTarget target, short type, float v,
                                 short pcInterp) {
        super(target);
        lengthType = type;
        lengthValue = v;
        percentageInterpretation = pcInterp;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableLengthValue res;
        if (result == null) {
            res = new AnimatableLengthValue(target);
        } else {
            res = (AnimatableLengthValue) result;
        }
        short oldLengthType = res.lengthType;
        float oldLengthValue = res.lengthValue;
        short oldPercentageInterpretation = res.percentageInterpretation;
        res.lengthType = lengthType;
        res.lengthValue = lengthValue;
        res.percentageInterpretation = percentageInterpretation;
        if (to != null) {
            AnimatableLengthValue toLength = (AnimatableLengthValue) to;
            float toValue;
            if (!compatibleTypes
                    (res.lengthType, res.percentageInterpretation,
                     toLength.lengthType, toLength.percentageInterpretation)) {
                res.lengthValue = target.svgToUserSpace
                    (res.lengthValue, res.lengthType,
                     res.percentageInterpretation);
                res.lengthType = SVGLength.SVG_LENGTHTYPE_NUMBER;
                toValue = toLength.target.svgToUserSpace
                    (toLength.lengthValue, toLength.lengthType,
                     toLength.percentageInterpretation);
            } else {
                toValue = toLength.lengthValue;
            }
            res.lengthValue += interpolation * (toValue - res.lengthValue);
        }
        if (accumulation != null) {
            AnimatableLengthValue accLength = (AnimatableLengthValue) accumulation;
            float accValue;
            if (!compatibleTypes
                    (res.lengthType, res.percentageInterpretation,
                     accLength.lengthType,
                     accLength.percentageInterpretation)) {
                res.lengthValue = target.svgToUserSpace
                    (res.lengthValue, res.lengthType,
                     res.percentageInterpretation);
                res.lengthType = SVGLength.SVG_LENGTHTYPE_NUMBER;
                accValue = accLength.target.svgToUserSpace
                    (accLength.lengthValue, accLength.lengthType,
                     accLength.percentageInterpretation);
            } else {
                accValue = accLength.lengthValue;
            }
            res.lengthValue += multiplier * accValue;
        }
        if (oldPercentageInterpretation != res.percentageInterpretation
                || oldLengthType != res.lengthType
                || oldLengthValue != res.lengthValue) {
            res.hasChanged = true;
        }
        return res;
    }
    public static boolean compatibleTypes(short t1, short pi1, short t2,
                                          short pi2) {
        return t1 == t2
            && (t1 != SVGLength.SVG_LENGTHTYPE_PERCENTAGE || pi1 == pi2)
            || t1 == SVGLength.SVG_LENGTHTYPE_NUMBER
                && t2 == SVGLength.SVG_LENGTHTYPE_PX
            || t1 == SVGLength.SVG_LENGTHTYPE_PX
                && t2 == SVGLength.SVG_LENGTHTYPE_NUMBER;
    }
    public int getLengthType() {
        return lengthType;
    }
    public float getLengthValue() {
        return lengthValue;
    }
    public boolean canPace() {
        return true;
    }
    public float distanceTo(AnimatableValue other) {
        AnimatableLengthValue o = (AnimatableLengthValue) other;
        float v1 = target.svgToUserSpace(lengthValue, lengthType,
                                         percentageInterpretation);
        float v2 = target.svgToUserSpace(o.lengthValue, o.lengthType,
                                         o.percentageInterpretation);
        return Math.abs(v1 - v2);
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableLengthValue
            (target, SVGLength.SVG_LENGTHTYPE_NUMBER, 0f,
             percentageInterpretation);
    }
    public String getCssText() {
        return formatNumber(lengthValue) + UNITS[lengthType - 1];
    }
}
