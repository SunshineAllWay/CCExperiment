package org.apache.batik.parser;
import java.io.*;
import org.apache.batik.test.*;
public class LengthParserFailureTest extends AbstractTest {
    protected String sourceLength;
    public LengthParserFailureTest(String slength) {
        sourceLength = slength;
    }
    public TestReport runImpl() throws Exception {
        LengthParser pp = new LengthParser();
        try {
            pp.parse(new StringReader(sourceLength));
        } catch (ParseException e) {
            return reportSuccess();
        }
        DefaultTestReport report = new DefaultTestReport(this);
        report.setErrorCode("parse.without.error");
        report.addDescriptionEntry("input.text", sourceLength);
        report.setPassed(false);
        return report;
    }
}
