package org.apache.tools.ant.taskdefs.optional.extension;
public final class Compatibility {
    private final String name;
    Compatibility(final String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
}