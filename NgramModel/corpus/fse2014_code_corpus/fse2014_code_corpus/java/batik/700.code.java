package org.apache.batik.dom.util;
import java.util.List;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
public class ListNodeList implements NodeList {
    protected List list;
    public ListNodeList(List list) {
        this.list = list;
    }
    public Node item(int index) {
        if ((index < 0) || (index > list.size()))
            return null;
        return (Node)list.get(index);
    }
    public int getLength() {
        return list.size();
    }
}
