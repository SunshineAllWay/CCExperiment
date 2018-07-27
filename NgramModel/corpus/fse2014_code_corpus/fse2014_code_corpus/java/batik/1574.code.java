package org.apache.batik.test;
public class AssertEqualsException extends AssertException {
    public static final String ENTRY_KEY_REF_OBJECT
        = "AssertEqualsException.entry.key.ref.object";
    public static final String ENTRY_KEY_CMP_OBJECT
        = "AssertEqualsException.entry.key.cmp.object";
    public static final String ASSERTION_TYPE = "assertEquals";
    protected Object ref, cmp;
    public AssertEqualsException(Object ref, Object cmp){
        this.ref = ref;
        this.cmp = cmp;
    }
    public void addDescription(TestReport report){
        report.addDescriptionEntry(ENTRY_KEY_REF_OBJECT, ref);
        report.addDescriptionEntry(ENTRY_KEY_CMP_OBJECT, cmp);
    }
    public String getAssertionType(){
        return ASSERTION_TYPE;
    }
}
