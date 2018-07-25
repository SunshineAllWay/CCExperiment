package parseunit.entity.astnode.statement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;

import parseunit.entity.astnode.expression.CBExpression;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */

public class CBSwitchStatement extends CBStatement {
	private CBExpression expression;
	private List<CBStatement> statements;
	
	public CBSwitchStatement(SwitchStatement n) {
		super(n);
		expression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
		statements = new ArrayList<CBStatement>();
		for(int index = 0;index<n.statements().size();index++)
			statements.add( (CBStatement)
					CBASTNodeBuilder.build((Statement)n.statements().get(index))
					);
	}

}
