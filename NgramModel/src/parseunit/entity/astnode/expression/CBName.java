package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Name;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

public class CBName extends CBExpression {
	private Name name;
	
	public CBName(Name n) {
		super(n);
		name = n;
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		// ASTNode type not match
		if(! (tar instanceof CBName)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBName temTar = (CBName)tar;
		
		MapUtil.addTokenMapping(tokenMap, name.getFullyQualifiedName(), 
				temTar.getName().getFullyQualifiedName(),nodemap,e);
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
	 * @return the name
	 */
	public Name getName() {
		return name;
	}

	
	
}
