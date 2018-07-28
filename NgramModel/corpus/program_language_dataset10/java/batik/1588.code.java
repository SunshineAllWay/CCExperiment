package org.apache.batik.test;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
public class SimpleTestReportProcessor implements TestReportProcessor {
    public static final String MESSAGES_TEST_SUITE_STATUS_TEST_PASSED
        = "SimpleTestReportProcessor.messages.test.suite.status.testPassed";
    public static final String MESSAGES_TEST_SUITE_STATUS_TEST_FAILED
        = "SimpleTestReportProcessor.messages.test.suite.status.testFailed";
    public static final String MESSAGES_TEST_SUITE_STATUS
        = "SimpleTestReportProcessor.messages.test.suite.status";
    public static final String MESSAGES_TEST_SUITE_ERROR_CODE
        = "SimpleTestReportProcessor.messages.test.suite.error.code";
    private PrintWriter printWriter;
    public void setPrintWriter(PrintWriter printWriter){
        this.printWriter = printWriter;
    }
    public void processReport(TestReport report)
        throws TestException{
        try{
            PrintWriter out = printWriter;
            if(printWriter == null){
                out = new PrintWriter(new OutputStreamWriter(System.out));
            }
            processReport(report, "", out);
            out.flush();
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(INTERNAL_ERROR,
                                    new Object[] { e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);
        }
    }
    public void processReport(TestReport report, String prefix, PrintWriter out){
        String status = report.hasPassed() 
            ? Messages.formatMessage(MESSAGES_TEST_SUITE_STATUS_TEST_PASSED, null)
            : Messages.formatMessage(MESSAGES_TEST_SUITE_STATUS_TEST_FAILED, null);
        out.println(Messages.formatMessage(MESSAGES_TEST_SUITE_STATUS,
                                                  new Object[]{ report.getTest().getName(),
                                                                status }));
        if(!report.hasPassed()){
            out.println(Messages.formatMessage(MESSAGES_TEST_SUITE_ERROR_CODE, 
                                                      new Object[]{report.getErrorCode()}));
        }
        TestReport.Entry[] entries = report.getDescription();
        int n = entries != null ? entries.length : 0;
        for(int i=0; i<n; i++){
            out.print(prefix + entries[i].getKey() + " : " );
            printValue(entries[i].getValue(), prefix + "    ", out);
        }
    }
    protected void printValue(Object value, String prefix, PrintWriter out){
        if(!(value instanceof TestReport)){
            out.println(value);
        }
        else{
            out.println();
            processReport((TestReport)value, prefix, out);
        }
    }
}
