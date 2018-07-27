package org.apache.batik.util.resources;
public class ResourceFormatException extends RuntimeException {
    protected String className;
    protected String key;
    public ResourceFormatException(String s, String className, String key) {
        super(s);
        this.className = className;
        this.key = key;
    }
    public String getClassName() {
        return className;
    }
    public String getKey() {
        return key;
    }
    public String toString() {
        return super.toString()+" ("+getKey()+", bundle: "+getClassName()+")";
    }
}
