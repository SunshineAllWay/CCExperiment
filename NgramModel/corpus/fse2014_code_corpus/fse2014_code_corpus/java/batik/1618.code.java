package org.apache.batik.test.xml;
import org.apache.batik.test.DefaultTestSuite;
public class DummyValidTestSuite extends DefaultTestSuite{
    public DummyValidTestSuite(){
        addTest(new DummyValidTest() {{setId("1");}});
        addTest(new DummyValidTest() {{setId("2");}});
    }
}
