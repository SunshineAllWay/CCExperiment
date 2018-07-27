package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.File;
import java.util.Vector;
public abstract class BaseTest {
    protected boolean haltOnError = false;
    protected boolean haltOnFail = false;
    protected boolean filtertrace = true;
    protected boolean fork = false;
    protected String ifProperty = null;
    protected String unlessProperty = null;
    protected Vector formatters = new Vector();
    protected File destDir = null;
    protected String failureProperty;
    protected String errorProperty;
    private Object ifCond, unlessCond;
    public void setFiltertrace(boolean value) {
        filtertrace = value;
    }
    public boolean getFiltertrace() {
        return filtertrace;
    }
    public void setFork(boolean value) {
        fork = value;
    }
    public boolean getFork() {
        return fork;
    }
    public void setHaltonerror(boolean value) {
        haltOnError = value;
    }
    public void setHaltonfailure(boolean value) {
        haltOnFail = value;
    }
    public boolean getHaltonerror() {
        return haltOnError;
    }
    public boolean getHaltonfailure() {
        return haltOnFail;
    }
    public void setIf(Object ifCondition) {
        ifCond = ifCondition;
        ifProperty = ifCondition != null ? String.valueOf(ifCondition) : null;
    }
    public void setIf(String propertyName) {
        setIf((Object) propertyName);
    }
    public Object getIfCondition() {
        return ifCond;
    }
    public void setUnless(Object unlessCondition) {
        unlessCond = unlessCondition;
        unlessProperty = unlessCondition != null
            ? String.valueOf(unlessCondition) : null;
    }
    public void setUnless(String propertyName) {
        setUnless((Object) propertyName);
    }
    public Object getUnlessCondition() {
        return unlessCond;
    }
    public void addFormatter(FormatterElement elem) {
        formatters.addElement(elem);
    }
    public void setTodir(File destDir) {
        this.destDir = destDir;
    }
    public String getTodir() {
        if (destDir != null) {
            return destDir.getAbsolutePath();
        }
        return null;
    }
    public String getFailureProperty() {
        return failureProperty;
    }
    public void setFailureProperty(String failureProperty) {
        this.failureProperty = failureProperty;
    }
    public String getErrorProperty() {
        return errorProperty;
    }
    public void setErrorProperty(String errorProperty) {
        this.errorProperty = errorProperty;
    }
}
