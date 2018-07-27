package org.apache.batik.bridge.svg12;
import java.util.ArrayList;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class DefaultContentSelector extends AbstractContentSelector {
    protected SelectedNodes selectedContent;
    public DefaultContentSelector(ContentManager cm,
                                  XBLOMContentElement content,
                                  Element bound) {
        super(cm, content, bound);
    }
    public NodeList getSelectedContent() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
        }
        return selectedContent;
    }
    boolean update() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
            return true;
        }
        return selectedContent.update();
    }
    protected class SelectedNodes implements NodeList {
        protected ArrayList nodes = new ArrayList(10);
        public SelectedNodes() {
            update();
        }
        protected boolean update() {
            ArrayList oldNodes = (ArrayList) nodes.clone();
            nodes.clear();
            for (Node n = boundElement.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (isSelected(n)) {
                    continue;
                }
                nodes.add(n);
            }
            int nodesSize = nodes.size();
            if (oldNodes.size() != nodesSize) {
                return true;
            }
            for (int i = 0; i < nodesSize; i++) {
                if (oldNodes.get(i) != nodes.get(i)) {
                    return true;
                }
            }
            return false;
        }
        public Node item(int index) {
            if (index < 0 || index >= nodes.size()) {
                return null;
            }
            return (Node) nodes.get(index);
        }
        public int getLength() {
            return nodes.size();
        }
    }
}
