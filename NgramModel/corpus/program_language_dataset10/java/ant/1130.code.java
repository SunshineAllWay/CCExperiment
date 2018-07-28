package org.apache.tools.ant.types.selectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
public class FilenameSelectorTest extends BaseSelectorTest {
    public FilenameSelectorTest(String name) {
        super(name);
    }
    public BaseSelector getInstance() {
        return new FilenameSelector();
    }
    public void testValidate() {
        FilenameSelector s = (FilenameSelector)getInstance();
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("FilenameSelector did not check for required fields");
        } catch (BuildException be1) {
            assertEquals("The name or regex attribute is required", be1.getMessage());
        }
        s = (FilenameSelector)getInstance();
        Parameter param = new Parameter();
        param.setName("garbage in");
        param.setValue("garbage out");
        Parameter[] params = {param};
        s.setParameters(params);
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("FilenameSelector did not check for valid parameter element");
        } catch (BuildException be2) {
            assertEquals("Invalid parameter garbage in", be2.getMessage());
        }
    }
    public void testSelectionBehaviour() {
        FilenameSelector s;
        String results;
        try {
            makeBed();
            s = (FilenameSelector)getInstance();
            s.setName("no match possible");
            results = selectionString(s);
            assertEquals("FFFFFFFFFFFF", results);
            s = (FilenameSelector)getInstance();
            s.setName("*.gz");
            results = selectionString(s);
            s = (FilenameSelector)getInstance();
            s.setName("**/*.gz");
            s.setNegate(true);
            results = selectionString(s);
            assertEquals("TTTFTTTFFTTT", results);
            s = (FilenameSelector)getInstance();
            s.setName("**/*.GZ");
            s.setCasesensitive(false);
            results = selectionString(s);
            assertEquals("FFFTFFFTTFFF", results);
            s = (FilenameSelector)getInstance();
            Parameter param1 = new Parameter();
            param1.setName("name");
            param1.setValue("**/*.bz2");
            Parameter[] params = {param1};
            s.setParameters(params);
            results = selectionString(s);
            assertEquals("FFTFFFFFFTTF", results);
        }
        finally {
            cleanupBed();
        }
    }
}
