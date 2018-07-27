package org.apache.tools.ant.taskdefs.optional.script;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
public class ScriptDefBase extends Task implements DynamicConfigurator {
    private Map nestedElementMap = new HashMap();
    private Map attributes = new HashMap();
    private String text;
    public void execute() {
        getScript().executeScript(attributes, nestedElementMap, this);
    }
    private ScriptDef getScript() {
        String name = getTaskType();
        Map scriptRepository
            = (Map) getProject().getReference(MagicNames.SCRIPT_REPOSITORY);
        if (scriptRepository == null) {
            throw new BuildException("Script repository not found for " + name);
        }
        ScriptDef definition = (ScriptDef) scriptRepository.get(getTaskType());
        if (definition == null) {
            throw new BuildException("Script definition not found for " + name);
        }
        return definition;
    }
    public Object createDynamicElement(String name)  {
        List nestedElementList = (List) nestedElementMap.get(name);
        if (nestedElementList == null) {
            nestedElementList = new ArrayList();
            nestedElementMap.put(name, nestedElementList);
        }
        Object element = getScript().createNestedElement(name);
        nestedElementList.add(element);
        return element;
    }
    public void setDynamicAttribute(String name, String value) {
        ScriptDef definition = getScript();
        if (!definition.isAttributeSupported(name)) {
                throw new BuildException("<" + getTaskType()
                    + "> does not support the \"" + name + "\" attribute");
        }
        attributes.put(name, value);
    }
    public void addText(String text) {
        this.text = getProject().replaceProperties(text);
    }
    public String getText() {
        return text;
    }
    public void fail(String message) {
        throw new BuildException(message);
    }
}
