package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
final class CeilingCall extends FunctionCall {
    public CeilingCall(QName fname, Vector arguments) {
	super(fname, arguments);
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	argument(0).translate(classGen, methodGen);
	il.append(new INVOKESTATIC(cpg.addMethodref(MATH_CLASS,
						    "ceil", "(D)D")));
    }
}