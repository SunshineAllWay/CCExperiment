package parseunit.entity.astnode.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.entity.astnode.CBASTNode;
import parseunit.entity.astnode.statement.variabledeclaration.CBVariableDeclaratrionFragment;
import parseunit.entity.astnode.visitor.CBVariableDeclarationStatementVisitor;
import parseunit.util.MapUtil;

public class CBVariableDeclarationStatement extends CBStatement {
	private Type type;
	private List<CBVariableDeclaratrionFragment> fragmentList;
//	private CBVariableDeclarationStatementVisitor visitor;
	
	public CBVariableDeclarationStatement(VariableDeclarationStatement n) {
		super(n);
		type = n.getType();
		fragmentList = new ArrayList<>();
		for(int i = 0;i<n.fragments().size();i++){
			fragmentList.add(new CBVariableDeclaratrionFragment(
					(VariableDeclarationFragment) n.fragments().get(i)));
		}
	}

	
	/**
	 * VariableDeclarationStatement mapTokens
	 * map type
	 * map fragmentList
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		if(! (tar instanceof CBVariableDeclarationStatement) ){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBVariableDeclarationStatement tarTem = (CBVariableDeclarationStatement)tar;
		// map type
		MapUtil.addTokenMapping(tokenMap, type.toString(), tarTem.getType().toString()
				,nodemap,e);
		// map fragments
		//TODO if size not match only compare the same size items
		int minSize = fragmentList.size();
		if(tarTem.fragmentList.size()<minSize)
			minSize = tarTem.fragmentList.size();
		for(int index = 0;index< minSize;index++){
			fragmentList.get(index).mapTokens(tarTem.fragmentList.get(index), tokenMap,
					nodemap,e);
		}
		
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
	 * @return the type
	 */
	public Type getType() {
		return type;
	}


	/**
	 * @return the fragmentList
	 */
	public List<CBVariableDeclaratrionFragment> getFragmentList() {
		return fragmentList;
	}

	
	
}
