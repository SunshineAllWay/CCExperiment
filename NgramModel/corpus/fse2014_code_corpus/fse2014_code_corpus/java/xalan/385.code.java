package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
final class UnsupportedElement extends SyntaxTreeNode {
    private Vector _fallbacks = null;
    private ErrorMsg _message = null;
    private boolean _isExtension = false;
    public UnsupportedElement(String uri, String prefix, String local, boolean isExtension) {
	super(uri, prefix, local);
	_isExtension = isExtension;
    }
    public void setErrorMessage(ErrorMsg message) {
	_message = message;
    }
    public void display(int indent) {
	indent(indent);
	Util.println("Unsupported element = " + _qname.getNamespace() +
		     ":" + _qname.getLocalPart());
	displayContents(indent + IndentIncrement);
    }
    private void processFallbacks(Parser parser) {
	Vector children = getContents();
	if (children != null) {
	    final int count = children.size();
	    for (int i = 0; i < count; i++) {
		SyntaxTreeNode child = (SyntaxTreeNode)children.elementAt(i);
		if (child instanceof Fallback) {
		    Fallback fallback = (Fallback)child;
		    fallback.activate();
		    fallback.parseContents(parser);
		    if (_fallbacks == null) {
		    	_fallbacks = new Vector();
		    }
		    _fallbacks.addElement(child);
		}
	    }
	}
    }
    public void parseContents(Parser parser) {
    	processFallbacks(parser);
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {	
	if (_fallbacks != null) {
	    int count = _fallbacks.size();
	    for (int i = 0; i < count; i++) {
	        Fallback fallback = (Fallback)_fallbacks.elementAt(i);
	        fallback.typeCheck(stable);
	    }
	}
	return Type.Void;
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	if (_fallbacks != null) {
	    int count = _fallbacks.size();
	    for (int i = 0; i < count; i++) {
	        Fallback fallback = (Fallback)_fallbacks.elementAt(i);
	        fallback.translate(classGen, methodGen);
	    }
	}
	else {		
	    ConstantPoolGen cpg = classGen.getConstantPool();
	    InstructionList il = methodGen.getInstructionList();
	    final int unsupportedElem = cpg.addMethodref(BASIS_LIBRARY_CLASS, "unsupported_ElementF",
                                                         "(" + STRING_SIG + "Z)V");	 
	    il.append(new PUSH(cpg, getQName().toString()));
	    il.append(new PUSH(cpg, _isExtension));
	    il.append(new INVOKESTATIC(unsupportedElem));		
	}
    }
}
