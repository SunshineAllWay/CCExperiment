package org.apache.batik.anim.values;
import java.util.Arrays;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatablePathDataValue extends AnimatableValue {
    protected short[] commands;
    protected float[] parameters;
    protected AnimatablePathDataValue(AnimationTarget target) {
        super(target);
    }
    public AnimatablePathDataValue(AnimationTarget target, short[] commands,
                                   float[] parameters) {
        super(target);
        this.commands = commands;
        this.parameters = parameters;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to, float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatablePathDataValue toValue = (AnimatablePathDataValue) to;
        AnimatablePathDataValue accValue =
            (AnimatablePathDataValue) accumulation;
        boolean hasTo = to != null;
        boolean hasAcc = accumulation != null;
        boolean canInterpolate = hasTo
            && toValue.parameters.length == parameters.length
            && Arrays.equals(toValue.commands, commands);
        boolean canAccumulate = hasAcc
            && accValue.parameters.length == parameters.length
            && Arrays.equals(accValue.commands, commands);
        AnimatablePathDataValue base;
        if (!canInterpolate && hasTo && interpolation >= 0.5) {
            base = toValue;
        } else {
            base = this;
        }
        int cmdCount = base.commands.length;
        int paramCount = base.parameters.length;
        AnimatablePathDataValue res;
        if (result == null) {
            res = new AnimatablePathDataValue(target);
            res.commands = new short[cmdCount];
            res.parameters = new float[paramCount];
            System.arraycopy(base.commands, 0, res.commands, 0, cmdCount);
        } else {
            res = (AnimatablePathDataValue) result;
            if (res.commands == null || res.commands.length != cmdCount) {
                res.commands = new short[cmdCount];
                System.arraycopy(base.commands, 0, res.commands, 0, cmdCount);
                res.hasChanged = true;
            } else {
                if (!Arrays.equals(base.commands, res.commands)) {
                    System.arraycopy(base.commands, 0, res.commands, 0,
                                     cmdCount);
                    res.hasChanged = true;
                }
            }
        }
        for (int i = 0; i < paramCount; i++) {
            float newValue = base.parameters[i];
            if (canInterpolate) {
                newValue += interpolation * (toValue.parameters[i] - newValue);
            }
            if (canAccumulate) {
                newValue += multiplier * accValue.parameters[i];
            }
            if (res.parameters[i] != newValue) {
                res.parameters[i] = newValue;
                res.hasChanged = true;
            }
        }
        return res;
    }
    public short[] getCommands() {
        return commands;
    }
    public float[] getParameters() {
        return parameters;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        short[] cmds = new short[commands.length];
        System.arraycopy(commands, 0, cmds, 0, commands.length);
        float[] params = new float[parameters.length];
        return new AnimatablePathDataValue(target, cmds, params);
    }
    protected static final char[] PATH_COMMANDS = {
        ' ', 'z', 'M', 'm', 'L', 'l', 'C', 'c', 'Q', 'q', 'A', 'a', 'H', 'h',
        'V', 'v', 'S', 's', 'T', 't'
    };
    protected static final int[] PATH_PARAMS = {
        0, 0, 2, 2, 2, 2, 6, 6, 4, 4, 7, 7, 1, 1, 1, 1, 4, 4, 2, 2
    };
    public String toStringRep() {
        StringBuffer sb = new StringBuffer();
        int k = 0;
        for (int i = 0; i < commands.length; i++) {
            sb.append(PATH_COMMANDS[commands[i]]);
            for (int j = 0; j < PATH_PARAMS[commands[i]]; j++) {
                sb.append(' ');
                sb.append(parameters[k++]);
            }
        }
        return sb.toString();
    }
}
