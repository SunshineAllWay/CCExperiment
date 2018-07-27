package org.apache.tools.ant.taskdefs.condition;
import junit.framework.TestCase;
public class ContainsTest extends TestCase {
    public ContainsTest(String name) {
        super(name);
    }
    public void testCaseSensitive() {
        Contains con = new Contains();
        con.setString("abc");
        con.setSubstring("A");
        assertTrue(!con.eval());
        con.setCasesensitive(false);
        assertTrue(con.eval());
    }
}
