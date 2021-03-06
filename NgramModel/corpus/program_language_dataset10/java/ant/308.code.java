package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildException;
public class Equals implements Condition {
    private static final int REQUIRED = 1 | 2;
    private Object arg1, arg2;
    private boolean trim = false;
    private boolean caseSensitive = true;
    private int args;
    private boolean forcestring = false;
    public void setArg1(Object arg1) {
        if (arg1 instanceof String) {
            setArg1((String) arg1);
        } else {
            setArg1Internal(arg1);
        }
    }
    public void setArg1(String a1) {
        setArg1Internal(a1);
    }
    private void setArg1Internal(Object arg1) {
        this.arg1 = arg1;
        args |= 1;
    }
    public void setArg2(Object arg2) {
        if (arg2 instanceof String) {
            setArg2((String) arg2);
        } else {
            setArg2Internal(arg2);
        }
    }
    public void setArg2(String a2) {
        setArg2Internal(a2);
    }
    private void setArg2Internal(Object arg2) {
        this.arg2 = arg2;
        args |= 2;
    }
    public void setTrim(boolean b) {
        trim = b;
    }
    public void setCasesensitive(boolean b) {
        caseSensitive = b;
    }
    public void setForcestring(boolean forcestring) {
        this.forcestring = forcestring;
    }
    public boolean eval() throws BuildException {
        if ((args & REQUIRED) != REQUIRED) {
            throw new BuildException("both arg1 and arg2 are required in equals");
        }
        if (arg1 == arg2 || arg1 != null && arg1.equals(arg2)) {
            return true;
        }
        if (forcestring) {
            arg1 = arg1 == null || arg1 instanceof String ? arg1 : arg1.toString();
            arg2 = arg2 == null || arg2 instanceof String ? arg2 : arg2.toString();
        }
        if (arg1 instanceof String && trim) {
            arg1 = ((String) arg1).trim();
        }
        if (arg2 instanceof String && trim) {
            arg2 = ((String) arg2).trim();
        }
        if (arg1 instanceof String && arg2 instanceof String) {
            String s1 = (String) arg1;
            String s2 = (String) arg2;
            return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
        }
        return false;
    }
}
