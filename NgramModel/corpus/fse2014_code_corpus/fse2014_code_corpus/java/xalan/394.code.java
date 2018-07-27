package org.apache.xalan.xsltc.compiler;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.ReferenceType;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
import org.apache.xml.utils.XML11Char;
final class WithParam extends Instruction {
    private QName _name;
    protected String _escapedName;
    private Expression _select;
    private boolean _doParameterOptimization = false;
    public void display(int indent) {
	indent(indent);
	Util.println("with-param " + _name);
	if (_select != null) {
	    indent(indent + IndentIncrement);
	    Util.println("select " + _select.toString());
	}
	displayContents(indent + IndentIncrement);
    }
    public String getEscapedName() {
	return _escapedName;
    }    
    public QName getName() {
        return _name;	
    }
    public void setName(QName name) {
	_name = name;
	_escapedName = Util.escape(name.getStringRep());
    }    
    public void setDoParameterOptimization(boolean flag) {
    	_doParameterOptimization = flag;
    }
    public void parseContents(Parser parser) {
	final String name = getAttribute("name");
	if (name.length() > 0) {
            if (!XML11Char.isXML11ValidQName(name)) {
                ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, name,
                                            this);
                parser.reportError(Constants.ERROR, err);
            }
	    setName(parser.getQNameIgnoreDefaultNs(name));
	}
        else {
	    reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
        }
	final String select = getAttribute("select");
	if (select.length() > 0) {
	    _select = parser.parseExpression(this, "select", null);
	}
	parseChildren(parser);
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	if (_select != null) {
	    final Type tselect = _select.typeCheck(stable);
	    if (tselect instanceof ReferenceType == false) {
		_select = new CastExpr(_select, Type.Reference);
	    }
	}
	else {
	    typeCheckContents(stable);
	}
	return Type.Void;
    }
    public void translateValue(ClassGenerator classGen,
			       MethodGenerator methodGen) {
	if (_select != null) {
	    _select.translate(classGen, methodGen);
	    _select.startIterator(classGen, methodGen);
	}
	else if (hasContents()) {
	    compileResultTree(classGen, methodGen);
	}
	else {
	    final ConstantPoolGen cpg = classGen.getConstantPool();
	    final InstructionList il = methodGen.getInstructionList();
	    il.append(new PUSH(cpg, Constants.EMPTYSTRING));
	}
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	if (_doParameterOptimization) {
	    translateValue(classGen, methodGen);
	    return;
	}
	String name = Util.escape(getEscapedName());
	il.append(classGen.loadTranslet());
	il.append(new PUSH(cpg, name)); 
	translateValue(classGen, methodGen);
	il.append(new PUSH(cpg, false));
	il.append(new INVOKEVIRTUAL(cpg.addMethodref(TRANSLET_CLASS,
						     ADD_PARAMETER,
						     ADD_PARAMETER_SIG)));
	il.append(POP); 
    }
}
