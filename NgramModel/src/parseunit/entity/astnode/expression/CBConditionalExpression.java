package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ConditionalExpression;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBConditionalExpression extends CBExpression {
	private CBExpression conditionExpression;
	private CBExpression thenExpression;
	private CBExpression elseExpression;
	
	public CBConditionalExpression(ConditionalExpression n) {
		super(n);
		conditionExpression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
		thenExpression = (CBExpression) CBASTNodeBuilder.build(n.getThenExpression());
		elseExpression = (CBExpression) CBASTNodeBuilder.build(n.getElseExpression());
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBConditionalExpression)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBConditionalExpression temTar = (CBConditionalExpression)tar;
		conditionExpression.mapTokens(temTar.getConditionExpression(), tokenMap, nodemap, e);
		thenExpression.mapTokens(temTar.getThenExpression(), tokenMap, nodemap, e);
		elseExpression.mapTokens(temTar.getElseExpression(), tokenMap, nodemap, e);
		
		
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
	 * @return the conditionExpression
	 */
	public CBExpression getConditionExpression() {
		return conditionExpression;
	}

	/**
	 * @return the thenExpression
	 */
	public CBExpression getThenExpression() {
		return thenExpression;
	}

	/**
	 * @return the elseExpression
	 */
	public CBExpression getElseExpression() {
		return elseExpression;
	}

	
}
