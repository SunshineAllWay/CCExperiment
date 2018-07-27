package org.apache.batik.test;
public class ParametrizedTest extends AbstractTest {
    protected String A = "initial_A_value";
    protected String B = "initial_B_value";
    protected String expectedA = "unset";
    protected String expectedB = "unset";
    public void setA(String A) {
        this.A = A;
    }
    public void setB(String B) {
        this.B = B;
    }
    public void setExpectedA(String expectedA) {
        this.expectedA = expectedA;
    }
    public void setExpectedB(String expectedB) {
        this.expectedB = expectedB;
    }
    public String getA() {
        return A;
    }
    public String getB() {
        return B;
    }
    public String getExpectedA() {
        return expectedA;
    }
    public String getExpectedB() {
        return expectedB;
    }
    public TestReport runImpl() throws Exception {
        if (!A.equals(expectedA) || !B.equals(expectedB)) {
            TestReport r = reportError("Unexpected A or B value");
            r.addDescriptionEntry("expected.A", expectedA);
            r.addDescriptionEntry("actual.A", A);
            r.addDescriptionEntry("expected.B", expectedB);
            r.addDescriptionEntry("actual.B", B);
            return r;
        }
        return reportSuccess();
    }
}
