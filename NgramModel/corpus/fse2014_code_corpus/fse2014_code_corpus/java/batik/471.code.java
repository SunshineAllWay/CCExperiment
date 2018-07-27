package org.apache.batik.dom;
import org.w3c.dom.Node;
public class GenericEntity extends AbstractEntity {
    protected boolean readonly;
    protected GenericEntity() {
    }
    public GenericEntity(String           name,
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
        return new GenericEntity();
    }
}
