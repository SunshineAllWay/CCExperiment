package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Project;
import junit.framework.TestCase;
public class RmicTest extends TestCase {
    private Project project;
    private Rmic rmic;
    public RmicTest(String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        project.init();
        rmic = new Rmic();
        rmic.setProject(project);
    }
    public void testCompilerArg() {
        String[] args = rmic.getCurrentCompilerArgs();
        assertNotNull(args);
        assertEquals("no args", 0, args.length);
        Rmic.ImplementationSpecificArgument arg = rmic.createCompilerArg();
        String ford = "Ford";
        String prefect = "Prefect";
        String testArg = ford + " " + prefect;
        arg.setValue(testArg);
        args = rmic.getCurrentCompilerArgs();
        assertEquals("unconditional single arg", 1, args.length);
        assertEquals(testArg, args[0]);
        arg.setCompiler("weblogic");
        args = rmic.getCurrentCompilerArgs();
        assertNotNull(args);
        assertEquals("implementation is weblogic but build.rmic is null",
                     0, args.length);
        project.setProperty("build.rmic", "sun");
        args = rmic.getCurrentCompilerArgs();
        assertNotNull(args);
        assertEquals("implementation is weblogic but build.rmic is sun",
                     0, args.length);
        project.setProperty("build.rmic", "weblogic");
        args = rmic.getCurrentCompilerArgs();
        assertEquals("both are weblogic", 1, args.length);
        assertEquals(testArg, args[0]);
    }
    public void testCompilerAttribute() {
        String compiler = rmic.getCompiler();
        assertNotNull(compiler);
        assertEquals("expected sun or kaffe, but found "+compiler,compiler,"default");
        project.setNewProperty("build.rmic", "weblogic");
        compiler = rmic.getCompiler();
        assertNotNull(compiler);
        assertEquals("weblogic", compiler);
        rmic.setCompiler("kaffe");
        compiler = rmic.getCompiler();
        assertNotNull(compiler);
        assertEquals("kaffe", compiler);
    }
}
