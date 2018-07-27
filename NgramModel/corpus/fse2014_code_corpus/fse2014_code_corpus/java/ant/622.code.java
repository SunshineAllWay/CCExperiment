package org.apache.tools.ant.types;
import org.apache.tools.ant.Project;
public class Substitution extends DataType {
    public static final String DATA_TYPE_NAME = "substitution";
    private String expression;
    public Substitution() {
        this.expression = null;
    }
    public void setExpression(String expression) {
        this.expression = expression;
    }
    public String getExpression(Project p) {
        if (isReference()) {
            return getRef(p).getExpression(p);
        }
        return expression;
    }
    public Substitution getRef(Project p) {
        return (Substitution) getCheckedRef(p);
    }
}
