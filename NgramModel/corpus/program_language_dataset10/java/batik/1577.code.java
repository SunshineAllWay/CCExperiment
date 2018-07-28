package org.apache.batik.test;
public class AssertTrueException extends AssertException {
    public static final String ASSERTION_TYPE = "assertTrue";
    protected Object ref, cmp;
    public AssertTrueException(){
    }
    public void addDescription(TestReport report){
    }
    public String getAssertionType(){
        return ASSERTION_TYPE;
    }
}
