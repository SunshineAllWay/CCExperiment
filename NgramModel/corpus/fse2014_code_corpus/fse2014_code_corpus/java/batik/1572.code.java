package org.apache.batik.test;
import java.io.StringWriter;
import java.io.PrintWriter;
public abstract class AbstractTest implements Test {
    protected String id = "";
    protected TestSuite parent;
    protected String name;
    private DefaultTestReport report 
        = new DefaultTestReport(this) {
                {
                    setErrorCode(ERROR_INTERNAL_TEST_FAILURE);
                    setPassed(false);
                }
            };
    public String getName(){
        if(name == null){
            if (id != null && !"".equals(id)){
                return id;
            } else {
                return getClass().getName();
            }
        }
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getId(){
        return id;
    }
    public String getQualifiedId(){
        if(parent == null){
            return getId();
        }
        return getParent().getQualifiedId() + "." + getId();
    }
    public void setId(String id){
        if(id == null){
            throw new IllegalArgumentException();
        }
        this.id = id;
    }
    public TestSuite getParent(){
        return parent;
    }
    public void setParent(TestSuite parent){
        this.parent = parent;
    }
    public TestReport run(){
        try{
            return runImpl();
        } catch(TestErrorConditionException e){
            return e.getTestReport(this);
        } catch(Exception e){
            try {
                StringWriter trace = new StringWriter();
                e.printStackTrace(new PrintWriter(trace));
                TestReport.Entry[] entries = new TestReport.Entry[]{
                    new TestReport.Entry
                        (Messages.formatMessage
                         (TestReport.ENTRY_KEY_INTERNAL_TEST_FAILURE_EXCEPTION_CLASS, null),
                         e.getClass().getName()),
                    new TestReport.Entry
                        (Messages.formatMessage
                         (TestReport.ENTRY_KEY_INTERNAL_TEST_FAILURE_EXCEPTION_MESSAGE, null),
                         e.getMessage()),
                    new TestReport.Entry
                        (Messages.formatMessage
                         (TestReport.ENTRY_KEY_INTERNAL_TEST_FAILURE_EXCEPTION_STACK_TRACE, null),
                         trace.toString())
                        };
                report.setDescription(entries);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            e.printStackTrace();
            System.out.println("SERIOUS ERROR");
            return report;
        }
    }
    public TestReport runImpl() throws Exception {
        boolean passed = runImplBasic();
        DefaultTestReport report = new DefaultTestReport(this);
        if(!passed){
            report.setErrorCode(TestReport.ERROR_TEST_FAILED);
        }
        report.setPassed(passed);
        return report;
    }
    public boolean runImplBasic() throws Exception {
        return true;
    }
    public TestReport reportSuccess() {
        DefaultTestReport report = new DefaultTestReport(this);
        report.setPassed(true);
        return report;
    }
    public TestReport reportError(String errorCode){
        DefaultTestReport report = new DefaultTestReport(this);
        report.setErrorCode(errorCode);
        report.setPassed(false);
        return report;
    }
    public void error(String errorCode) throws TestErrorConditionException {
        throw new TestErrorConditionException(errorCode);
    }
    public void assertNull(Object ref) throws AssertNullException {
        if(ref != null){
            throw new AssertNullException();
        }
    }
    public void assertTrue(boolean b) throws AssertTrueException {
        if (!b){
            throw new AssertTrueException();
        }
    }
    public void assertEquals(Object ref, Object cmp) throws AssertEqualsException {
        if(ref == null && cmp != null){
            throw new AssertEqualsException(ref, cmp);
        }
        if(ref != null && !ref.equals(cmp)){
            throw new AssertEqualsException(ref, cmp);
        }
    }
    public void assertEquals(int ref, int cmp) throws AssertEqualsException {
        assertEquals(new Integer(ref), new Integer(cmp));
    }
    public TestReport reportException(String errorCode,
                                      Exception e){
        DefaultTestReport report 
            = new DefaultTestReport(this);
        StringWriter trace = new StringWriter();
        e.printStackTrace(new PrintWriter(trace));
        report.setErrorCode(errorCode);
        TestReport.Entry[] entries = new TestReport.Entry[]{
            new TestReport.Entry
                (Messages.formatMessage
                 (TestReport.ENTRY_KEY_REPORTED_TEST_FAILURE_EXCEPTION_CLASS, null),
                 e.getClass().getName()),
            new TestReport.Entry
                (Messages.formatMessage
                 (TestReport.ENTRY_KEY_REPORTED_TEST_FAILURE_EXCEPTION_MESSAGE, null),
                 e.getMessage()),
            new TestReport.Entry
                (Messages.formatMessage
                 (TestReport.ENTRY_KEY_REPORTED_TEST_FAILURE_EXCEPTION_STACK_TRACE, null),
                 trace.toString())
                };
        report.setDescription(entries);
        report.setPassed(false);
        return report;
    }
}
