package org.apache.batik.test;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
public class DefaultTestSuiteReport implements TestSuiteReport {
    public static final String ERROR_CHILD_TEST_FAILED
        = "DefaultTestSuiteReport.error.child.test.failed";
    public static final String ENTRY_KEY_FAILED_CHILD_TEST_REPORT
        = "DefaultTestSuiteReport.entry.key.failed.child.test.report";
    public static final String ENTRY_KEY_PASSED_CHILD_TEST_REPORT
        = "DefaultTestSuiteReport.entry.key.passed.child.test.report";
    protected List reports = new ArrayList();
    protected TestSuite testSuite;
    protected Entry[] description;
    protected TestSuiteReport parent;
    public DefaultTestSuiteReport(TestSuite testSuite){
        if(testSuite == null){
            throw new IllegalArgumentException();
        }
        this.testSuite = testSuite;
    }
    public Test getTest(){
        return testSuite;
    }
    public String getErrorCode(){
        if(hasPassed()){
            return null;
        }
        else{
            return ERROR_CHILD_TEST_FAILED;
        }
    }
    public TestSuiteReport getParentReport(){
        return parent;
    }
    public void setParentReport(TestSuiteReport parent){
        this.parent = parent;
    }
    public boolean hasPassed(){
        Iterator iter = reports.iterator();
        boolean passed = true;
        while(iter.hasNext()){
            TestReport childReport = (TestReport)iter.next();
            passed = passed && childReport.hasPassed();
        }
        return passed;
    }
    public void addDescriptionEntry(String key,
                                    Object value){
        addDescriptionEntry(new Entry(key, value));
    }
    protected void addDescriptionEntry(Entry entry){
        if(description == null){
            description = new Entry[1];
            description[0] = entry;
        }
        else{
            Entry[] oldDescription = description;
            description = new Entry[description.length + 1];
            System.arraycopy(oldDescription, 0, description, 0,
                             oldDescription.length);
            description[oldDescription.length] = entry;
        }
    }
    public Entry[] getDescription(){
        Iterator iter = reports.iterator();
        List descs = new ArrayList();
        while(iter.hasNext()){
            TestReport childReport = (TestReport)iter.next();
            if(!childReport.hasPassed()){
                TestReport.Entry entry
                    = new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_FAILED_CHILD_TEST_REPORT, null),
                                           childReport);
                descs.add(entry);
            }
        }
        iter = reports.iterator();
        while(iter.hasNext()){
            TestReport childReport = (TestReport)iter.next();
            if(childReport.hasPassed()){
                TestReport.Entry entry
                    = new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_PASSED_CHILD_TEST_REPORT, null),
                                           childReport);
                descs.add(entry);
            }
        }
        TestReport.Entry[] entries = null;
        if(descs.size() > 0){
            entries = new TestReport.Entry[descs.size()];
            descs.toArray(entries);
        }
        if(description != null){
            TestReport.Entry[] e = entries;
            entries = new TestReport.Entry[e.length + description.length];
            System.arraycopy(e, 0, entries, 0, e.length);
            System.arraycopy(description, 0, entries, e.length, description.length);
        }
        return entries;
    }
    public void addReport(TestReport report){
        if(report == null){
            throw new IllegalArgumentException();
        }
        report.setParentReport(this);
        reports.add(report);
    }
    public TestReport[] getChildrenReports(){
        int nReports = reports.size();
        TestReport[] r = new TestReport[nReports];
        reports.toArray(r);
        return r;
    }
}
