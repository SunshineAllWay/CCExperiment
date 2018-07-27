package org.apache.batik.test;
import java.util.List;
import java.util.LinkedList;
public abstract class AbstractTestSuite implements TestSuite {
    protected List children = new LinkedList();
    public void addTest(Test test){
        if(test != null){
            children.add(test);
        }
    }
}
