package org.apache.tools.ant;
public interface DynamicAttribute {
    void setDynamicAttribute(String name, String value)
            throws BuildException;
}
