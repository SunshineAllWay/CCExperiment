package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTM;
final class Step extends RelativeLocationPath {
    private int _axis;
    private Vector _predicates;
    private boolean _hadPredicates = false;
    private int _nodeType;
    public Step(int axis, int nodeType, Vector predicates) {
	_axis = axis;
	_nodeType = nodeType;
	_predicates = predicates;
    }
    public void setParser(Parser parser) {
	super.setParser(parser);
	if (_predicates != null) {
	    final int n = _predicates.size();
	    for (int i = 0; i < n; i++) {
		final Predicate exp = (Predicate)_predicates.elementAt(i);
		exp.setParser(parser);
		exp.setParent(this);
	    }
	}
    }
    public int getAxis() {
	return _axis;
    }
    public void setAxis(int axis) {
	_axis = axis;
    }
    public int getNodeType() {
	return _nodeType;
    }
    public Vector getPredicates() {
	return _predicates;
    }
    public void addPredicates(Vector predicates) {
	if (_predicates == null) {
	    _predicates = predicates;
	}
	else {
	    _predicates.addAll(predicates);
	}
    }
    private boolean hasParentPattern() {
	final SyntaxTreeNode parent = getParent();
	return (parent instanceof ParentPattern ||
		parent instanceof ParentLocationPath ||
		parent instanceof UnionPathExpr ||
		parent instanceof FilterParentPath);
    }
    private boolean hasPredicates() {
	return _predicates != null && _predicates.size() > 0;
    }
    private boolean isPredicate() {
	SyntaxTreeNode parent = this;
	while (parent != null) {
	    parent = parent.getParent();
	    if (parent instanceof Predicate) return true;
	}
	return false;
    }
    public boolean isAbbreviatedDot() {
	return _nodeType == NodeTest.ANODE && _axis == Axis.SELF;
    }
    public boolean isAbbreviatedDDot() {
	return _nodeType == NodeTest.ANODE && _axis == Axis.PARENT;
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	_hadPredicates = hasPredicates();
	if (isAbbreviatedDot()) {
	    _type =  (hasParentPattern() || hasPredicates() ) ? 
		Type.NodeSet : Type.Node;
	}
	else {
	    _type = Type.NodeSet;
	}
	if (_predicates != null) {
	    final int n = _predicates.size();
	    for (int i = 0; i < n; i++) {
		final Expression pred = (Expression)_predicates.elementAt(i);
		pred.typeCheck(stable);
	    }
	}
	return _type;
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	if (hasPredicates()) {
	    translatePredicates(classGen, methodGen);
	} else {
            int star = 0;
            String name = null;
            final XSLTC xsltc = getParser().getXSLTC();
            if (_nodeType >= DTM.NTYPES) {
		final Vector ni = xsltc.getNamesIndex();
                name = (String)ni.elementAt(_nodeType-DTM.NTYPES);
                star = name.lastIndexOf('*');
            }
	    if (_axis == Axis.ATTRIBUTE && _nodeType != NodeTest.ATTRIBUTE
		&& _nodeType != NodeTest.ANODE && !hasParentPattern()
                && star == 0)
	    {
		int iter = cpg.addInterfaceMethodref(DOM_INTF,
						     "getTypedAxisIterator",
						     "(II)"+NODE_ITERATOR_SIG);
		il.append(methodGen.loadDOM());
		il.append(new PUSH(cpg, Axis.ATTRIBUTE));
		il.append(new PUSH(cpg, _nodeType));
		il.append(new INVOKEINTERFACE(iter, 3));
		return;
	    }
	    SyntaxTreeNode parent = getParent();
	    if (isAbbreviatedDot()) {
		if (_type == Type.Node) {
		    il.append(methodGen.loadContextNode());
		}
		else {
		    if (parent instanceof ParentLocationPath){
			int init = cpg.addMethodref(SINGLETON_ITERATOR,
						    "<init>",
						    "("+NODE_SIG+")V");
			il.append(new NEW(cpg.addClass(SINGLETON_ITERATOR)));
			il.append(DUP);
			il.append(methodGen.loadContextNode());
			il.append(new INVOKESPECIAL(init));
		    } else {
			int git = cpg.addInterfaceMethodref(DOM_INTF,
						"getAxisIterator",
						"(I)"+NODE_ITERATOR_SIG);
			il.append(methodGen.loadDOM());
			il.append(new PUSH(cpg, _axis));
			il.append(new INVOKEINTERFACE(git, 2));
		    }
		}
		return;
	    }
	    if ((parent instanceof ParentLocationPath) &&
		(parent.getParent() instanceof ParentLocationPath)) {
		if ((_nodeType == NodeTest.ELEMENT) && (!_hadPredicates)) {
		    _nodeType = NodeTest.ANODE;
		}
	    }
	    switch (_nodeType) {
	    case NodeTest.ATTRIBUTE:
		_axis = Axis.ATTRIBUTE;
	    case NodeTest.ANODE:
		int git = cpg.addInterfaceMethodref(DOM_INTF,
						    "getAxisIterator",
						    "(I)"+NODE_ITERATOR_SIG);
		il.append(methodGen.loadDOM());
		il.append(new PUSH(cpg, _axis));
		il.append(new INVOKEINTERFACE(git, 2));
		break;
	    default:
		if (star > 1) {
		    final String namespace;
		    if (_axis == Axis.ATTRIBUTE)
			namespace = name.substring(0,star-2);
		    else
			namespace = name.substring(0,star-1);
		    final int nsType = xsltc.registerNamespace(namespace);
		    final int ns = cpg.addInterfaceMethodref(DOM_INTF,
						    "getNamespaceAxisIterator",
						    "(II)"+NODE_ITERATOR_SIG);
		    il.append(methodGen.loadDOM());
		    il.append(new PUSH(cpg, _axis));
		    il.append(new PUSH(cpg, nsType));
		    il.append(new INVOKEINTERFACE(ns, 3));
		    break;
		}
	    case NodeTest.ELEMENT:
		final int ty = cpg.addInterfaceMethodref(DOM_INTF,
						"getTypedAxisIterator",
						"(II)"+NODE_ITERATOR_SIG);
		il.append(methodGen.loadDOM());
		il.append(new PUSH(cpg, _axis));
		il.append(new PUSH(cpg, _nodeType));
		il.append(new INVOKEINTERFACE(ty, 3));
		break;
	    }
	}
    }
    public void translatePredicates(ClassGenerator classGen,
				    MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	int idx = 0;
	if (_predicates.size() == 0) {
	    translate(classGen, methodGen);
	}
	else {
	    final Predicate predicate = (Predicate)_predicates.lastElement();
	    _predicates.remove(predicate);
	    if (predicate.isNodeValueTest()) {
		Step step = predicate.getStep();
		il.append(methodGen.loadDOM());
		if (step.isAbbreviatedDot()) {
		    translate(classGen, methodGen);
		    il.append(new ICONST(DOM.RETURN_CURRENT));
		}
		else {
		    ParentLocationPath path = new ParentLocationPath(this,step);
		    try {
			path.typeCheck(getParser().getSymbolTable());
		    }
		    catch (TypeCheckError e) { }
		    path.translate(classGen, methodGen);
		    il.append(new ICONST(DOM.RETURN_PARENT));
		}
		predicate.translate(classGen, methodGen);
		idx = cpg.addInterfaceMethodref(DOM_INTF,
						GET_NODE_VALUE_ITERATOR,
						GET_NODE_VALUE_ITERATOR_SIG);
		il.append(new INVOKEINTERFACE(idx, 5));
	    }            
	    else if (predicate.isNthDescendant()) {
		il.append(methodGen.loadDOM());
		il.append(new ICONST(predicate.getPosType()));
		predicate.translate(classGen, methodGen);
		il.append(new ICONST(0));
		idx = cpg.addInterfaceMethodref(DOM_INTF,
						"getNthDescendant",
						"(IIZ)"+NODE_ITERATOR_SIG);
		il.append(new INVOKEINTERFACE(idx, 4));
	    }
	    else if (predicate.isNthPositionFilter()) {
		idx = cpg.addMethodref(NTH_ITERATOR_CLASS,
				       "<init>",
				       "("+NODE_ITERATOR_SIG+"I)V");
		translatePredicates(classGen, methodGen); 
                LocalVariableGen iteratorTemp
                        = methodGen.addLocalVariable("step_tmp1",
                                         Util.getJCRefType(NODE_ITERATOR_SIG),
                                         null, null);
                iteratorTemp.setStart(
                        il.append(new ASTORE(iteratorTemp.getIndex())));
		predicate.translate(classGen, methodGen);
                LocalVariableGen predicateValueTemp
                        = methodGen.addLocalVariable("step_tmp2",
                                         Util.getJCRefType("I"),
                                         null, null);
                predicateValueTemp.setStart(
                        il.append(new ISTORE(predicateValueTemp.getIndex())));
		il.append(new NEW(cpg.addClass(NTH_ITERATOR_CLASS)));
		il.append(DUP);
                iteratorTemp.setEnd(
                        il.append(new ALOAD(iteratorTemp.getIndex())));
                predicateValueTemp.setEnd(
                        il.append(new ILOAD(predicateValueTemp.getIndex())));
		il.append(new INVOKESPECIAL(idx));
	    }
	    else {
		idx = cpg.addMethodref(CURRENT_NODE_LIST_ITERATOR,
				       "<init>",
				       "("
				       + NODE_ITERATOR_SIG
				       + CURRENT_NODE_LIST_FILTER_SIG
				       + NODE_SIG
				       + TRANSLET_SIG
				       + ")V");
		translatePredicates(classGen, methodGen); 
                LocalVariableGen iteratorTemp
                        = methodGen.addLocalVariable("step_tmp1",
                                         Util.getJCRefType(NODE_ITERATOR_SIG),
                                         null, null);
                iteratorTemp.setStart(
                        il.append(new ASTORE(iteratorTemp.getIndex())));
		predicate.translateFilter(classGen, methodGen);
                LocalVariableGen filterTemp
                        = methodGen.addLocalVariable("step_tmp2",
                              Util.getJCRefType(CURRENT_NODE_LIST_FILTER_SIG),
                              null, null);
                filterTemp.setStart(
                        il.append(new ASTORE(filterTemp.getIndex())));
		il.append(new NEW(cpg.addClass(CURRENT_NODE_LIST_ITERATOR)));
		il.append(DUP);
                iteratorTemp.setEnd(
                        il.append(new ALOAD(iteratorTemp.getIndex())));
                filterTemp.setEnd(il.append(new ALOAD(filterTemp.getIndex())));
		il.append(methodGen.loadCurrentNode());
		il.append(classGen.loadTranslet());
		if (classGen.isExternal()) {
		    final String className = classGen.getClassName();
		    il.append(new CHECKCAST(cpg.addClass(className)));
		}
		il.append(new INVOKESPECIAL(idx));
	    }
	}
    }
    public String toString() {
	final StringBuffer buffer = new StringBuffer("step(\"");
    buffer.append(Axis.getNames(_axis)).append("\", ").append(_nodeType);
	if (_predicates != null) {
	    final int n = _predicates.size();
	    for (int i = 0; i < n; i++) {
		final Predicate pred = (Predicate)_predicates.elementAt(i);
		buffer.append(", ").append(pred.toString());
	    }
	}
	return buffer.append(')').toString();
    }
}
