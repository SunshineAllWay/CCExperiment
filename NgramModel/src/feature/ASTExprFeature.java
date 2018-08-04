package feature;

import org.eclipse.jdt.core.dom.*;
import parseunit.MyASTNode;
import parseunit.MyMethodNode;
import parseunit.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTExprFeature {
    public MyMethodNode myMethodNode;
    public ArrayList<Integer> exprTypeCntList;
    public int exprCnt;
    public double[] feature;

    public ASTExprFeature(MyMethodNode pMyMethodNode) {
        this.myMethodNode = pMyMethodNode;
        exprCnt = 0;
        exprTypeCntList = new ArrayList<>();
        extractFeature();
        feature = new double [exprTypeCntList.size()];

        for (int i = 0; i < feature.length; i++) {
            feature[i] = (exprCnt == 0) ? 0 : exprTypeCntList.get(i) * 1.0 / exprCnt;
        }
    }

    public void extractFeature() {
        NodeVisitor visitor = new NodeVisitor();
        myMethodNode.methodNode.accept(visitor);
        List<MyASTNode> astNodeList = myMethodNode.nodeList;

        //http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTNode.html
        int[] exprFeature = new int[26];

        for (int i = 0; i < astNodeList.size(); i++) {
            ASTNode node = astNodeList.get(i).astNode;

            if (node instanceof Expression) {
                exprCnt++;
            } else {
                continue;
            }

            if (node instanceof Annotation) {
                exprFeature[0]++;
            } else if (node instanceof ArrayAccess) {
                exprFeature[1]++;
            } else if (node instanceof ArrayCreation) {
                exprFeature[2]++;
            } else if (node instanceof ArrayInitializer) {
                exprFeature[3]++;
            } else if (node instanceof Assignment) {
                exprFeature[4]++;
            } else if (node instanceof BooleanLiteral) {
                exprFeature[5]++;
            } else if (node instanceof CastExpression) {
                exprFeature[6]++;
            } else if (node instanceof CharacterLiteral) {
                exprFeature[7]++;
            } else if (node instanceof ClassInstanceCreation) {
                exprFeature[8]++;
            } else if (node instanceof ConditionalExpression) {
                exprFeature[9]++;
            } else if (node instanceof FieldAccess) {
                exprFeature[10]++;
            } else if (node instanceof InfixExpression) {
                exprFeature[11]++;
            } else if (node instanceof InstanceofExpression) {
                exprFeature[12]++;
            } else if (node instanceof MethodInvocation) {
                exprFeature[13]++;
            } else if (node instanceof Name) {
                exprFeature[14]++;
            } else if (node instanceof NullLiteral) {
                exprFeature[15]++;
            } else if (node instanceof NumberLiteral) {
                exprFeature[16]++;
            } else if (node instanceof ParenthesizedExpression) {
                exprFeature[17]++;
            } else if (node instanceof PostfixExpression) {
                exprFeature[18]++;
            } else if (node instanceof PrefixExpression) {
                exprFeature[19]++;
            } else if (node instanceof StringLiteral) {
                exprFeature[20]++;
            } else if (node instanceof SuperFieldAccess) {
                exprFeature[21]++;
            } else if (node instanceof SuperMethodInvocation) {
                exprFeature[22]++;
            } else if (node instanceof ThisExpression) {
                exprFeature[23]++;
            } else if (node instanceof TypeLiteral) {
                exprFeature[24]++;
            } else if (node instanceof VariableDeclarationExpression) {
                exprFeature[25]++;
            }
        }

        for (int i = 0; i < 26; i++) {
            exprTypeCntList.add(exprFeature[i]);
        }
    }

    public double[] getFeature() {
        return this.feature;
    }
}
