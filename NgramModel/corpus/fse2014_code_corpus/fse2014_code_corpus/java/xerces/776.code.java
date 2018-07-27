package dom.traversal;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
public class BasicTest extends AbstractTestCase {
    private static final String DOC1 = "<root>1<a/>2<b/>3<c/>4<d/><!-- foo -->5<e/>6<?target data?></root>";
    private static final String DOC2 = "<root>1<a>2<b/>7<e/>9</a>3<c>5<d/>0<!-- bar -->8<f/>6</c>4</root>";
    public void testGetFirstChild1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getFirstElementChild();
        assertEquals("a", e.getNodeName());
    }
    public void testGetFirstChild2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getFirstElementChild();
        assertEquals("a", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getFirstElementChild();
        assertEquals("b", e.getNodeName());
    }
    public void testGetLastChild1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getLastElementChild();
        assertEquals("e", e.getNodeName());
    }
    public void testGetLastChild2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getLastElementChild();
        assertEquals("c", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getLastElementChild();
        assertEquals("f", e.getNodeName());
    }
    public void testGetNextElementSibling1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getFirstElementChild();
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("b", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("c", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("d", e.getNodeName());
    }
    public void testGetNextElementSibling2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getFirstElementChild();
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("c", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getFirstElementChild();
        assertEquals("d", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("f", e.getNodeName());
    }
    public void testGetPreviousElementSibling1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getLastElementChild();
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("d", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("c", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("b", e.getNodeName());
    }
    public void testGetPreviousElementSibling2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getLastElementChild();
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("a", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getLastElementChild();
        assertEquals("e", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("b", e.getNodeName());
    }
    public void testChildElementCount1() {
        ElementTraversal et = parse(DOC1);
        assertEquals(5, et.getChildElementCount());
    }
    public void testChildElementCount2() {
        ElementTraversal et = parse(DOC2);
        assertEquals(2, et.getChildElementCount());
        ElementTraversal et2 = toElementTraversal(et.getFirstElementChild());
        assertEquals(2, et2.getChildElementCount());
        et2 = toElementTraversal(et.getLastElementChild());
        assertEquals(2, et2.getChildElementCount());
    }
}
