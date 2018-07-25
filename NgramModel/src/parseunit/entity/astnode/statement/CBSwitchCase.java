package parseunit.entity.astnode.statement;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;

import parseunit.entity.astnode.expression.CBExpression;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * expression null - default
 * all the operations in the whole switchStatement
 * @author guzuxing
 *
 */
public class CBSwitchCase extends CBStatement {
	private CBExpression expression;
	public CBSwitchCase(SwitchCase n) {
		super(n);
		if(n.isDefault())
			expression = null;
		else
			expression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
	}

}
