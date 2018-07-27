package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildException;
public abstract class EnumeratedAttribute {
    protected String value;
    private int index = -1;
    public abstract String[] getValues();
    protected EnumeratedAttribute() {
    }
    public static EnumeratedAttribute getInstance(
        Class clazz,
        String value) throws BuildException {
        if (!EnumeratedAttribute.class.isAssignableFrom(clazz)) {
            throw new BuildException(
                "You have to provide a subclass from EnumeratedAttribut as clazz-parameter.");
        }
        EnumeratedAttribute ea = null;
        try {
            ea = (EnumeratedAttribute) clazz.newInstance();
        } catch (Exception e) {
            throw new BuildException(e);
        }
        ea.setValue(value);
        return ea;
    }
    public final void setValue(String value) throws BuildException {
        int idx = indexOfValue(value);
        if (idx == -1) {
            throw new BuildException(value + " is not a legal value for this attribute");
        }
        this.index = idx;
        this.value = value;
    }
    public final boolean containsValue(String value) {
        return (indexOfValue(value) != -1);
    }
    public final int indexOfValue(String value) {
        String[] values = getValues();
        if (values == null || value == null) {
            return -1;
        }
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                return i;
            }
        }
        return -1;
    }
    public final String getValue() {
        return value;
    }
    public final int getIndex() {
        return index;
    }
    public String toString() {
        return getValue();
    }
}
