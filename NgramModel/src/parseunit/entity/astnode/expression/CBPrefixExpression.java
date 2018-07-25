package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBPrefixExpression extends CBExpression {
	private CBExpression expression;
	private String operator;
	public CBPrefixExpression(PrefixExpression n) {
		super(n);
		expression = (CBExpression) CBASTNodeBuilder.build(n.getOperand());
		operator = n.getOperator().toString();
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBPrefixExpression)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBPrefixExpression temTar = (CBPrefixExpression)tar;
		expression.mapTokens(temTar.getExpression(), tokenMap, nodemap, e);
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		
		return operator+expression.toCBString();
	}
	/**
	 * @return the expression
	 */
	public CBExpression getExpression() {
		return expression;
	}
	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

}
