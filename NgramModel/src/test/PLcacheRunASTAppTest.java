package test;

import parseunit.SingleJavaStream;
import parseunit.ASTGenerator;
import parseunit.MyMethodNode;

import java.io.File;
import java.util.List;

public class PLcacheRunASTAppTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\program_language_dataset9\\demo.java");
        ASTGenerator astGenerator = new ASTGenerator(currentFile);
        List<MyMethodNode> methodNodeList = astGenerator.getMethodNodeList();
        System.out.println(methodNodeList.size());
    }
}