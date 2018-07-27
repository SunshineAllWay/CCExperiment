package org.apache.tools.ant.util;
import junit.framework.TestCase;
import java.io.File;
public class JAXPUtilsTest extends TestCase {
    public JAXPUtilsTest(String name){
        super(name);
    }
    public void testGetSystemId(){
        File file = null;
        if ( File.separatorChar == '\\' ){
            file = new File("d:\\jdk");
        } else {
            file = new File("/user/local/bin");
        }
        String systemid = JAXPUtils.getSystemId(file);
        assertTrue("SystemIDs should start by file:/", systemid.startsWith("file:/"));
        assertTrue("SystemIDs should not start with file:////", !systemid.startsWith("file:////"));
    }
}
