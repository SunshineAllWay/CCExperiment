package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

/**
 * TODO test
 * @author guzuxing
 *
 */
public class CBCharacterLiteral extends CBExpression {
	private char value;
	public CBCharacterLiteral(CharacterLiteral n) {
		super(n);
		value = n.charValue();
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBCharacterLiteral)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBCharacterLiteral temTar = (CBCharacterLiteral)tar;
		MapUtil.addTokenMapping(tokenMap, toCBString(), temTar.toCBString()
				,nodemap,e);
		
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		// TODO Auto-generated method stub
		return "Char";
	}
	/**
	 * @return the value
	 */
	public char getValue() {
		return value;
	}

	
	
	
	
}
