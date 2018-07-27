package org.apache.batik.util.gui.resource;
public class MissingListenerException extends RuntimeException {
    private String className;
    private String key;
    public MissingListenerException(String s, String className, String key) {
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
