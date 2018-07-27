import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import static com.sun.mirror.util.DeclarationVisitors.*;
import java.util.Map;
public class DistributedAnnotationProcessor implements AnnotationProcessor {
    public AnnotationProcessorEnvironment env;
    public DistributedAnnotationProcessor(AnnotationProcessorEnvironment env) {
        this.env = env;
    }
    public void echo(String text) {
        env.getMessager().printNotice(text);
    }
    public void process() {
        echo("DistributedAnnotationProcessor-is-go");
        Map<String, String> options=env.getOptions();
        for(String key:options.keySet()) {
            echo("Option ["+key+"] = "+options.get(key));
        }
        for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations()) {
            typeDecl.accept(getDeclarationScanner(new ClassVisitor(),
                    NO_OP));
        }
    }
    private class ClassVisitor extends SimpleDeclarationVisitor {
        public void visitClassDeclaration(ClassDeclaration d) {
            echo("visiting "+ d.getQualifiedName());
        }
    }
}
