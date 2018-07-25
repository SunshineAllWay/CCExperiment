package parseunit.entity.astnode.statement;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Statement;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;

/**
 * TODO need test
 * @author guzuxing
 *
 */

public class CBEmptyStatement extends CBStatement {

	public CBEmptyStatement(Statement n) {
		super(n);
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		return "";
	}
	
	

}
