package org.apache.xerces.dom;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
public class TextImpl 
    extends CharacterDataImpl 
    implements CharacterData, Text {
    static final long serialVersionUID = -5294980852957403469L;
    public TextImpl(){}
    public TextImpl(CoreDocumentImpl ownerDoc, String data) {
        super(ownerDoc, data);
    }
    public void setValues(CoreDocumentImpl ownerDoc, String data){
        flags=0;
        nextSibling = null;
        previousSibling=null;
        setOwnerDocument(ownerDoc);
        super.data = data;
    }
    public short getNodeType() {
        return Node.TEXT_NODE;
    }
    public String getNodeName() {
        return "#text";
    }
    public void setIgnorableWhitespace(boolean ignore) {
        if (needsSyncData()) {
            synchronizeData();
        }
        isIgnorableWhitespace(ignore);
    } 
    public boolean isElementContentWhitespace() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return internalIsIgnorableWhitespace();
    }
    public String getWholeText(){
        if (needsSyncData()) {
            synchronizeData();
        }
        StringBuffer buffer = new StringBuffer();
        if (data != null && data.length() != 0) {
            buffer.append(data);
        }
        getWholeTextBackward(this.getPreviousSibling(), buffer, this.getParentNode());
        String temp = buffer.toString();
        buffer.setLength(0);
        getWholeTextForward(this.getNextSibling(), buffer, this.getParentNode());
        return temp + buffer.toString();
    }
    protected void insertTextContent(StringBuffer buf) throws DOMException {
         String content = getNodeValue();
         if (content != null) {
             buf.insert(0, content);
         }
     }
    private boolean getWholeTextForward(Node node, StringBuffer buffer, Node parent){
    	boolean inEntRef = false;
    	if (parent!=null) {
    		inEntRef = parent.getNodeType()==Node.ENTITY_REFERENCE_NODE;
    	}
        while (node != null) {
            short type = node.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                if (getWholeTextForward(node.getFirstChild(), buffer, node)){
                    return true;
                }
            }
            else if (type == Node.TEXT_NODE || 
                     type == Node.CDATA_SECTION_NODE) {
                ((NodeImpl)node).getTextContent(buffer);
            }
            else {
                return true; 
            }
            node = node.getNextSibling();
        }
        if (inEntRef) {
            getWholeTextForward(parent.getNextSibling(), buffer, parent.getParentNode());
        		return true;
        }
        return false;
    }
    private boolean getWholeTextBackward(Node node, StringBuffer buffer, Node parent){
    	boolean inEntRef = false;
    	if (parent!=null) {
    		inEntRef = parent.getNodeType()==Node.ENTITY_REFERENCE_NODE;
    	}
        while (node != null) {
            short type = node.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                if (getWholeTextBackward(node.getLastChild(), buffer, node)){
                    return true;
                }
            }
            else if (type == Node.TEXT_NODE || 
                     type == Node.CDATA_SECTION_NODE) {
                ((TextImpl)node).insertTextContent(buffer);
            }
            else {
                return true; 
            }
            node = node.getPreviousSibling();
        }
        if (inEntRef) {
        	getWholeTextBackward(parent.getPreviousSibling(), buffer, parent.getParentNode());
            return true;
        }
        return false;
    }
    public Text replaceWholeText(String content) throws DOMException {
        if (needsSyncData()) {
            synchronizeData();
        }
        Node parent = this.getParentNode();
        if (content == null || content.length() == 0) {
            if (parent != null) { 
                parent.removeChild(this);
            }
            return null;
        }
        if (ownerDocument().errorChecking) {
            if (!canModifyPrev(this)) {
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "NO_MODIFICATION_ALLOWED_ERR", null));
            }
            if (!canModifyNext(this)) {
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "NO_MODIFICATION_ALLOWED_ERR", null));
            }
        }
        Text currentNode = null;
        if (isReadOnly()) {
            Text newNode = this.ownerDocument().createTextNode(content);
            if (parent != null) { 
                parent.insertBefore(newNode, this);
                parent.removeChild(this);
                currentNode = newNode;
            } else {
                return newNode;
            }
        } else {
            this.setData(content);
            currentNode = this;
        }
        Node prev = currentNode.getPreviousSibling();
        while (prev != null) {
            if ((prev.getNodeType() == Node.TEXT_NODE)
                    || (prev.getNodeType() == Node.CDATA_SECTION_NODE)
                    || (prev.getNodeType() == Node.ENTITY_REFERENCE_NODE && hasTextOnlyChildren(prev))) {
                parent.removeChild(prev);
                prev = currentNode;
            } else {
                break;
            }
            prev = prev.getPreviousSibling();
        }
        Node next = currentNode.getNextSibling();
        while (next != null) {
            if ((next.getNodeType() == Node.TEXT_NODE)
                    || (next.getNodeType() == Node.CDATA_SECTION_NODE)
                    || (next.getNodeType() == Node.ENTITY_REFERENCE_NODE && hasTextOnlyChildren(next))) {
                parent.removeChild(next);
                next = currentNode;
            } else {
                break;
            }
            next = next.getNextSibling();
        }
        return currentNode;
    }
    private boolean canModifyPrev(Node node) {
        boolean textLastChild = false;
        Node prev = node.getPreviousSibling();
        while (prev != null) {
            short type = prev.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                Node lastChild = prev.getLastChild();
                if (lastChild == null) {
                    return false;
                }
                while (lastChild != null) {
                    short lType = lastChild.getNodeType();
                    if (lType == Node.TEXT_NODE
                            || lType == Node.CDATA_SECTION_NODE) {
                        textLastChild = true;
                    } else if (lType == Node.ENTITY_REFERENCE_NODE) {
                        if (!canModifyPrev(lastChild)) {
                            return false;
                        } else {
                            textLastChild = true;
                        }
                    } else {
                        if (textLastChild) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                    lastChild = lastChild.getPreviousSibling();
                }
            } else if (type == Node.TEXT_NODE
                    || type == Node.CDATA_SECTION_NODE) {
            } else {
                return true;
            }
            prev = prev.getPreviousSibling();
        }
        return true;
    }
    private boolean canModifyNext(Node node) {
        boolean textFirstChild = false;
        Node next = node.getNextSibling();
        while (next != null) {
            short type = next.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                Node firstChild = next.getFirstChild();
                if (firstChild == null) {
                    return false;
                }
                while (firstChild != null) {
                    short lType = firstChild.getNodeType();
                    if (lType == Node.TEXT_NODE
                            || lType == Node.CDATA_SECTION_NODE) {
                        textFirstChild = true;
                    } else if (lType == Node.ENTITY_REFERENCE_NODE) {
                        if (!canModifyNext(firstChild)) {
                            return false;
                        } else {
                            textFirstChild = true;
                        }
                    } else {
                        if (textFirstChild) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                    firstChild = firstChild.getNextSibling();
                }
            } else if (type == Node.TEXT_NODE
                    || type == Node.CDATA_SECTION_NODE) {
            } else {
                return true;
            }
            next = next.getNextSibling();
        }
        return true;
    }
    private boolean hasTextOnlyChildren(Node node) {
        Node child = node;
        if (child == null) {
            return false;
        }
        child = child.getFirstChild();
        while (child != null) {
            int type = child.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                return hasTextOnlyChildren(child);
            } 
            else if (type != Node.TEXT_NODE
                    && type != Node.CDATA_SECTION_NODE
                    && type != Node.ENTITY_REFERENCE_NODE) {
                return false;
            }
            child = child.getNextSibling();
        }
        return true;
    }
    public boolean isIgnorableWhitespace() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return internalIsIgnorableWhitespace();
    } 
    public Text splitText(int offset) 
        throws DOMException {
        if (isReadOnly()) {
            throw new DOMException(
            DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        if (offset < 0 || offset > data.length() ) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, 
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
        }
        Text newText =
            getOwnerDocument().createTextNode(data.substring(offset));
        setNodeValue(data.substring(0, offset));
        Node parentNode = getParentNode();
        if (parentNode != null) {
            parentNode.insertBefore(newText, nextSibling);
        }
        return newText;
    } 
    public void replaceData (String value){
        data = value;
    }
    public String removeData (){
        String olddata=data;
        data = "";
        return olddata;
    }
} 
