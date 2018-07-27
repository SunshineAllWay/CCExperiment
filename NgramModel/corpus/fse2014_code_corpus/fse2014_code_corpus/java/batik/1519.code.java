package org.apache.batik.parser;
import java.io.*;
import org.apache.batik.test.*;
public class PathParserFailureTest extends AbstractTest {
    protected String sourcePath;
    public PathParserFailureTest(String spath) {
        sourcePath = spath;
    }
    public TestReport runImpl() throws Exception {
        PathParser pp = new PathParser();
        try {
            pp.parse(new StringReader(sourcePath));
        } catch (Exception e) {
            return reportSuccess();
        }
        DefaultTestReport report = new DefaultTestReport(this);
        report.setErrorCode("parse.without.error");
        report.addDescriptionEntry("input.text", sourcePath);
        report.setPassed(false);
        return report;
    }
}
