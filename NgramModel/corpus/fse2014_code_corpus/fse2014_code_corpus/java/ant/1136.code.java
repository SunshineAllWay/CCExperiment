package org.apache.tools.ant.types.selectors;
import org.apache.tools.ant.BuildFileTest;
public class SignedSelectorTest extends BuildFileTest {
    public SignedSelectorTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/selectors/signedselector.xml");
    }
    public void testSelectSigned() {
        executeTarget("selectsigned");
    }
    public void testNotSelected() {
        executeTarget("notselected");
    }
    public void testName() {
        executeTarget("name");
    }
}
