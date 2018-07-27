package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
final class NamespaceUriCall extends NameBase {
    public NamespaceUriCall(QName fname) {
	super(fname);
    }
    public NamespaceUriCall(QName fname, Vector arguments) {
	super(fname, arguments);
    }
    public void translate(ClassGenerator classGen,
			  MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	final int getNamespace = cpg.addInterfaceMethodref(DOM_INTF,
							   "getNamespaceName",
							   "(I)"+STRING_SIG);
	super.translate(classGen, methodGen);
	il.append(new INVOKEINTERFACE(getNamespace, 2));
    }
}
