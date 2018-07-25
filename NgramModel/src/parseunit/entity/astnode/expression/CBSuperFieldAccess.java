package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBSuperFieldAccess extends CBExpression {
	private CBName name;
	
	public CBSuperFieldAccess(SuperFieldAccess n) {
		super(n);
		name = (CBName) CBASTNodeBuilder.build(n.getName());
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBSuperFieldAccess)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBSuperFieldAccess temTar = (CBSuperFieldAccess)tar;
		name.mapTokens(temTar.getName(), tokenMap, nodemap, e);
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		return "super."+name.toCBString();
	}

	/**
	 * @return the name
	 */
	public CBName getName() {
		return name;
	}

	
}
