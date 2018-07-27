package org.apache.xerces.dom;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
public class CDATASectionImpl 
    extends TextImpl 
    implements CDATASection {
    static final long serialVersionUID = 2372071297878177780L;
    public CDATASectionImpl(CoreDocumentImpl ownerDoc, String data) {
        super(ownerDoc, data);
    }  
    public short getNodeType() {
        return Node.CDATA_SECTION_NODE;
    }
    public String getNodeName() {
        return "#cdata-section";
    }
} 
