package org.apache.tools.ant.types.selectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
public class DepthSelectorTest extends BaseSelectorTest {
    public DepthSelectorTest(String name) {
        super(name);
    }
    public BaseSelector getInstance() {
        return new DepthSelector();
    }
    public void testValidate() {
        DepthSelector s = (DepthSelector)getInstance();
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("DepthSelector did not check for required fields");
        } catch (BuildException be1) {
            assertEquals("You must set at least one of the min or the " +
                    "max levels.", be1.getMessage());
        }
        s = (DepthSelector)getInstance();
        s.setMin(5);
        s.setMax(2);
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("DepthSelector did not check for maximum being higher "
                    + "than minimum");
        } catch (BuildException be2) {
            assertEquals("The maximum depth is lower than the minimum.",
                    be2.getMessage());
        }
        s = (DepthSelector)getInstance();
        Parameter param = new Parameter();
        param.setName("garbage in");
        param.setValue("garbage out");
        Parameter[] params = new Parameter[1];
        params[0] = param;
        s.setParameters(params);
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("DepthSelector did not check for valid parameter element");
        } catch (BuildException be3) {
            assertEquals("Invalid parameter garbage in", be3.getMessage());
        }
        s = (DepthSelector)getInstance();
        param = new Parameter();
        param.setName("min");
        param.setValue("garbage out");
        params[0] = param;
        s.setParameters(params);
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("DepthSelector accepted bad minimum as parameter");
        } catch (BuildException be4) {
            assertEquals("Invalid minimum value garbage out",
                    be4.getMessage());
        }
        s = (DepthSelector)getInstance();
        param = new Parameter();
        param.setName("max");
        param.setValue("garbage out");
        params[0] = param;
        s.setParameters(params);
        try {
            s.isSelected(basedir,filenames[0],files[0]);
            fail("DepthSelector accepted bad maximum as parameter");
        } catch (BuildException be5) {
            assertEquals("Invalid maximum value garbage out",
                    be5.getMessage());
        }
    }
    public void testSelectionBehaviour() {
        DepthSelector s;
        String results;
        try {
            makeBed();
            s = (DepthSelector)getInstance();
            s.setMin(20);
            s.setMax(25);
            results = selectionString(s);
            assertEquals("FFFFFFFFFFFF", results);
            s = (DepthSelector)getInstance();
            s.setMin(0);
            results = selectionString(s);
            assertEquals("TTTTTTTTTTTT", results);
            s = (DepthSelector)getInstance();
            s.setMin(1);
            results = selectionString(s);
            assertEquals("FFFFFTTTTTTT", results);
            s = (DepthSelector)getInstance();
            s.setMax(0);
            results = selectionString(s);
            assertEquals("TTTTTFFFFFFF", results);
            s = (DepthSelector)getInstance();
            s.setMin(1);
            s.setMax(1);
            results = selectionString(s);
            assertEquals("FFFFFTTTFFFT", results);
        }
        finally {
            cleanupBed();
        }
    }
}
