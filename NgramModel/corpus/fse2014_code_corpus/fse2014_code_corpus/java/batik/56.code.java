package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableRectValue extends AnimatableValue {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected AnimatableRectValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableRectValue(AnimationTarget target, float x, float y,
                               float w, float h) {
        super(target);
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableRectValue res;
        if (result == null) {
            res = new AnimatableRectValue(target);
        } else {
            res = (AnimatableRectValue) result;
        }
        float newX = x, newY = y, newWidth = width, newHeight = height;
        if (to != null) {
            AnimatableRectValue toValue = (AnimatableRectValue) to;
            newX += interpolation * (toValue.x - x);
            newY += interpolation * (toValue.y - y);
            newWidth += interpolation * (toValue.width - width);
            newHeight += interpolation * (toValue.height - height);
        }
        if (accumulation != null && multiplier != 0) {
            AnimatableRectValue accValue = (AnimatableRectValue) accumulation;
            newX += multiplier * accValue.x;
            newY += multiplier * accValue.y;
            newWidth += multiplier * accValue.width;
            newHeight += multiplier * accValue.height;
        }
        if (res.x != newX || res.y != newY
                || res.width != newWidth || res.height != newHeight) {
            res.x = newX;
            res.y = newY;
            res.width = newWidth;
            res.height = newHeight;
            res.hasChanged = true;
        }
        return res;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableRectValue(target, 0f, 0f, 0f, 0f);
    }
    public String toStringRep() {
        StringBuffer sb = new StringBuffer();
        sb.append(x);
        sb.append(',');
        sb.append(y);
        sb.append(',');
        sb.append(width);
        sb.append(',');
        sb.append(height);
        return sb.toString();
    }
}
