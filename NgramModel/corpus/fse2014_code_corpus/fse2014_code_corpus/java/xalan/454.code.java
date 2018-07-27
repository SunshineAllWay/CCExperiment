package org.apache.xalan.xsltc.compiler.util;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.Constants;
public final class VoidType extends Type {
    protected VoidType() {}
    public String toString() {
	return "void";
    }
    public boolean identicalTo(Type other) {
	return this == other;
    }
    public String toSignature() {
	return "V";
    }
    public org.apache.bcel.generic.Type toJCType() {
	return null;	
    }
    public Instruction POP() {
        return NOP;
    }
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen,
			    Type type) {
	if (type == Type.String) {
	    translateTo(classGen, methodGen, (StringType) type);
	}
	else {
	    ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
					toString(), type.toString());
	    classGen.getParser().reportError(Constants.FATAL, err);
	}
    }
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen,
			    StringType type) {
	final InstructionList il = methodGen.getInstructionList();
	il.append(new PUSH(classGen.getConstantPool(), ""));
    }
    public void translateFrom(ClassGenerator classGen, MethodGenerator methodGen,
			      Class clazz) {
	if (!clazz.getName().equals("void")) {
	    ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
					toString(), clazz.getName());
	    classGen.getParser().reportError(Constants.FATAL, err);
	}
    }
}
