package org.apache.xalan.xsltc.compiler.util;
import org.apache.xalan.xsltc.compiler.SyntaxTreeNode;
public class TypeCheckError extends Exception {
    static final long serialVersionUID = 3246224233917854640L;
    ErrorMsg _error = null;
    SyntaxTreeNode _node = null;
    public TypeCheckError(SyntaxTreeNode node) {
	super();
	_node = node;
    }
    public TypeCheckError(ErrorMsg error) {
	super();
	_error = error;
    }
    public TypeCheckError(String code, Object param) {
	super();
	_error = new ErrorMsg(code, param);
    }
    public TypeCheckError(String code, Object param1, Object param2) {
	super();
	_error = new ErrorMsg(code, param1, param2);
    }
    public ErrorMsg getErrorMsg() {
        return _error;
    }
    public String getMessage() {
        return toString();
    }
    public String toString() {
	String result;
	if (_error == null) {
            if (_node != null) {
                _error = new ErrorMsg(ErrorMsg.TYPE_CHECK_ERR,
                                      _node.toString());
	    } else {
	        _error = new ErrorMsg(ErrorMsg.TYPE_CHECK_UNK_LOC_ERR);
	    }
        }
	return _error.toString();
    }
}
