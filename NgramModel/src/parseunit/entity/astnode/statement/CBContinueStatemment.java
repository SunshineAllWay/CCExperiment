package parseunit.entity.astnode.statement;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Statement;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBContinueStatemment extends CBStatement {
	private String identifier;
	public CBContinueStatemment(ContinueStatement n) {
		super(n);
		if(n.getLabel() == null){
			identifier = null;
		}else {
			identifier = n.getLabel().toString();
		}
	}
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map, java.util.Map, parseunit.entity.ASTNodeMappingElement)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String, List> tokenMap,
			Map<String, List<ASTNodeMappingElement>> nodemap,
			ASTNodeMappingElement e) {
		if(! (tar instanceof CBBreakStatement) ){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBContinueStatemment tarTem = (CBContinueStatemment)tar;
		if(identifier != null || tarTem.getIdentifier() != null){
			MapUtil.addTokenMapping(tokenMap, identifier, tarTem.getIdentifier()
					, nodemap, e);
		}
		
	}



	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		// TODO Auto-generated method stub
		return "Break";
	}



	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
}
