package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildException;
public class Quantifier extends EnumeratedAttribute {
    private static final String[] VALUES
        = new String[] {"all", "each", "every", "any", "some", "one",
                        "majority", "most", "none"};
    public static final Quantifier ALL = new Quantifier("all");
    public static final Quantifier ANY = new Quantifier("any");
    public static final Quantifier ONE = new Quantifier("one");
    public static final Quantifier MAJORITY = new Quantifier("majority");
    public static final Quantifier NONE = new Quantifier("none");
    private abstract static class Predicate {
        abstract boolean eval(int t, int f);
    }
    private static final Predicate ALL_PRED = new Predicate() {
        boolean eval(int t, int f) { return f == 0; }
    };
    private static final Predicate ANY_PRED = new Predicate() {
        boolean eval(int t, int f) { return t > 0; }
    };
    private static final Predicate ONE_PRED = new Predicate() {
        boolean eval(int t, int f) { return t == 1; }
    };
    private static final Predicate MAJORITY_PRED = new Predicate() {
        boolean eval(int t, int f) { return t > f; }
    };
    private static final Predicate NONE_PRED = new Predicate() {
        boolean eval(int t, int f) { return t == 0; }
    };
    private static final Predicate[] PREDS = new Predicate[VALUES.length];
    static {
        PREDS[0] = ALL_PRED;
        PREDS[1] = ALL_PRED;
        PREDS[2] = ALL_PRED;
        PREDS[3] = ANY_PRED;
        PREDS[4] = ANY_PRED;
        PREDS[5] = ONE_PRED;
        PREDS[6] = MAJORITY_PRED;
        PREDS[7] = MAJORITY_PRED;
        PREDS[8] = NONE_PRED;
    }
    public Quantifier() {
    }
    public Quantifier(String value) {
        setValue(value);
    }
    public String[] getValues() {
        return VALUES;
    }
    public boolean evaluate(boolean[] b) {
        int t = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i]) {
                t++;
            }
        }
        return evaluate(t, b.length - t);
    }
    public boolean evaluate(int t, int f) {
        int index = getIndex();
        if (index == -1) {
            throw new BuildException("Quantifier value not set.");
        }
        return PREDS[index].eval(t, f);
    }
}
