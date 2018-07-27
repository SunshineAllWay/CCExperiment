package org.apache.tools.ant.types;
public class FlexInteger {
    private Integer value;
    public FlexInteger(String value) {
        this.value = Integer.decode(value);
    }
    public int intValue() {
        return value.intValue();
    }
    public String toString() {
        return value.toString();
    }
}
