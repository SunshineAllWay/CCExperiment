package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatablePaintValue extends AnimatableColorValue {
    public static final int PAINT_NONE              = 0;
    public static final int PAINT_CURRENT_COLOR     = 1;
    public static final int PAINT_COLOR             = 2;
    public static final int PAINT_URI               = 3;
    public static final int PAINT_URI_NONE          = 4;
    public static final int PAINT_URI_CURRENT_COLOR = 5;
    public static final int PAINT_URI_COLOR         = 6;
    public static final int PAINT_INHERIT           = 7;
    protected int paintType;
    protected String uri;
    protected AnimatablePaintValue(AnimationTarget target) {
        super(target);
    }
    protected AnimatablePaintValue(AnimationTarget target, float r, float g,
                                   float b) {
        super(target, r, g, b);
    }
    public static AnimatablePaintValue createNonePaintValue
            (AnimationTarget target) {
        AnimatablePaintValue v = new AnimatablePaintValue(target);
        v.paintType = PAINT_NONE;
        return v;
    }
    public static AnimatablePaintValue createCurrentColorPaintValue
            (AnimationTarget target) {
        AnimatablePaintValue v = new AnimatablePaintValue(target);
        v.paintType = PAINT_CURRENT_COLOR;
        return v;
    }
    public static AnimatablePaintValue createColorPaintValue
            (AnimationTarget target, float r, float g, float b) {
        AnimatablePaintValue v = new AnimatablePaintValue(target, r, g, b);
        v.paintType = PAINT_COLOR;
        return v;
    }
    public static AnimatablePaintValue createURIPaintValue
            (AnimationTarget target, String uri) {
        AnimatablePaintValue v = new AnimatablePaintValue(target);
        v.uri = uri;
        v.paintType = PAINT_URI;
        return v;
    }
    public static AnimatablePaintValue createURINonePaintValue
            (AnimationTarget target, String uri) {
        AnimatablePaintValue v = new AnimatablePaintValue(target);
        v.uri = uri;
        v.paintType = PAINT_URI_NONE;
        return v;
    }
    public static AnimatablePaintValue createURICurrentColorPaintValue
            (AnimationTarget target, String uri) {
        AnimatablePaintValue v = new AnimatablePaintValue(target);
        v.uri = uri;
        v.paintType = PAINT_URI_CURRENT_COLOR;
        return v;
    }
    public static AnimatablePaintValue createURIColorPaintValue
            (AnimationTarget target, String uri, float r, float g, float b) {
        AnimatablePaintValue v = new AnimatablePaintValue(target, r, g, b);
        v.uri = uri;
        v.paintType = PAINT_URI_COLOR;
        return v;
    }
    public static AnimatablePaintValue createInheritPaintValue
            (AnimationTarget target) {
        AnimatablePaintValue v = new AnimatablePaintValue(target);
        v.paintType = PAINT_INHERIT;
        return v;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatablePaintValue res;
        if (result == null) {
            res = new AnimatablePaintValue(target);
        } else {
            res = (AnimatablePaintValue) result;
        }
        if (paintType == PAINT_COLOR) {
            boolean canInterpolate = true;
            if (to != null) {
                AnimatablePaintValue toPaint = (AnimatablePaintValue) to;
                canInterpolate = toPaint.paintType == PAINT_COLOR;
            }
            if (accumulation != null) {
                AnimatablePaintValue accPaint =
                    (AnimatablePaintValue) accumulation;
                canInterpolate =
                    canInterpolate && accPaint.paintType == PAINT_COLOR;
            }
            if (canInterpolate) {
                res.paintType = PAINT_COLOR;
                return super.interpolate
                    (res, to, interpolation, accumulation, multiplier);
            }
        }
        int newPaintType;
        String newURI;
        float newRed, newGreen, newBlue;
        if (to != null && interpolation >= 0.5) {
            AnimatablePaintValue toValue = (AnimatablePaintValue) to;
            newPaintType = toValue.paintType;
            newURI = toValue.uri;
            newRed = toValue.red;
            newGreen = toValue.green;
            newBlue = toValue.blue;
        } else {
            newPaintType = paintType;
            newURI = uri;
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }
        if (res.paintType != newPaintType
                || res.uri == null
                || !res.uri.equals(newURI)
                || res.red != newRed
                || res.green != newGreen
                || res.blue != newBlue) {
            res.paintType = newPaintType;
            res.uri = newURI;
            res.red = newRed;
            res.green = newGreen;
            res.blue = newBlue;
            res.hasChanged = true;
        }
        return res;
    }
    public int getPaintType() {
        return paintType;
    }
    public String getURI() {
        return uri;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return AnimatablePaintValue.createColorPaintValue(target, 0f, 0f, 0f);
    }
    public String getCssText() {
        switch (paintType) {
            case PAINT_NONE:
                return "none";
            case PAINT_CURRENT_COLOR:
                return "currentColor";
            case PAINT_COLOR:
                return super.getCssText();
            case PAINT_URI:
                return "url(" + uri + ")";
            case PAINT_URI_NONE:
                return "url(" + uri + ") none";
            case PAINT_URI_CURRENT_COLOR:
                return "url(" + uri + ") currentColor";
            case PAINT_URI_COLOR:
                return "url(" + uri + ") " + super.getCssText();
            default: 
                return "inherit";
        }
    }
}
