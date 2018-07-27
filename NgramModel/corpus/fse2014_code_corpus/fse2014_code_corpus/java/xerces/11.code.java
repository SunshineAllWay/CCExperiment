package dom.traversal;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
 public class NameNodeFilter implements NodeFilter {
    String fName;
    boolean fMatch = true;
        public void setName(String name) {
            this.fName = name;
        }
        public String getName() {
            return this.fName;
        }
        public void setMatch(boolean match) {
            this.fMatch = match;
        }
        public boolean getMatch() {
            return this.fMatch;
        }
        public short acceptNode(Node n) {
            if (fName == null || fMatch && n.getNodeName().equals(fName) 
            ||  !fMatch && !n.getNodeName().equals(fName))
                return FILTER_ACCEPT;
            else 
                return FILTER_REJECT;
        }
    }
