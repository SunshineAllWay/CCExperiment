package org.apache.batik.dom;
import org.w3c.dom.Node;
public class GenericNotation extends AbstractNotation {
    protected boolean readonly;
    protected GenericNotation() {
    }
    public GenericNotation(String           name,
                           String           pubId,
                           String           sysId,
                           AbstractDocument owner) {
        ownerDocument = owner;
        setNodeName(name);
        setPublicId(pubId);
        setSystemId(sysId);
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected Node newNode() {
        return new GenericNotation();
    }
}
