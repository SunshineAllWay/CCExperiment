package org.apache.tools.ant.taskdefs.optional.extension;
public final class DeweyDecimal extends org.apache.tools.ant.util.DeweyDecimal {
    public DeweyDecimal(final int[] components) {
        super(components);
    }
    public DeweyDecimal(final String string)
        throws NumberFormatException {
        super(string);
    }
}
