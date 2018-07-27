package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildFileTest;
public class DescriptionTest extends BuildFileTest {
    public DescriptionTest(String name) {
        super(name);
    }
    public void setUp() {
    }
    public void tearDown() {
    }
    public void test1() {
        configureProject("src/etc/testcases/types/description1.xml");
        assertEquals("Single description failed", "Test Project Description", project.getDescription());
    }
    public void test2() {
        configureProject("src/etc/testcases/types/description2.xml");
        assertEquals("Multi line description failed", "Multi Line\nProject Description", project.getDescription());
    }
    public void test3() {
        configureProject("src/etc/testcases/types/description3.xml");
        assertEquals("Multi instance description failed", "Multi Instance Project Description", project.getDescription());
    }
    public void test4() {
        configureProject("src/etc/testcases/types/description4.xml");
        assertEquals("Multi instance nested description failed", "Multi Instance Nested Project Description", project.getDescription());
    }
}
