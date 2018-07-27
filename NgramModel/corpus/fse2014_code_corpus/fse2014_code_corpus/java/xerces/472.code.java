package org.apache.xerces.impl.xs.opti;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
public class ElementImpl extends DefaultElement {
    SchemaDOM schemaDOM;
    Attr[] attrs;
    int row;
    int col;
    int parentRow;
    int line;
    int column;
    int charOffset;
    String fAnnotation;
    String fSyntheticAnnotation;
    public ElementImpl(int line, int column, int offset) {
        row = -1;
        col = -1;
        parentRow = -1;
        nodeType = Node.ELEMENT_NODE;
        this.line = line;
        this.column = column;
        charOffset = offset;
    }
    public ElementImpl(int line, int column) {
        this(line, column, -1);
    }
    public ElementImpl(String prefix, String localpart, String rawname,
            String uri, int line, int column, int offset) {
        super(prefix, localpart, rawname, uri, Node.ELEMENT_NODE);
        row = -1;
        col = -1;
        parentRow = -1;
        this.line = line;
        this.column = column;
        charOffset = offset;
    }
    public ElementImpl(String prefix, String localpart, String rawname,
            String uri, int line, int column) {
        this(prefix, localpart, rawname, uri, line, column, -1);
    }
    public Document getOwnerDocument() {
        return schemaDOM;
    }
    public Node getParentNode() {
        return schemaDOM.relations[row][0];
    }
    public boolean hasChildNodes() {
        if (parentRow == -1) {
            return false;
        }
        else {
            return true;
        }
    }
    public Node getFirstChild() {
        if (parentRow == -1) {
            return null;
        }
        return schemaDOM.relations[parentRow][1];
    }
    public Node getLastChild() {
        if (parentRow == -1) {
            return null;
        }
        int i=1;
        for (; i<schemaDOM.relations[parentRow].length; i++) {
            if (schemaDOM.relations[parentRow][i] == null) {
                return schemaDOM.relations[parentRow][i-1];
            }
        }
        if (i ==1) {
            i++;
        }
        return schemaDOM.relations[parentRow][i-1];
    }
    public Node getPreviousSibling() {
        if (col == 1) {
            return null;
        }
        return schemaDOM.relations[row][col-1];
    }
    public Node getNextSibling() {
        if (col == schemaDOM.relations[row].length-1) {
            return null;
        }
        return schemaDOM.relations[row][col+1];
    }
    public NamedNodeMap getAttributes() {
        return new NamedNodeMapImpl(attrs);
    }
    public boolean hasAttributes() {
        return (attrs.length == 0 ? false : true);
    }
    public String getTagName() {
        return rawname;
    }
    public String getAttribute(String name) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getName().equals(name)) {
                return attrs[i].getValue();
            }
        }
        return "";
    }
    public Attr getAttributeNode(String name) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getName().equals(name)) {
                return attrs[i];
            }
        }
        return null;
    }
    public String getAttributeNS(String namespaceURI, String localName) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getLocalName().equals(localName) && nsEquals(attrs[i].getNamespaceURI(), namespaceURI)) {
                return attrs[i].getValue();
            }
        }
        return "";
    }
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getName().equals(localName) && nsEquals(attrs[i].getNamespaceURI(), namespaceURI)) {
                return attrs[i];
            }
        }
        return null;
    }
    public boolean hasAttribute(String name) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getName().equals(localName) && nsEquals(attrs[i].getNamespaceURI(), namespaceURI)) {
                return true;
            }
        }
        return false;
    }
    public void setAttribute(String name, String value) {
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].getName().equals(name)) {
                attrs[i].setValue(value);
                return;
            }
        }
    }
    public int getLineNumber() {
        return line;
    }
    public int getColumnNumber() {
        return column;
    }
    public int getCharacterOffset() {
        return charOffset;
    }
    public String getAnnotation() {
        return fAnnotation;
    }
    public String getSyntheticAnnotation() {
        return fSyntheticAnnotation;
    }
    private static boolean nsEquals(String nsURI_1, String nsURI_2) {
        if (nsURI_1 == null) {
            return (nsURI_2 == null);
        }
        else {
            return nsURI_1.equals(nsURI_2);
        }
    }
}
