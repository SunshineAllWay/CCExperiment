package parseunit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MyMethodNode {

	public MethodDeclaration methodNode = null;
	public List<MyASTNode> nodeList = null;

	public List<int[]> mapping = null;

	public MyMethodNode() {
		this.methodNode = null;
		this.nodeList = new ArrayList<>();
		this.mapping = new ArrayList<>();
	}

}
