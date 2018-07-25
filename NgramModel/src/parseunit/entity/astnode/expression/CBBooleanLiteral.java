package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.BooleanLiteral;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

/**
 * @author guzuxing
 *
 */
public class CBBooleanLiteral extends CBExpression {
	private boolean value;
	public CBBooleanLiteral(BooleanLiteral n) {
		super(n);
		value = n.booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBBooleanLiteral)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBBooleanLiteral temTar = (CBBooleanLiteral)tar;
		MapUtil.addTokenMapping(tokenMap, toCBString(), temTar.toCBString()
				,nodemap,e);
	}


	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		return "Boolean";
	}

	/**
	 * @return the value
	 */
	public boolean getValue() {
		return value;
	}

	
}
