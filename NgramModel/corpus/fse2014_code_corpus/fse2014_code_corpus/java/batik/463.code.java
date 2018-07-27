package org.apache.batik.dom;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
public class GenericCDATASection
        extends AbstractText
        implements CDATASection {
    protected boolean readonly;
    protected GenericCDATASection() {
    }
    public GenericCDATASection(String value, AbstractDocument owner) {
        ownerDocument = owner;
        setNodeValue(value);
    }
    public String getNodeName() {
        return "#cdata-section";
    }
    public short getNodeType() {
        return CDATA_SECTION_NODE;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected Text createTextNode(String text) {
        return getOwnerDocument().createCDATASection(text);
    }
    protected Node newNode() {
        return new GenericCDATASection();
    }
}
