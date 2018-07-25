package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBStringLiteral extends CBExpression {
	private String value;
	
	public CBStringLiteral(StringLiteral n) {
		super(n);
		value = n.getLiteralValue();
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBStringLiteral)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}

		CBStringLiteral temTar = (CBStringLiteral)tar;
		MapUtil.addTokenMapping(tokenMap, value, temTar.getValue()
				,nodemap,e);
		
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		return value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	
}
