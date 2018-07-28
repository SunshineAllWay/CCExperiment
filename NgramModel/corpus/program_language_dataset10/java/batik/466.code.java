package org.apache.batik.dom;
import org.w3c.dom.Node;
public class GenericDocumentFragment extends AbstractDocumentFragment {
    protected boolean readonly;
    protected GenericDocumentFragment() {
    }
    public GenericDocumentFragment(AbstractDocument owner) {
        ownerDocument = owner;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected Node newNode() {
        return new GenericDocumentFragment();
    }
}
