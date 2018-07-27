package org.apache.batik.test;
public abstract class AssertException extends TestErrorConditionException {
    public static final String ENTRY_KEY_ASSERTION_TYPE 
        = "AssertException.entry.key.assertion.type";
    public TestReport getTestReport(Test test){
        DefaultTestReport report = new DefaultTestReport(test);
        report.setErrorCode(TestReport.ERROR_ASSERTION_FAILED);
        report.addDescriptionEntry(ENTRY_KEY_ASSERTION_TYPE,
                                   getAssertionType());
        addDescription(report);
        addStackTraceDescription(report);  
        report.setPassed(false);
        return report;
    }
    public abstract void addDescription(TestReport report);
    public abstract String getAssertionType();
}
