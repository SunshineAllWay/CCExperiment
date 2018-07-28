package org.apache.batik.dom;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
public class GenericText extends AbstractText {
    protected boolean readonly;
    protected GenericText() {
    }
    public GenericText(String value, AbstractDocument owner) {
        ownerDocument = owner;
        setNodeValue(value);
    }
    public String getNodeName() {
        return "#text";
    }
    public short getNodeType() {
        return TEXT_NODE;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected Text createTextNode(String text) {
        return getOwnerDocument().createTextNode(text);
    }
    protected Node newNode() {
        return new GenericText();
    }
}
