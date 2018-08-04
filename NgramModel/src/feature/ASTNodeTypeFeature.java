package feature;
import parseunit.MyASTNode;
import parseunit.MyMethodNode;
import parseunit.NodeVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ASTNodeTypeFeature {
    public MyMethodNode myMethodNode;
    public ArrayList<Integer> typeCntList;
    public int topNodeCount;
    double[] feature;

    public ASTNodeTypeFeature(MyMethodNode pMyMethodNode) {
        this.myMethodNode = pMyMethodNode;
        topNodeCount = 0;
        typeCntList = new ArrayList<>();
        extractFeature();
        feature = new double [typeCntList.size()];

        for (int i = 0; i < feature.length; i++) {
            feature[i] = (topNodeCount == 0) ? 0 : typeCntList.get(i) * 1.0 / topNodeCount;
        }
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
    }

    public double[] getFeature() {
        return this.feature;
    }
}
