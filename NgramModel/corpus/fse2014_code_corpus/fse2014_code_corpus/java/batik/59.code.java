package org.apache.batik.anim.values;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.apache.batik.dom.anim.AnimationTarget;
public abstract class AnimatableValue {
    protected static DecimalFormat decimalFormat = new DecimalFormat
        ("0.0###########################################################",
         new DecimalFormatSymbols(Locale.ENGLISH));
    protected AnimationTarget target;
    protected boolean hasChanged = true;
    protected AnimatableValue(AnimationTarget target) {
        this.target = target;
    }
    public static String formatNumber(float f) {
        return decimalFormat.format(f);
    }
    public abstract AnimatableValue interpolate(AnimatableValue result,
                                                AnimatableValue to,
                                                float interpolation,
                                                AnimatableValue accumulation,
                                                int multiplier);
    public abstract boolean canPace();
    public abstract float distanceTo(AnimatableValue other);
    public abstract AnimatableValue getZeroValue();
    public String getCssText() {
        return null;
    }
    public boolean hasChanged() {
        boolean ret = hasChanged;
        hasChanged = false;
        return ret;
    }
    public String toStringRep() {
        return getCssText();
    }
    public String toString() {
        return getClass().getName() + "[" + toStringRep() + "]";
    }
}
