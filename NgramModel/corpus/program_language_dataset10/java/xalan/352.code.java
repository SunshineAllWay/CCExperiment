package org.apache.xalan.xsltc.compiler;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
public abstract class Pattern extends Expression {
    public abstract Type typeCheck(SymbolTable stable) throws TypeCheckError;
    public abstract void translate(ClassGenerator classGen,
				   MethodGenerator methodGen);
    public abstract double getPriority();
}