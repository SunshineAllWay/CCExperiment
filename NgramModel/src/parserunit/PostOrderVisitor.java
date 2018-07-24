package parserunit;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;

public class PostOrderVisitor {
    public CompilationUnit parseResult;
    public ArrayList<String> tokenseq;

    public PostOrderVisitor(CompilationUnit result) {
        parseResult = result;
        tokenseq = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor() {
            public boolean visit(FieldDeclaration node) {
                for (Object obj : node.fragments()) {
                    VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
                    System.out.println(v.getName());
                    tokenseq.add(v.getName().toString());
                }
                return true;
            }

            public boolean visit(MethodDeclaration node) {
                System.out.println(node.getName());
                tokenseq.add(node.getName().toString());
                Block body = node.getBody();
                body.accept(this);
                return true;
            }

            public boolean visit(TypeDeclaration node) {
                System.out.println(node.getName());
                tokenseq.add(node.getName().toString());
                return true;
            }
        };

        parseResult.accept(visitor);
    }
}
