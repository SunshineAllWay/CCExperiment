package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
final class LocalNameCall extends NameBase {
    public LocalNameCall(QName fname) {
	super(fname);
    }
    public LocalNameCall(QName fname, Vector arguments) {
	super(fname, arguments);
    }
    public void translate(ClassGenerator classGen,
			  MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	final int getNodeName = cpg.addInterfaceMethodref(DOM_INTF,
							  "getNodeName",
							  "(I)"+STRING_SIG);
	final int getLocalName = cpg.addMethodref(BASIS_LIBRARY_CLASS,
						  "getLocalName",
						  "(Ljava/lang/String;)"+
						  "Ljava/lang/String;");
	super.translate(classGen, methodGen);
	il.append(new INVOKEINTERFACE(getNodeName, 2));
	il.append(new INVOKESTATIC(getLocalName));
    }
}
