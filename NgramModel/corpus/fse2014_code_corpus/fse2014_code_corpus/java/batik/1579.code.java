package org.apache.batik.test;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
public class DefaultTestSuite extends AbstractTest implements TestSuite {
    private String name = null;
    protected List tests = new ArrayList();
    public void addTest(Test test){
        if(test == null){
            throw new IllegalArgumentException();
        }
        test.setParent(this);
        tests.add(test);
    }
    public void removeTest(Test test){
        tests.remove(test);
    }
    public TestReport runImpl(){
        Iterator iter = tests.iterator();
        DefaultTestSuiteReport report
            = new DefaultTestSuiteReport(this);
        while(iter.hasNext()){
            Test t = (Test)iter.next();
            System.err.println("Running " + t.getName());
            TestReport tr = t.run();
            if (tr == null){
                System.out.println("ERROR" + t.getId() + " returned a null report");
            }
            report.addReport(tr);
        }
        return report;
    }
    public String getName(){
        if(name != null){
            return name;
        }
        String id = getId();
        if(id != null && !"".equals(id)){
            return id;
        }
        return this.getClass().getName();
    }
    public void setName(String name){
        if(name == null && !"".equals(name)){      
            throw new IllegalArgumentException();
        }
        this.name = name;
    }
    public Test[] getChildrenTests(){
        Test[] children = new Test[tests.size()];
        tests.toArray(children);
        return children;
    }
    public int getChildrenCount(){
        return tests.size();
    }
}
