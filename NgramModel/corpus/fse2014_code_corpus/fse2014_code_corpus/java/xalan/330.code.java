package org.apache.xalan.xsltc.compiler;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
final class LiteralExpr extends Expression {
    private final String _value;
    private final String _namespace;
    public LiteralExpr(String value) {
	_value = value;
	_namespace = null;
    }
    public LiteralExpr(String value, String namespace) {
	_value = value;
	_namespace = namespace.equals(Constants.EMPTYSTRING) ? null : namespace;
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	return _type = Type.String;
    }
    public String toString() {
	return "literal-expr(" + _value + ')';
    }
    protected boolean contextDependent() {
	return false;
    }
    protected String getValue() {
	return _value;
    }
    protected String getNamespace() {
	return _namespace;
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	il.append(new PUSH(cpg, _value));
    }
}
