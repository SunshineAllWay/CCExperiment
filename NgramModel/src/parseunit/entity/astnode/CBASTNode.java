package parseunit.entity.astnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.util.MapUtil;

public class CBASTNode extends AbstractCBASTNode {

	public CBASTNode(ASTNode n) {
		super(n);
	}

	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		MapUtil.addTokenMapping(tokenMap, toCBString(), tar.toCBString(),nodemap,e);

	}
	
	@Override
	public String toCBString() {
		// TODO Auto-generated method stub
		return node.toString();
	}

}
