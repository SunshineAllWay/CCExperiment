package org.apache.batik.test;
public class TestReportValidator extends AbstractTest {
    private Test test;
    private boolean expectedStatus;
    private String expectedErrorCode;
    static final String ERROR_UNEXPECTED_TEST_STATUS
        = "TestReportValidator.error.unexpected.test.status";
    static final String ERROR_UNEXPECTED_ERROR_CODE
        = "TestReportValidator.error.unexpected.error.code";
    public static final String ENTRY_KEY_EXPECTED_ERROR_CODE
        = "TestReportValidator.entry.key.expected.error.code";
    public static final String ENTRY_KEY_RECEIVED_ERROR_CODE
        = "TestReportValidator.entry.key.received.error.code";
    public static final String ENTRY_KEY_EXPECTED_STATUS
        = "TestReportValidator.entry.key.expected.status";
    public static final String ENTRY_KEY_RECEIVED_STATUS
        = "TestReportValidator.entry.key.received.status";
    public TestReportValidator(Test test,
                               boolean expectedStatus,
                               String expectedErrorCode){
        setConfig(test,
                  expectedStatus,
                  expectedErrorCode);
    }
    protected TestReportValidator(){
    }
    protected void setConfig(Test test,
                             boolean expectedStatus,
                             String expectedErrorCode){
        this.expectedErrorCode = expectedErrorCode;
        this.test = test;
        this.expectedStatus = expectedStatus;
    }
    public TestReport runImpl() throws Exception {
        TestReport tr = test.run();
        DefaultTestReport r = new DefaultTestReport(this);
        if( tr.hasPassed() != expectedStatus ){
            TestReport.Entry expectedStatusEntry
                = new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_EXPECTED_STATUS, null),
                                       ( new Boolean(expectedStatus)).toString());
            TestReport.Entry receivedStatusEntry
                = new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_RECEIVED_STATUS, null),
                                       (new Boolean(tr.hasPassed())).toString());
            r.setDescription(new TestReport.Entry[]{ expectedStatusEntry, receivedStatusEntry });
            r.setErrorCode(ERROR_UNEXPECTED_TEST_STATUS);
            r.setPassed(false);
        }
        else if( tr.getErrorCode() != expectedErrorCode ){
            TestReport.Entry expectedErrorCodeEntry
                = new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_EXPECTED_ERROR_CODE, null),
                                       expectedErrorCode);
            TestReport.Entry receivedErrorCodeEntry
                = new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_RECEIVED_ERROR_CODE, null),
                                       tr.getErrorCode());
            r.setDescription(new TestReport.Entry[]{ expectedErrorCodeEntry, receivedErrorCodeEntry });
            r.setErrorCode(ERROR_UNEXPECTED_ERROR_CODE);
            r.setPassed(false);
        }
        return r;
    }
}
