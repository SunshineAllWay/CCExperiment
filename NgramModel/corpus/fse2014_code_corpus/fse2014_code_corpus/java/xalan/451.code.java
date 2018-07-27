package org.apache.xalan.xsltc.compiler.util;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.xalan.xsltc.compiler.Constants;
import org.apache.xalan.xsltc.compiler.FlowList;
import org.apache.xalan.xsltc.compiler.NodeTest;
public abstract class Type implements Constants {
    public static final Type Int        = new IntType();
    public static final Type Real       = new RealType();
    public static final Type Boolean    = new BooleanType();
    public static final Type NodeSet    = new NodeSetType();
    public static final Type String     = new StringType();
    public static final Type ResultTree = new ResultTreeType();
    public static final Type Reference  = new ReferenceType();
    public static final Type Void       = new VoidType();    
    public static final Type Object     = new ObjectType(java.lang.Object.class);
    public static final Type Node       = new NodeType(NodeTest.ANODE);
    public static final Type Root       = new NodeType(NodeTest.ROOT);
    public static final Type Element    = new NodeType(NodeTest.ELEMENT);
    public static final Type Attribute  = new NodeType(NodeTest.ATTRIBUTE);
    public static final Type Text       = new NodeType(NodeTest.TEXT);
    public static final Type Comment    = new NodeType(NodeTest.COMMENT);
    public static final Type Processing_Instruction = new NodeType(NodeTest.PI);
    public static Type newObjectType(String javaClassName) {
        if (javaClassName == "java.lang.Object") {
            return Type.Object;
        }
        else if (javaClassName == "java.lang.String") {
            return Type.String;
        } 
        else {
            return new ObjectType(javaClassName);
        }
    }
    public static Type newObjectType(Class clazz) {
        if (clazz == java.lang.Object.class) {
            return Type.Object;
        }
        else if (clazz == java.lang.String.class) {
            return Type.String;
        }
        else {
            return new ObjectType(clazz);
        }
    }
    public abstract String toString();
    public abstract boolean identicalTo(Type other);
    public boolean isNumber() {
	return false;
    }
    public boolean implementedAsMethod() {
	return false;
    }
    public boolean isSimple() {
	return false;
    }
    public abstract org.apache.bcel.generic.Type toJCType();
    public int distanceTo(Type type) {
	return type == this ? 0 : Integer.MAX_VALUE;
    }
    public abstract String toSignature();
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    Type type) {
	ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
				    toString(), type.toString());
	classGen.getParser().reportError(Constants.FATAL, err);
    }
    public FlowList translateToDesynthesized(ClassGenerator classGen, 
					     MethodGenerator methodGen, 
					     Type type) {
	FlowList fl = null;
	if (type == Type.Boolean) {
	    fl = translateToDesynthesized(classGen, methodGen,
					  (BooleanType)type);
	}
	else {
	    translateTo(classGen, methodGen, type);
	}
	return fl;
    }
    public FlowList translateToDesynthesized(ClassGenerator classGen, 
					     MethodGenerator methodGen, 
					     BooleanType type) {
	ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
				    toString(), type.toString());
	classGen.getParser().reportError(Constants.FATAL, err);
	return null;
    }
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    Class clazz) {
	ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
				    toString(), clazz.getClass().toString());
	classGen.getParser().reportError(Constants.FATAL, err);
    }
    public void translateFrom(ClassGenerator classGen, MethodGenerator methodGen,
			      Class clazz) {
	ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
				    clazz.getClass().toString(), toString());
	classGen.getParser().reportError(Constants.FATAL, err);
    }
    public void translateBox(ClassGenerator classGen,
			     MethodGenerator methodGen) {
	ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
				    toString(), "["+toString()+"]");
	classGen.getParser().reportError(Constants.FATAL, err);
    }
    public void translateUnBox(ClassGenerator classGen,
			       MethodGenerator methodGen) {
	ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
				    "["+toString()+"]", toString());
	classGen.getParser().reportError(Constants.FATAL, err);
    }
    public String getClassName() {
	return(EMPTYSTRING);
    }
    public Instruction ADD() {
	return null;		
    }
    public Instruction SUB() {
	return null;		
    }
    public Instruction MUL() {
	return null;		
    }
    public Instruction DIV() {
	return null;		
    }
    public Instruction REM() {
	return null;		
    }
    public Instruction NEG() {
	return null;		
    }
    public Instruction LOAD(int slot) {
	return null;		
    }
    public Instruction STORE(int slot) {
	return null;		
    }
    public Instruction POP() {
	return POP;
    }
    public BranchInstruction GT(boolean tozero) {
	return null;		
    }
    public BranchInstruction GE(boolean tozero) {
	return null;		
    }
    public BranchInstruction LT(boolean tozero) {
	return null;		
    }
    public BranchInstruction LE(boolean tozero) {
	return null;		
    }
    public Instruction CMP(boolean less) {
	return null;		
    }
    public Instruction DUP() {
	return DUP;	
    }
}
