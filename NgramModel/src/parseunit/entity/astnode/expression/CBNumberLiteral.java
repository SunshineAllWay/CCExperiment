package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBNumberLiteral extends CBExpression {
	private String value;
	public CBNumberLiteral(NumberLiteral n) {
		super(n);
		value = n.getToken();
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBNumberLiteral)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBNumberLiteral temTar = (CBNumberLiteral)tar;
		MapUtil.addTokenMapping(tokenMap, toCBString(), temTar.toCBString()
				,nodemap,e);
		
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		// TODO Auto-generated method stub
		return value;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	
}
