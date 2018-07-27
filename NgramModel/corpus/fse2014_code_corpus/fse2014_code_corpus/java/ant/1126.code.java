package org.apache.tools.ant.types.selectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
public class ContainsSelectorTest extends BaseSelectorTest {
    private Project project;
    public ContainsSelectorTest(String name) {
        super(name);
    }
    public BaseSelector getInstance() {
        return new ContainsSelector();
    }
    public void testValidate() {
        ContainsSelector s = (ContainsSelector)getInstance();
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("ContainsSelector did not check for required field 'text'");
        } catch (BuildException be1) {
            assertEquals("The text attribute is required", be1.getMessage());
        }
        s = (ContainsSelector)getInstance();
        Parameter param = new Parameter();
        param.setName("garbage in");
        param.setValue("garbage out");
        Parameter[] params = {param};
        s.setParameters(params);
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("ContainsSelector did not check for valid parameter element");
        } catch (BuildException be2) {
            assertEquals("Invalid parameter garbage in", be2.getMessage());
        }
    }
    public void testSelectionBehaviour() {
        ContainsSelector s;
        String results;
        try {
            makeBed();
            s = (ContainsSelector)getInstance();
            s.setText("no such string in test files");
            results = selectionString(s);
            assertEquals("TFFFFFFFFFFT", results);
            s = (ContainsSelector)getInstance();
            s.setText("Apache Ant");
            results = selectionString(s);
            assertEquals("TFFFTFFFFFFT", results);
            s = (ContainsSelector)getInstance();
            s.setText("apache ant");
            s.setCasesensitive(true);
            results = selectionString(s);
            assertEquals("TFFFFFFFFFFT", results);
            s = (ContainsSelector)getInstance();
            s.setText("apache ant");
            s.setCasesensitive(false);
            results = selectionString(s);
            assertEquals("TFFFTFFFFFFT", results);
            s = (ContainsSelector)getInstance();
            s.setText("ApacheAnt");
            s.setIgnorewhitespace(true);
            results = selectionString(s);
            assertEquals("TFFFTFFFFFFT", results);
            s = (ContainsSelector)getInstance();
            s.setText("A p a c h e    A n t");
            s.setIgnorewhitespace(true);
            results = selectionString(s);
            assertEquals("TFFFTFFFFFFT", results);
        }
        finally {
            cleanupBed();
        }
    }
}
