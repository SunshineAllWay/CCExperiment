package org.apache.tools.ant.util.depend.bcel;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ConstantNameAndType;
public class DependencyVisitor extends EmptyVisitor {
    private Hashtable dependencies = new Hashtable();
    private ConstantPool constantPool;
    public Enumeration getDependencies() {
        return dependencies.keys();
    }
    public void clearDependencies() {
        dependencies.clear();
    }
    public void visitConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }
    public void visitConstantClass(ConstantClass constantClass) {
        String classname
             = constantClass.getConstantValue(constantPool).toString();
        addSlashClass(classname);
    }
    public void visitConstantNameAndType(ConstantNameAndType obj) {
        String name = obj.getName(constantPool);
        if (obj.getSignature(constantPool).equals("Ljava/lang/Class;")
                && name.startsWith("class$")) {
            String classname
                = name.substring("class$".length()).replace('$', '.');
            int index = classname.lastIndexOf(".");
            if (index > 0) {
                char start;
                int index2 = classname.lastIndexOf(".", index - 1);
                if (index2 != -1) {
                    start = classname.charAt(index2 + 1);
                } else {
                    start = classname.charAt(0);
                }
                if ((start > 0x40) && (start < 0x5B)) {
                    classname = classname.substring(0, index) + "$"
                        + classname.substring(index + 1);
                    addClass(classname);
                } else {
                    addClass(classname);
                }
            } else {
                addClass(classname);
            }
        }
    }
    public void visitField(Field field) {
        addClasses(field.getSignature());
    }
    public void visitJavaClass(JavaClass javaClass) {
        addClass(javaClass.getClassName());
    }
    public void visitMethod(Method method) {
        String signature = method.getSignature();
        int pos = signature.indexOf(")");
        addClasses(signature.substring(1, pos));
        addClasses(signature.substring(pos + 1));
    }
    void addClass(String classname) {
        dependencies.put(classname, classname);
    }
    private void addClasses(String string) {
        StringTokenizer tokens = new StringTokenizer(string, ";");
        while (tokens.hasMoreTokens()) {
            String descriptor = tokens.nextToken();
            int pos = descriptor.indexOf('L');
            if (pos != -1) {
                addSlashClass(descriptor.substring(pos + 1));
            }
        }
    }
    private void addSlashClass(String classname) {
        addClass(classname.replace('/', '.'));
    }
}
