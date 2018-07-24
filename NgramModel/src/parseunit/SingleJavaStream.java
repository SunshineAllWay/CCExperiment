package parseunit;

import java.io.File;

public class SingleJavaStream {
    public SingleJavaParser parser;
    public PostOrderVisitor visitor;

    public  SingleJavaStream(File javaFile) {
        parser = new SingleJavaParser(javaFile);
        visitor = new PostOrderVisitor(parser.result);
    }
}
