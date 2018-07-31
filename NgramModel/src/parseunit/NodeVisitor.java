package parseunit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.sun.org.apache.xpath.internal.operations.Mod;
import jdk.internal.dynalink.linker.MethodHandleTransformer;
import org.eclipse.jdt.core.dom.*;

public class NodeVisitor extends ASTVisitor {

	public HashSet<ASTNode> nodeSet = new HashSet<>();
	public List<String> tokenList = new ArrayList<>();

	public void handleType(Type node) {
		if (node instanceof PrimitiveType) {
			PrimitiveType pnode = (PrimitiveType) node;
			nodeSet.add(pnode);
			tokenList.add(pnode.getPrimitiveTypeCode().toString());
		} else if (node instanceof ArrayType) {
			ArrayType pnode = (ArrayType) node;
			preVisit(pnode.getElementType());
			tokenList.add("[ ]");
		} else if (node instanceof QualifiedType) {
			QualifiedType pnode = (QualifiedType) node;
			nodeSet.add(pnode);
			tokenList.add(pnode.getName().getIdentifier());
		} else if (node instanceof SimpleType) {
			SimpleType pnode = (SimpleType) node;
			nodeSet.add(pnode);
			tokenList.add(pnode.getName().toString());
		} else if (node instanceof UnionType) {
			UnionType pnode = (UnionType) node;
			nodeSet.add(pnode);
			tokenList.add(pnode.toString());
		} else if (node instanceof WildcardType) {
			WildcardType pnode = (WildcardType) node;
			nodeSet.add(pnode);
			tokenList.add(pnode.toString());
		}
	}

	public void handleExpression(Expression node) {
		if (node instanceof SimpleName) {
			SimpleName pnode = (SimpleName) node;
			tokenList.add(pnode.getIdentifier());
		} else if (node instanceof NumberLiteral) {
			NumberLiteral pnode = (NumberLiteral) node;
			tokenList.add(pnode.getToken());
		} else if (node instanceof ArrayAccess) {
			Expression array = ((ArrayAccess) node).getArray();
			Expression index = ((ArrayAccess) node).getIndex();

			handleExpression(array);
			nodeSet.add(array);
			tokenList.add("[");
			handleExpression(index);
			nodeSet.add(index);
			tokenList.add("]");
		}
	}

	@Override
	public void preVisit(ASTNode node) {
		if (nodeSet.contains(node)) {
			return;
		}
		nodeSet.add(node);
		if (node instanceof Modifier) {
			Modifier pnode = (Modifier) node;
			tokenList.add(pnode.toString());
		} else if (node instanceof SimpleName) {
			SimpleName pnode = (SimpleName) node;
			if (pnode.getParent() instanceof Type) {
				return;
			}
			tokenList.add(pnode.getIdentifier());
		} else if (node instanceof FieldDeclaration) {
			FieldDeclaration pnode = (FieldDeclaration) node;
			for (Object obj : pnode.fragments()) {
				VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
				tokenList.add(v.getName().getIdentifier());
			}
		} else if (node instanceof TypeDeclaration) {
			TypeDeclaration pnode = (TypeDeclaration) node;
			tokenList.add(pnode.getName().getIdentifier());
		} else if (node instanceof Type) {
			handleType((Type)node);
		} else if (node instanceof TypeParameter) {
			TypeParameter pnode = (TypeParameter) node;
			tokenList.add(pnode.getName().getIdentifier());
		} else if (node instanceof Expression) {
			handleExpression((Expression)node);
		}
	}

	public ArrayList<ASTNode> getASTNodes() {
		ArrayList<ASTNode> ASTNodeList = new ArrayList<>();
		ASTNodeList.addAll(nodeSet);
		return ASTNodeList;
	}
}
