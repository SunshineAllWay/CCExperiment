package iounit;

import feature.ASTExprFeature;
import feature.ASTNodeTypeFeature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import parseunit.ASTGenerator;
import parseunit.MethodContextParser;
import parseunit.MyMethodNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.sqrt;

public class MethodContextProcessor {
    public ArrayList<MyMethodNode> nodeList;
    public HashMap<MyMethodNode, ArrayList<String>> nodeToContextMap;
    public HashMap<MyMethodNode, Double> nodeToSimMap;

    public MethodContextProcessor(File file, MyMethodNode node) {
        ASTGenerator gen = new ASTGenerator(file);
        nodeList = new ArrayList<>();
        nodeList.addAll(gen.getMethodNodeList());

        nodeToContextMap = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            MethodContextParser parser = new MethodContextParser(nodeList.get(i));
            nodeToContextMap.put(nodeList.get(i), parser.context);
        }

        nodeToSimMap = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            double sim = getSimiliarity(node, nodeList.get(i));
            nodeToSimMap.put(nodeList.get(i), new Double(sim));
        }
    }

    public double getSimiliarity(MyMethodNode node1, MyMethodNode node2) {
        ASTExprFeature expr_feature1 = new ASTExprFeature(node1);
        ASTExprFeature expr_feature2 = new ASTExprFeature(node2);

        ASTNodeTypeFeature nodetype_feature1 = new ASTNodeTypeFeature(node1);
        ASTNodeTypeFeature nodetype_feature2 = new ASTNodeTypeFeature(node2);

        double similarity = 1.0;

        double[] exprfeature1 = expr_feature1.getFeature();
        double[] exprfeature2 = expr_feature2.getFeature();
        double innerproduct = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < exprfeature1.length; i++) {
            innerproduct += exprfeature1[i] * exprfeature2[i];
            norm1 += exprfeature1[i] * exprfeature1[i];
            norm2 += exprfeature2[i] * exprfeature2[i];
        }
        double sim1 = innerproduct / sqrt(norm1 * norm2);

        double[] nodetypefeature1 = nodetype_feature1.getFeature();
        double[] nodetypefeature2 = nodetype_feature2.getFeature();
        innerproduct = 0.0;
        norm1 = 0.0;
        norm2 = 0.0;
        for (int i = 0; i < nodetypefeature1.length; i++) {
            innerproduct += nodetypefeature1[i] * nodetypefeature2[i];
            norm1 += nodetypefeature1[i] * nodetypefeature2[i];
            norm2 += nodetypefeature1[i] * nodetypefeature2[i];
        }
        double sim2 = innerproduct / sqrt(norm1 * norm2);

        similarity = (sim1 + sim2) / 2;

        return similarity;
    }
}
