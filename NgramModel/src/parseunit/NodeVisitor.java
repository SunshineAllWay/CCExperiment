package parseunit;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.eclipse.jdt.core.dom.*;

public class NodeVisitor extends ASTVisitor {

	public List<ASTNode> nodeList = new ArrayList<ASTNode>();
	public List<String> tokenList = new ArrayList<>();

	@Override
	public void preVisit(ASTNode node) {
		nodeList.add(node);
	}

	public List<ASTNode> getASTNodes() {
		return nodeList;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		for (Object obj : node.fragments()) {
			VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
			System.out.println(v.getName());
			tokenList.add(v.getName().toString());
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		System.out.println(node.getName());
		tokenList.add(node.getName().toString());
		return true;
	}
}
