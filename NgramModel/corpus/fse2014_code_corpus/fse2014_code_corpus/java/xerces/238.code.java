package org.apache.xerces.dom;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
public abstract class NodeImpl
    implements Node, NodeList, EventTarget, Cloneable, Serializable{
    public static final short TREE_POSITION_PRECEDING   = 0x01;
    public static final short TREE_POSITION_FOLLOWING   = 0x02;
    public static final short TREE_POSITION_ANCESTOR    = 0x04;
    public static final short TREE_POSITION_DESCENDANT  = 0x08;
    public static final short TREE_POSITION_EQUIVALENT  = 0x10;
    public static final short TREE_POSITION_SAME_NODE   = 0x20;
    public static final short TREE_POSITION_DISCONNECTED = 0x00;
    public static final short DOCUMENT_POSITION_DISCONNECTED = 0x01;
    public static final short DOCUMENT_POSITION_PRECEDING = 0x02;
    public static final short DOCUMENT_POSITION_FOLLOWING = 0x04;
    public static final short DOCUMENT_POSITION_CONTAINS = 0x08;
    public static final short DOCUMENT_POSITION_IS_CONTAINED = 0x10;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 0x20;
    static final long serialVersionUID = -6316591992167219696L;
    public static final short ELEMENT_DEFINITION_NODE = 21;
    protected NodeImpl ownerNode; 
    protected short flags;
    protected final static short READONLY     = 0x1<<0;
    protected final static short SYNCDATA     = 0x1<<1;
    protected final static short SYNCCHILDREN = 0x1<<2;
    protected final static short OWNED        = 0x1<<3;
    protected final static short FIRSTCHILD   = 0x1<<4;
    protected final static short SPECIFIED    = 0x1<<5;
    protected final static short IGNORABLEWS  = 0x1<<6;
    protected final static short HASSTRING    = 0x1<<7;
    protected final static short NORMALIZED = 0x1<<8;
    protected final static short ID           = 0x1<<9;
    protected NodeImpl(CoreDocumentImpl ownerDocument) {
        ownerNode = ownerDocument;
    } 
    public NodeImpl() {}
    public abstract short getNodeType();
    public abstract String getNodeName();
    public String getNodeValue()
        throws DOMException {
        return null;            
    }
    public void setNodeValue(String x) 
        throws DOMException {
    }
    public Node appendChild(Node newChild) throws DOMException {
    	return insertBefore(newChild, null);
    }
    public Node cloneNode(boolean deep) {
        if (needsSyncData()) {
            synchronizeData();
	}
    	NodeImpl newnode;
    	try {
            newnode = (NodeImpl)clone();
    	}
    	catch (CloneNotSupportedException e) {
            throw new RuntimeException("**Internal Error**" + e);
    	}
    	newnode.ownerNode      = ownerDocument();
        newnode.isOwned(false);
        newnode.isReadOnly(false);
        ownerDocument().callUserDataHandlers(this, newnode,
                                             UserDataHandler.NODE_CLONED);
    	return newnode;
    } 
    public Document getOwnerDocument() {
        if (isOwned()) {
            return ownerNode.ownerDocument();
        } else {
            return (Document) ownerNode;
        }
    }
    CoreDocumentImpl ownerDocument() {
        if (isOwned()) {
            return ownerNode.ownerDocument();
        } else {
            return (CoreDocumentImpl) ownerNode;
        }
    }
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (!isOwned()) {
            ownerNode = doc;
        }
    }
    protected int getNodeNumber() {
        int nodeNumber;
        CoreDocumentImpl cd = (CoreDocumentImpl)(this.getOwnerDocument());
        nodeNumber = cd.getNodeNumber(this);   
        return nodeNumber;
    }
    public Node getParentNode() {
        return null;            
    }
    NodeImpl parentNode() {
        return null;
    }
    public Node getNextSibling() {
        return null;            
    }
    public Node getPreviousSibling() {
        return null;            
    }
    ChildNode previousSibling() {
        return null;            
    }
    public NamedNodeMap getAttributes() {
    	return null; 
    }
    public boolean hasAttributes() {
        return false;           
    }
    public boolean hasChildNodes() {
        return false;
    }
    public NodeList getChildNodes() {
        return this;
    }
    public Node getFirstChild() {
    	return null;
    }
    public Node getLastChild() {
	return null;
    }
    public Node insertBefore(Node newChild, Node refChild) 
	throws DOMException {
	throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                 "HIERARCHY_REQUEST_ERR", null));
    }
    public Node removeChild(Node oldChild) 
		throws DOMException {
	throw new DOMException(DOMException.NOT_FOUND_ERR, 
              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                 "NOT_FOUND_ERR", null));
    }
    public Node replaceChild(Node newChild, Node oldChild)
        throws DOMException {
	throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                 "HIERARCHY_REQUEST_ERR", null));
    }
    public int getLength() {
	return 0;
    }
    public Node item(int index) {
	return null;
    }
    public void normalize() {
    }
    public boolean isSupported(String feature, String version)
    {
        return ownerDocument().getImplementation().hasFeature(feature,
                                                              version);
    }
    public String getNamespaceURI()
    {
        return null;
    }
    public String getPrefix()
    {
        return null;
    }
    public void setPrefix(String prefix)
        throws DOMException
    {
	throw new DOMException(DOMException.NAMESPACE_ERR, 
              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                 "NAMESPACE_ERR", null));
    }
    public String             getLocalName()
    {
        return null;
    }
    public void addEventListener(String type, EventListener listener,
                                 boolean useCapture) {
        ownerDocument().addEventListener(this, type, listener, useCapture);
    }
    public void removeEventListener(String type, EventListener listener,
                                    boolean useCapture) {
        ownerDocument().removeEventListener(this, type, listener, useCapture);
    }
    public boolean dispatchEvent(Event event) {
        return ownerDocument().dispatchEvent(this, event);
    }
    public String getBaseURI() {
        return null;
    }
    public short compareTreePosition(Node other) {
        if (this==other) 
          return (TREE_POSITION_SAME_NODE | TREE_POSITION_EQUIVALENT);
        short thisType = this.getNodeType();
        short otherType = other.getNodeType();
        if (thisType == Node.ENTITY_NODE || 
            thisType == Node.NOTATION_NODE ||
            otherType == Node.ENTITY_NODE ||
            otherType == Node.NOTATION_NODE ) {
          return TREE_POSITION_DISCONNECTED; 
        }
        Node node; 
        Node thisAncestor = this;
        Node otherAncestor = other;
        int thisDepth=0;
        int otherDepth=0;
        for (node=this; node != null; node = node.getParentNode()) {
            thisDepth +=1;
            if (node == other) 
              return (TREE_POSITION_ANCESTOR | TREE_POSITION_PRECEDING);
            thisAncestor = node;
        }
        for (node=other; node!=null; node=node.getParentNode()) {
            otherDepth +=1;
            if (node == this) 
              return (TREE_POSITION_DESCENDANT | TREE_POSITION_FOLLOWING);
            otherAncestor = node;
        }
        Node thisNode = this;
        Node otherNode = other;
        int thisAncestorType = thisAncestor.getNodeType();
        int otherAncestorType = otherAncestor.getNodeType();
        if (thisAncestorType == Node.ATTRIBUTE_NODE)  {
           thisNode = ((AttrImpl)thisAncestor).getOwnerElement();
        }
        if (otherAncestorType == Node.ATTRIBUTE_NODE) {
           otherNode = ((AttrImpl)otherAncestor).getOwnerElement();
        }
        if (thisAncestorType == Node.ATTRIBUTE_NODE &&  
            otherAncestorType == Node.ATTRIBUTE_NODE &&  
            thisNode==otherNode)              
            return TREE_POSITION_EQUIVALENT;
        if (thisAncestorType == Node.ATTRIBUTE_NODE) {
            thisDepth=0;
            for (node=thisNode; node != null; node=node.getParentNode()) {
                thisDepth +=1;
                if (node == otherNode) 
                  {
                  return TREE_POSITION_PRECEDING;
                  }
                thisAncestor = node;
            }
        }
        if (otherAncestorType == Node.ATTRIBUTE_NODE) {
            otherDepth=0;
            for (node=otherNode; node != null; node=node.getParentNode()) {
                otherDepth +=1;
                if (node == thisNode) 
                  return TREE_POSITION_FOLLOWING;
                otherAncestor = node;
            }
        }
        if (thisAncestor != otherAncestor) 
          return TREE_POSITION_DISCONNECTED; 
        if (thisDepth > otherDepth) {
          for (int i=0; i<thisDepth - otherDepth; i++)
            thisNode = thisNode.getParentNode();
          if (thisNode == otherNode) 
            return TREE_POSITION_PRECEDING;
        }
        else {
          for (int i=0; i<otherDepth - thisDepth; i++)
            otherNode = otherNode.getParentNode();
          if (otherNode == thisNode) 
            return TREE_POSITION_FOLLOWING;
        }
        Node thisNodeP, otherNodeP;
        for (thisNodeP=thisNode.getParentNode(),
                  otherNodeP=otherNode.getParentNode();
             thisNodeP!=otherNodeP;) {
             thisNode = thisNodeP;
             otherNode = otherNodeP;
             thisNodeP = thisNodeP.getParentNode();
             otherNodeP = otherNodeP.getParentNode();
        }
        for (Node current=thisNodeP.getFirstChild(); 
                  current!=null;
                  current=current.getNextSibling()) {
               if (current==otherNode) {
                 return TREE_POSITION_PRECEDING;
               }
               else if (current==thisNode) {
                 return TREE_POSITION_FOLLOWING;
               }
        }
        return 0;
    }
    public short compareDocumentPosition(Node other) throws DOMException {
        if (this==other) 
          return 0; 
        if (other != null && !(other instanceof NodeImpl)) {
            String msg = DOMMessageFormatter.formatMessage(
               DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
        }
        Document thisOwnerDoc, otherOwnerDoc;
        if (this.getNodeType() == Node.DOCUMENT_NODE) 
          thisOwnerDoc = (Document)this;
        else
          thisOwnerDoc = this.getOwnerDocument();
        if (other.getNodeType() == Node.DOCUMENT_NODE) 
          otherOwnerDoc = (Document)other;
        else
          otherOwnerDoc = other.getOwnerDocument();
        if (thisOwnerDoc != otherOwnerDoc && 
            thisOwnerDoc !=null && 
            otherOwnerDoc !=null) 
 {
          int otherDocNum = ((CoreDocumentImpl)otherOwnerDoc).getNodeNumber();
          int thisDocNum = ((CoreDocumentImpl)thisOwnerDoc).getNodeNumber();
          if (otherDocNum > thisDocNum)  
            return DOCUMENT_POSITION_DISCONNECTED | 
                   DOCUMENT_POSITION_FOLLOWING | 
                   DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
          else
            return DOCUMENT_POSITION_DISCONNECTED | 
                   DOCUMENT_POSITION_PRECEDING | 
                   DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }
        Node node; 
        Node thisAncestor = this;
        Node otherAncestor = other;
        int thisDepth=0;
        int otherDepth=0;
        for (node=this; node != null; node = node.getParentNode()) {
            thisDepth +=1;
            if (node == other) 
              return (DOCUMENT_POSITION_CONTAINS | 
                      DOCUMENT_POSITION_PRECEDING);
            thisAncestor = node;
        }
        for (node=other; node!=null; node=node.getParentNode()) {
            otherDepth +=1;
            if (node == this) 
              return (DOCUMENT_POSITION_IS_CONTAINED | 
                      DOCUMENT_POSITION_FOLLOWING);
            otherAncestor = node;
        }
        int thisAncestorType = thisAncestor.getNodeType();
        int otherAncestorType = otherAncestor.getNodeType();
        Node thisNode = this;
        Node otherNode = other;
        switch (thisAncestorType) {
          case Node.NOTATION_NODE:
          case Node.ENTITY_NODE: {
            DocumentType container = thisOwnerDoc.getDoctype();
            if (container == otherAncestor) return 
                   (DOCUMENT_POSITION_CONTAINS | DOCUMENT_POSITION_PRECEDING);
            switch (otherAncestorType) {
              case Node.NOTATION_NODE: 
              case Node.ENTITY_NODE:  {
                if (thisAncestorType != otherAncestorType) 
                 return ((thisAncestorType>otherAncestorType) ? 
                    DOCUMENT_POSITION_PRECEDING:DOCUMENT_POSITION_FOLLOWING);
                else {
                 if (thisAncestorType == Node.NOTATION_NODE)
                     if (((NamedNodeMapImpl)container.getNotations()).precedes(otherAncestor,thisAncestor))
                       return (DOCUMENT_POSITION_PRECEDING | 
                               DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                     else
                       return (DOCUMENT_POSITION_FOLLOWING | 
                               DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                 else
                     if (((NamedNodeMapImpl)container.getEntities()).precedes(otherAncestor,thisAncestor))
                       return (DOCUMENT_POSITION_PRECEDING | 
                               DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                     else
                       return (DOCUMENT_POSITION_FOLLOWING | 
                               DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                }
              }
            }
            thisNode = thisAncestor = thisOwnerDoc;
            break;
          } 
          case Node.DOCUMENT_TYPE_NODE: {
            if (otherNode == thisOwnerDoc) 
              return (DOCUMENT_POSITION_PRECEDING | 
                      DOCUMENT_POSITION_CONTAINS);
            else if (thisOwnerDoc!=null && thisOwnerDoc==otherOwnerDoc)
              return (DOCUMENT_POSITION_FOLLOWING);
            break;
          }
          case Node.ATTRIBUTE_NODE: {
            thisNode = ((AttrImpl)thisAncestor).getOwnerElement();
            if (otherAncestorType==Node.ATTRIBUTE_NODE) {
              otherNode = ((AttrImpl)otherAncestor).getOwnerElement();
              if (otherNode == thisNode) {
                if (((NamedNodeMapImpl)thisNode.getAttributes()).precedes(other,this))
                  return (DOCUMENT_POSITION_PRECEDING | 
                          DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                else
                  return (DOCUMENT_POSITION_FOLLOWING | 
                          DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
              }
            }
            thisDepth=0;
            for (node=thisNode; node != null; node=node.getParentNode()) {
                thisDepth +=1;
                if (node == otherNode) 
                  {
                  return (DOCUMENT_POSITION_CONTAINS | 
                          DOCUMENT_POSITION_PRECEDING);
                  }
                thisAncestor = node;
            }
          }
        }
        switch (otherAncestorType) {
          case Node.NOTATION_NODE:
          case Node.ENTITY_NODE: {
          DocumentType container = thisOwnerDoc.getDoctype();
            if (container == this) return (DOCUMENT_POSITION_IS_CONTAINED | 
                                          DOCUMENT_POSITION_FOLLOWING);
            otherNode = otherAncestor = thisOwnerDoc;
            break;
          }
          case Node.DOCUMENT_TYPE_NODE: {
            if (thisNode == otherOwnerDoc) 
              return (DOCUMENT_POSITION_FOLLOWING | 
                      DOCUMENT_POSITION_IS_CONTAINED);
            else if (otherOwnerDoc!=null && thisOwnerDoc==otherOwnerDoc)
              return (DOCUMENT_POSITION_PRECEDING);
            break;
          }
          case Node.ATTRIBUTE_NODE: {
            otherDepth=0;
            otherNode = ((AttrImpl)otherAncestor).getOwnerElement();
            for (node=otherNode; node != null; node=node.getParentNode()) {
                otherDepth +=1;
                if (node == thisNode) 
                  return DOCUMENT_POSITION_FOLLOWING | 
                         DOCUMENT_POSITION_IS_CONTAINED;
                otherAncestor = node;
            }
          }
        }
        if (thisAncestor != otherAncestor) {
          int thisAncestorNum, otherAncestorNum;
          thisAncestorNum = ((NodeImpl)thisAncestor).getNodeNumber();
          otherAncestorNum = ((NodeImpl)otherAncestor).getNodeNumber();
          if (thisAncestorNum > otherAncestorNum) 
            return DOCUMENT_POSITION_DISCONNECTED | 
                   DOCUMENT_POSITION_FOLLOWING | 
                   DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
          else
            return DOCUMENT_POSITION_DISCONNECTED | 
                   DOCUMENT_POSITION_PRECEDING | 
                   DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }
        if (thisDepth > otherDepth) {
          for (int i=0; i<thisDepth - otherDepth; i++)
            thisNode = thisNode.getParentNode();
          if (thisNode == otherNode) 
{
            return DOCUMENT_POSITION_PRECEDING;
          }
        }
        else {
          for (int i=0; i<otherDepth - thisDepth; i++)
            otherNode = otherNode.getParentNode();
          if (otherNode == thisNode) 
            return DOCUMENT_POSITION_FOLLOWING;
        }
        Node thisNodeP, otherNodeP;
        for (thisNodeP=thisNode.getParentNode(),
                  otherNodeP=otherNode.getParentNode();
             thisNodeP!=otherNodeP;) {
             thisNode = thisNodeP;
             otherNode = otherNodeP;
             thisNodeP = thisNodeP.getParentNode();
             otherNodeP = otherNodeP.getParentNode();
        }
        for (Node current=thisNodeP.getFirstChild(); 
                  current!=null;
                  current=current.getNextSibling()) {
               if (current==otherNode) {
                 return DOCUMENT_POSITION_PRECEDING;
               }
               else if (current==thisNode) {
                 return DOCUMENT_POSITION_FOLLOWING;
               }
        }
        return 0;
    }
    public String getTextContent() throws DOMException {
        return getNodeValue();  
    }
    void getTextContent(StringBuffer buf) throws DOMException {
        String content = getNodeValue();
        if (content != null) {
            buf.append(content);
        }
    }
    public void setTextContent(String textContent)
        throws DOMException {
        setNodeValue(textContent);
    }
    public boolean isSameNode(Node other) {
        return this == other;
    }
    public boolean isDefaultNamespace(String namespaceURI){
        short type = this.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE: {             
            String namespace = this.getNamespaceURI();
            String prefix = this.getPrefix();
            if (prefix == null || prefix.length() == 0) {
                if (namespaceURI == null) {
                    return (namespace == namespaceURI);
                }
                return namespaceURI.equals(namespace);
            }
            if (this.hasAttributes()) {
                ElementImpl elem = (ElementImpl)this;
                NodeImpl attr = (NodeImpl)elem.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns");
                if (attr != null) {
                    String value = attr.getNodeValue();
                    if (namespaceURI == null) {
                        return (namespace == value);
                    }
                    return namespaceURI.equals(value);
                }
            }
            NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
            if (ancestor != null) {
                return ancestor.isDefaultNamespace(namespaceURI);
            }
            return false;
        }
        case Node.DOCUMENT_NODE:{
                return((NodeImpl)((Document)this).getDocumentElement()).isDefaultNamespace(namespaceURI);
            }
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            return false;
        case Node.ATTRIBUTE_NODE:{
                if (this.ownerNode.getNodeType() == Node.ELEMENT_NODE) {
                    return ownerNode.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
        default:{   
                NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
        }
    }
    public String lookupPrefix(String namespaceURI){
        if (namespaceURI == null) {
            return null;
        }
        short type = this.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE: {
                this.getNamespaceURI(); 
                return lookupNamespacePrefix(namespaceURI, (ElementImpl)this);
            }
        case Node.DOCUMENT_NODE:{
                return((NodeImpl)((Document)this).getDocumentElement()).lookupPrefix(namespaceURI);
            }
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            return null;
        case Node.ATTRIBUTE_NODE:{
                if (this.ownerNode.getNodeType() == Node.ELEMENT_NODE) {
                    return ownerNode.lookupPrefix(namespaceURI);
                }
                return null;
            }
        default:{   
                NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.lookupPrefix(namespaceURI);
                }
                return null;
            }
        }
    }
    public String lookupNamespaceURI(String specifiedPrefix) {
        short type = this.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE : {  
                String namespace = this.getNamespaceURI();
                String prefix = this.getPrefix();
                if (namespace !=null) {
                    if (specifiedPrefix== null && prefix==specifiedPrefix) {
                        return namespace;
                    } else if (prefix != null && prefix.equals(specifiedPrefix)) {
                        return namespace;
                    }
                } 
                if (this.hasAttributes()) {
                    NamedNodeMap map = this.getAttributes();
                    int length = map.getLength();
                    for (int i=0;i<length;i++) {
                        Node attr = map.item(i);
                        String attrPrefix = attr.getPrefix();
                        String value = attr.getNodeValue();
                        namespace = attr.getNamespaceURI();
                        if (namespace !=null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
                            if (specifiedPrefix == null &&
                                attr.getNodeName().equals("xmlns")) {
                                return value.length() > 0 ? value : null;
                            } else if (attrPrefix !=null && 
                                       attrPrefix.equals("xmlns") &&
                                       attr.getLocalName().equals(specifiedPrefix)) {
                                return value.length() > 0 ? value : null;
                            }
                        }
                    }
                }
                NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
        case Node.DOCUMENT_NODE : {   
                return((NodeImpl)((Document)this).getDocumentElement()).lookupNamespaceURI(specifiedPrefix);
            }
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            return null;
        case Node.ATTRIBUTE_NODE:{
                if (this.ownerNode.getNodeType() == Node.ELEMENT_NODE) {
                    return ownerNode.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
        default:{ 
                NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
        }
    }
    Node getElementAncestor(Node currentNode) {
        Node parent = currentNode.getParentNode();
        while (parent != null) {
            short type = parent.getNodeType();
            if (type == Node.ELEMENT_NODE) {
                return parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }
    String lookupNamespacePrefix(String namespaceURI, ElementImpl el){
        String namespace = this.getNamespaceURI();
        String prefix = this.getPrefix();
        if (namespace!=null && namespace.equals(namespaceURI)) {
            if (prefix != null) {
                String foundNamespace =  el.lookupNamespaceURI(prefix);
                if (foundNamespace !=null && foundNamespace.equals(namespaceURI)) {
                    return prefix;
                }
            }
        }
        if (this.hasAttributes()) {
            NamedNodeMap map = this.getAttributes();
            int length = map.getLength();
            for (int i=0;i<length;i++) {
                Node attr = map.item(i);
                String attrPrefix = attr.getPrefix();
                String value = attr.getNodeValue();
                namespace = attr.getNamespaceURI();
                if (namespace !=null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
                    if (((attr.getNodeName().equals("xmlns")) ||
                         (attrPrefix !=null && attrPrefix.equals("xmlns")) &&
                         value.equals(namespaceURI))) {
                        String localname= attr.getLocalName();
                        String foundNamespace = el.lookupNamespaceURI(localname);
                        if (foundNamespace !=null && foundNamespace.equals(namespaceURI)) {
                            return localname;
                        }
                    }
                }
            }
        }
        NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
        if (ancestor != null) {
            return ancestor.lookupNamespacePrefix(namespaceURI, el);
        }
        return null;
    }
    public boolean isEqualNode(Node arg) {
        if (arg == this) {
            return true;
        }
        if (arg.getNodeType() != getNodeType()) {
            return false;
        }
        if (getNodeName() == null) {
            if (arg.getNodeName() != null) {
                return false;
            }
        }
        else if (!getNodeName().equals(arg.getNodeName())) {
            return false;
        }
        if (getLocalName() == null) {
            if (arg.getLocalName() != null) {
                return false;
            }
        }
        else if (!getLocalName().equals(arg.getLocalName())) {
            return false;
        }
        if (getNamespaceURI() == null) {
            if (arg.getNamespaceURI() != null) {
                return false;
            }
        }
        else if (!getNamespaceURI().equals(arg.getNamespaceURI())) {
            return false;
        }
        if (getPrefix() == null) {
            if (arg.getPrefix() != null) {
                return false;
            }
        }
        else if (!getPrefix().equals(arg.getPrefix())) {
            return false;
        }
        if (getNodeValue() == null) {
            if (arg.getNodeValue() != null) {
                return false;
            }
        }
        else if (!getNodeValue().equals(arg.getNodeValue())) {
            return false;
        }
        return true;
    }
    public Object getFeature(String feature, String version) {
        return isSupported(feature, version) ? this : null;
    }
    public Object setUserData(String key, 
                              Object data, 
                              UserDataHandler handler) {
        return ownerDocument().setUserData(this, key, data, handler);
    }
    public Object getUserData(String key) {
        return ownerDocument().getUserData(this, key);
    }
	protected Hashtable getUserDataRecord(){
        return ownerDocument().getUserDataRecord(this);
	}
    public void setReadOnly(boolean readOnly, boolean deep) {
        if (needsSyncData()) {
            synchronizeData();
        }
    	isReadOnly(readOnly);
    } 
    public boolean getReadOnly() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return isReadOnly();
    } 
    public void setUserData(Object data) {
        ownerDocument().setUserData(this, data);
    }
    public Object getUserData() {
        return ownerDocument().getUserData(this);
    }
    protected void changed() {
        ownerDocument().changed();
    }
    protected int changes() {
        return ownerDocument().changes();
    }
    protected void synchronizeData() {
        needsSyncData(false);
    }
    protected Node getContainer() {
       return null;
    }
    final boolean isReadOnly() {
        return (flags & READONLY) != 0;
    }
    final void isReadOnly(boolean value) {
        flags = (short) (value ? flags | READONLY : flags & ~READONLY);
    }
    final boolean needsSyncData() {
        return (flags & SYNCDATA) != 0;
    }
    final void needsSyncData(boolean value) {
        flags = (short) (value ? flags | SYNCDATA : flags & ~SYNCDATA);
    }
    final boolean needsSyncChildren() {
        return (flags & SYNCCHILDREN) != 0;
    }
    public final void needsSyncChildren(boolean value) {
        flags = (short) (value ? flags | SYNCCHILDREN : flags & ~SYNCCHILDREN);
    }
    final boolean isOwned() {
        return (flags & OWNED) != 0;
    }
    final void isOwned(boolean value) {
        flags = (short) (value ? flags | OWNED : flags & ~OWNED);
    }
    final boolean isFirstChild() {
        return (flags & FIRSTCHILD) != 0;
    }
    final void isFirstChild(boolean value) {
        flags = (short) (value ? flags | FIRSTCHILD : flags & ~FIRSTCHILD);
    }
    final boolean isSpecified() {
        return (flags & SPECIFIED) != 0;
    }
    final void isSpecified(boolean value) {
        flags = (short) (value ? flags | SPECIFIED : flags & ~SPECIFIED);
    }
    final boolean internalIsIgnorableWhitespace() {
        return (flags & IGNORABLEWS) != 0;
    }
    final void isIgnorableWhitespace(boolean value) {
        flags = (short) (value ? flags | IGNORABLEWS : flags & ~IGNORABLEWS);
    }
    final boolean hasStringValue() {
        return (flags & HASSTRING) != 0;
    }
    final void hasStringValue(boolean value) {
        flags = (short) (value ? flags | HASSTRING : flags & ~HASSTRING);
    }
    final boolean isNormalized() {
        return (flags & NORMALIZED) != 0;
    }
    final void isNormalized(boolean value) {
        if (!value && isNormalized() && ownerNode != null) {
            ownerNode.isNormalized(false);
        }
        flags = (short) (value ?  flags | NORMALIZED : flags & ~NORMALIZED);
    }
    final boolean isIdAttribute() {
        return (flags & ID) != 0;
    }
    final void isIdAttribute(boolean value) {
        flags = (short) (value ? flags | ID : flags & ~ID);
    }
    public String toString() {
        return "["+getNodeName()+": "+getNodeValue()+"]";
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (needsSyncData()) {
            synchronizeData();
        }
        out.defaultWriteObject();
    } 
} 
