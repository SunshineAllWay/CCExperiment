package parseunit.entity.astnode.statement;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import parseunit.entity.astnode.expression.CBExpression;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */

public class CBWhileStatement extends CBStatement {
	private CBExpression expression;
	private CBStatement body; 
	public CBWhileStatement(WhileStatement n) {
		super(n);
		expression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
		body = (CBStatement) CBASTNodeBuilder.build(n.getBody());
	}

}
