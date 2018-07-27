package org.apache.xerces.impl.xs.opti;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public class TextImpl extends DefaultText {
    String fData = null;
    SchemaDOM fSchemaDOM = null;
    int fRow;
    int fCol;
    public TextImpl(StringBuffer str, SchemaDOM sDOM, int row, int col) {
        fData = str.toString();
        fSchemaDOM = sDOM;
        fRow = row;
        fCol = col;
        rawname = prefix = localpart = uri = null;
        nodeType = Node.TEXT_NODE;
    }
    public String getNodeName() {
        return "#text";
    }
    public Node getParentNode() {
        return fSchemaDOM.relations[fRow][0];
    }
    public Node getPreviousSibling() {
        if (fCol == 1) {
            return null;
        }
        return fSchemaDOM.relations[fRow][fCol-1];
    }
    public Node getNextSibling() {
        if (fCol == fSchemaDOM.relations[fRow].length-1) {
            return null;
        }
        return fSchemaDOM.relations[fRow][fCol+1];
    }
    public String getData()
                            throws DOMException {
        return fData;
    }
    public int getLength() {
        if(fData == null) return 0;
        return fData.length();
    }
    public String substringData(int offset, 
                                int count)
                                throws DOMException {
        if(fData == null) return null;
        if(count < 0 || offset < 0 || offset > fData.length()) 
    	    throw new DOMException(DOMException.INDEX_SIZE_ERR, "parameter error");
        if(offset+count >= fData.length()) 
            return fData.substring(offset);
        return fData.substring(offset, offset+count);
    }
}
