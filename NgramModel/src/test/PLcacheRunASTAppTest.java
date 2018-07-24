package test;

import parserunit.SingleJavaStream;

import java.io.File;

public class PLcacheRunASTAppTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\program_language_dataset9\\demo.java");
        SingleJavaStream stream = new SingleJavaStream(currentFile);
        System.out.println(stream.visitor.tokenseq.size());
    }
}