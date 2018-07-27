package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
public class NodeGetUserDataTest extends DOM3Test {
    static class UserHandler implements UserDataHandler {
        int count = 0;
        public void handle(short op,
                           String key,
                           Object data,
                           Node src,
                           Node dest) {
            count++;
        }
        public int getCount() {
            return count;
        }
    }
    public boolean runImplBasic() throws Exception {
        UserHandler udh = new UserHandler();
        Document doc = newDoc();
        AbstractNode n = (AbstractNode) doc.createElementNS(null, "test");
        n.setUserData("key", "val", udh);
        ((AbstractDocument) doc).renameNode(n, null, "abc");
        return udh.getCount() == 1
                && ((String) n.getUserData("key")).equals("val");
    }
}
