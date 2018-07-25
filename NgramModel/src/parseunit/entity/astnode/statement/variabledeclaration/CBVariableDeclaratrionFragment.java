package parseunit.entity.astnode.statement.variabledeclaration;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.entity.astnode.CBASTNode;
import parseunit.entity.astnode.expression.CBExpression;
import parseunit.entity.astnode.statement.CBStatement;
import parseunit.entity.astnode.statement.CBVariableDeclarationStatement;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;


public class CBVariableDeclaratrionFragment extends CBVariableDeclaration {
	public CBVariableDeclaratrionFragment(VariableDeclarationFragment n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		if(! (tar instanceof CBVariableDeclaratrionFragment) ){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		CBVariableDeclaratrionFragment temTar = (CBVariableDeclaratrionFragment)tar;
		MapUtil.addTokenMapping(tokenMap, name, temTar.getName(),nodemap,e);
		if(initializer != null)
			initializer.mapTokens(temTar.getInitializer(), tokenMap,nodemap,e);
	}
	
}
