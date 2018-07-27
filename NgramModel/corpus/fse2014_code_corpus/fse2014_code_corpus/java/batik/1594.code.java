package org.apache.batik.test;
public interface TestReport {
    String ERROR_INTERNAL_TEST_FAILURE
        = "TestReport.error.internal.test.failure";
    String ERROR_TEST_FAILED
        = "TestReport.error.test.failed";
    String ERROR_ASSERTION_FAILED
        = "TestReport.error.assertion.failed";
    String
        ENTRY_KEY_INTERNAL_TEST_FAILURE_EXCEPTION_CLASS
        = "TestReport.entry.key.internal.test.failure.exception.class";
    String
        ENTRY_KEY_INTERNAL_TEST_FAILURE_EXCEPTION_MESSAGE
        = "TestReport.entry.key.internal.test.failure.exception.message";
    String
        ENTRY_KEY_INTERNAL_TEST_FAILURE_EXCEPTION_STACK_TRACE
        = "TestReport.entry.key.internal.test.failure.exception.stack.trace";
    String
        ENTRY_KEY_REPORTED_TEST_FAILURE_EXCEPTION_CLASS
        = "TestReport.entry.key.reported.test.failure.exception.class";
    String
        ENTRY_KEY_REPORTED_TEST_FAILURE_EXCEPTION_MESSAGE
        = "TestReport.entry.key.reported.test.failure.exception.message";
    String
        ENTRY_KEY_REPORTED_TEST_FAILURE_EXCEPTION_STACK_TRACE
        = "TestReport.entry.key.reported.test.failure.exception.stack.trace";
    String
        ENTRY_KEY_ERROR_CONDITION_STACK_TRACE
        = "TestReport.entry.key.error.condition.stack.trace";
    class Entry {
        private String entryKey;
        private Object entryValue;
        public Entry(String entryKey,
                     Object entryValue){
            this.entryKey = entryKey;
            this.entryValue = entryValue;
        }
        public final String getKey(){
            return entryKey;
        }
        public final Object getValue(){
            return entryValue;
        }
    }
    boolean hasPassed();
    String getErrorCode();
    Entry[] getDescription();
    void addDescriptionEntry(String key,
                                    Object value);
    Test getTest();
    TestSuiteReport getParentReport();
    void setParentReport(TestSuiteReport parent);
}
