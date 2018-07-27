package org.apache.xalan.xsltc.compiler;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.BooleanType;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
final class If extends Instruction {
    private Expression _test;
    private boolean    _ignore = false;
    public void display(int indent) {
	indent(indent);
	Util.println("If");
	indent(indent + IndentIncrement);
	System.out.print("test ");
	Util.println(_test.toString());
	displayContents(indent + IndentIncrement);
    }
    public void parseContents(Parser parser) {
	_test = parser.parseExpression(this, "test", null);
        if (_test.isDummy()) {
	    reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "test");
	    return;
        }
	Object result = _test.evaluateAtCompileTime();
	if (result != null && result instanceof Boolean) {
	    _ignore = !((Boolean) result).booleanValue();
	}
	parseChildren(parser);
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	if (_test.typeCheck(stable) instanceof BooleanType == false) {
	    _test = new CastExpr(_test, Type.Boolean);
	}
	if (!_ignore) {
	    typeCheckContents(stable);
	}
	return Type.Void;
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final InstructionList il = methodGen.getInstructionList();
	_test.translateDesynthesized(classGen, methodGen);
	final InstructionHandle truec = il.getEnd();
	if (!_ignore) {
	    translateContents(classGen, methodGen);
	}
	_test.backPatchFalseList(il.append(NOP));
	_test.backPatchTrueList(truec.getNext());
    }
}
