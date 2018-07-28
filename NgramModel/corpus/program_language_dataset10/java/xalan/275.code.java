package org.apache.xalan.xsltc.compiler;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
final class ApplyImports extends Instruction {
    private QName      _modeName;
    private int        _precedence;
    public void display(int indent) {
	indent(indent);
	Util.println("ApplyTemplates");
	indent(indent + IndentIncrement);
	if (_modeName != null) {
	    indent(indent + IndentIncrement);
	    Util.println("mode " + _modeName);
	}
    }
    public boolean hasWithParams() {
	return hasContents();
    }
    private int getMinPrecedence(int max) {
        Stylesheet includeRoot = getStylesheet();
        while (includeRoot._includedFrom != null) {
            includeRoot = includeRoot._includedFrom;
        }
        return includeRoot.getMinimumDescendantPrecedence();
    }
    public void parseContents(Parser parser) {
	Stylesheet stylesheet = getStylesheet();
	stylesheet.setTemplateInlining(false);
	Template template = getTemplate();
	_modeName = template.getModeName();
	_precedence = template.getImportPrecedence();
	parseChildren(parser);	
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	typeCheckContents(stable);		
	return Type.Void;
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final Stylesheet stylesheet = classGen.getStylesheet();
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	final int current = methodGen.getLocalIndex("current");
	il.append(classGen.loadTranslet());
	il.append(methodGen.loadDOM());
    il.append(methodGen.loadIterator());
	il.append(methodGen.loadHandler());
    il.append(methodGen.loadCurrentNode());
        if (stylesheet.hasLocalParams()) {
            il.append(classGen.loadTranslet());
            final int pushFrame = cpg.addMethodref(TRANSLET_CLASS,
                                                   PUSH_PARAM_FRAME,
                                                   PUSH_PARAM_FRAME_SIG);
            il.append(new INVOKEVIRTUAL(pushFrame));
        }
	final int maxPrecedence = _precedence;
	final int minPrecedence = getMinPrecedence(maxPrecedence);
	final Mode mode = stylesheet.getMode(_modeName);
	String functionName = mode.functionName(minPrecedence, maxPrecedence);
	final String className = classGen.getStylesheet().getClassName();
	final String signature = classGen.getApplyTemplatesSigForImport();
	final int applyTemplates = cpg.addMethodref(className,
						    functionName,
						    signature);
	il.append(new INVOKEVIRTUAL(applyTemplates));
        if (stylesheet.hasLocalParams()) {
            il.append(classGen.loadTranslet());
            final int pushFrame = cpg.addMethodref(TRANSLET_CLASS,
                                                   POP_PARAM_FRAME,
                                                   POP_PARAM_FRAME_SIG);
            il.append(new INVOKEVIRTUAL(pushFrame));
        }
    }
}
