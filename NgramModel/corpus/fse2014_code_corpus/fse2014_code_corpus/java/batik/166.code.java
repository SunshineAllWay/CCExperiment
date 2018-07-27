package org.apache.batik.bridge;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.anim.ColorAnimation;
import org.apache.batik.anim.values.AnimatableColorValue;
import org.apache.batik.anim.values.AnimatablePaintValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.util.SVGTypes;
public class SVGAnimateColorElementBridge extends SVGAnimateElementBridge {
    public String getLocalName() {
        return SVG_ANIMATE_COLOR_TAG;
    }
    public Bridge getInstance() {
        return new SVGAnimateColorElementBridge();
    }
    protected AbstractAnimation createAnimation(AnimationTarget target) {
        AnimatableValue from = parseAnimatableValue(SVG_FROM_ATTRIBUTE);
        AnimatableValue to = parseAnimatableValue(SVG_TO_ATTRIBUTE);
        AnimatableValue by = parseAnimatableValue(SVG_BY_ATTRIBUTE);
        return new ColorAnimation(timedElement,
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
    protected boolean canAnimateType(int type) {
        return type == SVGTypes.TYPE_COLOR || type == SVGTypes.TYPE_PAINT;
    }
    protected boolean checkValueType(AnimatableValue v) {
        if (v instanceof AnimatablePaintValue) {
            return ((AnimatablePaintValue) v).getPaintType()
                == AnimatablePaintValue.PAINT_COLOR;
        }
        return v instanceof AnimatableColorValue;
    }
}
