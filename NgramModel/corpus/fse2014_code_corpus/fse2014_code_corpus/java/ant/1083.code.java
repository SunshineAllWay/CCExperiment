package org.apache.tools.ant.taskdefs.optional.perforce;
import junit.framework.TestCase;
import org.apache.oro.text.perl.Perl5Util;
public class P4ChangeTest extends TestCase {
    protected P4Change p4change;
    public P4ChangeTest(String s) {
        super(s);
    }
    protected void setUp() throws Exception {
        p4change = new P4Change();
    }
    public void testBackslash(){
        String input = "comment with a / inside";
        String output = P4Change.backslash(input);
        assertEquals("comment with a \\/ inside", output);
    }
    public void testSubstitute(){
        Perl5Util util = new Perl5Util();
        String tosubstitute = "xx<here>xx";
        String input = P4Change.backslash("/a/b/c/");
        String output = util.substitute("s/<here>/" + input + "/", tosubstitute);
        assertEquals("xx/a/b/c/xx", output);
    }
}
