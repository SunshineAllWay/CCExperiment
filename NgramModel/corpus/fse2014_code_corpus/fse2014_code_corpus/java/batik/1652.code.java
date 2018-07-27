package org.apache.batik.util;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
public class ParsedURLDataTest extends AbstractTest {
    public static final String ERROR_CANNOT_PARSE_URL
        = "ParsedURLTest.error.cannot.parse.url";
    public static final String ERROR_WRONG_RESULT
        = "ParsedURLTest.error.wrong.result";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "ParsedURLTest.entry.key.error.description";
    protected String base = null;
    protected String ref  = null;
    public ParsedURLDataTest(String url, String ref ){
        this.base = url;
        this.ref  = ref;
    }
    public String getName() {
        return ref + " -- " + super.getName();
    }
    public TestReport runImpl() throws Exception {
        DefaultTestReport report
            = new DefaultTestReport(this);
        ParsedURL purl;
        try {
            purl  = new ParsedURL(base);
        } catch(Exception e) {
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_PARSE_URL);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                    (TestMessages.formatMessage
                     (ENTRY_KEY_ERROR_DESCRIPTION, null),
                     TestMessages.formatMessage
                     (ERROR_CANNOT_PARSE_URL,
                      new String[]{"null", 
                                   base,
                                   trace.toString()}))
                    });
            report.setPassed(false);
            return report;
        }
        byte[] data = new byte[5];
        int num = 0;
        try {
            InputStream is = purl.openStream();
            num = is.read(data);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<num; i++) {
            int val = ((int)data[i])&0xFF;
            if (val < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val) + " ");
        }
        String info = ( "CT: " + purl.getContentType() + 
                       " CE: " + purl.getContentEncoding() + 
                       " DATA: " + sb +
                       "URL: " + purl);
        if (ref.equals(info)) {
            report.setPassed(true);
            return report;
        }
        report.setErrorCode(ERROR_WRONG_RESULT);
        report.setDescription(new TestReport.Entry[] {
          new TestReport.Entry
            (TestMessages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
             TestMessages.formatMessage
             (ERROR_WRONG_RESULT, new String[]{info, ref }))
            });
        report.setPassed(false);
        return report;
    }
}
