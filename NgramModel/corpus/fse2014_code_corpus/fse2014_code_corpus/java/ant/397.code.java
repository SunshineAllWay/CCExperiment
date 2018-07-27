package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
public abstract class ConstantCPInfo extends ConstantPoolEntry {
    private Object value;
    protected ConstantCPInfo(int tagValue, int entries) {
        super(tagValue, entries);
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object newValue) {
        value = newValue;
    }
}
