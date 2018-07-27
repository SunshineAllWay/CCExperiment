package org.apache.xalan.xsltc.compiler;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.IF_ICMPNE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.BooleanType;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.IntType;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.NodeSetType;
import org.apache.xalan.xsltc.compiler.util.NodeType;
import org.apache.xalan.xsltc.compiler.util.NumberType;
import org.apache.xalan.xsltc.compiler.util.RealType;
import org.apache.xalan.xsltc.compiler.util.ReferenceType;
import org.apache.xalan.xsltc.compiler.util.ResultTreeType;
import org.apache.xalan.xsltc.compiler.util.StringType;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.runtime.Operators;
final class EqualityExpr extends Expression {
    private final int _op;
    private Expression _left;
    private Expression _right;
    public EqualityExpr(int op, Expression left, Expression right) {
	_op = op;
	(_left = left).setParent(this);
	(_right = right).setParent(this);
    }
    public void setParser(Parser parser) {
	super.setParser(parser);
	_left.setParser(parser);
	_right.setParser(parser);
    }
    public String toString() {
        return Operators.getOpNames(_op) + '(' + _left + ", " + _right + ')';
    }
    public Expression getLeft() {
	return _left;
    }
    public Expression getRight() {
	return _right;
    }
    public boolean getOp() {
        return (_op != Operators.NE);
    }
    public boolean hasPositionCall() {
	if (_left.hasPositionCall()) return true;
	if (_right.hasPositionCall()) return true;
	return false;
    }
    public boolean hasLastCall() {
	if (_left.hasLastCall()) return true;
	if (_right.hasLastCall()) return true;
	return false;
    }
    private void swapArguments() {
	final Expression temp = _left;
	_left = _right;
	_right = temp;
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	final Type tleft = _left.typeCheck(stable); 
	final Type tright = _right.typeCheck(stable);
	if (tleft.isSimple() && tright.isSimple()) {
	    if (tleft != tright) {
		if (tleft instanceof BooleanType) {
		    _right = new CastExpr(_right, Type.Boolean);
		}
		else if (tright instanceof BooleanType) {
		    _left = new CastExpr(_left, Type.Boolean);
		}
		else if (tleft instanceof NumberType || 
			 tright instanceof NumberType) {
		    _left = new CastExpr(_left, Type.Real);
		    _right = new CastExpr(_right, Type.Real);
		}
		else {		
		    _left = new CastExpr(_left,   Type.String);
		    _right = new CastExpr(_right, Type.String);
		}
	    }
	}
	else if (tleft instanceof ReferenceType) {
	    _right = new CastExpr(_right, Type.Reference);
	}
	else if (tright instanceof ReferenceType) {
	    _left = new CastExpr(_left, Type.Reference);
	}
	else if (tleft instanceof NodeType && tright == Type.String) {
	    _left = new CastExpr(_left, Type.String);
	}
	else if (tleft == Type.String && tright instanceof NodeType) {
	    _right = new CastExpr(_right, Type.String);
	}
	else if (tleft instanceof NodeType && tright instanceof NodeType) {
	    _left = new CastExpr(_left, Type.String);
	    _right = new CastExpr(_right, Type.String);
	}
	else if (tleft instanceof NodeType && tright instanceof NodeSetType) {
	}
	else if (tleft instanceof NodeSetType && tright instanceof NodeType) {
	    swapArguments();	
	}
	else {	
	    if (tleft instanceof NodeType) {
		_left = new CastExpr(_left, Type.NodeSet);
	    }
	    if (tright instanceof NodeType) {
		_right = new CastExpr(_right, Type.NodeSet);
	    }
	    if (tleft.isSimple() ||
		tleft instanceof ResultTreeType &&
		tright instanceof NodeSetType) {
		swapArguments();
	    }
	    if (_right.getType() instanceof IntType) {
		_right = new CastExpr(_right, Type.Real);
	    }
	}
	return _type = Type.Boolean;
    }
    public void translateDesynthesized(ClassGenerator classGen,
				       MethodGenerator methodGen) {
	final Type tleft = _left.getType();
	final InstructionList il = methodGen.getInstructionList();
	if (tleft instanceof BooleanType) {
	    _left.translate(classGen, methodGen);
	    _right.translate(classGen, methodGen);
        _falseList.add(il.append(_op == Operators.EQ ? 
				     (BranchInstruction)new IF_ICMPNE(null) :
				     (BranchInstruction)new IF_ICMPEQ(null)));
	}
	else if (tleft instanceof NumberType) {
	    _left.translate(classGen, methodGen);
	    _right.translate(classGen, methodGen);
	    if (tleft instanceof RealType) {
		il.append(DCMPG);
        _falseList.add(il.append(_op == Operators.EQ ? 
					 (BranchInstruction)new IFNE(null) : 
					 (BranchInstruction)new IFEQ(null)));
	    }
	    else {
            _falseList.add(il.append(_op == Operators.EQ ? 
					 (BranchInstruction)new IF_ICMPNE(null) :
					 (BranchInstruction)new IF_ICMPEQ(null)));
	    }
	}
	else {
	    translate(classGen, methodGen);
	    desynthesize(classGen, methodGen);
	}
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	final Type tleft = _left.getType();
	Type tright = _right.getType();
	if (tleft instanceof BooleanType || tleft instanceof NumberType) {
	    translateDesynthesized(classGen, methodGen);
	    synthesize(classGen, methodGen);
	    return;
	}
	if (tleft instanceof StringType) {
	    final int equals = cpg.addMethodref(STRING_CLASS,
						"equals",
						"(" + OBJECT_SIG +")Z");
	    _left.translate(classGen, methodGen);
	    _right.translate(classGen, methodGen);
	    il.append(new INVOKEVIRTUAL(equals));
        if (_op == Operators.NE) {
		il.append(ICONST_1);
		il.append(IXOR);			
	    }
	    return;
	}
	BranchHandle truec, falsec;
	if (tleft instanceof ResultTreeType) {
	    if (tright instanceof BooleanType) {
		_right.translate(classGen, methodGen);
        if (_op == Operators.NE) {
		    il.append(ICONST_1);
		    il.append(IXOR); 
		}
		return;
	    }
	    if (tright instanceof RealType) {
		_left.translate(classGen, methodGen);
		tleft.translateTo(classGen, methodGen, Type.Real);
		_right.translate(classGen, methodGen);
		il.append(DCMPG);
        falsec = il.append(_op == Operators.EQ ? 
				   (BranchInstruction) new IFNE(null) : 
				   (BranchInstruction) new IFEQ(null));
		il.append(ICONST_1);
		truec = il.append(new GOTO(null));
		falsec.setTarget(il.append(ICONST_0));
		truec.setTarget(il.append(NOP));
		return;
	    }
	    _left.translate(classGen, methodGen);
	    tleft.translateTo(classGen, methodGen, Type.String);
	    _right.translate(classGen, methodGen);
	    if (tright instanceof ResultTreeType) {
		tright.translateTo(classGen, methodGen, Type.String);
	    }
	    final int equals = cpg.addMethodref(STRING_CLASS,
						"equals",
						"(" +OBJECT_SIG+ ")Z");
	    il.append(new INVOKEVIRTUAL(equals));
        if (_op == Operators.NE) {
		il.append(ICONST_1);
		il.append(IXOR);			
	    }
	    return;
	}
	if (tleft instanceof NodeSetType && tright instanceof BooleanType) {
	    _left.translate(classGen, methodGen);
	    _left.startIterator(classGen, methodGen);
	    Type.NodeSet.translateTo(classGen, methodGen, Type.Boolean);
	    _right.translate(classGen, methodGen);
	    il.append(IXOR); 
        if (_op == Operators.EQ) {
		il.append(ICONST_1);
		il.append(IXOR); 
	    }
	    return;
	}
	if (tleft instanceof NodeSetType && tright instanceof StringType) {
	    _left.translate(classGen, methodGen);
	    _left.startIterator(classGen, methodGen); 
	    _right.translate(classGen, methodGen);
	    il.append(new PUSH(cpg, _op));
	    il.append(methodGen.loadDOM());
	    final int cmp = cpg.addMethodref(BASIS_LIBRARY_CLASS,
					     "compare",
					     "("
					     + tleft.toSignature() 
					     + tright.toSignature()
					     + "I"
					     + DOM_INTF_SIG
					     + ")Z");
	    il.append(new INVOKESTATIC(cmp));
	    return;
	}
	_left.translate(classGen, methodGen);
	_left.startIterator(classGen, methodGen);
	_right.translate(classGen, methodGen);
	_right.startIterator(classGen, methodGen);
	if (tright instanceof ResultTreeType) {
	    tright.translateTo(classGen, methodGen, Type.String);	
	    tright = Type.String;
	}
	il.append(new PUSH(cpg, _op));
	il.append(methodGen.loadDOM());
	final int compare = cpg.addMethodref(BASIS_LIBRARY_CLASS,
					     "compare",
					     "("
					     + tleft.toSignature() 
					     + tright.toSignature()
					     + "I"
					     + DOM_INTF_SIG
					     + ")Z");
	il.append(new INVOKESTATIC(compare));
    }
}
