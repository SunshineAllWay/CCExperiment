package org.apache.batik.test;
public interface Test {
    String getName();
    String getQualifiedId();
    String getId();
    void setId(String id);
    TestReport run();
    TestSuite getParent();
    void setParent(TestSuite parent);
}
