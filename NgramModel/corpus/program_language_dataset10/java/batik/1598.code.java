package org.apache.batik.test;
public interface TestSuiteReport extends TestReport{
    TestReport[] getChildrenReports();
}
