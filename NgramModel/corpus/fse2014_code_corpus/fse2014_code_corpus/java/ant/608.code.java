package org.apache.tools.ant.types;
public final class Parameter {
    private String name = null;
    private String type = null;
    private String value = null;
    public void setName(final String name) {
        this.name = name;
    }
    public void setType(final String type) {
        this.type = type;
    }
    public void setValue(final String value) {
        this.value = value;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getValue() {
        return value;
    }
}
