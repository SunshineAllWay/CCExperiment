package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
public class Assertions extends DataType implements Cloneable {
    private Boolean enableSystemAssertions;
    private ArrayList assertionList = new ArrayList();
    public void addEnable(EnabledAssertion assertion) {
        checkChildrenAllowed();
        assertionList.add(assertion);
    }
    public void addDisable(DisabledAssertion assertion) {
        checkChildrenAllowed();
        assertionList.add(assertion);
    }
    public void setEnableSystemAssertions(Boolean enableSystemAssertions) {
        checkAttributesAllowed();
        this.enableSystemAssertions = enableSystemAssertions;
    }
    public void setRefid(Reference ref) {
        if (assertionList.size() > 0 || enableSystemAssertions != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }
    private Assertions getFinalReference() {
        if (getRefid() == null) {
            return this;
        } else {
            Object o = getRefid().getReferencedObject(getProject());
            if (!(o instanceof Assertions)) {
                throw new BuildException("reference is of wrong type");
            }
            return (Assertions) o;
        }
    }
    public int size() {
        Assertions clause = getFinalReference();
        return clause.getFinalSize();
    }
    private int getFinalSize() {
        return assertionList.size() + (enableSystemAssertions != null ? 1 : 0);
    }
    public void applyAssertions(List commandList) {
        getProject().log("Applying assertions", Project.MSG_DEBUG);
        Assertions clause = getFinalReference();
        if (Boolean.TRUE.equals(clause.enableSystemAssertions)) {
            getProject().log("Enabling system assertions", Project.MSG_DEBUG);
            commandList.add("-enablesystemassertions");
        } else if (Boolean.FALSE.equals(clause.enableSystemAssertions)) {
            getProject().log("disabling system assertions", Project.MSG_DEBUG);
            commandList.add("-disablesystemassertions");
        }
        Iterator it = clause.assertionList.iterator();
        while (it.hasNext()) {
            BaseAssertion assertion = (BaseAssertion) it.next();
            String arg = assertion.toCommand();
            getProject().log("adding assertion " + arg, Project.MSG_DEBUG);
            commandList.add(arg);
        }
    }
    public void applyAssertions(CommandlineJava command) {
        Assertions clause = getFinalReference();
        if (Boolean.TRUE.equals(clause.enableSystemAssertions)) {
            addVmArgument(command, "-enablesystemassertions");
        } else if (Boolean.FALSE.equals(clause.enableSystemAssertions)) {
            addVmArgument(command, "-disablesystemassertions");
        }
        Iterator it = clause.assertionList.iterator();
        while (it.hasNext()) {
            BaseAssertion assertion = (BaseAssertion) it.next();
            String arg = assertion.toCommand();
            addVmArgument(command, arg);
        }
    }
    public void applyAssertions(final ListIterator commandIterator) {
        getProject().log("Applying assertions", Project.MSG_DEBUG);
        Assertions clause = getFinalReference();
        if (Boolean.TRUE.equals(clause.enableSystemAssertions)) {
            getProject().log("Enabling system assertions", Project.MSG_DEBUG);
            commandIterator.add("-enablesystemassertions");
        } else if (Boolean.FALSE.equals(clause.enableSystemAssertions)) {
            getProject().log("disabling system assertions", Project.MSG_DEBUG);
            commandIterator.add("-disablesystemassertions");
        }
        Iterator it = clause.assertionList.iterator();
        while (it.hasNext()) {
            BaseAssertion assertion = (BaseAssertion) it.next();
            String arg = assertion.toCommand();
            getProject().log("adding assertion " + arg, Project.MSG_DEBUG);
            commandIterator.add(arg);
        }
    }
    private static void addVmArgument(CommandlineJava command, String arg) {
        Commandline.Argument argument;
        argument = command.createVmArgument();
        argument.setValue(arg);
    }
    public Object clone() throws CloneNotSupportedException {
        Assertions that = (Assertions) super.clone();
        that.assertionList = (ArrayList) assertionList.clone();
        return that;
    }
    public abstract static class BaseAssertion {
        private String packageName;
        private String className;
        public void setClass(String className) {
            this.className = className;
        }
        public void setPackage(String packageName) {
            this.packageName = packageName;
        }
        protected String getClassName() {
            return className;
        }
        protected String getPackageName() {
            return packageName;
        }
        public abstract String getCommandPrefix();
        public String toCommand() {
            if (getPackageName() != null && getClassName() != null) {
                throw new BuildException("Both package and class have been set");
            }
            StringBuffer command = new StringBuffer(getCommandPrefix());
            if (getPackageName() != null) {
                command.append(':');
                command.append(getPackageName());
                if (!command.toString().endsWith("...")) {
                    command.append("...");
                }
            } else if (getClassName() != null) {
                command.append(':');
                command.append(getClassName());
            }
            return command.toString();
        }
    }
    public static class EnabledAssertion extends BaseAssertion {
        public String getCommandPrefix() {
            return "-ea";
        }
    }
    public static class DisabledAssertion extends BaseAssertion {
        public String getCommandPrefix() {
            return "-da";
        }
    }
}
