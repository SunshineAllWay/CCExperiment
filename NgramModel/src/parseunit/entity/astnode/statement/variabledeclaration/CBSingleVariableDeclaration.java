package parseunit.entity.astnode.statement.variabledeclaration;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

public class CBSingleVariableDeclaration extends CBVariableDeclaration {
	private String type;
	
	public CBSingleVariableDeclaration(SingleVariableDeclaration n) {
		super(n);
		type = n.getType().toString();
	}

	
	
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBSingleVariableDeclaration) ){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		CBSingleVariableDeclaration temTar = (CBSingleVariableDeclaration)tar;
		MapUtil.addTokenMapping(tokenMap, type, temTar.getType(), nodemap, e);
		MapUtil.addTokenMapping(tokenMap, name, temTar.getName(),nodemap,e);
		if(initializer != null)
			initializer.mapTokens(temTar.getInitializer(), tokenMap,nodemap,e);
	}



	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}


	
}
