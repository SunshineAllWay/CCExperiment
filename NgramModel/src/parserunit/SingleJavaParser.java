package parserunit;

import org.eclipse.jdt.core.dom.*;

import java.io.*;

public class SingleJavaParser {
    public ASTParser astParser;
    public ASTNode root;
    public CompilationUnit result;

    public SingleJavaParser(File javaFile) {
        byte[] input = null;

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaFile));
            input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        astParser = ASTParser.newParser(AST.JLS8);
        astParser.setSource(new String(input).toCharArray());

        /**
         *  K_COMPILATION_UNIT：编译单元，即一个Java文件
         *  K_CLASS_BODY_DECLARATIONS：类的声明
         *  K_EXPRESSION：单个表达式
         *  K_STATEMENTS：语句块
         */
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);

        result = (CompilationUnit) (astParser.createAST(null));
        root = result.getRoot();
    }
}
