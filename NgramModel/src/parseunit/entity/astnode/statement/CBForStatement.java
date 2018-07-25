package parseunit.entity.astnode.statement;

import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;

import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBForStatement extends CBStatement {
	private CBStatement body;
	public CBForStatement(ForStatement n) {
		super(n);
		body = (CBStatement) CBASTNodeBuilder.build(n.getBody());
	}
	/**
	 * @return the body
	 */
	public CBStatement getBody() {
		return body;
	}

	
}
