package org.apache.batik.test;
public interface TestReportProcessor {
    String INTERNAL_ERROR =
        "TestReportProcessor.error.code.internal.error";
    void processReport(TestReport report)
        throws TestException;
}
