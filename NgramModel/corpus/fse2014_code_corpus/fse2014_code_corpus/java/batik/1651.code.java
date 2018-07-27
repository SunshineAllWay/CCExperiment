package org.apache.batik.util;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URL;
public class Base64Test extends AbstractTest {
    public static final String ERROR_BAD_ACTION_STRING
        = "Base64Test.error.bad.action.string";
    public static final String ERROR_CANNOT_READ_IN_URL
        = "Base64Test.error.cannot.read.in.url";
    public static final String ERROR_CANNOT_READ_REF_URL
        = "Base64Test.error.cannot.read.ref.url";
    public static final String ERROR_WRONG_RESULT
        = "Base64Test.error.wrong.result";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "Base64Test.entry.key.error.description";
    protected String action = null;
    protected URL    in     = null;
    protected URL    ref    = null;
    public Base64Test(String action, URL in, URL ref) {
        this.action = action;
        this.in     = in;
        this.ref    = ref;
    }
    public Base64Test(URL in) {
        this.action = "ROUND";
        this.in     = in;
    }
    public String getName() {
        return action + " -- " + in + " -- " + super.getName();
    }
    public TestReport runImpl() throws Exception {
        DefaultTestReport report
            = new DefaultTestReport(this);
        InputStream inIS;
        try {
            inIS = in.openStream();
        } catch(Exception e) {
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_READ_IN_URL);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                    (TestMessages.formatMessage
                     (ENTRY_KEY_ERROR_DESCRIPTION, null),
                     TestMessages.formatMessage
                     (ERROR_CANNOT_READ_IN_URL,
                      new String[]{in.toString(), trace.toString()}))
                    });
            report.setPassed(false);
            return report;
        }
        if (action.equals("ROUND"))
            this.ref = in;
        else if (!action.equals("ENCODE") &&
                 !action.equals("DECODE")) {
            report.setErrorCode(ERROR_BAD_ACTION_STRING);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                    (TestMessages.formatMessage
                     (ENTRY_KEY_ERROR_DESCRIPTION, null),
                     TestMessages.formatMessage(ERROR_BAD_ACTION_STRING,
                                            new String[]{action}))
                    });
            report.setPassed(false);
            return report;
        }
        InputStream refIS;
        try {
            refIS = ref.openStream();
        } catch(Exception e) {
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_READ_REF_URL);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                    (TestMessages.formatMessage
                     (ENTRY_KEY_ERROR_DESCRIPTION, null),
                     TestMessages.formatMessage
                     (ERROR_CANNOT_READ_REF_URL,
                      new String[]{ref.toString(), trace.toString()}))
                    });
            report.setPassed(false);
            return report;
        }
        if (action.equals("ENCODE") ||
            action.equals("ROUND")) {
          PipedOutputStream pos = new PipedOutputStream();
          OutputStream os = new Base64EncoderStream(pos);
          Thread t = new StreamCopier(inIS, os);
          inIS = new PipedInputStream(pos);
          t.start();
        }
        if (action.equals("DECODE")||
            action.equals("ROUND")) {
            inIS = new Base64DecodeStream(inIS);
        }
        int mismatch = compareStreams(inIS, refIS, action.equals("ENCODE"));
        if (mismatch == -1) {
          report.setPassed(true);
          return report;
        }
        report.setErrorCode(ERROR_WRONG_RESULT);
        report.setDescription(new TestReport.Entry[] {
          new TestReport.Entry
            (TestMessages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
             TestMessages.formatMessage(ERROR_WRONG_RESULT,
                                    new String[]{ String.valueOf( mismatch )} ))
            });
        report.setPassed(false);
        return report;
    }
    public static int compareStreams(InputStream is1, InputStream is2,
                              boolean skipws) {
        byte [] data1 = new byte[100];
        byte [] data2 = new byte[100];
        int off1=0;
        int off2=0;
        int idx=0;
        try {
            while(true) {
                int len1 = is1.read(data1, off1, data1.length-off1);
                int len2 = is2.read(data2, off2, data2.length-off2);
                if (off1 != 0) {
                    if (len1 == -1)
                        len1 = off1;
                    else
                        len1 += off1;
                }
                if (off2 != 0) {
                    if (len2 == -1)
                        len2 = off2;
                    else
                        len2 += off2;
                }
                if (len1 == -1) {
                    if (len2 == -1)
                        break; 
                    if (!skipws)
                        return idx;
                    for (int i2=0; i2<len2; i2++)
                        if ((data2[i2] != '\n') &&
                            (data2[i2] != '\r') &&
                            (data2[i2] != ' '))
                            return idx+i2;
                    off1 = off2 = 0;
                    continue;
                }
                if (len2 == -1) {
                    if (!skipws)
                        return idx;
                    for (int i1=0; i1<len1; i1++)
                        if ((data1[i1] != '\n') &&
                            (data1[i1] != '\r') &&
                            (data1[i1] != ' '))
                            return idx+i1;
                    off1 = off2 = 0;
                    continue;
                }
                int i1=0;
                int i2=0;
                while((i1<len1) && (i2<len2)) {
                    if (skipws) {
                        if ((data1[i1] == '\n') ||
                            (data1[i1] == '\r') ||
                            (data1[i1] == ' ')) {
                            i1++;
                            continue;
                        }
                        if ((data2[i2] == '\n') ||
                            (data2[i2] == '\r') ||
                            (data2[i2] == ' ')) {
                            i2++;
                            continue;
                        }
                    }
                    if (data1[i1] != data2[i2])
                        return idx+i2;
                    i1++;
                    i2++;
                }
                if (i1 != len1)
                    System.arraycopy(data1, i1, data1, 0, len1-i1);
                if (i2 != len2)
                    System.arraycopy(data2, i2, data2, 0, len2-i2);
                off1 = len1-i1;
                off2 = len2-i2;
                idx+=i2;
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return idx;
        }
        return -1;
    }
    static class StreamCopier extends Thread {
        InputStream src;
        OutputStream dst;
        public StreamCopier(InputStream src,
                            OutputStream dst) {
            this.src = src;
            this.dst = dst;
        }
        public void run() {
            try {
                byte [] data = new byte[1000];
                while(true) {
                    int len = src.read(data, 0, data.length);
                    if (len == -1) break;
                    dst.write(data, 0, len);
                }
            } catch (IOException ioe) {
            }
            try {
                dst.close();
            } catch (IOException ioe) {
            }
        }
    }
}
