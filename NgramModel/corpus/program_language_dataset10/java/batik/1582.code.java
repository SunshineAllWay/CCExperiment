package org.apache.batik.test;
import org.apache.batik.test.TestReport;
public class MemoryLeakTestValidator extends MemoryLeakTest {
    public MemoryLeakTestValidator() {
    }
    Link start;
    public TestReport doSomething() throws Exception {
        for (int i=0; i<20; i++) 
            registerObjectDesc(new Object(), "Obj#"+i);
        for (int i=0; i<10; i++) {
            Pair p1 = new Pair();
            Pair p2 = new Pair();
            p1.mate(p2);
            registerObjectDesc(p2, "Pair#"+i);
        }
        Link p = null;
        for (int i=0; i<10; i++) {
            p = new Link(p);
            registerObjectDesc(p, "Link#"+i);
        }
        return null;
    }
    public static class Pair {
        Pair myMate;
        public Pair() { }
        public void mate(Pair p) {
            this.myMate = p;
            p.myMate    = this;
        }
    }
    public static class Link {
        public Link prev;
        public Link(Link prev) {
            this.prev = prev;
        }
    }
}
