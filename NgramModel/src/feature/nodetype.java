package feature;
import parseunit.MyASTNode;
import parseunit.MyMethodNode;
import parseunit.NodeVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class nodetype {
    public MyMethodNode myMethodNode;
    public ArrayList<Integer> typeCntList;
    public ArrayList<Integer> exprTypeCntList;
    public int topNodeCount;

    public nodetype(MyMethodNode pMyMethodNode) {
        this.myMethodNode = pMyMethodNode;
        topNodeCount = 0;
        typeCntList = new ArrayList<>();
        exprTypeCntList = new ArrayList<>();
        extractFeature();
    }

    public void extractFeature() {
        NodeVisitor visitor = new NodeVisitor();
        myMethodNode.methodNode.accept(visitor);
        List<MyASTNode> astNodeList = myMethodNode.nodeList;

        for (int i = 0; i < astNodeList.size(); i++) {
            if ((astNodeList.get(i).astNode.getParent()) instanceof MethodDeclaration) {
                topNodeCount++;
            }
        }

        int exprCnt = 0;
        int stateCnt = 0;
        int varDecCnt = 0;
        int importDecCnt = 0;
        int typeCnt = 0;

        for (int i = 0; i < astNodeList.size(); i++) {
            ASTNode node = astNodeList.get(i).astNode;
            if (node instanceof Expression) {
                exprCnt++;
            } else if (node instanceof Statement) {
                stateCnt++;
            } else if (node instanceof VariableDeclaration) {
                varDecCnt++;
            } else if (node instanceof ImportDeclaration) {
                importDecCnt++;
            } else if (node instanceof Type) {
                typeCnt++;
            }
        }

        typeCntList.add(exprCnt);
        typeCntList.add(stateCnt);
        typeCntList.add(varDecCnt);
        typeCntList.add(importDecCnt);
        typeCntList.add(typeCnt);

        //http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTNode.html

    }
}
