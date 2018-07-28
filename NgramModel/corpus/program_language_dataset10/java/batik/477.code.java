package org.apache.batik.dom;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.HashTable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.stylesheets.LinkStyle;
import org.w3c.dom.stylesheets.StyleSheet;
public class StyleSheetProcessingInstruction
    extends AbstractProcessingInstruction
    implements LinkStyle {
    protected boolean readonly;
    protected transient StyleSheet sheet;
    protected StyleSheetFactory factory;
    protected transient HashTable pseudoAttributes;
    protected StyleSheetProcessingInstruction() {
    }
    public StyleSheetProcessingInstruction(String            data,
                                           AbstractDocument  owner,
                                           StyleSheetFactory f) {
        ownerDocument = owner;
        setData(data);
        factory = f;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    public void setNodeName(String v) {
    }
    public String getTarget() {
        return "xml-stylesheet";
    }
    public StyleSheet getSheet() {
        if (sheet == null) {
            sheet = factory.createStyleSheet(this, getPseudoAttributes());
        }
        return sheet;
    }
    public HashTable getPseudoAttributes() {
        if (pseudoAttributes == null) {
            pseudoAttributes = new HashTable();
            pseudoAttributes.put("alternate", "no");
            pseudoAttributes.put("media",     "all");
            DOMUtilities.parseStyleSheetPIData(data, pseudoAttributes);
        }
        return pseudoAttributes;
    }
    public void setData(String data) throws DOMException {
        super.setData(data);
        sheet = null;
        pseudoAttributes = null;
    }
    protected Node newNode() {
        return new StyleSheetProcessingInstruction();
    }
}
