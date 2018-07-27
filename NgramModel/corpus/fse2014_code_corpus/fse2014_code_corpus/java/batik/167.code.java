package org.apache.batik.bridge;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.AnimationEngine;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.anim.SimpleAnimation;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.util.SMILConstants;
public class SVGAnimateElementBridge extends SVGAnimationElementBridge {
    public String getLocalName() {
        return SVG_ANIMATE_TAG;
    }
    public Bridge getInstance() {
        return new SVGAnimateElementBridge();
    }
    protected AbstractAnimation createAnimation(AnimationTarget target) {
        AnimatableValue from = parseAnimatableValue(SVG_FROM_ATTRIBUTE);
        AnimatableValue to = parseAnimatableValue(SVG_TO_ATTRIBUTE);
        AnimatableValue by = parseAnimatableValue(SVG_BY_ATTRIBUTE);
        return new SimpleAnimation(timedElement,
                                   this,
                                   parseCalcMode(),
                                   parseKeyTimes(),
                                   parseKeySplines(),
                                   parseAdditive(),
                                   parseAccumulate(),
                                   parseValues(),
                                   from,
                                   to,
                                   by);
    }
    protected int parseCalcMode() {
        if (animationType == AnimationEngine.ANIM_TYPE_CSS
                && !targetElement.isPropertyAdditive(attributeLocalName)
            || animationType == AnimationEngine.ANIM_TYPE_XML
                && !targetElement.isAttributeAdditive(attributeNamespaceURI,
                                                      attributeLocalName)) {
            return SimpleAnimation.CALC_MODE_DISCRETE;
        }
        String calcModeString = element.getAttributeNS(null,
                                                       SVG_CALC_MODE_ATTRIBUTE);
        if (calcModeString.length() == 0) {
            return getDefaultCalcMode();
        } else if (calcModeString.equals(SMILConstants.SMIL_LINEAR_VALUE)) {
            return SimpleAnimation.CALC_MODE_LINEAR;
        } else if (calcModeString.equals(SMILConstants.SMIL_DISCRETE_VALUE)) {
            return SimpleAnimation.CALC_MODE_DISCRETE;
        } else if (calcModeString.equals(SMILConstants.SMIL_PACED_VALUE)) {
            return SimpleAnimation.CALC_MODE_PACED;
        } else if (calcModeString.equals(SMILConstants.SMIL_SPLINE_VALUE)) {
            return SimpleAnimation.CALC_MODE_SPLINE;
        }
        throw new BridgeException
            (ctx, element, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] { SVG_CALC_MODE_ATTRIBUTE, calcModeString });
    }
    protected boolean parseAdditive() {
        String additiveString = element.getAttributeNS(null,
                                                       SVG_ADDITIVE_ATTRIBUTE);
        if (additiveString.length() == 0
                || additiveString.equals(SMILConstants.SMIL_REPLACE_VALUE)) {
            return false;
        } else if (additiveString.equals(SMILConstants.SMIL_SUM_VALUE)) {
            return true;
        }
        throw new BridgeException
            (ctx, element, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] { SVG_ADDITIVE_ATTRIBUTE, additiveString });
    }
    protected boolean parseAccumulate() {
        String accumulateString =
            element.getAttributeNS(null, SVG_ACCUMULATE_ATTRIBUTE);
        if (accumulateString.length() == 0 ||
                accumulateString.equals(SMILConstants.SMIL_NONE_VALUE)) {
            return false;
        } else if (accumulateString.equals(SMILConstants.SMIL_SUM_VALUE)) {
            return true;
        }
        throw new BridgeException
            (ctx, element, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
             new Object[] { SVG_ACCUMULATE_ATTRIBUTE, accumulateString });
    }
    protected AnimatableValue[] parseValues() {
        boolean isCSS = animationType == AnimationEngine.ANIM_TYPE_CSS;
        String valuesString = element.getAttributeNS(null,
                                                     SVG_VALUES_ATTRIBUTE);
        int len = valuesString.length();
        if (len == 0) {
            return null;
        }
        ArrayList values = new ArrayList(7);
        int i = 0, start = 0, end;
        char c;
outer:  while (i < len) {
            while (valuesString.charAt(i) == ' ') {
                i++;
                if (i == len) {
                    break outer;
                }
            }
            start = i++;
            if (i != len) {
                c = valuesString.charAt(i);
                while (c != ';') {
                    i++;
                    if (i == len) {
                        break;
                    }
                    c = valuesString.charAt(i);
                }
            }
            end = i++;
            AnimatableValue val = eng.parseAnimatableValue
                (element, animationTarget, attributeNamespaceURI,
                 attributeLocalName, isCSS, valuesString.substring(start, end));
            if (!checkValueType(val)) {
                throw new BridgeException
                    (ctx, element, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] { SVG_VALUES_ATTRIBUTE, valuesString });
            }
            values.add(val);
        }
        AnimatableValue[] ret = new AnimatableValue[values.size()];
        return (AnimatableValue[]) values.toArray(ret);
    }
    protected float[] parseKeyTimes() {
        String keyTimesString =
            element.getAttributeNS(null, SVG_KEY_TIMES_ATTRIBUTE);
        int len = keyTimesString.length();
        if (len == 0) {
            return null;
        }
        ArrayList keyTimes = new ArrayList(7);
        int i = 0, start = 0, end;
        char c;
outer:  while (i < len) {
            while (keyTimesString.charAt(i) == ' ') {
                i++;
                if (i == len) {
                    break outer;
                }
            }
            start = i++;
            if (i != len) {
                c = keyTimesString.charAt(i);
                while (c != ' ' && c != ';') {
                    i++;
                    if (i == len) {
                        break;
                    }
                    c = keyTimesString.charAt(i);
                }
            }
            end = i++;
            try {
                float keyTime =
                    Float.parseFloat(keyTimesString.substring(start, end));
                keyTimes.add(new Float(keyTime));
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, element, nfEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] { SVG_KEY_TIMES_ATTRIBUTE, keyTimesString });
            }
        }
        len = keyTimes.size();
        float[] ret = new float[len];
        for (int j = 0; j < len; j++) {
            ret[j] = ((Float) keyTimes.get(j)).floatValue();
        }
        return ret;
    }
    protected float[] parseKeySplines() {
        String keySplinesString =
            element.getAttributeNS(null, SVG_KEY_SPLINES_ATTRIBUTE);
        int len = keySplinesString.length();
        if (len == 0) {
            return null;
        }
        List keySplines = new ArrayList(7);
        int count = 0, i = 0, start = 0, end;
        char c;
outer:  while (i < len) {
            while (keySplinesString.charAt(i) == ' ') {
                i++;
                if (i == len) {
                    break outer;
                }
            }
            start = i++;
            if (i != len) {
                c = keySplinesString.charAt(i);
                while (c != ' ' && c != ',' && c != ';') {
                    i++;
                    if (i == len) {
                        break;
                    }
                    c = keySplinesString.charAt(i);
                }
                end = i++;
                if (c == ' ') {
                    do {
                        if (i == len) {
                            break;
                        }
                        c = keySplinesString.charAt(i++);
                    } while (c == ' ');
                    if (c != ';' && c != ',') {
                        i--;
                    }
                }
                if (c == ';') {
                    if (count == 3) {
                        count = 0;
                    } else {
                        throw new BridgeException
                            (ctx, element,
                             ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                             new Object[] { SVG_KEY_SPLINES_ATTRIBUTE,
                                            keySplinesString });
                    }
                } else {
                    count++;
                }
            } else {
                end = i++;
            }
            try {
                float keySplineValue =
                    Float.parseFloat(keySplinesString.substring(start, end));
                keySplines.add(new Float(keySplineValue));
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, element, nfEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] { SVG_KEY_SPLINES_ATTRIBUTE, keySplinesString });
            }
        }
        len = keySplines.size();
        float[] ret = new float[len];
        for (int j = 0; j < len; j++) {
            ret[j] = ((Float) keySplines.get(j)).floatValue();
        }
        return ret;
    }
    protected int getDefaultCalcMode() {
        return SimpleAnimation.CALC_MODE_LINEAR;
    }
    protected boolean canAnimateType(int type) {
        return true;
    }
}
