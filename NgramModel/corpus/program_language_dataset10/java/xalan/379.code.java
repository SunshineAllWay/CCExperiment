package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
class TopLevelElement extends SyntaxTreeNode {
    protected Vector _dependencies = null;
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	return typeCheckContents(stable);
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	ErrorMsg msg = new ErrorMsg(ErrorMsg.NOT_IMPLEMENTED_ERR,
				    getClass(), this);
	getParser().reportError(FATAL, msg);
    }
    public InstructionList compile(ClassGenerator classGen,
				   MethodGenerator methodGen) {
	final InstructionList result, save = methodGen.getInstructionList();
	methodGen.setInstructionList(result = new InstructionList());
	translate(classGen, methodGen);
	methodGen.setInstructionList(save);
	return result;
    }
    public void display(int indent) {
	indent(indent);
	Util.println("TopLevelElement");
	displayContents(indent + IndentIncrement);
    }
    public void addDependency(TopLevelElement other) {
	if (_dependencies == null) {
	    _dependencies = new Vector();
	}
	if (!_dependencies.contains(other)) {
	    _dependencies.addElement(other);
	}
    }
    public Vector getDependencies() {
	return _dependencies;
    }
}