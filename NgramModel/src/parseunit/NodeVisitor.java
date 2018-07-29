package parseunit;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.eclipse.jdt.core.dom.*;

public class NodeVisitor extends ASTVisitor {

	public HashSet<ASTNode> nodeSet = new HashSet<>();
	public List<String> tokenList = new ArrayList<>();

	@Override
	public void preVisit(ASTNode node) {
		if (nodeSet.contains(node)) {
			return;
		}
		nodeSet.add(node);

		if (node instanceof FieldDeclaration) {
			FieldDeclaration pnode = (FieldDeclaration) node;
			for (Object obj : pnode.fragments()) {
				VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
				//System.out.println(v.getName());
				tokenList.add(v.getName().getIdentifier());
			}
		} else if (node instanceof TypeDeclaration) {
			TypeDeclaration pnode = (TypeDeclaration) node;
			//System.out.println(pnode.getName());
			tokenList.add(pnode.getName().getIdentifier());
		} else if (node instanceof VariableDeclaration) {
			VariableDeclaration pnode = (VariableDeclaration) node;
			//System.out.println(pnode.getName().getIdentifier());
			tokenList.add(pnode.getName().getIdentifier());
		}
	}

	public ArrayList<ASTNode> getASTNodes() {
		ArrayList<ASTNode> ASTNodeList = new ArrayList<>();
		ASTNodeList.addAll(nodeSet);
		return ASTNodeList;
	}
}
