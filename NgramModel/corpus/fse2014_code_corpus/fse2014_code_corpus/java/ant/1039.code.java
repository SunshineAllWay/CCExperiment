package org.apache.tools.ant.taskdefs.dir1;
public class B extends org.apache.tools.ant.taskdefs.dir2.A {
    static {
        System.out.println("B CLASS INITIALIZATION");
        setA(new B());
    }
    public String toString() {
        return "I am a B.";
    }
}
