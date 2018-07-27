package org.apache.batik.test;
public interface TestSuite extends Test {
    void addTest(Test test);
    void removeTest(Test test);
    Test[] getChildrenTests();
    int getChildrenCount();
}
