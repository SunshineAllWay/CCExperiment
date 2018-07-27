package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.Task;
public class DynamicTask extends Task implements DynamicConfigurator {
    public void execute() {
    }
    public void setDynamicAttribute(String name, String value) {
        getProject().setNewProperty(name, value);
    }
    public Object createDynamicElement(String name) {
        return new Sub();
    }
    public class Sub implements DynamicConfigurator {
        public void setDynamicAttribute(String name, String value) {
            getProject().setNewProperty(name, value);
        }
        public Object createDynamicElement(String name) {
            return null;
        }
    }
}
