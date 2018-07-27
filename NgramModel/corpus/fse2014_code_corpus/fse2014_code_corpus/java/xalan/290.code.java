package org.apache.xalan.xsltc.compiler;
public interface Closure {
    public boolean inInnerClass();
    public Closure getParentClosure();
    public String getInnerClassName();
    public void addVariable(VariableRefBase variableRef);
}
