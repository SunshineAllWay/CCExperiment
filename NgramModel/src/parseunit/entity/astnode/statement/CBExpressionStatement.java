package parseunit.entity.astnode.statement;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.entity.astnode.expression.CBExpression;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */

public class CBExpressionStatement extends CBStatement {
	private CBExpression expression;
	public CBExpressionStatement(ExpressionStatement n) {
		super(n);
		expression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		if(! (tar instanceof CBExpressionStatement) ){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBExpressionStatement tarTem = (CBExpressionStatement)tar;
		expression.mapTokens(tarTem.getExpression(), tokenMap,nodemap,e);
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		// TODO Auto-generated method stub
		return super.toCBString();
	}
	/**
	 * @return the expression
	 */
	public CBExpression getExpression() {
		return expression;
	}

	
}
