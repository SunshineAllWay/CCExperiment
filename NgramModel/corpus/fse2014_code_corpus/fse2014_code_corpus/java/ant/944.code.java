package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class AvailableTest extends BuildFileTest {
    public AvailableTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/available.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void test2() {
        expectBuildException("test2", "required argument not specified");
    }
    public void test3() {
        expectBuildException("test3", "required argument not specified");
    }
    public void test4() {
        executeTarget("test4");
        assertTrue(project.getProperty("test") == null);
    }
    public void test5() {
        executeTarget("test5");
        assertEquals("true", project.getProperty("test"));
    }
    public void test6() {
        executeTarget("test6");
        assertTrue(project.getProperty("test") == null);
    }
    public void test7() {
        executeTarget("test7");
        assertEquals("true", project.getProperty("test"));
    }
    public void test8() {
        executeTarget("test8");
        assertTrue(project.getProperty("test") == null);
    }
    public void test9() {
        executeTarget("test9");
        assertEquals("true", project.getProperty("test"));
    }
    public void test10() {
        executeTarget("test10");
        assertEquals("true", project.getProperty("test"));
    }
    public void test11() {
        executeTarget("test11");
        assertNull(project.getProperty("test"));
    }
    public void test12() {
        executeTarget("test12");
        assertNull(project.getProperty("test"));
        assertEquals("true", project.getProperty(""));
    }
    public void test13() {
        executeTarget("test13");
        assertNull(project.getProperty("test"));
    }
    public void test13b() {
        executeTarget("test13b");
        assertEquals("true", project.getProperty("test"));
    }
    public void test15() {
        executeTarget("test15");
        assertNull(project.getProperty("test"));
    }
    public void test16() {
        executeTarget("test16");
        assertEquals("true", project.getProperty("test"));
    }
    public void test17() {
        executeTarget("test17");
        assertEquals("true", project.getProperty("test"));
    }
    public void test18() {
        executeTarget("test18");
        assertNull(project.getProperty("test"));
    }
    public void test19() {
        expectBuildException("test19", "Invalid value for type attribute.");
    }
    public void test20() {
        executeTarget("test20");
        assertNull(project.getProperty("test"));
    }
    public void test21() {
        executeTarget("test21");
        assertEquals("true", project.getProperty("test"));
    }
    public void test22() {
        executeTarget("test22");
        assertEquals("true", project.getProperty("test"));
    }
    public void test23() {
        executeTarget("test23");
        assertEquals("true", project.getProperty("test"));
    }
    public void test24() {
        executeTarget("test24");
        assertEquals("true", project.getProperty("test"));
    }
    public void testSearchInPathNotThere() {
        executeTarget("searchInPathNotThere");
        assertNull(project.getProperty("test"));
    }
    public void testSearchInPathIsThere() {
        executeTarget("searchInPathIsThere");
        assertEquals("true", project.getProperty("test"));
    }
    public void testDoubleBasedir() {
        executeTarget("testDoubleBasedir");
    }
    public void testSearchParents() {
        executeTarget("search-parents");
    }
    public void testSearchParentsNot() {
        executeTarget("search-parents-not");
    }
}
