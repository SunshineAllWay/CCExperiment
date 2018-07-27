package org.apache.tools.ant.util;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import junit.framework.TestCase;
public class CollectionUtilsTest extends TestCase {
    public CollectionUtilsTest(String name) {
        super(name);
    }
    public void testVectorEquals() {
        assertTrue(!CollectionUtils.equals(null, new Vector()));
        assertTrue(!CollectionUtils.equals(new Vector(), null));
        assertTrue(CollectionUtils.equals(new Vector(), new Vector()));
        Vector v1 = new Vector();
        Stack s2 = new Stack();
        v1.addElement("foo");
        s2.push("foo");
        assertTrue(CollectionUtils.equals(v1, s2));
        assertTrue(CollectionUtils.equals(s2, v1));
        v1.addElement("bar");
        assertTrue(!CollectionUtils.equals(v1, s2));
        assertTrue(!CollectionUtils.equals(s2, v1));
        s2.push("bar");
        assertTrue(CollectionUtils.equals(v1, s2));
        assertTrue(CollectionUtils.equals(s2, v1));
        s2.push("baz");
        assertTrue(!CollectionUtils.equals(v1, s2));
        assertTrue(!CollectionUtils.equals(s2, v1));
        v1.addElement("baz");
        assertTrue(CollectionUtils.equals(v1, s2));
        assertTrue(CollectionUtils.equals(s2, v1));
        v1.addElement("zyzzy");
        s2.push("zyzzy2");
        assertTrue(!CollectionUtils.equals(v1, s2));
        assertTrue(!CollectionUtils.equals(s2, v1));
    }
    public void testDictionaryEquals() {
        assertTrue(!CollectionUtils.equals(null, new Hashtable()));
        assertTrue(!CollectionUtils.equals(new Hashtable(), null));
        assertTrue(CollectionUtils.equals(new Hashtable(), new Properties()));
        Hashtable h1 = new Hashtable();
        Properties p2 = new Properties();
        h1.put("foo", "");
        p2.put("foo", "");
        assertTrue(CollectionUtils.equals(h1, p2));
        assertTrue(CollectionUtils.equals(p2, h1));
        h1.put("bar", "");
        assertTrue(!CollectionUtils.equals(h1, p2));
        assertTrue(!CollectionUtils.equals(p2, h1));
        p2.put("bar", "");
        assertTrue(CollectionUtils.equals(h1, p2));
        assertTrue(CollectionUtils.equals(p2, h1));
        p2.put("baz", "");
        assertTrue(!CollectionUtils.equals(h1, p2));
        assertTrue(!CollectionUtils.equals(p2, h1));
        h1.put("baz", "");
        assertTrue(CollectionUtils.equals(h1, p2));
        assertTrue(CollectionUtils.equals(p2, h1));
        h1.put("zyzzy", "");
        p2.put("zyzzy2", "");
        assertTrue(!CollectionUtils.equals(h1, p2));
        assertTrue(!CollectionUtils.equals(p2, h1));
        p2.put("zyzzy", "");
        h1.put("zyzzy2", "");
        assertTrue(CollectionUtils.equals(h1, p2));
        assertTrue(CollectionUtils.equals(p2, h1));
        h1.put("dada", "1");
        p2.put("dada", "2");
        assertTrue(!CollectionUtils.equals(h1, p2));
        assertTrue(!CollectionUtils.equals(p2, h1));
    }
}
