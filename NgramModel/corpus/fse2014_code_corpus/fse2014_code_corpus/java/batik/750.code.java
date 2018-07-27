package org.apache.batik.ext.awt.image;
public final class CompositeRule implements java.io.Serializable {
    public static final int RULE_OVER = 1;
    public static final int RULE_IN = 2;
    public static final int RULE_OUT = 3;
    public static final int RULE_ATOP = 4;
    public static final int RULE_XOR = 5;
    public static final int RULE_ARITHMETIC = 6;
    public static final int RULE_MULTIPLY = 7;
    public static final int RULE_SCREEN = 8;
    public static final int RULE_DARKEN = 9;
    public static final int RULE_LIGHTEN = 10;
    public static final CompositeRule OVER = new CompositeRule(RULE_OVER);
    public static final CompositeRule IN = new CompositeRule(RULE_IN);
    public static final CompositeRule OUT = new CompositeRule(RULE_OUT);
    public static final CompositeRule ATOP = new CompositeRule(RULE_ATOP);
    public static final CompositeRule XOR = new CompositeRule(RULE_XOR);
    public static CompositeRule ARITHMETIC
        (float k1, float k2, float k3, float k4) {
        return new CompositeRule(k1, k2, k3, k4);
    }
    public static final CompositeRule MULTIPLY =
        new CompositeRule(RULE_MULTIPLY);
    public static final CompositeRule SCREEN =
        new CompositeRule(RULE_SCREEN);
    public static final CompositeRule DARKEN =
        new CompositeRule(RULE_DARKEN);
    public static final CompositeRule LIGHTEN =
        new CompositeRule(RULE_LIGHTEN);
    public int getRule() {
        return rule;
    }
    private int rule;
    private float k1, k2, k3, k4;
    private CompositeRule(int rule) {
        this.rule = rule;
    }
    private CompositeRule(float k1, float k2, float k3, float k4) {
        rule = RULE_ARITHMETIC;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
    }
    public float [] getCoefficients() {
        if (rule != RULE_ARITHMETIC)
            return null;
        return new float[] {k1, k2, k3, k4};
    }
    private Object readResolve() throws java.io.ObjectStreamException {
        switch(rule){
        case RULE_OVER:
            return OVER;
        case RULE_IN:
            return IN;
        case RULE_OUT:
            return OUT;
        case RULE_ATOP:
            return ATOP;
        case RULE_XOR:
            return XOR;
        case RULE_ARITHMETIC:
            return this;
        case RULE_MULTIPLY:
            return MULTIPLY;
        case RULE_SCREEN:
            return SCREEN;
        case RULE_DARKEN:
            return DARKEN;
        case RULE_LIGHTEN:
            return LIGHTEN;
        default:
            throw new Error("Unknown Composite Rule type");
        }
    }
    public String toString() {
        switch(rule){
        case RULE_OVER:
            return "[CompositeRule: OVER]";
        case RULE_IN:
            return "[CompositeRule: IN]";
        case RULE_OUT:
            return "[CompositeRule: OUT]";
        case RULE_ATOP:
            return "[CompositeRule: ATOP]";
        case RULE_XOR:
            return "[CompositeRule: XOR]";
        case RULE_ARITHMETIC:
            return ("[CompositeRule: ARITHMATIC k1:" +
                    k1 + " k2: " + k2 + " k3: " + k3 + " k4: " + k4 + ']' );
        case RULE_MULTIPLY:
            return "[CompositeRule: MULTIPLY]";
        case RULE_SCREEN:
            return "[CompositeRule: SCREEN]";
        case RULE_DARKEN:
            return "[CompositeRule: DARKEN]";
        case RULE_LIGHTEN:
            return "[CompositeRule: LIGHTEN]";
        default:
            throw new Error("Unknown Composite Rule type");
        }
    }
}
