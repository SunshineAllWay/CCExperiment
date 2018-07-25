package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBFieldAccess extends CBExpression {
	private CBExpression expression;
	private CBName fieldName;
	
	public CBFieldAccess(FieldAccess n) {
		super(n);
		expression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
		fieldName = (CBName) CBASTNodeBuilder.build(n.getName());
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBFieldAccess)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBFieldAccess temTar = (CBFieldAccess)tar;
		expression.mapTokens(temTar.getExpression(), tokenMap, nodemap, e);
		fieldName.mapTokens(temTar.getFieldName(), tokenMap, nodemap, e);
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

	/**
	 * @return the fieldName
	 */
	public CBName getFieldName() {
		return fieldName;
	}
	
	

}
