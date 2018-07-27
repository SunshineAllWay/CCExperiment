package org.apache.tools.ant.util;
import java.io.File;
import junit.framework.TestCase;
public class UnPackageNameMapperTest extends TestCase {
    public UnPackageNameMapperTest(String name) { super(name); }
    public void testMapping() {
        UnPackageNameMapper mapper = new UnPackageNameMapper();
        mapper.setFrom("TEST-*.xml");
        mapper.setTo("*.java");
        String file ="TEST-org.apache.tools.ant.util.UnPackageNameMapperTest.xml";
        String result = mapper.mapFileName(file)[0];
        String expected = fixupPath("org/apache/tools/ant/util/UnPackageNameMapperTest.java");
        assertEquals(expected, result);
    }
    private String fixupPath(String file) {
        return file.replace('/', File.separatorChar);
    }
}
