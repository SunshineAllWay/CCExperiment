package test;

import parseunit.ASTGenerator;
import parseunit.MyASTNode;
import parseunit.MyMethodNode;
import parseunit.NodeVisitor;

import java.io.File;
import java.util.List;

public class PLcacheRunASTAppTest {
    public static void main(String[] args) {
        File f = new File("corpus\\program_language_dataset9\\demo.java");
        ASTGenerator gen = new ASTGenerator(f);
        int n = gen.methodNodeList.size();

        NodeVisitor visitor = new NodeVisitor();
        for (int i = 0; i < n; i++) {
            MyMethodNode node1 = gen.methodNodeList.get(i);
            node1.methodNode.accept(visitor);
            List<MyASTNode> myASTNodeList = node1.nodeList;

            for (int j = 0; j < myASTNodeList.size(); j++) {
                MyASTNode node2 = myASTNodeList.get(j);
                node2.astNode.accept(visitor);
            }
        }

        for (int i = 0; i < visitor.tokenList.size(); i++) {
            System.out.println(visitor.tokenList.get(i));
        }
        System.out.println(visitor.tokenList.size());
        System.out.println(n);
    }
}