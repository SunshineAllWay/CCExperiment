package org.apache.tools.ant.taskdefs.dir2;
public class A {
    public static void main(String [] args) {
        System.out.println("MAIN");
        System.out.println(a);
    }
    static A a=new A();
    static {
        System.out.println("A CLASS INITIALIZATION");
    }
    protected static void setA(A oa) {
        a=oa;
    }
    public String toString() {
        return "I am a A.";
    }
}
