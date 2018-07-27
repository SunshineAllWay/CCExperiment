package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class XMLFormatterWithCDATAOnSystemOut extends BuildFileTest {
    private static String DIR = "src/etc/testcases/taskdefs/optional/junit";
    private static String REPORT =
        "TEST-" + XMLFormatterWithCDATAOnSystemOut.class.getName() + ".xml";
    private static String TESTDATA =
        "<ERROR>" +
        "<![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "  <RESPONSE>" +
        "    <GDS/>" +
        "    <ERROR>" +
        "      <ID/>" +
        "      <MESSAGE/>" +
        "      <REQUEST_TYPE/>" +
        "      <RESEND/>" +
        "      <RAW_RESPONSE/>" +
        "    </ERROR>" +
        "  </RESPONSE>" +
        "]]>" +
        "</ERROR>";
    public XMLFormatterWithCDATAOnSystemOut(String name) {
        super(name);
    }
    public void testOutput() {
        System.out.println(TESTDATA);
    }
    public void testBuildfile() throws IOException {
        configureProject(DIR + "/cdataoutput.xml");
        if (getProject().getProperty("cdata.inner") == null) {
            executeTarget("run-junit");
            File f = getProject().resolveFile(REPORT);
            FileReader reader = null;
            try {
                reader = new FileReader(f);
                String content = FileUtils.readFully(reader);
                assertTrue(content.indexOf("</RESPONSE>&#x5d;&#x5d;&gt;"
                                           + "</ERROR>") > 0);
            } finally {
                if (reader != null) {
                    reader.close();
                }
                f.delete();
            }
        }
    }
}
